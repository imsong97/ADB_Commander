# ADB Commander

A desktop GUI tool that helps you write and execute ADB commands with a built-in save feature.

## Skills
MVVM + Repository pattern for managing saved commands in the SQLite database

| Layer     | Library                           |
|-----------|-----------------------------------|
| UI        | Compose Multiplatform (Material3) |
| Async     | Kotlin Coroutines                 |
| Database  | Jetbrains Exposed (SQLite)        |
| Target    | JVM Desktop (macOS / Windows)     |


## Features

### Send Broadcast Tab
- Build `am broadcast`, `am start`, and `am startservice` commands via GUI
- Select command type from a dropdown (broadcast / start / start-service)
- Add typed extras with key-value pairs (String, Int, Long, Boolean, Float, URI)
- Preview the completed command before running
- Copy the completed command to clipboard

### ADB Command Tab
- Enter any ADB command and execute it directly

### Common
- Save frequently used commands with a custom title
- Load saved commands from the left sidebar
- Saved commands are persisted in a local SQLite database (`.adbcommander/adbcommander.db` in the project root)

## Requirements

- JDK 17+
- ADB installed and available in `PATH`

## Build (native installer)

```bash
# macOS (.dmg)
./gradlew packageDmg

# macOS (.pkg)
./gradlew packagePkg

# Windows (.msi)
./gradlew packageMsi

# Windows (.exe)
./gradlew packageExe
```
## License
[Apache License 2.0](LICENSE)
