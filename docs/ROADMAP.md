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
- [x] Immutable const declarations and reassignment protection
- [x] print() and + expressions
- [x] Engine API to execute NXS source and files
- [x] Bootstrap script and desktop CI integration test
- [ ] Functions and parameters
- [x] `if`, `eif` and `else` conditional blocks with optional condition parentheses
- [x] Comparison operators and AND / OR / NOT logic
- [ ] Loops
- [ ] Script modules and imports
- [ ] Runtime-to-node and component bindings
- [ ] Script lifecycle callbacks

## Runtime
- [ ] Persistent node identity
- [ ] Components and lifecycle
- [x] Android scene document model and readable JSON-based .nscene format (v1)
- [x] Scene Tree: select, add child, rename and delete nodes
- [x] Control node with attachable Button, Joystick, Arrows and Panel components
- [x] Inspector: edit node name, position, rotation and scale
- [x] Save scene documents to Scenes/Main.nscene
- [ ] Native C++ .nscene loading and serialization
- [ ] Resource references and validation
- [ ] Fixed-step update loop

## 2D systems
- [x] Interactive Android 2D scene viewport with grid and origin
- [x] Touch selection and direct node dragging in the viewport
- [x] Pan and pinch-to-zoom navigation
- [ ] Backend-independent renderer API
- [ ] First graphics backend
- [x] Android Sprite2D node and image URI assignment
- [x] Play Mode texture loading and sprite drawing
- [ ] Texture atlases, animation and resource import pipeline
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

## Editor 0.7.0 — first stage
- [x] NXS `const` declarations with immutable values
- [x] Control scene node
- [x] Attach and remove Button, Joystick, Arrows and Panel control components
- [ ] Sprite assignment and viewport rendering
- [ ] NXS script attachment to scene nodes


## Editor 0.8.0 — Play Mode foundation
- [x] Launch the saved Main.nscene from the scene editor
- [x] Fullscreen immersive Android runtime activity
- [x] Runtime rendering for Node2D and Control nodes
- [x] Touch interaction for Button, Joystick and Arrows components
- [x] Panel component rendering
- [ ] Runtime movement and gameplay APIs exposed to NXS scripts
- [ ] Run Project and Run Current Scene selection
- [ ] Sprite rendering and resource loading


## Editor 0.9.0 — conditions and sprites
- [x] NXS if / eif / else conditional execution
- [x] Optional parentheses around conditions
- [x] Comparison operators: ==, !=, >, >=, <, <=
- [x] Logical operators: AND, OR, NOT
- [x] Sprite2D scene node
- [x] Android image picker and persistent texture URI in .nscene
- [x] Play Mode loads and renders assigned sprite textures
- [ ] Sprite preview in the editor viewport
- [ ] Sprite animation and texture atlases
- [ ] NXS script attachment and gameplay callbacks
