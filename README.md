# Androidecosystems (NEXUS)

NEXUS is an Android multi-device management application, intended to support
ADMIN and USER roles in one APK. The current build is Phase 1: a Compose
foundation with system-aware light/dark themes, bottom navigation, settings,
and a roadmap. Later-phase feature areas clearly identify planned functionality;
there is no backend yet.

## Architecture

The project is a single `:app` Gradle module, written in Kotlin with Jetpack
Compose, Material 3, and Navigation Compose.

- `core/` holds shared models and roadmap data.
- `ui/theme/` contains the NEXUS light/dark palettes and Compose theme.
- `ui/navigation/` defines the top-level destinations and navigation graph.
- `ui/screens/` contains the screens and shared UI components.
- `MainActivity` configures edge-to-edge display and hosts the Compose app.

The current screens are explicitly marked as planned where their underlying
service or feature is not implemented. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
for the longer-term architecture and delivery plan.

## Development requirements

- JDK 17
- Android SDK Platform 35
- Android SDK Build-Tools 35.0.0
- Gradle 8.14.3 (provided by the checked-in wrapper)

Android Gradle Plugin is 8.9.1; Kotlin and the Compose compiler plugin are
2.0.21. The app uses `compileSdk`/`targetSdk` 35 and `minSdk` 27. The full
version matrix and CI setup are documented in [docs/TOOLCHAIN.md](docs/TOOLCHAIN.md).

## Build and test

From the repository root, run:

```bash
bash ./gradlew --version
bash ./gradlew clean
bash ./gradlew testDebugUnitTest
bash ./gradlew assembleDebug
```

The debug APK is written to
`app/build/outputs/apk/debug/app-debug.apk`. `bash ./gradlew` works regardless
of executable-bit handling, and `gradlew` is also tracked as executable.

## GitHub Actions APK

The Android workflow runs for pushes to `main` and pull requests (and can also
be started manually). It configures JDK 17, installs and accepts licences for
the required Android SDK packages, validates the Gradle wrapper, runs unit
tests, builds the debug APK, and uploads it as the
`Androidecosystems-debug-apk` artifact.

To download it, open the repository's **Actions** tab, select a successful
Android CI run, and download **Androidecosystems-debug-apk** from the run's
Artifacts section. GitHub artifacts are retained for 14 days.

The workflow also runs Android lint. Tagged `v*` builds retain the existing
release-publication flow; release signing is optional and reads credentials
only from GitHub Actions secrets.
