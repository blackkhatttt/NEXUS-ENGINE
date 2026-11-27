#pragma once

#include "Math/Vector2.h"

namespace nexus::physics
{
    enum class BodyType2D
    {
        Static,
        Character,
        Rigid
    };

    class Body2D
    {
    public:
        Body2D(BodyType2D type, Vector2 position, Vector2 size);
        virtual ~Body2D() = default;

        Body2D(const Body2D&) = delete;
        Body2D& operator=(const Body2D&) = delete;

        [[nodiscard]] BodyType2D getType() const noexcept;
        [[nodiscard]] Vector2 getPosition() const noexcept;
        [[nodiscard]] Vector2 getSize() const noexcept;
        [[nodiscard]] Vector2 getVelocity() const noexcept;
        [[nodiscard]] bool isOnFloor() const noexcept;
        [[nodiscard]] bool isOnCeiling() const noexcept;
        [[nodiscard]] bool isOnWall() const noexcept;
        void setPosition(Vector2 position) noexcept;
        void setSize(Vector2 size) noexcept;
        void setVelocity(Vector2 velocity) noexcept;

    private:
        friend class PhysicsWorld2D;
        BodyType2D type_;
        Vector2 position_;
        Vector2 size_;
        Vector2 velocity_{};
        bool onFloor_ = false;
        bool onCeiling_ = false;
        bool onWall_ = false;
    };

    class StaticBody2D final : public Body2D
    {
    public:
        explicit StaticBody2D(Vector2 position = {}, Vector2 size = {1.0F, 1.0F});
    };

    class CharacterBody2D final : public Body2D
    {
    public:
        explicit CharacterBody2D(Vector2 position = {}, Vector2 size = {1.0F, 1.0F});
        void move(Vector2 velocity) noexcept;
    };

    class RigidBody2D final : public Body2D
    {
    public:
        explicit RigidBody2D(Vector2 position = {}, Vector2 size = {1.0F, 1.0F});
        void setMass(float mass) noexcept;
        [[nodiscard]] float getMass() const noexcept;
        void setGravityScale(float scale) noexcept;
        [[nodiscard]] float getGravityScale() const noexcept;

    private:
        friend class PhysicsWorld2D;
        float mass_ = 1.0F;
        float gravityScale_ = 1.0F;
    };
}
