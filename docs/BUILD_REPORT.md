# Build report: Phase 1 (Foundation)

Project: NEXUS
Repository: not yet pushed
Commit SHA: n/a
Target platform: Android (minSdk 26, targetSdk 35)
Build variant: debug (CI), release only on tags
Runner image: ubuntu-latest (planned)
JDK / runtime: 17 Temurin (CI)
Build system version: Gradle 8.14.3 wrapper, AGP 8.9.1, Kotlin 2.0.21
Platform SDK: 35
Build command: `./gradlew testDebugUnitTest assembleDebug`
Test commands: `./gradlew testDebugUnitTest`, `./gradlew lintDebug`

## Results

| Check | Result |
|---|---|
| Gradle build (`assembleDebug`) | **NOT RUN.** No Android SDK and no network for Gradle in the authoring environment |
| Unit tests (`RoadmapTest`, 4 tests) | **NOT RUN** for the same reason |
| Lint | **NOT RUN** |
| Install test on a device | **NOT RUN** |
| Runtime test devices / OS versions | none |
| Gradle Wrapper generated | Done, with Gradle 8.14.3; `gradlew`, `gradlew.bat`, jar and properties present |
| All XML resources and manifest parse | Pass |
| Workflow YAML and version catalog parse | Pass |
| Every `libs.*` alias used in Gradle files exists in the catalog | Pass |
| Every `R.drawable` reference has a file | Pass |
| No keystore, password or API key in the repository | Pass |

Artifact path: `app/build/outputs/apk/debug/app-debug.apk` (produced by CI, not yet produced)
Signing status: debug-signed unless release secrets are configured (see README)

## Unresolved risks

1. **The Kotlin and Gradle files have never been compiled.** The first CI run is the real test. Most likely failure areas, in order: Compose API signatures in `Theme.kt` (colour scheme parameter names), plugin or dependency resolution, lint.
2. **Version compatibility** is chosen from the official requirements (AGP 8.9.1 with Gradle 8.14.3, JDK 17, Kotlin 2.0.21, API 35) but not exercised.
3. **`setup-android` pin.** The Greyhat-A PDF names `v4.0.4`; the action's releases page lists `v4.0.1` as latest, so `v4.0.1` is used. If CI reports that this action cannot be resolved or fails in `sdkmanager`, send the raw log.
4. Actions are pinned by tag, not commit SHA.

## If the first run fails

Send the raw log of the failing step. Classify it first (runner/SDK, Gradle configuration, Kotlin compile, packaging), then fix the smallest confirmed cause. Do not upgrade versions or rewrite files on a guess.

## Evidence

Workflow run URL: pending first push.
