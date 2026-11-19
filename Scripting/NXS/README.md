# NXS Runtime

NXS is the game scripting language used by NEXUS ENGINE. This first C++ runtime supports the project header, start block, typed variables, assignment, printing, comments and plus expressions.

## Example

\`\`\`nxs
game "NEXUS PROJECT" start {
    # Project greeting
    var title Text = "Hello from NXS"
    var lives Number = 3
    var speed NumberD = 8.4
    var active FalseTrue = true
    print(title)
    print("Lives: " + lives)
    print("Speed: " + speed)
    lives = 5
    print("Updated lives: " + lives)
}
\`\`\`

## Supported features
- Text stores strings.
- Number stores integers.
- NumberD stores decimal numbers.
- FalseTrue stores true or false.
- Variable declarations use var name Type = value.
- Existing variables can be assigned new values of the same type.
- print(expression) writes a value to engine output.
- + concatenates text or adds numeric values.
- # begins a line comment.
- Newlines and semicolons separate statements.

## C++ API
Use nexus::nxs::Runtime::execute(source) for in-memory source or executeFile(path) for a .nxs file. nexus::Engine exposes runNXS() and runNXSFile() and forwards script output and errors to the engine logger.

Scene-node access, components, functions, conditions, loops, imports and engine callbacks are future milestones.
