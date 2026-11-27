# NetherNexus 2D Physics

The first physics foundation is a small C++20, axis-aligned 2D simulation module.

## Body types

- `StaticBody2D`: immovable collision geometry.
- `CharacterBody2D`: velocity-driven body for player and character movement.
- `RigidBody2D`: body affected by world gravity, with configurable gravity scale and mass metadata.

## Collision layers and masks

Each body has a 32-bit collision layer and collision mask. A layer identifies the groups the body belongs to; a mask identifies the groups it can detect. The default layer is bit 0 (`1u`) and the default mask enables all 32 layers.

A pair collides only when both filters agree:

- A's mask includes at least one bit from B's layer.
- B's mask includes at least one bit from A's layer.

For example, `setCollisionLayer(1u << 1)` places a body on layer 2, while `setCollisionMask(1u << 1)` lets it detect that layer. These values are bitmasks, not one-based layer numbers.

## Basic usage

```cpp
#include "Physics/PhysicsWorld2D.h"

nexus::physics::PhysicsWorld2D world({0.0F, 980.0F});
nexus::physics::StaticBody2D floor({-500.0F, 300.0F}, {1000.0F, 32.0F});
nexus::physics::CharacterBody2D player({0.0F, 0.0F}, {24.0F, 48.0F});

world.addBody(floor);
world.addBody(player);

player.move({180.0F, 0.0F});
world.step(1.0F / 60.0F);
```

Positions describe the top-left corner of each rectangular body. Sizes are clamped to non-negative values. The world stores non-owning body pointers, so bodies must outlive their registration; remove them or clear the world before destroying them.

## Current scope and limitations

This is an initial foundation, not a complete production physics engine. It currently provides rectangular AABB overlap resolution on separate X/Y movement axes, basic character velocity, and gravity for rigid bodies. Bodies also expose per-step floor, ceiling, and wall contact flags (`isOnFloor`, `isOnCeiling`, `isOnWall`), reset at the beginning of each simulation step. It does not yet include rotation-aware shapes, slopes, continuous collision detection, friction, restitution, joints, sleeping, broadphase acceleration, or a full force/impulse solver. The mass property is reserved for later solver work and does not yet change motion.

Run `ctest` after configuring the project to execute the physics regression test alongside the existing engine tests.
