#pragma once
#include "Math/Vector2.h"
#include <string>
#include <vector>

namespace nexus
{
    class Node2D
    {
    public:
        explicit Node2D(std::string name = "Node2D");
        virtual ~Node2D() = default;
        Node2D(const Node2D&) = delete;
        Node2D& operator=(const Node2D&) = delete;

        void addChild(Node2D& child);
        bool removeChild(const Node2D& child);
        [[nodiscard]] const std::string& getName() const noexcept;
        void setName(std::string name);
        [[nodiscard]] Node2D* getParent() const noexcept;
        [[nodiscard]] const std::vector<Node2D*>& getChildren() const noexcept;

        Vector2 position{};
        float rotation = 0.0F;
        Vector2 scale{1.0F, 1.0F};

    private:
        std::string name_;
        Node2D* parent_ = nullptr;
        std::vector<Node2D*> children_;
    };
}
