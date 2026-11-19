#include "Core/Engine.h"
#include "Core/Logger.h"
#include "Core/Version.h"
#include <string>

namespace nexus
{
    bool Engine::initialize()
    {
        if (initialized_)
        {
            Logger::warning("Engine is already initialized.");
            return true;
        }
        Logger::info(std::string(ENGINE_NAME) + " " + std::string(VERSION_STRING));
        Logger::info("Initializing core systems...");
        initialized_ = true;
        Logger::info("Engine initialized.");
        return true;
    }

    void Engine::shutdown()
    {
        if (!initialized_) return;
        Logger::info("Shutting down engine...");
        initialized_ = false;
        Logger::info("Engine stopped.");
    }

    bool Engine::isInitialized() const noexcept { return initialized_; }
}
