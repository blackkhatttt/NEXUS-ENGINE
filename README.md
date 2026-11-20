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
- const name Type = value immutable declarations
- Text, Number, NumberD and FalseTrue
- Assignment to declared variables (constants are immutable)
- print(expression)
- + for text concatenation and numeric addition
- if / eif / else conditional blocks (parentheses around conditions are optional)
- ==, !=, >, >=, <, <= comparisons
- AND, OR and NOT logical operators
- # comments and newline / semicolon separators

Example script: Projects/Bootstrap/main.nxs.

This is an early interpreter integration, not the complete NXS language. Functions, loops, objects, engine-node APIs and event callbacks are not implemented yet. NXS `const` declarations are immutable and reject reassignment.

## Current milestone
The C++ engine core, initial Node2D hierarchy, NXS lexer/interpreter and C++ engine-to-NXS execution API are in place. Desktop CI runs the NXS bootstrap test. Android workflow compiles the same native runtime into the Android library and packages the NEXUS Hub APK.

## License
The repository's existing LICENSE file governs this project.

## Editor 0.7.0 — first stage
The Android Scene Editor adds a `Control` node. A Control node can have UI components attached to it: `Button`, `Joystick`, `Arrows` and `Panel`. Component assignments are stored in the `.nscene` document. This milestone stores the component configuration; it does not yet render interactive controls in a running game.


### NEXUS Editor 0.8.0 — Play Mode
The Android editor can launch the saved `Scenes/Main.nscene` in a fullscreen immersive runtime. The first runtime renderer displays `Node2D` nodes and `Control` components; Button, Joystick and Arrows respond to touch, while Panel is rendered as an overlay. This is the first Play Mode foundation: NXS gameplay callbacks, sprite rendering, and project/current-scene selection remain in development.


### NEXUS Editor 0.9.0 — conditions and sprites
NXS now supports `if`, `eif` and `else` blocks. Conditions may be written with or without surrounding parentheses. Comparison operators are `==`, `!=`, `>`, `>=`, `<` and `<=`; logical operators are `AND`, `OR` and `NOT`.

The Android scene editor adds `Sprite2D` nodes and an image picker for assigning a texture. The selected image URI is saved in the `.nscene` document, and Play Mode loads and draws the assigned texture. Sprite dimensions use the source image dimensions multiplied by the node scale. This is the first texture path; animation, atlases, filtering controls, and a desktop renderer are still future work.
