# Toolchain (pinned)

| Component | Version | Where set |
|---|---|---|
| Gradle (wrapper) | 8.14.3 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.9.1 | `gradle/libs.versions.toml` |
| Kotlin and Compose compiler plugin | 2.0.21 | `gradle/libs.versions.toml` |
| Compose BOM | 2024.12.01 | `gradle/libs.versions.toml` |
| JDK (CI and local) | 17 (Temurin) | `.github/workflows/android-build.yml` |
| compileSdk / targetSdk | 35 | `app/build.gradle.kts` |
| minSdk | 27 (Android 8.1) | `app/build.gradle.kts` |

Compatibility basis: AGP 8.9 needs Gradle 8.11.1 or newer and JDK 17; AGP 8.9 supports API 35; the Compose compiler is a Kotlin plugin from Kotlin 2.0, so its version equals the Kotlin version.

Gradle is configured for a 1 GiB heap, one worker, no parallel project execution,
and in-process Kotlin compilation to keep builds reliable on memory-constrained
Codespaces and CI runners.

GitHub Actions uses `actions/checkout@v4`, `actions/setup-java@v4`, `android-actions/setup-android@v4`, `gradle/actions/setup-gradle@v4`, `gradle/actions/wrapper-validation@v4`, and `actions/upload-artifact@v4`. The workflow accepts SDK licences and explicitly installs `platforms;android-35` and `build-tools;35.0.0`; it does not install the obsolete `tools` package.

Note on `setup-android`: the Greyhat-A standard names `v4.0.4`, but the action's releases page lists v4.0.1 as the latest, so this project pins `v4.0.1`. If you later want stricter supply-chain control, replace each tag with the reviewed full commit SHA.

Change policy: update versions only as a deliberate toolchain change, after reading the official compatibility table, never as a reaction to an unrelated error.
