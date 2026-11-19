# NEXUS Editor for Android

NEXUS Editor 0.2.0 is the first functional Android project workspace for NEXUS ENGINE.

## Features
- Create a project folder using Android's Storage Access Framework.
- Generate `project.nproject`, `Assets/`, `Scenes/`, and `Scripts/main.nxs`.
- Open an existing project folder.
- Open and edit NXS scripts.
- Save scripts back to the selected project folder.
- Execute NXS source through the native C++ runtime and display output/errors in the editor.

## Project structure
```
MyProject/
├── project.nproject
├── Assets/
├── Scenes/
└── Scripts/
    └── main.nxs
```

## Build
Requires JDK 17, Android SDK, NDK 27.0.12077973, CMake 3.22.1, and Gradle 8.9.

From the repository root:
```bash
cd Android
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

The editor currently provides text editing and execution for the supported NXS 0.1 syntax. Scene visualization, node tree, inspector, syntax highlighting, and the graphical 2D viewport are future milestones.
