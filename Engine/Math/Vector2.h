#pragma once

namespace nexus
{
    struct Vector2
    {
        float x = 0.0F;
        float y = 0.0F;

        constexpr Vector2() = default;
        constexpr Vector2(float xValue, float yValue) : x(xValue), y(yValue) {}
        [[nodiscard]] constexpr Vector2 operator+(const Vector2& other) const { return {x + other.x, y + other.y}; }
        [[nodiscard]] constexpr Vector2 operator-(const Vector2& other) const { return {x - other.x, y - other.y}; }
        [[nodiscard]] constexpr Vector2 operator*(float scalar) const { return {x * scalar, y * scalar}; }
        constexpr Vector2& operator+=(const Vector2& other) { x += other.x; y += other.y; return *this; }
        [[nodiscard]] constexpr bool operator==(const Vector2& other) const { return x == other.x && y == other.y; }
    };
}
