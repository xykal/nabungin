#!/usr/bin/env bash
# Push repo ini ke GitHub sebagai xykal, tanpa nulis token ke dalam repo.
# Pakai: GH_TOKEN=ghp_xxx REPO_NAME=nabungin bash scripts/push-to-github.sh
set -euo pipefail

: "${GH_TOKEN:?Set GH_TOKEN dulu (export GH_TOKEN=...)}"
REPO_NAME="${REPO_NAME:-nabungin}"
VISIBILITY="${VISIBILITY:-public}"
OWNER="${OWNER:-xykal}"

git config --local user.name "xykal"
git config --local user.email "xykal@users.noreply.github.com"

if [ ! -d .git ]; then
  git init -b main
fi

git add -A
if git diff --cached --quiet; then
  echo "Nggak ada perubahan buat di-commit."
else
  git commit -m "${COMMIT_MSG:-feat: Nabungin - aplikasi tabungan offline-first (Kotlin + Jetpack Compose)}"
fi

# Bikin repo kalau belum ada (idempotent).
STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  "https://api.github.com/repos/${OWNER}/${REPO_NAME}")

if [ "$STATUS" = "404" ]; then
  curl -sS -X POST \
    -H "Authorization: Bearer ${GH_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    "https://api.github.com/user/repos" \
    -d "{\"name\":\"${REPO_NAME}\",\"private\":$([ "$VISIBILITY" = "private" ] && echo true || echo false),\"description\":\"Nabungin - app tabungan offline-first (Kotlin + Jetpack Compose)\"}" \
    > /dev/null
  echo "Repo ${OWNER}/${REPO_NAME} dibuat (${VISIBILITY})."
fi

git remote remove origin 2>/dev/null || true
git remote add origin "https://x-access-token:${GH_TOKEN}@github.com/${OWNER}/${REPO_NAME}.git"
git push -u origin main

echo "Beres. Buka https://github.com/${OWNER}/${REPO_NAME}/actions buat lihat build APK."
