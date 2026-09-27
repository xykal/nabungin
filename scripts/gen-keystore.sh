#!/usr/bin/env bash
# Bikin keystore release + cetak nilai yang harus ditempel ke GitHub Secrets.
# Output: keystore.properties (gitignored) dan release.keystore.base64
set -euo pipefail

KEYSTORE_FILE="${1:-release.keystore}"
ALIAS="${2:-nabungin}"

if [ -f "$KEYSTORE_FILE" ]; then
  echo "$KEYSTORE_FILE sudah ada. Hapus dulu kalau mau bikin baru." >&2
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "keytool nggak ada. Install JDK 17 dulu." >&2
  exit 1
fi

read -r -s -p "Password keystore (min 6 karakter): " STORE_PASS
echo
read -r -s -p "Ulangi password: " STORE_PASS_2
echo
[ "$STORE_PASS" = "$STORE_PASS_2" ] || { echo "Password nggak sama." >&2; exit 1; }

keytool -genkeypair -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 -validity 10950 \
  -storetype PKCS12 \
  -storepass "$STORE_PASS" \
  -keypass "$STORE_PASS" \
  -dname "CN=Nabungin, OU=xykal, O=xykal, L=Jakarta, C=ID"

base64 -w0 "$KEYSTORE_FILE" > "${KEYSTORE_FILE}.base64"

cat > keystore.properties <<EOF
storeFile=${KEYSTORE_FILE}
storePassword=${STORE_PASS}
keyAlias=${ALIAS}
keyPassword=${STORE_PASS}
EOF
chmod 600 keystore.properties

cat <<MSG

Selesai. Sekarang isi GitHub Secrets (Settings > Secrets and variables > Actions):
  KEYSTORE_BASE64    -> isi file ${KEYSTORE_FILE}.base64
  KEYSTORE_PASSWORD  -> password keystore
  KEY_ALIAS          -> ${ALIAS}
  KEY_PASSWORD       -> password yang sama

File keystore.properties, *.keystore, dan *.base64 sudah masuk .gitignore.
JANGAN pernah commit file-file itu.
MSG
