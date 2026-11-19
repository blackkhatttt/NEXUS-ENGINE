#include "Core/Engine.h"
#include "Core/Logger.h"
#include "Scene/Node2D.h"
#include <string>
int main() {
 nexus::Engine engine;
 if (!engine.initialize()) { nexus::Logger::error("Engine startup failed."); return 1; }
 nexus::Node2D root("Main");
 nexus::Node2D player("Player");
 root.addChild(player);
 nexus::Logger::info("Bootstrap scene created.");
 nexus::Logger::info("Root node: " + root.getName());
 nexus::Logger::info("Child count: " + std::to_string(root.getChildren().size()));
 if (!engine.runNXSFile("Projects/Bootstrap/main.nxs")) { engine.shutdown(); return 1; }
 engine.shutdown();
 return 0;
}
