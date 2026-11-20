# NEXUS Editor for Android

NEXUS Editor 0.6.0 is the Android project workspace for NEXUS ENGINE.

## Features
- Redesigned NEXUS Hub with project workspace cards and engine status.
- Custom blue/cyan bird launcher icon.
- Create NXS scripts directly from the project workspace; new scripts are saved in `Scripts/` and opened in the editor.
- Create projects using Android's Storage Access Framework.
- New projects use `project.nscene` as the canonical project manifest.
- Backward compatibility with the earlier `project.nproject` marker.
- Automatic project detection when selecting a project folder or a parent folder containing projects.
- SAF-compatible marker detection that does not rely on MIME classification for the custom `.nscene` extension.
- Recent project shortcut with persisted folder permission.
- Scene editor with Scene Tree, Inspector and interactive 2D viewport.
- Edit and run NXS scripts through the native C++ runtime.

## Project structure
```
MyProject/
├── project.nscene
├── project.nproject       # compatibility marker for older editor versions
├── Assets/
├── Scenes/
│   └── Main.nscene
└── Scripts/
    └── main.nxs
```

The project manifest is JSON and includes the project name, engine version, main scene and main NXS script. A scene file such as `Scenes/Main.nscene` stores the node hierarchy and transforms; the root-level `project.nscene` identifies the project itself.

## Automatic detection
Use **Open project** and select either:
- The project folder itself, containing `project.nscene` or legacy `project.nproject`.
- A parent folder whose immediate child folders contain one of those project markers.

If multiple projects are detected, the editor displays a selection list. The last opened project can be reopened from the home screen.

## Build
Requires JDK 17, Android SDK, NDK 27.0.12077973, CMake 3.22.1, and Gradle 8.9.

From the repository root:
```bash
cd Android
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`


### Project file extension compatibility
- Project manifests and scene files are created as `text/plain` through Android's Storage Access Framework so providers preserve the exact `.nscene` and `.nproject` names instead of appending `.json`.
- Existing projects with `project.nscene.json` or `project.nproject.json` are also detected for compatibility.
