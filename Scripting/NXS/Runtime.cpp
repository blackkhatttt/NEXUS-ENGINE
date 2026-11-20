#include "Runtime.h"
#include "Lexer.h"
#include <charconv>
#include <cstdint>
#include <fstream>
#include <sstream>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <utility>
#include <variant>
#include <vector>

namespace nexus::nxs {
namespace {
using Value = std::variant<std::string, std::int64_t, double, bool>;

std::string lower(std::string value) {
 for (char& c : value) if (c >= 'A' && c <= 'Z') c = static_cast<char>(c - 'A' + 'a');
 return value;
}
std::string valueToString(const Value& value) {
 if (const auto* text = std::get_if<std::string>(&value)) return *text;
 if (const auto* number = std::get_if<std::int64_t>(&value)) return std::to_string(*number);
 if (const auto* decimal = std::get_if<double>(&value)) { std::ostringstream stream; stream << *decimal; return stream.str(); }
 return std::get<bool>(value) ? "true" : "false";
}

class Parser final {
 struct ParseError { std::string message; std::size_t line; };
public:
 explicit Parser(std::vector<Token> tokens) : tokens_(std::move(tokens)) {}
 ExecutionResult run() {
  ExecutionResult result;
  try {
   skipSeparators();
   if (checkIdentifier("game")) {
    advance();
    if (!check(TokenType::String)) fail("Expected a project name after 'game'.");
    result.projectName = advance().lexeme;
    skipSeparators();
    consumeIdentifier("start", "Expected 'start' after the project name.");
   }
   skipSeparators();
   const bool hasBlock = match(TokenType::LeftBrace);
   while (!isAtEnd() && !(hasBlock && check(TokenType::RightBrace))) {
    skipSeparators();
    if (hasBlock && check(TokenType::RightBrace)) break;
    if (isAtEnd()) break;
    statement(result);
    if (!check(TokenType::Separator) && !(hasBlock && check(TokenType::RightBrace)) && !isAtEnd())
     fail("Expected a new line or ';' after the statement.");
   }
   if (hasBlock) consume(TokenType::RightBrace, "Expected '}' to close the start block.");
   skipSeparators();
   if (!isAtEnd()) fail("Unexpected content after the start block.");
   result.success = true;
  } catch (const ParseError& error) {
   result.error = error.message;
   result.errorLine = error.line;
  }
  return result;
}
private:
 [[noreturn]] void fail(const std::string& message) const { throw ParseError{message, peek().line}; }
 const Token& peek() const { return tokens_[current_]; }
 const Token& previous() const { return tokens_[current_ - 1]; }
 bool isAtEnd() const { return peek().type == TokenType::End; }
 bool check(TokenType type) const { return peek().type == type; }
 const Token& advance() { if (!isAtEnd()) ++current_; return previous(); }
 bool match(TokenType type) { if (!check(type)) return false; advance(); return true; }
 bool checkIdentifier(const std::string& keyword) const { return check(TokenType::Identifier) && lower(peek().lexeme) == keyword; }
 void consumeIdentifier(const std::string& keyword, const std::string& message) { if (!checkIdentifier(keyword)) fail(message); advance(); }
 void consume(TokenType type, const std::string& message) { if (!check(type)) fail(message); advance(); }
 void skipSeparators() { while (match(TokenType::Separator)) {} }

 void statement(ExecutionResult& result) {
  if (checkIdentifier("print")) {
   advance();
   consume(TokenType::LeftParen, "Expected '(' after print.");
   Value value = expression();
   consume(TokenType::RightParen, "Expected ')' after print value.");
   result.output.push_back(valueToString(value));
   return;
  }
  if (checkIdentifier("var") || checkIdentifier("const")) {
   const bool isConstant = checkIdentifier("const");
   advance();
   if (!check(TokenType::Identifier)) fail("Expected a variable name after 'var'.");
   const std::string name = advance().lexeme;
   if (!check(TokenType::Identifier)) fail("Expected a type: Text, Number, NumberD or FalseTrue.");
   const std::string type = lower(advance().lexeme);
   consume(TokenType::Equal, "Expected '=' before the variable value.");
   Value value = expression();
   validateType(type, value);
   if (variables_.contains(name)) fail("Variable '" + name + "' is already declared.");
   variables_.emplace(name, std::move(value));
   types_.emplace(name, type);
   if (isConstant) constants_.insert(name);
   return;
  }
  if (check(TokenType::Identifier)) {
   const std::string name = advance().lexeme;
   consume(TokenType::Equal, "Expected '=' after variable name.");
   Value value = expression();
   const auto found = variables_.find(name);
   if (found == variables_.end()) fail("Unknown variable '" + name + "'.");
   if (constants_.contains(name)) fail("Cannot assign to constant '" + name + "'.");
   validateType(types_.at(name), value);
   found->second = std::move(value);
   return;
  }
  fail("Unknown statement. Supported statements: var, const and print().");
 }

 void validateType(const std::string& type, const Value& value) const {
  const bool known = type == "text" || type == "number" || type == "numberd" || type == "falsetrue";
  if (!known) fail("Unknown NXS type '" + type + "'.");
  const bool valid =
   (type == "text" && std::holds_alternative<std::string>(value)) ||
   (type == "number" && std::holds_alternative<std::int64_t>(value)) ||
   (type == "numberd" && std::holds_alternative<double>(value)) ||
   (type == "falsetrue" && std::holds_alternative<bool>(value));
  if (!valid) fail("Value does not match the declared type '" + type + "'.");
 }

 static double numericValue(const Value& value) {
  if (const auto* integer = std::get_if<std::int64_t>(&value)) return static_cast<double>(*integer);
  return std::get<double>(value);
 }
 Value expression() {
  Value left = primary();
  while (match(TokenType::Plus)) {
   Value right = primary();
   if (std::holds_alternative<std::string>(left) || std::holds_alternative<std::string>(right))
    left = valueToString(left) + valueToString(right);
   else if (std::holds_alternative<std::int64_t>(left) && std::holds_alternative<std::int64_t>(right))
    left = std::get<std::int64_t>(left) + std::get<std::int64_t>(right);
   else if ((std::holds_alternative<double>(left) || std::holds_alternative<std::int64_t>(left)) &&
            (std::holds_alternative<double>(right) || std::holds_alternative<std::int64_t>(right)))
    left = numericValue(left) + numericValue(right);
   else fail("The '+' operator requires text or numeric values.");
  }
  return left;
 }
 Value primary() {
  if (match(TokenType::String)) return previous().lexeme;
  if (match(TokenType::Number)) {
   const std::string& text = previous().lexeme;
   if (text.find('.') != std::string::npos) {
    try { return std::stod(text); } catch (...) { fail("Invalid decimal number."); }
   }
   std::int64_t number = 0;
   const auto parsed = std::from_chars(text.data(), text.data() + text.size(), number);
   if (parsed.ec != std::errc{}) fail("Invalid integer number.");
   return number;
  }
  if (checkIdentifier("true")) { advance(); return true; }
  if (checkIdentifier("false")) { advance(); return false; }
  if (match(TokenType::Identifier)) {
   const std::string name = previous().lexeme;
   const auto found = variables_.find(name);
   if (found == variables_.end()) fail("Unknown variable '" + name + "'.");
   return found->second;
  }
  fail("Expected a value: text, number, true, false or variable.");
 }
 std::vector<Token> tokens_;
 std::size_t current_ = 0;
 std::unordered_map<std::string, Value> variables_;
 std::unordered_map<std::string, std::string> types_;
 std::unordered_set<std::string> constants_;
};
}

ExecutionResult Runtime::execute(std::string_view source) const {
 Lexer lexer(source);
 auto tokens = lexer.tokenize();
 for (const Token& token : tokens) {
  if (token.type == TokenType::End && token.lexeme == "Unterminated string")
   return {false, {}, {}, "Unterminated string.", token.line};
  if (token.type == TokenType::End && token.lexeme.rfind("Unexpected", 0) == 0)
   return {false, {}, {}, token.lexeme, token.line};
 }
 return Parser(std::move(tokens)).run();
}

ExecutionResult Runtime::executeFile(const std::string& path) const {
 std::ifstream file(path, std::ios::binary);
 if (!file) return {false, {}, {}, "Could not open NXS file: " + path, 0};
 std::ostringstream contents;
 contents << file.rdbuf();
 return execute(contents.str());
}
}
