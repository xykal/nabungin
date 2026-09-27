#!/usr/bin/env bash
set -u
mkdir -p smoke-results
failed=0
for variant in release debug; do
  package="dev.xykal.nabungin"
  [ "$variant" = debug ] && package="dev.xykal.nabungin.debug"
  echo "==== TEST $variant ($package) ===="
  adb logcat -c
  adb install -r "smoke-results/$variant.apk" || { echo 'install failed'; failed=1; continue; }
  adb shell am force-stop "$package"
  adb shell am start -W -n "$package/dev.xykal.nabungin.MainActivity"
  sleep 12
  adb logcat -d -v time > "smoke-results/$variant.log"
  echo "==== EXCEPTIONS $variant ===="
  grep -A55 -B2 -E "FATAL EXCEPTION|AndroidRuntime|NoSuchMethodError|ClassNotFoundException|VerifyError|InflateException" "smoke-results/$variant.log" | tail -160 || true
  pid=$(adb shell pidof "$package" | tr -d '\r' || true)
  echo "PID $variant: ${pid:-<dead>}"
  if [ -z "$pid" ] || grep -q "FATAL EXCEPTION" "smoke-results/$variant.log"; then failed=1; fi
  adb uninstall "$package" || true
done
exit "$failed"
