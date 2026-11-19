#include "Core/Engine.h"
#include "Core/Logger.h"
#include "Scene/Node2D.h"
#include <string>
int main() {
 nexus::Engine engine;
 if (!engine.initialize()) { nexus::Logger::error("Engine startup failed."); return 1; }
 nexus::Node2D root("Main");
 nexus::Node2D player("Player");
 player.setName("Hero");
 root.addChild(player);
 if (player.getName() != "Hero" || player.getParent() != &root) {
  nexus::Logger::error("Node2D scene tree validation failed.");
  engine.shutdown();
  return 2;
 }
 nexus::Logger::info("Bootstrap scene created.");
 nexus::Logger::info("Root node: " + root.getName());
 nexus::Logger::info("Child count: " + std::to_string(root.getChildren().size()));
 if (!engine.runNXSFile("Projects/Bootstrap/main.nxs")) { engine.shutdown(); return 1; }
 engine.shutdown();
 return 0;
}
