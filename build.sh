#!/bin/bash
# Build the app on this phone. Retries when a download fails; finished
# downloads are kept. The APK lands in app/build/outputs/apk/debug/.
cd "$(dirname "$0")"
for attempt in 1 2 3; do
  "$HOME/android-kit/gradle-8.14.3/bin/gradle" assembleDebug --console=plain --no-daemon 2>&1 | tee build.log
  code=${PIPESTATUS[0]}
  [ $code -eq 0 ] && { echo "BUILD OK: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"; exit 0; }
  grep -qE 'Could not GET|Could not HEAD|Could not download|UnknownHost|Connection reset|timed out' build.log || exit $code
  echo "A download failed; retrying ($attempt of 3)…"; sleep 10
done
exit $code
