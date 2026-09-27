# GitHub Secrets untuk build APK

Set di: `Repository -> Settings -> Secrets and variables -> Actions -> New repository secret`.

| Nama secret | Wajib? | Isi |
| --- | --- | --- |
| `KEYSTORE_BASE64` | Wajib | Hasil `base64 -w0 release.keystore` |
| `KEYSTORE_PASSWORD` | Wajib | Password keystore |
| `KEY_ALIAS` | Wajib | Alias key di keystore |
| `KEY_PASSWORD` | Wajib | Password key (PKCS12 biasanya sama dengan store password) |

Kalau ada secret signing kosong, workflow gagal sebelum build. Tidak ada fallback debug-key untuk APK release.

## Urutan setup

```bash
# 1. Bikin keystore (sekali saja, simpan baik-baik; hilang = nggak bisa update app di Play)
bash scripts/gen-keystore.sh

# 2. Ambil nilai base64 yang dicetak
cat release.keystore.base64

# 3. Tempel ke GitHub Secrets sesuai tabel di atas
```

## Yang TIDAK boleh dilakukan

- Jangan simpan keystore atau token di dalam repo. `.gitignore` sudah memblok `*.jks`, `*.keystore`, `keystore.properties`, `*.base64`, `.env`, dan folder `uploads/`.
- Jangan pakai personal access token (PAT) sebagai `secrets.GITHUB_TOKEN`. Workflow ini cuma butuh `GITHUB_TOKEN` bawaan Actions.
- Jangan print isi secret ke log. Workflow hanya mengecek ada/tidak, dan file `release.keystore` dihapus lagi lewat step `if: always()`.
- Kalau ada token/key yang pernah telanjur di-commit atau dibagikan, anggap bocor: revoke dulu, baru bikin yang baru.

## Trigger workflow

| Trigger | Hasil |
| --- | --- |
| Push ke `main`/`master` | Debug + release APK sebagai artifact |
| Pull request | Sama, tapi cache Gradle read-only (nggak nulis cache ke branch utama) |
| Push tag `v1.3.0` | Build + GitHub Release dengan APK dan `SHA256SUMS.txt` |
| `workflow_dispatch` biasa | Build manual, versionName jadi `1.2.0-ci.<run number>` |
| `workflow_dispatch` + `create_release` | Build + Release pakai tag `v<versionName>` |
| `workflow_dispatch` + `bump_version` | Naikin versionName patch, commit dengan identitas `xykal`, push ke branch |

Identitas commit otomatis:

```
git config --local user.name "xykal"
git config --local user.email "xykal@users.noreply.github.com"
```
