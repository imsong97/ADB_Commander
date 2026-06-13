# ADB Commander

A desktop GUI tool that helps you write and execute ADB commands with a built-in save feature.

## Tech Stack

| Layer    | Library                           |
|----------|-----------------------------------|
| UI       | Compose Multiplatform (Material3) |
| Async    | Kotlin Coroutines                 |
| Database | Jetbrains Exposed (SQLite)        |
| Target   | JVM Desktop (macOS / Windows)     |

## Architecture

MVVM + Repository (Data Layer)

<img width="1000" height="367" alt="Image" src="https://github.com/user-attachments/assets/503b7b53-8c7e-487d-8c53-2017533c873f" />

### Package

```
composeApp/
    ├── main.kt                         
    ├── presentation
    ├── ui
    ├── di
    ├── executor # Run ADB command
    └── preference

shared/
├── commonMain/data/
│       ├── CommandRepository.kt # Repository Interface
│       ├── CommandRepositoryImpl.kt   
│       ├── datasource
│       └── model
└── desktopMain/local
        ├── LocalDataSourceImpl.kt     
        └── database
```

## Features

### Send Broadcast Tab
- Build `am broadcast`, `am start`, and `am startservice` commands via GUI
- Select command type from a dropdown (broadcast / start / start-service)
- Add typed extras with key-value pairs (String, Int, Long, Boolean, Float, URI)
- Preview the completed command before running
- Copy the completed command to clipboard

### ADB Command Tab
- Enter any ADB command and execute it directly

### Collection
- 커맨드를 컬렉션으로 묶어 관리 (생성 / 삭제 / 이름 변경)
- 컬렉션 내 커맨드 저장 / 수정 / 삭제 / 이름 변경
- View 메뉴에서 컬렉션 개별 표시 여부 토글

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
