# NEXUS ENGINE

An original cross-platform game engine targeting desktop and Android.

> Early development — APIs and file formats may change.

## Architecture
- Engine core: C++20
- Editor and tools: C# (planned)
- Game scripting: NXS
- Math scripting: NXSM (independent language)
- First target: 2D, with architecture prepared for 3D
- Rendering: backend-independent API; OpenGL ES is the first planned backend
- Physics: custom basic 2D physics
- Scenes: readable text-based .nscene format (design in progress)

## Repository layout
- Engine/ — C++ runtime and scene foundation
- Scripting/NXS/ — NXS lexer and interpreter
- Editor/ — C# editor (planned)
- Platform/ — desktop and Android layers
- Projects/ — example projects and bootstrap
- docs/ — architecture and roadmap

## Build desktop runtime

Requirements: CMake 3.20+ and a C++20 compiler.

\`\`\`bash
cmake -S . -B build
cmake --build build
ctest --test-dir build --output-on-failure
./build/nexus_demo
\`\`\`

On Windows, run build/nexus_demo.exe.

## NXS integration — first milestone

NEXUS ENGINE now compiles and owns an NXS runtime. The engine API can execute source text with Engine::runNXS() or execute a script file with Engine::runNXSFile().

The first integration supports the established NXS prototype:
- game "PROJECT NAME" start { ... } project header and start block
- var name Type = value declarations
- Text, Number, NumberD and FalseTrue
- Assignment to declared variables
- print(expression)
- + for text concatenation and numeric addition
- # comments and newline / semicolon separators

Example script: Projects/Bootstrap/main.nxs.

This is the first interpreter integration, not the complete NXS language. Functions, conditions, loops, objects, engine-node APIs and event callbacks are not implemented yet.

## Current milestone
The C++ engine core, initial Node2D hierarchy, NXS lexer/interpreter and C++ engine-to-NXS execution API are in place. Desktop CI runs the NXS bootstrap test. Android workflow compiles the same native runtime into the Android library and packages the NEXUS Hub APK.

## License
The repository's existing LICENSE file governs this project.
