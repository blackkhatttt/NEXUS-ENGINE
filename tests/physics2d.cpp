#include "Physics/PhysicsWorld2D.h"
#include <cassert>

int main()
{
    using namespace nexus;
    using namespace nexus::physics;

    PhysicsWorld2D world({0.0F, 100.0F});
    StaticBody2D floor({-100.0F, 50.0F}, {200.0F, 10.0F});
    StaticBody2D wall({20.0F, 0.0F}, {10.0F, 50.0F});
    CharacterBody2D player({0.0F, 0.0F}, {10.0F, 10.0F});
    world.addBody(floor);
    world.addBody(wall);
    world.addBody(player);

    player.move({30.0F, 0.0F});
    world.step(1.0F);
    assert(player.getPosition().x == 10.0F);
    assert(player.getVelocity().x == 0.0F);

    RigidBody2D box({0.0F, 0.0F}, {10.0F, 10.0F});
    world.addBody(box);
    world.step(1.0F);
    assert(box.getPosition().y == 40.0F);
    assert(box.getVelocity().y == 0.0F);

    const auto count = world.getBodyCount();
    assert(count == 4);
    assert(world.removeBody(box));
    assert(world.getBodyCount() == 3);
    return 0;
}
