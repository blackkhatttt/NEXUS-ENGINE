#include "Scripting/NXS/Runtime.h"
#include <iostream>
#include <string>

int main() {
 const std::string source = R"NXS(
game "NXS CONDITIONS" start {
    var score Number = 75
    var alive FalseTrue = true

    if score > 100 {
        print("WRONG_IF")
    }
    eif score >= 50 AND alive {
        print("PASS_EIF")
    }
    else {
        print("WRONG_ELSE")
    }

    if (score == 75) {
        print("PASS_PARENTHESES")
    }
}
)NXS";
 const nexus::nxs::ExecutionResult result = nexus::nxs::Runtime{}.execute(source);
 if (!result.success) {
  std::cerr << "NXS condition test failed at line " << result.errorLine << ": " << result.error << '\n';
  return 1;
 }
 if (result.output.size() != 2 ||
     result.output[0] != "PASS_EIF" ||
     result.output[1] != "PASS_PARENTHESES") {
  std::cerr << "NXS condition output did not match expectations.\n";
  for (const auto& line : result.output) std::cerr << line << '\n';
  return 2;
 }
 std::cout << "NXS if/eif/else conditions passed.\n";
 return 0;
}
