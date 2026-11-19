# Android platform layer

NEXUS ENGINE is designed to run projects on Android as well as desktop.

## Current state

This folder currently contains the first native-library entry point for NDK builds. It is **not yet a complete Android application or APK**.

## Planned Android integration

- Android SDK and NDK build pipeline
- Gradle application wrapper
- Native shared library built with CMake
- Application lifecycle and pause/resume handling
- Touch input and multi-touch
- Screen-size, density, orientation and safe-area information
- OpenGL ES rendering surface and context lifecycle
- Asset packaging and project loading

## Initial native build

The Android CMake target is named `nexus_android`.

An Android NDK toolchain build can be configured with:

```bash
cmake -S . -B build-android \
  -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_PLATFORM=android-24

cmake --build build-android
```

`android-24` is a provisional CI baseline, not a final minimum-device requirement. The final minimum Android version will be decided after testing.

Building this native library alone does not produce an installable APK. The Gradle wrapper and Android application shell are still pending.
