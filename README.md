# NEXUS ENGINE

An original cross-platform game engine targeting **desktop and Android**.

> Early development — APIs and file formats may change.

## Agreed direction
- Engine core: C++20
- Editor and tools: C# (planned)
- Game scripting: NXS (syntax designed independently)
- Math scripting: NXSM (independent language)
- First target: 2D, with architecture prepared for 3D
- Rendering: backend-independent API; OpenGL ES is the first planned backend
- Physics: custom basic 2D physics
- Scenes: readable text-based .nscene format (design in progress)
- Resolution: hybrid; project chooses a base resolution and the engine adapts presentation

## Repository layout
- `Engine/` — C++ runtime
- `Editor/` — C# editor (planned)
- `Scripting/` — NXS runtime and bindings (planned)
- `Platform/` — desktop and Android layers (planned)
- `Projects/` — examples and bootstrap app
- `docs/` — architecture and roadmap

## Build
Requirements: CMake 3.20+ and a C++20 compiler.

```bash
cmake -S . -B build
cmake --build build
./build/nexus_demo
```

On Windows: `build\nexus_demo.exe`.

## Current milestone
The initial foundation includes engine startup/shutdown, logging, a Vector2 type, and a basic Node2D hierarchy. Rendering, physics, editor tooling, and NXS integration are future milestones.

## License
The repository's existing LICENSE file governs this project.
