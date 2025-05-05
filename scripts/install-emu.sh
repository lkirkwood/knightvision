#!/usr/bin/env sh

set -e

gradle assembleDebug
adb -s emulator-5554 install ./app/build/outputs/apk/debug/app-debug.apk
