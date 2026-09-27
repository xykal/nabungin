# PRD - Nabungin

## 1. Executive brief

Nabungin adalah aplikasi tabungan pribadi offline-first untuk Android. Masalah yang disasar: orang gagal nabung bukan karena nggak tahu harus nabung, tapi karena nggak ada visibilitas pace dan nggak ada feedback konsisten. Aplikasi keuangan mainstream mengharuskan login bank, kirim data ke server, dan UI-nya penuh fitur investasi yang bikin tujuan tabungan sederhana jadi ribet.

Real-world utility check:

- Dipakai tanpa internet, tanpa akun, tanpa permission jaringan sama sekali.
- Satu layar utama cukup buat jawab "hari ini harus nabung berapa".
- Data tetap milik user: export JSON bisa dibuka di editor teks biasa.

## 2. Target user

- Individu 18-35 yang punya 1-5 tujuan tabungan konkret (dana darurat, gadget, liburan, DP).
- Nggak sabar dengan aplikasi yang minta KYC atau sambung rekening.
- Ngerti nominal rupiah dan mau lihat angka apa adanya.

## 3. Scope v1.0 (sudah diimplementasikan)

1. Onboarding implisit: pertama buka langsung diisi contoh tujuan "Dana Darurat" supaya layar nggak kosong.
2. Tujuan: nama, target, deadline opsional, warna (6 pilihan), ikon (10 pilihan), kategori (6 pilihan).
3. Setoran: manual, backdate (hari ini/kemarin/tanggal lain), catatan, nominal dari keypad sendiri dengan chip cepat.
4. Auto-save: aturan per tujuan (nominal, interval harian/mingguan/bulanan, jam eksekusi). `AutoSaveWorker` cek tiap jam, setor hanya saat jatuh tempo, lalu kirim ringkasan notifikasi.
5. Pengingat harian: jam dipilih user, pesan menyesuaikan streak dan setoran hari itu.
6. Statistik: total, bulan berjalan, grafik 30 hari, hari aktif, rata-rata per hari aktif, ETA per tujuan, peringkat tujuan.
7. Pace: kebutuhan per hari dihitung dari sisa target dibagi hari tersisa; kalau tanpa deadline, app pakai laju rata-rata historis buat ETA.
8. Streak: hari beruntun dengan minimal satu setoran (toleran: hari ini belum setor tapi kemarin setor tetap dihitung).
9. Kunci aplikasi: PIN 6 digit (PBKDF2-HMAC-SHA256, 120k iterasi, salt 128-bit) plus biometrik opsional; re-lock otomatis saat app ke background.
10. Backup: export/import JSON via SAF, format berschema (`schema: 1`).
11. Tema: sistem/terang/gelap, palet kertas hangat dengan aksen moss green.

## 4. Proactive innovation (ngide yang tetap realistis)

- **Pace adaptif tanpa deadline**: kalau deadline kosong, ETA dihitung dari laju aktual user, bukan asumsi. User langsung lihat "dengan laju segini, target lu kelar 12 Mei 2027".
- **Auto-save idempotent**: aturan menyimpan `lastRunEpochDay`. Worker bisa jalan 24x sehari tanpa bikin setoran dobel.
- **Streak tanpa backend**: dihitung dari tabel `deposits` dengan `GROUP BY` di memori, jadi tetap konsisten setelah import backup.
- **Backup self-describing**: file JSON menyimpan `app` dan `schema`, jadi import bisa nolak file asing sebelum menyentuh database.
- **Notifikasi yang nggak nyampah**: pesan berubah sesuai konteks (streak jalan, hari ini belum setor, belum ada tujuan).
- **Zero-network manifest**: permission internet nggak dideklarasikan, jadi klaim privasi bisa dibuktikan lewat `aapt dump permissions`.

## 5. Non-goals v1.0

- Sinkronisasi cloud, multi-device, atau login.
- Sambung rekening bank / e-wallet.
- Investasi, bunga, atau proyeksi inflasi.
- Widget home screen dan Wear OS.

## 6. Roadmap berikutnya

| Prioritas | Item | Catatan teknis |
| --- | --- | --- |
| P0 | Widget "hari ini" | Glance API, baca Room lewat repository read-only |
| P0 | Multi-currency | Simpan nominal dalam minor unit + kode mata uang, refactor `Money` |
| P1 | Skema Room v2 + migrasi eksplisit | Tambah tabel `tags` dan kolom `currency`, tulis `Migration(1,2)` |
| P1 | Streak freeze | Satu hari libur terjadwal per minggu tanpa memutus streak |
| P2 | Auto-save berbasis persen pemasukan | Butuh input pendapatan bulanan di setelan |
| P2 | Grafik cumulative per tujuan | Canvas area chart dengan sumbu label |

## 7. Success metrics

- Time-to-first-deposit di bawah 30 detik dari cold start.
- 70% user dengan minimal satu tujuan masih mencatat setoran di minggu ke-3.
- APK release di bawah 6 MB dan cold start di bawah 700 ms di kelas menengah.
- Nol crash di Play Console / crash reporting lokal.
