#pragma once

namespace nexus
{
    class Engine final
    {
    public:
        Engine() = default;
        ~Engine() = default;
        Engine(const Engine&) = delete;
        Engine& operator=(const Engine&) = delete;

        bool initialize();
        void shutdown();
        [[nodiscard]] bool isInitialized() const noexcept;

    private:
        bool initialized_ = false;
    };
}
