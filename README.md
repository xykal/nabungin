# Nabungin

Offline-first savings app for Android. Native Kotlin + Jetpack Compose, Room, WorkManager, no server, no tracking.

Aplikasi tabungan Android, offline-first. Kotlin + Jetpack Compose native, Room, WorkManager. Tanpa server, tanpa tracking.

---

## English

### What it is

Nabungin tracks savings goals and deposits entirely on-device. Goal photos are kept offline, including in JSON backups. You create a goal (name, target amount, optional deadline, color, icon, category), log deposits, and the app computes the pace you need per day, an ETA for each goal, and a streak of consecutive saving days. Recurring rules let WorkManager deposit automatically on schedule, and an optional daily reminder nudges you when you skipped a day.

### Feature set

| Area | Detail |
| --- | --- |
| Goals | CRUD, target amount, deadline, 6 accent colors, 10 custom vector icons, 6 categories |
| Deposits | Manual entry with numeric keypad, quick-amount chips, backdated entry, notes, per-row delete |
| Auto-save | Per-goal rule: amount + interval (daily/weekly/monthly) + execution hour, run by `AutoSaveWorker` |
| Reminders | Daily notification at a user-picked time, content adapts to streak and today's deposits |
| Stats | Total saved, month total, 30-day bar chart, active days, average per active day, per-goal ETA ranking |
| Security | 6-digit PIN (PBKDF2-HMAC-SHA256, 120k iterations, 128-bit salt), BiometricPrompt, auto re-lock on background |
| Backup | JSON export/import through the Storage Access Framework |
| Design | Custom dark/light design system, 3D goal artwork, liquid Canvas progress, and consistent app mascot |

### Stack (2026 stable line)

- Kotlin 2.2.21, AGP 8.13.2, Gradle 8.14.3, JDK 17
- Compose BOM 2025.11.01 + Kotlin Compose compiler plugin
- Room 2.8.2 (KSP 2.2.21-2.0.5), WorkManager 2.10.5, DataStore 1.1.7, Biometric 1.1.0
- kotlinx-serialization 1.9.0 for backup format
- minSdk 26, target/compileSdk 36, R8 minify + resource shrinking on release

### Architecture

Single Gradle module, layered packages: `domain` (pure Kotlin, unit-tested), `data` (Room + DataStore + repository), `security`, `work`, `notify`, `ui` (theme, components, feature screens). Manual DI via `AppContainer` created in `NabunginApp` — no reflection, no annotation-processing cost at startup beyond Room's KSP output.

Read `docs/ARCHITECTURE.md` for the data model and threading rules, `docs/PRD.md` for scope and roadmap.

### Build

Build happens on GitHub Actions, nothing runs locally:

```
Actions -> Build APK -> Run workflow
```

The workflow runs `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`, then uploads both APKs plus `SHA256SUMS.txt` as artifacts. Pushing a `v*` tag (or enabling "create_release") publishes a GitHub Release with the APKs attached.

Release signing reads these repository secrets (see `docs/GITHUB_SECRETS.md`):

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Release requires all four secrets; CI fails closed if signing is missing. Back up the keystore and password offline before publishing.

### Install

```
adb install -r Nabungin-<version>-release.apk
```

### Security notes

- No network permission is declared. The APK cannot talk to the internet.
- `android:allowBackup="false"`, cloud backup and device transfer are excluded.
- PIN is never stored in plaintext; verification is constant-time.
- Never commit keystores, `keystore.properties`, or token files. `.gitignore` already blocks them — if a token ever lands in a repository, rotate it immediately.

---

## Bahasa Indonesia

### Ini apa

Nabungin nyatet tujuan tabungan dan setoran, semuanya di dalam HP. Foto tujuan ikut disimpan offline dan dalam backup JSON. Lu bikin tujuan (nama, target, deadline opsional, warna, ikon, kategori), catat setoran, lalu app ngitung kebutuhan nabung per hari, estimasi tanggal tercapai per tujuan, plus streak hari nabung beruntun. Ada aturan auto-save yang dijalanin WorkManager, dan pengingat harian buat hari yang kelewat.

### Fitur

| Bagian | Detail |
| --- | --- |
| Tujuan | CRUD, target nominal, deadline, 6 warna aksen, 10 ikon vector bikinan sendiri, 6 kategori |
| Setoran | Input manual pakai keypad numerik sendiri, chip nominal cepat, bisa backdate, catatan, hapus per baris |
| Auto-save | Aturan per tujuan: nominal + interval (harian/mingguan/bulanan) + jam eksekusi, dijalankan `AutoSaveWorker` |
| Pengingat | Notifikasi harian di jam pilihan user, isi pesan nyesuaikan streak dan setoran hari itu |
| Statistik | Total terkumpul, total bulan ini, grafik 30 hari, hari aktif, rata-rata per hari aktif, peringkat ETA per tujuan |
| Keamanan | PIN 6 digit (PBKDF2-HMAC-SHA256, 120k iterasi, salt 128-bit), BiometricPrompt, auto re-lock pas app ke background |
| Backup | Export/import JSON lewat Storage Access Framework |
| Desain | Design system terang/gelap, ilustrasi tujuan 3D, progres cair di Canvas, dan maskot seragam |

### Cara build

Build di GitHub Actions, nggak perlu setup lokal:

```
Actions -> Build APK -> Run workflow
```

Workflow jalanin `testDebugUnitTest`, `assembleDebug`, dan `assembleRelease`, lalu upload dua APK + `SHA256SUMS.txt` sebagai artifact. Push tag `v*` (atau centang "create_release") otomatis bikin GitHub Release dengan APK nempel.

Signing release baca secrets repo ini:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Rilis wajib memakai empat secret tersebut; CI berhenti bila signing tidak lengkap. Simpan cadangan keystore serta kata sandinya di luar repo.

### Install

```
adb install -r Nabungin-<versi>-release.apk
```

### Catatan keamanan

- Nggak ada permission internet di manifest. APK-nya nggak bisa keluar jaringan.
- `android:allowBackup="false"`, cloud backup dan device transfer dikecualikan.
- PIN nggak pernah disimpan plaintext, verifikasi pakai perbandingan constant-time.
- Jangan pernah commit keystore, `keystore.properties`, atau file token. `.gitignore` sudah ngeblok semua itu — kalau ada token yang telanjur masuk repo, langsung rotate.

---

## License / Lisensi

MIT. Lihat `LICENSE`.

## v1.1.0 — Savings plan / Rencana tabungan

**English:** Create any number of savings goals with a title, optional reason, target amount, daily plan, optional deadline, category, color and icon. The form recommends a daily amount based on the deadline and forecasts a date using your chosen plan. A plan is *not* an automatic deposit; only an actual "Tabung" action or a separately enabled auto-save rule changes the balance. Balance count-up, progress rings/tracks and 30-day bars animate and respect Android's animator scale. Room migrates v1 data to v2 without wiping existing goals and deposits. CI runs an emulator smoke test for both release and debug APKs before publishing.

**Bahasa Indonesia:** Bikin tabungan sebanyak yang lu mau: nama, alasan, target, rencana nominal per hari, deadline opsional, kategori, warna, dan ikon. Form ngasih saran nominal harian berdasarkan deadline sekaligus simulasi tanggal target kalau rutin. Rencana harian *bukan* uang yang sudah ditabung: saldo cuma bertambah kalau lu tekan "Tabung" atau sengaja mengaktifkan auto-save terpisah. Angka saldo, lingkar progres, garis progres, dan grafik 30 hari punya animasi yang mengikuti setelan animasi Android. Room migrasi data v1 ke v2 tanpa hapus riwayat. CI wajib buka APK release dan debug di emulator sebelum publikasi.

## v1.3.0 — Playful refresh / Tampilan baru

**English:** The same mascot is used in the adaptive launcher icon and headers. Offline 3D goal artwork replaces the green-screen illustrations; user-supplied photos remain supported. A high-contrast dark palette, on-change liquid progress, and a brief savings celebration respect Android's animation scale. The goal form separates required fields from optional customization, pins Save within reach, and shows field-specific errors. The app still stores balances only after a deposit or an explicitly enabled recurring rule.

**Bahasa Indonesia:** Maskot yang sama tampil di ikon launcher dan header. Ilustrasi 3D offline mengganti gambar berlatar hijau; foto pilihan sendiri tetap bisa dipakai. Palet gelap lebih kontras, progres cair dan perayaan singkat saat saldo naik mengikuti setelan animasi Android. Form memisahkan isian wajib dari personalisasi opsional, tombol Simpan tetap terjangkau, dan kesalahan ditunjukkan pada kolomnya. Saldo tetap hanya berubah setelah Tabung atau aturan berkala yang sengaja diaktifkan.
