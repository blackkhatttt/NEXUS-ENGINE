#pragma once
#include <string_view>

namespace nexus
{
    enum class LogLevel { Info, Warning, Error };

    class Logger final
    {
    public:
        static void log(LogLevel level, std::string_view message);
        static void info(std::string_view message);
        static void warning(std::string_view message);
        static void error(std::string_view message);
    };
}
