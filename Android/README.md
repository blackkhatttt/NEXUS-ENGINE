# NEXUS Hub for Android

First Android application shell for NEXUS ENGINE 0.1.

- Native Android activity named NEXUS Hub.
- Loads the C++ shared library through JNI.
- Displays the native engine version and bridge status.
- Create Project, Open Project, and Documentation are informational placeholders.

## Build
Requires JDK 17, Android SDK, NDK 27.0.12077973, CMake 3.22.1, and Gradle 8.9.

From the repository root:
```bash
cd Android
gradle assembleDebug
```

APK: app/build/outputs/apk/debug/app-debug.apk

This is a Hub shell, not yet a game editor. Project management, rendering, scene editing, and NXS integration are future work.
