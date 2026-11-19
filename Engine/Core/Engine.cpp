#include "Core/Engine.h"
#include "Core/Logger.h"
#include "Core/Version.h"
#include <string>
namespace nexus {
bool Engine::initialize() {
 if (initialized_) { Logger::warning("Engine is already initialized."); return true; }
 Logger::info(std::string(ENGINE_NAME) + " " + std::string(VERSION_STRING));
 Logger::info("Initializing core systems...");
 Logger::info("NXS runtime ready.");
 initialized_ = true;
 Logger::info("Engine initialized.");
 return true;
}
void Engine::shutdown() {
 if (!initialized_) return;
 Logger::info("Shutting down engine...");
 initialized_ = false;
 Logger::info("Engine stopped.");
}
bool Engine::isInitialized() const noexcept { return initialized_; }
bool Engine::publishNXSResult(const nxs::ExecutionResult& result) {
 if (!result.success) {
  const std::string location = result.errorLine > 0 ? " at line " + std::to_string(result.errorLine) : "";
  Logger::error("NXS error" + location + ": " + result.error);
  return false;
 }
 if (!result.projectName.empty()) Logger::info("NXS project: " + result.projectName);
 for (const std::string& line : result.output) Logger::info("[NXS] " + line);
 return true;
}
bool Engine::runNXS(std::string_view source) {
 if (!initialized_) { Logger::error("Cannot run NXS: engine is not initialized."); return false; }
 return publishNXSResult(nxsRuntime_.execute(source));
}
bool Engine::runNXSFile(const std::string& path) {
 if (!initialized_) { Logger::error("Cannot run NXS file: engine is not initialized."); return false; }
 return publishNXSResult(nxsRuntime_.executeFile(path));
}
}
