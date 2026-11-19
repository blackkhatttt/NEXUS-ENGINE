#pragma once
#include "Token.h"
#include <string>
#include <string_view>
#include <vector>
namespace nexus::nxs {
class Lexer final {
public:
 explicit Lexer(std::string_view source);
 [[nodiscard]] std::vector<Token> tokenize();
private:
 [[nodiscard]] bool isAtEnd() const noexcept;
 char advance();
 [[nodiscard]] char peek() const noexcept;
 [[nodiscard]] char peekNext() const noexcept;
 bool match(char expected);
 void scanToken();
 void scanIdentifier();
 void scanNumber();
 void scanString();
 void add(TokenType type);
 void add(TokenType type, std::string value);
 void skipComment();
 std::string source_;
 std::vector<Token> tokens_;
 std::size_t start_ = 0, current_ = 0, line_ = 1, column_ = 1, tokenColumn_ = 1;
};
}