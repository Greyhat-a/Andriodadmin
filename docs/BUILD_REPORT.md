# Build report

Project: Androidecosystems (NEXUS)

| Setting | Value |
|---|---|
| Android namespace/application ID | `com.nexus.app` |
| Build variant | debug |
| Build runner | local Codespaces environment |
| JDK used for verification | Temurin 17.0.13 |
| Gradle | 8.14.3 |
| Android Gradle Plugin | 8.9.1 |
| Kotlin and Compose compiler plugin | 2.0.21 |
| Android compile/target SDK | 35 |
Android minimum SDK: 27

## Verification results

| Check | Result |
|---|---|
| `bash ./gradlew --version` | Pass; Gradle 8.14.3 |
| `bash ./gradlew clean` | Pass with JDK 17 |
| `bash ./gradlew testDebugUnitTest` | Pass; 4 unit tests |
| `bash ./gradlew assembleDebug` | Pass with constrained local Gradle memory/workers |
| `bash ./gradlew lintDebug` | Pass |
| Debug APK exists and is non-empty | Pass; 9.4 MB |
| APK metadata | Pass; ID `com.nexus.app.debug`, min SDK 27, target SDK 35 |
| Workflow YAML parses | Pass |
| Android manifest and resource XML parse | Pass |
| Gradle wrapper tracked executable | Pass; mode `100755` |
| Tracked signing keys/Google service credentials | None found |

APK path: `app/build/outputs/apk/debug/app-debug.apk`

The shared Codespaces host had limited free memory; an unconstrained APK build
lost its Gradle daemon during dexing. The same build completed after constraining
Gradle to one worker and a 1 GiB heap. The GitHub Actions runner installs the
same JDK and SDK versions and uploads the APK as
`Androidecosystems-debug-apk`.

## Runtime limitations

No emulator or physical Android device was available, so install/launch behavior
was not device-tested. The current phase contains the foundation UI and
navigation; later-phase product functionality and a backend remain planned.
