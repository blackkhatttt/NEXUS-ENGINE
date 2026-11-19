# NEXUS ENGINE — Roadmap
## Foundation — 0.1
- [x] C++20 and CMake project
- [x] Engine lifecycle and logging
- [x] Basic Vector2 type
- [x] Initial Node2D parent/child hierarchy
- [x] Desktop and Android build workflows

## NXS runtime — first integration
- [x] C++ lexer and token model
- [x] Project header and start block
- [x] Text, Number, NumberD and FalseTrue values
- [x] Variable declarations and assignment
- [x] print() and + expressions
- [x] Engine API to execute NXS source and files
- [x] Bootstrap script and desktop CI integration test
- [ ] Functions and parameters
- [ ] Conditions and loops
- [ ] Script modules and imports
- [ ] Runtime-to-node and component bindings
- [ ] Script lifecycle callbacks

## Runtime
- [ ] Persistent node identity
- [ ] Components and lifecycle
- [x] Android scene document model and readable JSON-based .nscene format (v1)
- [x] Scene Tree: select, add child, rename and delete nodes
- [x] Inspector: edit node name, position, rotation and scale
- [x] Save scene documents to Scenes/Main.nscene
- [ ] Native C++ .nscene loading and serialization
- [ ] Resource references and validation
- [ ] Fixed-step update loop

## 2D systems
- [ ] Backend-independent renderer API
- [ ] First graphics backend
- [ ] Texture and sprite resources
- [ ] Camera2D and coordinate conversion
- [ ] Basic custom 2D physics

## Tooling and languages
- [ ] C# editor shell and docking layout
- [ ] Scene tree, inspector, viewport, filesystem and output panels
- [ ] NXS editor tooling and diagnostics
- [ ] NXSM integration as an independent language

## Platforms
- [ ] Desktop platform layer
- [ ] Android platform layer
- [ ] Hybrid base-resolution adaptation
