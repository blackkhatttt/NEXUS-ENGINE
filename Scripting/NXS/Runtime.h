#pragma once
#include <cstddef>
#include <string>
#include <string_view>
#include <vector>
namespace nexus::nxs {
struct ExecutionResult {
 bool success = false;
 std::string projectName;
 std::vector<std::string> output;
 std::string error;
 std::size_t errorLine = 0;
};
class Runtime final {
public:
 [[nodiscard]] ExecutionResult execute(std::string_view source) const;
 [[nodiscard]] ExecutionResult executeFile(const std::string& path) const;
};
}