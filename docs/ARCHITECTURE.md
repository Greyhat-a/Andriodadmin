# NEXUS architecture and plan

## 1. What NEXUS is

One Android APK, two roles. An **ADMIN** device manages the account's enrolled devices; **USER** devices report status and take part in sessions the device owner has consented to. An enrolled USER can become a **temporary ADMIN** for three hours by entering the account's Admin Elevation Passkey. The server, not the phone, decides whether that session is still valid.

## 2. Client architecture (Android)

The app is a single Gradle module `:app`, built with Kotlin, Jetpack Compose,
Material 3, and Navigation Compose. Device state is exposed through a
`StateFlow`-backed repository and ViewModel; Android-private persistence is
isolated behind store interfaces. Pairing validation and transfer history are
separate from UI code. The app has no authentication or networking backend.

```
com.nexus.app
  core/        device, pairing, transfer, academy models/repositories, Roadmap
  data/        SharedPreferences-backed local data stores
  ui/theme/    colour, typography, NexusTheme (follows system light/dark)
  ui/navigation/  Destination, NexusApp (bottom navigation)
  ui/screens/  functional Compose screens and shared components
  -- future backend integration --
  auth/ pairing/ roles/ monitoring/ battery/ location/ remote/ files/
  notifications/ academy/ settings/ security/
```

Rules: business validation lives in core repositories/protocol objects; device
UI state is collected from a ViewModel; every privileged action must be
authorized on a server, never only in the UI.

## 3. Backend (not built yet)

The master prompt requires a real backend. Recommended: **Firebase** (Authentication, Firestore, Cloud Functions, Cloud Messaging) plus small server-side functions for the security-critical parts.

| Responsibility | Where it lives |
|---|---|
| Account sign-in | Firebase Authentication |
| Devices, roles, presence, battery, location | Firestore with security rules |
| Invitation tokens (single-use, expiring, revocable) | Cloud Functions + Firestore |
| Passkey verification | Cloud Function: salted hash (Argon2id or scrypt), per-device and per-account rate limiting, audit log entry on every attempt |
| Temporary ADMIN sessions | Firestore document with a server-set `expiresAt`; every protected function checks it against server time; a scheduled function closes expired sessions and logs them |
| Signalling for remote sessions | WebSocket or Firestore-based signalling, WebRTC media, STUN plus TURN |
| Push | Firebase Cloud Messaging |
| File transfer coordination | Firebase Storage with short-lived signed URLs |

Decision needed from the owner before Phase 2: a Firebase project, and whether to use Firebase or a self-hosted API. Nothing in this repository can supply those; the phase cannot be completed without them.

## 4. External services you will need

- A **Firebase project** (Auth, Firestore, Functions, Messaging, Storage). Functions require the Blaze plan.
- A **TURN server** for remote sessions across networks (self-hosted coturn or a hosted TURN provider).
- A **domain you control** for App Links, serving `/.well-known/assetlinks.json` and the invitation landing page.
- A **release signing keystore**, stored only in GitHub Secrets.
- The current **OpenStreetMap** map needs network connectivity but no API key.
  Remote device markers still require the backend and explicit owner consent.
- Google Play services on the device for the system QR scanner. QR generation
  uses the local ZXing library and does not need an API key.

## 5. Android restrictions that shape the design

- **Screen capture** needs a MediaProjection consent dialog on the USER device each session and a foreground service of type `mediaProjection` with a visible notification. It cannot be started silently.
- **Remote interaction** is only possible through an Accessibility Service the user enables manually in system settings. It cannot bypass secure screens, and Google Play restricts non-accessibility uses.
- **Camera and microphone** need runtime permissions and a foreground service with a visible indicator; Android also shows its own privacy indicator. NEXUS will not start either secretly.
- **Playback (system) audio capture** works only for apps that allow it and requires Android 10+; microphone audio and playback audio are separate features.
- **Background location** needs a separate permission step on Android 10+ and a foreground service for continuous updates.
- **Background work** is limited by Doze and manufacturer battery managers. WorkManager gives periodic, deferrable sync (minimum 15 minutes); real-time presence needs a foreground service or FCM.
- **Starting an Activity from the background** is blocked on Android 10+ except in narrow cases. This is why the earlier Charge3 auto-launch problem was not a build issue.
- **Installed-app listing** needs `QUERY_ALL_PACKAGES`, which Google Play restricts; direct APK distribution is not affected, but the launch-app feature is limited to apps that expose a launcher intent.
- **Installing from a link** requires the user to allow "install unknown apps" for their browser or file manager. NEXUS cannot do that for them.
- **Temporary ADMIN cannot change any of the above.** It grants backend authorisation only, never Android permissions or root.
- **Current-device location** requires the user to grant coarse or fine location permission when requesting a fix. The app distinguishes a last-known fix from a refreshed fix and does not upload it.
- **File selection** uses Android's Storage Access Framework. User-selected URI grants and metadata are local; no file bytes are transferred to another device.

## 6. Implemented local behavior and boundaries

- Device registration, rename/edit, role assignment, search, role filters,
  sorting, details and confirmed deletion persist in app-private preferences.
  New profiles have unknown connection status and no battery/last-seen values.
- Pairing invitations contain a random 256-bit token and expire after ten
  minutes. The client checks invitation shape, expiration, complementary roles
  and duplicate local requests. A server must authenticate the issuer, validate
  the token, authorize both devices and complete pairing; the UI reports a
  locally recorded request rather than a successful connection.
- File selection, file metadata, viewer handoff, transfer-request cancellation
  and local history work on-device. `DeviceTransferTransport` is the integration
  seam for a real authenticated transport. Waiting/cancelled states do not
  claim that file bytes were sent.
- The map can display this phone's permission-authorized location on
  OpenStreetMap. A registered profile is not assumed to be this phone, and
  remote profile selection shows no marker without real shared location data.
- Academy contains twelve searchable tutorials, category filters and locally
  saved completion progress.
- Local ADMIN/USER labels are not security authorization. Temporary ADMIN
  access is deliberately absent until a backend can enforce the existing
  three-hour expiry requirement.

## 7. Phased plan

| Phase | Scope | Needs from you |
|---|---|---|
| 1 | Foundation, branding, themes, navigation, CI. **Built.** | Push to GitHub and run CI |
| 2 | Authentication, server-authorized ADMIN and USER experiences | Firebase project, `google-services.json` |
| 3 | Trusted QR/link invitations, App Links and remote enrollment. Local QR foundation exists. | Backend and domain for App Links |
| 4 | Temporary ADMIN: passkey, three-hour server-enforced expiry, renewal, revocation, delegation settings | Phase 2 backend |
| 5 | Remote device monitoring and battery intelligence. Local profiles exist. | Backend |
| 6 | Consent-based remote location. Current-device map foundation exists. | Backend; OpenStreetMap requires no key |
| 7 | Screen, audio and camera sessions | TURN server |
| 8 | Secure remote file transfer. Local file selection/history exists. | Authenticated transport and storage/backend |
| 9 | NEXUS Academy, written against the implemented interface. **Built.** | none |
| 10 | Security hardening, real-device test pass, signed release | Release keystore |

A phase counts as complete only when it builds in CI **and** has been tested on real devices. Local foundations are identified above; remote/backend phases remain planned.
