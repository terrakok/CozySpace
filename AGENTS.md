# CozySpace — Agent Notes

## Project structure

```
sharedUI/       — Kotlin Multiplatform library (JVM, JS, Wasm, iOS)
desktopApp/     — JVM desktop app (Compose, Nucleus packaging)
webApp/         — JS + Wasm web app
```

**Entry points**
- Desktop: `sharedUI/src/jvmMain/kotlin/dev/terrakok/cozyspace/Main.kt` → `DesktopApp()` composable
- Web: `webApp/build.gradle.kts` → `:webApp:composeCompatibilityBrowserDistribution`

## Commands

```bash
# Run desktop app
./gradlew :desktopApp:run

# Build desktop package (DMG / MSI / Deb)
./gradlew :desktopApp:packageReleaseDmg    # macOS
./gradlew :desktopApp:packageReleaseMsi    # Windows
./gradlew :desktopApp:packageReleaseDeb    # Linux
# Requires -PappVersion=x.y.z on CI tag pushes

# Build web app (deployable to gh-pages)
./gradlew :webApp:composeCompatibilityBrowserDistribution
```

## SoundPlayer (JVM)

- `expect`/`actual` pattern — `SoundPlayer` interface in `commonMain`, implementation in `jvmMain`
- JVM uses **TinySound** library compiled from source — JARs are vendored in `sharedUI/libs/`:
  - `tinysound.jar` (compiled from https://github.com/finnkuusisto/TinySound)
  - `jorbis-0.0.17.jar`, `tritonus_share.jar`, `vorbisspi1.0.3.jar` (Ogg Vorbis support)
- Audio assets are OGG Vorbis files in `commonMain/composeResources/files/`
- URIs use the `compose:` scheme (e.g. `compose:files/birds.ogg`) — the JVM resolver must handle this
- All 14 tracks are `resoure: environments` global mutable state in `Environment.kt`

## Key conventions

- Version catalog at `gradle/libs.versions.toml` — all dependencies go there
- `Environment.kt` top-level `environments` list is global mutable state — presets read/write it directly
- Uses Compose Multiplatform `movableContentOf` for passing content across screens
- Persistence via `multiplatform-settings` + `kotlinx-serialization` for preset data
- Desktop runs as tray app via `composenativetray` — no window visible until toggled
