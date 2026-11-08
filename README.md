# Androidecosystems (NEXUS)

NEXUS is an Android device-management application for ADMIN and USER device
profiles. It now includes locally persisted device registration and management,
an expiring pairing-invitation foundation, permission-gated current-device
location, local file selection/history, searchable Academy tutorials, and
system-aware light/dark Compose navigation.

There is **no account service or backend** in this repository. Local profiles
are not paired devices: pairing requests are not remotely verified or
completed, remote presence/battery/location are unavailable, and a requested
file transfer remains pending without sending bytes. The UI labels these
limits rather than showing fabricated remote data.

## Architecture

The project is a single `:app` Gradle module, written in Kotlin with Jetpack
Compose, Material 3, and Navigation Compose.

- `core/` holds device, pairing, transfer, tutorial models, repositories, and roadmap data.
- `data/` contains private SharedPreferences-backed device and transfer stores.
- `ui/theme/` contains the NEXUS light/dark palettes and Compose theme.
- `ui/navigation/` defines the top-level destinations and navigation graph.
- `ui/screens/` contains functional Compose screens and shared UI components.
- `MainActivity` configures edge-to-edge display and hosts the Compose app.

Device profiles and file-selection history are stored in the app's private
preferences; tutorial progress is stored locally as well. Pairing invitations
expire after ten minutes and include only a random invitation token, never a
permanent administrator credential. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
for the protocol boundary and backend requirements.

## Feature boundaries and configuration

- **Devices:** registration, search, role filtering, sorting, details, editing,
  and confirmed removal work locally. Battery, last-seen and online status are
  not shown as live values without an authenticated source.
- **Pairing:** QR invitations can be generated/scanned and locally validated
  for shape, roles, duplicate use and expiry. A backend is required for trusted
  invitation issuance, authorization, revocation and completed pairing.
- **Map:** OpenStreetMap is displayed in a WebView after the user grants Android
  location permission and separately opts in to sending coordinates to center
  the map. It needs network access but no map API key. Remote markers require
  backend location data and owner consent.
- **Files:** Android's system picker grants access to selected files and NEXUS
  records local metadata/history. A transfer request remains pending; no remote
  file transfer transport or backend is configured.
- **Academy:** twelve searchable, categorized guides and local completion
  progress are available offline.
- **QR scanning:** uses Google Play services code scanner; scanning availability
  depends on Google Play services on the device.

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
bash ./gradlew lint
bash ./gradlew clean assembleDebug
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
