#include "Lexer.h"
#include <cctype>
#include <utility>
namespace nexus::nxs {
Lexer::Lexer(std::string_view source) : source_(source) {}
bool Lexer::isAtEnd() const noexcept { return current_ >= source_.size(); }
char Lexer::advance() {
 const char value = source_[current_++];
 if (value == '\n') { ++line_; column_ = 1; } else { ++column_; }
 return value;
}
char Lexer::peek() const noexcept { return isAtEnd() ? '\0' : source_[current_]; }
char Lexer::peekNext() const noexcept { return current_ + 1 >= source_.size() ? '\0' : source_[current_ + 1]; }
bool Lexer::match(char expected) { if (isAtEnd() || source_[current_] != expected) return false; advance(); return true; }
void Lexer::add(TokenType type) { tokens_.push_back({type, source_.substr(start_, current_ - start_), line_, tokenColumn_}); }
void Lexer::add(TokenType type, std::string value) { tokens_.push_back({type, std::move(value), line_, tokenColumn_}); }
void Lexer::skipComment() { while (!isAtEnd() && peek() != '\n') advance(); }
void Lexer::scanIdentifier() { while (std::isalnum(static_cast<unsigned char>(peek())) || peek() == '_') advance(); add(TokenType::Identifier); }
void Lexer::scanNumber() {
 while (std::isdigit(static_cast<unsigned char>(peek()))) advance();
 if (peek() == '.' && std::isdigit(static_cast<unsigned char>(peekNext()))) { advance(); while (std::isdigit(static_cast<unsigned char>(peek()))) advance(); }
 add(TokenType::Number);
}
void Lexer::scanString() {
 std::string value;
 while (!isAtEnd() && peek() != '"') {
  char c = advance();
  if (c == '\\' && !isAtEnd()) {
   const char escaped = advance();
   switch (escaped) { case 'n': value.push_back('\n'); break; case 't': value.push_back('\t'); break; case '"': value.push_back('"'); break; case '\\': value.push_back('\\'); break; default: value.push_back('\\'); value.push_back(escaped); break; }
  } else value.push_back(c);
 }
 if (isAtEnd()) { tokens_.push_back({TokenType::End, "Unterminated string", line_, tokenColumn_}); return; }
 advance(); add(TokenType::String, std::move(value));
}
void Lexer::scanToken() {
 const char c = advance();
 switch (c) {
  case ' ': case '\r': case '\t': break;
  case '\n': case ';': add(TokenType::Separator); break;
  case '#': skipComment(); break;
  case '{': add(TokenType::LeftBrace); break;
  case '}': add(TokenType::RightBrace); break;
  case '(': add(TokenType::LeftParen); break;
  case ')': add(TokenType::RightParen); break;
  case '=': add(TokenType::Equal); break;
  case '+': add(TokenType::Plus); break;
  case '"': scanString(); break;
  default:
   if (std::isalpha(static_cast<unsigned char>(c)) || c == '_') scanIdentifier();
   else if (std::isdigit(static_cast<unsigned char>(c))) scanNumber();
   else tokens_.push_back({TokenType::End, std::string("Unexpected character: ") + c, line_, tokenColumn_});
   break;
 }
}
std::vector<Token> Lexer::tokenize() {
 while (!isAtEnd()) {
  start_ = current_; tokenColumn_ = column_; scanToken();
  if (!tokens_.empty() && tokens_.back().type == TokenType::End) break;
 }
 tokens_.push_back({TokenType::End, "", line_, column_});
 return tokens_;
}
}