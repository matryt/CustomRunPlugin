CustomRunPlugin
=================

Overview
--------
CustomRunPlugin is a simple IntelliJ Platform plugin (Kotlin) that adds a Run/Debug configuration type to run custom executables. The configuration supports three execution modes:

- Run the Rust compiler (`rustc`) if it is available on the system PATH.
- Run Cargo (`cargo`) if it is available on the system PATH.
- Run an arbitrary executable chosen from the local filesystem ("Other").

The configuration also accepts a raw command-line arguments string that will be parsed and passed to the executable at launch.

Key features
------------
- Select between `rustc`, `cargo`, or a custom executable file.
- Enter custom arguments for the chosen executable.
- UI implemented with the Kotlin UI DSL and standard IntelliJ components.
- Unit tests that exercise the core command-line building and validation logic.

Requirements
------------
- JDK 21
- Gradle (the project includes the Gradle wrapper)
- IntelliJ IDEA (for running the IDE sandbox via the `runIde` Gradle task)

Build and run
-------------
From the repository root, use the Gradle wrapper for all commands. Examples below show Unix/macOS and Windows.

```bash
# Unix / macOS
./gradlew clean test

# Windows
.\gradlew.bat clean test
```

Run an IDE sandbox with the plugin installed (for manual testing):

```bash
# Unix / macOS
./gradlew runIde

# Windows
.\gradlew.bat runIde
```

Package the plugin (creates a ZIP under `build/distributions`):

```bash
# Unix / macOS
./gradlew buildPlugin

# Windows
.\gradlew.bat buildPlugin
```

Quick usage
-----------
A short, manual guide to test the plugin from the running IDE sandbox:

1. Open Run | Edit Configurations...
2. Click + and select "Custom Run Configuration".
3. Choose the execution type from the dropdown:
   - `Rustc` to run `rustc` from PATH.
   - `Cargo` to run `cargo` from PATH.
   - `Other` to pick a local executable file.
4. If you selected `Other`, click the Browse button and choose an executable file on your filesystem.
5. Enter any command-line arguments in the "Arguments" field (they will be parsed and passed to the executable).
6. Click Apply, then Run.

Design and implementation notes
-------------------------------
- Command construction is centralized in a helper so arguments are parsed consistently and the configured working directory is applied.
- Error messages used by the plugin are centralized as constants to keep tests stable and make future internationalization easier.
- The settings editor uses standard components (`ComboBox`, `TextFieldWithBrowseButton`, `RawCommandLineEditor`) and aligns labels and fields following basic JetBrains UI Guidelines.

Windows executable heuristic
----------------------------
On Windows, file permissions do not always mark files as executable in the same way as on Unix. To be pragmatic and avoid false negatives, the plugin accepts common Windows executable extensions (for example `.exe`, `.bat`, `.cmd`, `.com`) in addition to checking `File.canExecute()`. This makes running local tools on Windows more robust, but it is a conservative heuristic; if you need stricter validation you can enable additional checks or preferences.

Project structure
-----------------
The implementation follows the standard IntelliJ plugin pattern:

- `src/main/kotlin/.../CustomRunConfigurationType.kt` — registration of the run configuration type and icon.
- `src/main/kotlin/.../CustomConfigurationFactory.kt` — factory that creates configuration instances.
- `src/main/kotlin/.../CustomRunConfigurationBase.kt` — core behavior: options API, command-line construction, validation, and `getState` implementation for launching processes.
- `src/main/kotlin/.../CustomRunConfigurationOptions.kt` — persistent options backing (uses `RunConfigurationOptions`).
- `src/main/kotlin/.../CustomSettingsEditor.kt` — the settings UI built with Kotlin UI DSL (combo for execution type, browse button for executable, arguments editor).
- `src/main/kotlin/.../ExecutableResolver.kt` — small SAM interface to abstract executable resolution (PATH lookup); used to make tests deterministic.

Testing approach
----------------
Unit tests are placed in `src/test/kotlin`. They focus on pure logic: building command lines, argument parsing, and error handling. The tests use a small `FakeExecutableResolver` to avoid depending on the host PATH or filesystem permissions.
