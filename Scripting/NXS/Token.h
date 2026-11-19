#pragma once
#include <cstddef>
#include <string>
namespace nexus::nxs {
enum class TokenType { End, Identifier, String, Number, LeftBrace, RightBrace, LeftParen, RightParen, Equal, Plus, Separator };
struct Token { TokenType type = TokenType::End; std::string lexeme; std::size_t line = 1; std::size_t column = 1; };
}