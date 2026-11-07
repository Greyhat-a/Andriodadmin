# NEXUS

*Your devices. Your network. Your control.*

Android multi-device management app (ADMIN and USER roles in one APK). Kotlin, Jetpack Compose, Material 3.

**Current state: Phase 1 of 10 (foundation).** The app installs, follows the system light/dark theme, and has five-tab navigation. Devices, Map, Files and Academy are honest placeholders that say which phase will build them. There is no backend, pairing, temporary ADMIN or remote control yet. See `docs/ARCHITECTURE.md` for the full plan and `docs/BUILD_REPORT.md` for what has and has not been verified.

## Repository layout

```
.github/workflows/android-build.yml   CI: tests, debug APK, lint, tagged releases
app/                                  Android application module
gradle/                               Gradle Wrapper and version catalog
docs/                                 Architecture, toolchain, build report
settings.gradle.kts  build.gradle.kts  gradle.properties  gradlew  gradlew.bat
```

The repository root is the folder containing `gradlew`. Push this folder's contents as the repository root, not inside another folder.

## Build locally

Requirements: JDK 17 and the Android SDK (API 35 platform and build tools; set `ANDROID_HOME`).

```bash
chmod +x ./gradlew
./gradlew testDebugUnitTest
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Build on GitHub

1. Create an empty repository and push this project so `gradlew` sits at the repository root.
2. Open the **Actions** tab. The *Android CI* workflow runs on every push to `main`.
3. Download the APK from the run's **Artifacts** section (`nexus-debug-apk`). It is debug-signed and installable on a phone with "install unknown apps" allowed.

Lint runs as a separate job so a lint finding never blocks the APK.

## Releases

Push a tag like `v0.1.0` and the workflow publishes a GitHub Release with the APK attached.

- Without signing secrets: the release contains a **debug-signed** APK, named `...-debug-signed.apk`.
- With signing secrets: it builds `assembleRelease`, signed with your key, named `...-release-signed.apk`.

Add these repository secrets to enable release signing: `NEXUS_KEYSTORE_BASE64` (output of `base64 -w0 your.jks`), `NEXUS_KEYSTORE_PASSWORD`, `NEXUS_KEY_ALIAS`, `NEXUS_KEY_PASSWORD`. Never commit a keystore. Keep a backup of it: losing it prevents updates to installed apps.

## Secrets and configuration

Nothing secret is stored in the repository. `.gitignore` excludes keystores, `google-services.json` and `.env` files. Firebase and backend configuration will be documented in the phase that introduces them.
