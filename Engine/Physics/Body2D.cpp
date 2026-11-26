#include "Physics/Body2D.h"
#include <algorithm>

namespace nexus::physics
{
    Body2D::Body2D(BodyType2D type, Vector2 position, Vector2 size)
        : type_(type), position_(position),
          size_{std::max(0.0F, size.x), std::max(0.0F, size.y)}
    {
    }

    BodyType2D Body2D::getType() const noexcept { return type_; }
    Vector2 Body2D::getPosition() const noexcept { return position_; }
    Vector2 Body2D::getSize() const noexcept { return size_; }
    Vector2 Body2D::getVelocity() const noexcept { return velocity_; }
    void Body2D::setPosition(Vector2 position) noexcept { position_ = position; }
    void Body2D::setSize(Vector2 size) noexcept
    {
        size_ = {std::max(0.0F, size.x), std::max(0.0F, size.y)};
    }
    void Body2D::setVelocity(Vector2 velocity) noexcept { velocity_ = velocity; }

    StaticBody2D::StaticBody2D(Vector2 position, Vector2 size)
        : Body2D(BodyType2D::Static, position, size) {}

    CharacterBody2D::CharacterBody2D(Vector2 position, Vector2 size)
        : Body2D(BodyType2D::Character, position, size) {}

    void CharacterBody2D::move(Vector2 velocity) noexcept { setVelocity(velocity); }

    RigidBody2D::RigidBody2D(Vector2 position, Vector2 size)
        : Body2D(BodyType2D::Rigid, position, size) {}

    void RigidBody2D::setMass(float mass) noexcept { mass_ = std::max(0.001F, mass); }
    float RigidBody2D::getMass() const noexcept { return mass_; }
    void RigidBody2D::setGravityScale(float scale) noexcept { gravityScale_ = scale; }
    float RigidBody2D::getGravityScale() const noexcept { return gravityScale_; }
}
