#pragma once
#include "Scripting/NXS/Runtime.h"
#include <string>
#include <string_view>
namespace nexus {
class Engine final {
public:
 Engine() = default;
 ~Engine() = default;
 Engine(const Engine&) = delete;
 Engine& operator=(const Engine&) = delete;
 bool initialize();
 void shutdown();
 [[nodiscard]] bool isInitialized() const noexcept;
 bool runNXS(std::string_view source);
 bool runNXSFile(const std::string& path);
private:
 bool publishNXSResult(const nxs::ExecutionResult& result);
 bool initialized_ = false;
 nxs::Runtime nxsRuntime_;
};
}
