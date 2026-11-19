#include "Core/Logger.h"
#include <iostream>

namespace nexus
{
    void Logger::log(LogLevel level, std::string_view message)
    {
        switch (level)
        {
            case LogLevel::Info: std::cout << "[NEXUS][INFO] "; break;
            case LogLevel::Warning: std::cout << "[NEXUS][WARNING] "; break;
            case LogLevel::Error: std::cerr << "[NEXUS][ERROR] "; break;
        }

        if (level == LogLevel::Error)
            std::cerr << message << '\n';
        else
            std::cout << message << '\n';
    }

    void Logger::info(std::string_view message) { log(LogLevel::Info, message); }
    void Logger::warning(std::string_view message) { log(LogLevel::Warning, message); }
    void Logger::error(std::string_view message) { log(LogLevel::Error, message); }
}
