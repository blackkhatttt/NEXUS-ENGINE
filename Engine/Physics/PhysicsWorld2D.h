#pragma once

#include "Physics/Body2D.h"
#include <vector>

namespace nexus::physics
{
    class PhysicsWorld2D
    {
    public:
        explicit PhysicsWorld2D(Vector2 gravity = {0.0F, 980.0F});

        void addBody(Body2D& body);
        bool removeBody(Body2D& body);
        void clear() noexcept;
        void setGravity(Vector2 gravity) noexcept;
        [[nodiscard]] Vector2 getGravity() const noexcept;
        [[nodiscard]] std::size_t getBodyCount() const noexcept;

        // Advances the simulation by delta seconds.
        void step(float delta);

    private:
        [[nodiscard]] static bool overlaps(const Body2D& a, const Body2D& b) noexcept;
        void moveAxis(Body2D& body, float amount, bool horizontal);

        Vector2 gravity_;
        std::vector<Body2D*> bodies_;
    };
}
