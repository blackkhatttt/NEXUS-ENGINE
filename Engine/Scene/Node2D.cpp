#include "Scene/Node2D.h"
#include <algorithm>
#include <stdexcept>
#include <utility>

namespace nexus
{
    Node2D::Node2D(std::string name) : name_(std::move(name)) {}

    void Node2D::addChild(Node2D& child)
    {
        if (&child == this) throw std::invalid_argument("A node cannot be its own child.");
        for (Node2D* ancestor = this; ancestor != nullptr; ancestor = ancestor->parent_)
            if (ancestor == &child) throw std::invalid_argument("Adding this child would create a scene cycle.");
        if (child.parent_ == this) return;
        if (child.parent_ != nullptr) child.parent_->removeChild(child);
        child.parent_ = this;
        children_.push_back(&child);
    }

    bool Node2D::removeChild(const Node2D& child)
    {
        const auto it = std::find(children_.begin(), children_.end(), &child);
        if (it == children_.end()) return false;
        (*it)->parent_ = nullptr;
        children_.erase(it);
        return true;
    }

    const std::string& Node2D::getName() const noexcept { return name_; }
    void Node2D::setName(std::string name) { name_ = std::move(name); }
    Node2D* Node2D::getParent() const noexcept { return parent_; }
    const std::vector<Node2D*>& Node2D::getChildren() const noexcept { return children_; }
}
