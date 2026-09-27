#!/usr/bin/env bash
# Naikin versionName default di app/build.gradle.kts (patch | minor | major).
set -euo pipefail

MODE="${1:-patch}"
FILE="app/build.gradle.kts"

if [ ! -f "$FILE" ]; then
  echo "Jalankan script ini dari root repo (app/build.gradle.kts nggak ketemu)." >&2
  exit 1
fi

CURRENT=$(grep -oE '\?: "[0-9]+\.[0-9]+\.[0-9]+"' "$FILE" | head -1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')
if [ -z "$CURRENT" ]; then
  echo "Nggak nemu pola versionName default di $FILE" >&2
  exit 1
fi

IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT"

case "$MODE" in
  major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
  patch) PATCH=$((PATCH + 1)) ;;
  *) echo "Mode harus patch|minor|major" >&2; exit 1 ;;
esac

NEW="${MAJOR}.${MINOR}.${PATCH}"
sed -i.bak "s/\?: \"${CURRENT}\"/?: \"${NEW}\"/" "$FILE"
rm -f "${FILE}.bak"

echo "versionName: ${CURRENT} -> ${NEW}"
