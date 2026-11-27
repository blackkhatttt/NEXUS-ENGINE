#include "Physics/PhysicsWorld2D.h"
#include <algorithm>

namespace nexus::physics
{
    PhysicsWorld2D::PhysicsWorld2D(Vector2 gravity) : gravity_(gravity) {}

    void PhysicsWorld2D::addBody(Body2D& body)
    {
        if (std::find(bodies_.begin(), bodies_.end(), &body) == bodies_.end())
            bodies_.push_back(&body);
    }

    bool PhysicsWorld2D::removeBody(Body2D& body)
    {
        const auto it = std::find(bodies_.begin(), bodies_.end(), &body);
        if (it == bodies_.end()) return false;
        bodies_.erase(it);
        return true;
    }

    void PhysicsWorld2D::clear() noexcept { bodies_.clear(); }
    void PhysicsWorld2D::setGravity(Vector2 gravity) noexcept { gravity_ = gravity; }
    Vector2 PhysicsWorld2D::getGravity() const noexcept { return gravity_; }
    std::size_t PhysicsWorld2D::getBodyCount() const noexcept { return bodies_.size(); }

    bool PhysicsWorld2D::overlaps(const Body2D& a, const Body2D& b) noexcept
    {
        const Vector2 ap = a.position_;
        const Vector2 bp = b.position_;
        const Vector2 as = a.size_;
        const Vector2 bs = b.size_;
        return ap.x < bp.x + bs.x && ap.x + as.x > bp.x &&
               ap.y < bp.y + bs.y && ap.y + as.y > bp.y;
    }

    void PhysicsWorld2D::moveAxis(Body2D& body, float amount, bool horizontal)
    {
        if (amount == 0.0F) return;

        const float start = horizontal ? body.position_.x : body.position_.y;
        const float end = start + amount;
        const float bodyExtent = horizontal ? body.size_.x : body.size_.y;
        float resolved = end;
        bool collided = false;

        for (Body2D* other : bodies_)
        {
            if (other == nullptr || other == &body) continue;
            if ((body.collisionMask_ & other->collisionLayer_) == 0u ||
                (other->collisionMask_ & body.collisionLayer_) == 0u)
                continue;

            const bool overlapsOtherAxis = horizontal
                ? (body.position_.y < other->position_.y + other->size_.y &&
                   body.position_.y + body.size_.y > other->position_.y)
                : (body.position_.x < other->position_.x + other->size_.x &&
                   body.position_.x + body.size_.x > other->position_.x);

            if (!overlapsOtherAxis) continue;

            const float obstacleStart = horizontal ? other->position_.x : other->position_.y;
            const float obstacleEnd = obstacleStart +
                (horizontal ? other->size_.x : other->size_.y);

            if (amount > 0.0F)
            {
                const float oldEdge = start + bodyExtent;
                const float newEdge = end + bodyExtent;
                if (oldEdge <= obstacleStart && newEdge > obstacleStart)
                {
                    const float candidate = obstacleStart - bodyExtent;
                    if (!collided || candidate < resolved) resolved = candidate;
                    collided = true;
                }
            }
            else
            {
                const float oldEdge = start;
                const float newEdge = end;
                if (oldEdge >= obstacleEnd && newEdge < obstacleEnd)
                {
                    const float candidate = obstacleEnd;
                    if (!collided || candidate > resolved) resolved = candidate;
                    collided = true;
                }
            }
        }

        if (horizontal) body.position_.x = resolved;
        else body.position_.y = resolved;

        if (collided)
        {
            if (horizontal)
            {
                body.velocity_.x = 0.0F;
                body.onWall_ = true;
            }
            else
            {
                body.velocity_.y = 0.0F;
                if (amount > 0.0F) body.onFloor_ = true;
                else body.onCeiling_ = true;
            }
        }
    }

    void PhysicsWorld2D::step(float delta)
    {
        if (delta <= 0.0F) return;

        for (Body2D* body : bodies_)
        {
            if (body == nullptr || body->type_ == BodyType2D::Static) continue;

            body->onFloor_ = false;
            body->onCeiling_ = false;
            body->onWall_ = false;

            if (body->type_ == BodyType2D::Rigid)
            {
                auto& rigid = static_cast<RigidBody2D&>(*body);
                body->velocity_ += gravity_ * (rigid.gravityScale_ * delta);
            }

            const Vector2 displacement = body->velocity_ * delta;
            moveAxis(*body, displacement.x, true);
            moveAxis(*body, displacement.y, false);
        }
    }
}