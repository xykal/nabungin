# Arsitektur - Nabungin

## Modul dan paket

Satu modul Gradle (`:app`) dengan pemisahan paket berlapis. Dipilih sadar: modul tunggal menghindari overhead konfigurasi Gradle dan mempercepat cold start build, sementara boundary tetap dijaga di level paket dan tipe data.

```
app/src/main/java/dev/xykal/nabungin/
  core/            AppContainer (DI manual), appViewModel helper
  domain/          model, format, SavingsMath (pure Kotlin, bisa di-unit-test dari JVM)
  data/local/      Room: entity, DAO, database, projection
  data/prefs/      DataStore preferences: AppSettings, ThemeMode
  data/repo/       SavingsRepository: satu-satunya pintu ke data untuk UI dan worker
  data/backup/     DTO @Serializable + BackupManager (SAF stream in/out)
  security/        PinHasher (PBKDF2), Biometrics (BiometricPrompt wrapper)
  notify/          Notification channel dan builder
  work/            AutoSaveWorker, ReminderWorker, WorkScheduler
  ui/theme/        palet, tipografi, CompositionLocal warna
  ui/icons/        ImageVector bikinan sendiri (path murni 24x24)
  ui/components/   design system: card, button, ring, bar chart, keypad, bottom sheet
  ui/{home,goal,history,stats,settings,lock,nav}/  layar per fitur + ViewModel
```

## Aliran data

```
Room DAO (Flow)  ->  SavingsRepository (map/combine)  ->  ViewModel (stateIn)  ->  Composable
        ^                                                        |
        |                                                        v
        +------------ tulis lewat suspend function (setoran, tujuan, aturan) ----+
```

- UI tidak pernah menyentuh DAO langsung.
- ViewModel memakai `stateIn(..., SharingStarted.WhileSubscribed(5_000))` supaya query berhenti saat layar nggak dilihat.
- Semua operasi tulis `suspend` dan dijalankan di `viewModelScope` (Room otomatis pindah ke IO dispatcher).

## Model data

`goals`

| kolom | tipe | catatan |
| --- | --- | --- |
| id | Long PK autoGenerate | |
| name | String | |
| targetAmount | Long | rupiah utuh, tanpa desimal |
| deadlineEpochDay | Long? | `LocalDate.toEpochDay()` |
| accentIndex | Int | indeks ke `GoalAccents` |
| iconKey | String | kunci ke `AppIcons.byKey` |
| category | String | |
| createdAtMillis | Long | dipakai buat laju rata-rata |
| archived | Boolean | soft delete, disiapkan buat fitur arsip |

`deposits`

| kolom | tipe | catatan |
| --- | --- | --- |
| id | Long PK | |
| goalId | Long FK | `ON DELETE CASCADE` + index |
| amount | Long | |
| epochDay | Long | index, dipakai buat query 30 hari |
| note | String | |
| source | String | `manual` / `auto` / `import` |
| createdAtMillis | Long | tie-breaker saat urut tanggal sama |

`auto_rules`

| kolom | tipe | catatan |
| --- | --- | --- |
| id | Long PK | |
| goalId | Long FK unique | satu aturan per tujuan |
| amount, interval, hour, minute | | interval: `daily` / `weekly` / `monthly` |
| enabled | Boolean | |
| lastRunEpochDay | Long? | kunci idempotensi worker |

Tanggal disimpan sebagai epoch day, nominal sebagai Long. Alasan: nggak ada ambiguitas timezone, nggak ada floating point error di uang, dan `GROUP BY epochDay` jadi query murah.

## Concurrency dan background

- WorkManager dikonfigurasi lewat `Configuration.Provider` di `NabunginApp`, `WorkManagerInitializer` dilepas dari manifest supaya startup nggak nunggu inisialisasi default.
- `WorkScheduler.syncAutoSave` memasang periodic work 1 jam (policy `UPDATE`, jadi aman dipanggil berulang).
- `WorkScheduler.syncReminder` menghitung `initialDelay` sampai jam pilihan user berikutnya, lalu periodic work harian.
- `AutoSaveWorker` dan `ReminderWorker` mengambil `AppContainer` dari `applicationContext`; keduanya memeriksa ulang kondisi sebelum menulis data.
- `PIN` verification dijalankan di `Dispatchers.Default` karena PBKDF2 120k iterasi memakan puluhan sampai ratusan milidetik.

## UI

- Tema di-inject via `CompositionLocal` (`LocalNabunginColors`) supaya composable dapat warna semantik tanpa lewat `MaterialTheme.colorScheme` yang generik.
- Komponen berat digambar langsung dengan Canvas: `ProgressRing` (drawArc), `BarChart` (drawRoundRect). Tidak ada dependency chart pihak ketiga.
- Ikon adalah `ImageVector` manual dengan viewport 24x24 sehingga tidak bergantung pada icon pack dan tidak ada layout shift.
- Animasi memakai `animateFloatAsState` dengan spring; tidak ada animasi yang memblok UI thread.
- Semua layar kecuali lock screen memakai `statusBarsPadding()`; bottom bar memakai `navigationBarsPadding()` sendiri karena posisinya mengapung.

## Keamanan

- Tidak ada permission jaringan di manifest.
- `allowBackup=false` + rule `data-extraction-rules`/`full-backup-content` meng-exclude semua domain (database, file, sharedpref) supaya data tabungan tidak naik ke cloud backup Google.
- PIN: PBKDF2-HMAC-SHA256, 120k iterasi, panjang kunci 256-bit, salt acak 128-bit per pemasangan; perbandingan constant-time (`Arrays.equals`).
- Backup JSON tidak pernah berisi PIN atau setelan keamanan, cuma data tujuan/setoran/aturan.
- R8 di release: minify + shrinkResources, dengan keep rule eksplisit untuk entity Room, DTO serialization, dan `ListenableWorker`.

## Testing

- Unit test JVM (`app/src/test`): `SavingsMath` (progress, pace, ETA, streak, agregasi harian) dan `Format` (parse, format rupiah, label tanggal). Jalan di CI sebelum assemble.
- Instrumented test slot sudah disiapkan lewat dependency `androidx.test` + `compose-ui-test-junit4` untuk Compose UI test berikutnya.

## Build pipeline

1. `testDebugUnitTest`
2. `assembleDebug` (applicationId suffix `.debug`, versionName suffix `-debug`)
3. `assembleRelease` (R8, resource shrink, signing dari secrets kalau tersedia)
4. `sha256sum` lalu upload artifact
5. Tag `v*` atau input `create_release` -> GitHub Release
