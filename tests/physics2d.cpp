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
    assert(player.isOnWall());
    assert(!player.isOnFloor());

    RigidBody2D box({0.0F, 0.0F}, {10.0F, 10.0F});
    world.addBody(box);
    world.step(1.0F);
    assert(box.getPosition().y == 40.0F);
    assert(box.getVelocity().y == 0.0F);
    assert(box.isOnFloor());

    const auto count = world.getBodyCount();
    assert(count == 4);
    assert(world.removeBody(box));
    assert(world.getBodyCount() == 3);

    // Collision layers and masks must agree in both directions.
    PhysicsWorld2D filterWorld({0.0F, 0.0F});
    StaticBody2D filteredWall({20.0F, 0.0F}, {10.0F, 20.0F});
    CharacterBody2D filteredPlayer({0.0F, 0.0F}, {10.0F, 10.0F});
    filteredWall.setCollisionLayer(2u);
    filteredPlayer.setCollisionLayer(1u);
    filteredPlayer.setCollisionMask(1u);
    filterWorld.addBody(filteredWall);
    filterWorld.addBody(filteredPlayer);

    filteredPlayer.move({30.0F, 0.0F});
    filterWorld.step(1.0F);
    assert(filteredPlayer.getPosition().x == 30.0F);
    assert(!filteredPlayer.isOnWall());

    filteredPlayer.setPosition({0.0F, 0.0F});
    filteredPlayer.setCollisionMask(2u);
    filteredPlayer.move({30.0F, 0.0F});
    filterWorld.step(1.0F);
    assert(filteredPlayer.getPosition().x == 10.0F);
    assert(filteredPlayer.isOnWall());

    return 0;
}
