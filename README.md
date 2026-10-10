# HtzCars

Minecraft 1.21.1 mod for NeoForge (client and server). Mod id: `htzcars`.

Current state: project skeleton only (build, CI and an empty main class that logs a startup message).

## Build

Requirements: JDK 21 and Gradle 8.14.3 (no Gradle wrapper is committed).

```
gradle build
```

The mod jar is written to `build/libs/`. Versions and mod metadata are defined in `gradle.properties`.

## CI (GitHub Actions)

- `.github/workflows/build.yml`: runs on push, pull request and manual dispatch. It builds the mod with JDK 21 (Temurin) and Gradle 8.14.3 and uploads the jars from `build/libs` as the `htzcars-jars` artifact. It also runs temporary `findClass` steps used to inspect the NeoForge/Minecraft API.
- `.github/workflows/inspect.yml`: manual workflow that runs the temporary Gradle tasks `inspectApi` (members declared in a class) or `findClass` (full names of classes by simple name) and prints the result in the log.

## License

Not decided yet (`mod_license` in `gradle.properties` is a placeholder).
