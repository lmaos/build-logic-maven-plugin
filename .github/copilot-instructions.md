# build-logic-maven-plugin

## Build and test

- Use system Maven (`mvn`); there is no Maven wrapper checked into this repository.
- `mvn compile` compiles the plugin and regenerates the Maven plugin descriptor.
- `mvn package` builds the plugin JAR.
- `mvn test` runs the full JUnit 5 suite, including the Maven plugin harness tests.
- `mvn -Dtest=ExpressionCalculatorTest test` runs a single test class.
- `mvn "-Dtest=RunBuildLogicMojoTest#testHttpMojoExecute" test` runs a single test method.

## High-level architecture

- This project is packaged as a `maven-plugin`. `HelloMojo` is a minimal sample goal; `RunBuildLogicMojo` is the real entrypoint for the XML DSL.
- `RunBuildLogicMojo` wires the runtime by registering supported top-level tags on `DefaultActionFactory`, choosing the `Compare` implementation, seeding built-in variables (`allowWriteDir`, `basedir`, `project.basedir`), and executing the `<main>` configuration node through `DefaultActionExecute`.
- `DefaultActionFactory` plus `Action`, `GroupAction`, `CodeBlockAction`, and `FunctionAction` make up the DSL interpreter. The factory maps Plexus XML tags to Java action classes, binds XML attributes to fields by reflection, recursively parses child actions, and splits dotted tags such as `<list.add>` or `<call.deploy>` into a base tag plus `tagMethod`.
- Runtime variables live in the thread-local `FunctionVariablesReference` stack. `FunctionVariables` resolves variables from inner scope to outer scope, and code blocks/functions push new scopes as they execute.
- `ActionParam` is the runtime context passed through execution. It carries the current `Mojo`, `MavenProject`, `ActionExecute`, `Compare`, logger access, and variable scope.
- The expression engine under `src/main/java/com/clmcat/maven/plugins/calculator` is a second subsystem. `ExpressionCalculatorCompare` uses `IterativeExpressionCalculator` for `test=` evaluation, while the recursive calculator remains as the reference implementation. Shared semantics live in `ExpressionRuntimeSupport`, and extensible operators live in the singleton `OperatorRegistry`.
- Tests are split into two groups: expression-engine regression tests in `src/test/java/com/clmcat/calculator/test` and plugin harness tests in `src/test/java/com/clmcat/plugins/test` backed by fixture POMs under `src/test/resources/plugin-test-*`.

## Key conventions

- Executable DSL belongs under `<main>`. Functions can only be defined at the root or `<main>` level.
- Dotted tag names mean “same action, different method/variant”, not a nested namespace. Follow the existing `tagMethod` pattern for additions (`<var.int>`, `<list.add>`, `<str.substr>`, `<func.deploy>`, `<call.deploy>`, `<base64.encode>`).
- Action attributes are strict. `DefaultActionFactory` reflects XML attributes onto fields with the same name and throws on unknown attributes, so new DSL attributes need matching non-static, non-final fields.
- Variable names must match `[$0-9a-zA-Z_]+`. Function names are stricter: `[$0-9a-zA-Z]+`.
- Variable interpolation uses `${...}` through `Format.formatString`, and it supports Java method calls on variables, not just plain replacement. README examples such as `${str.substring(int 0, int 5)}` and `${response.headers.getHeader(String "Content-Type")}` match how the runtime is intended to be used.
- File mutation is intentionally constrained by `allowWriteDir`, which defaults to `project.basedir` in `RunBuildLogicMojo`. `DeleteAction` only bypasses that boundary with `force="true"`.
- `test=` conditions use `ExpressionCalculatorCompare` by default. Bare numbers or strings are not valid final boolean expressions; use explicit comparisons unless you are relying on supported truthiness rules for booleans, files, collections, maps, or arrays.
- Nested DSL blocks usually register their own child-only tags locally instead of globally. For example, `if` adds `then`/`elseif`/`else`, `http` adds `header`/`content`/`response`, `call` adds `arg`, and `list` adds `item`.
- `OperatorRegistry` is a mutable singleton. Tests that register or remove operators should reset it to defaults in setup/teardown to avoid cross-test leakage.
