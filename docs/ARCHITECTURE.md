# NEXUS architecture and plan

## 1. What NEXUS is

One Android APK, two roles. An **ADMIN** device manages the account's enrolled devices; **USER** devices report status and take part in sessions the device owner has consented to. An enrolled USER can become a **temporary ADMIN** for three hours by entering the account's Admin Elevation Passkey. The server, not the phone, decides whether that session is still valid.

## 2. Client architecture (Android)

Single Gradle module `:app` for now. Feature code is separated by package so it can be split into Gradle modules later without rewriting. Kotlin, Jetpack Compose, Material 3, MVVM with unidirectional data flow, Coroutines and Flow.

```
com.nexus.app
  core/        shared models and utilities (Roadmap today)
  ui/theme/    colour, typography, NexusTheme (follows system light/dark)
  ui/navigation/  Destination, NexusApp (bottom navigation)
  ui/screens/  screens and shared components
  -- added in later phases --
  auth/ pairing/ roles/ monitoring/ battery/ location/ remote/ files/
  notifications/ academy/ settings/ security/
```

Rules: no business logic in Activities or composables; every screen reads state from a ViewModel; every privileged action is authorised on the server, never only in the UI.

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
- A **Google Maps API key** (or an OpenStreetMap-based map) for Phase 6.

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

## 6. Phased plan

| Phase | Scope | Needs from you |
|---|---|---|
| 1 | Foundation, branding, themes, navigation, CI. **Built in this delivery.** | Push to GitHub and run CI |
| 2 | Authentication, ADMIN and USER roles | Firebase project, `google-services.json` |
| 3 | QR and link pairing, App Links | Domain for App Links |
| 4 | Temporary ADMIN: passkey, three-hour server-enforced expiry, renewal, revocation, delegation settings | Phase 2 backend |
| 5 | Device monitoring and battery intelligence | none |
| 6 | Location | Maps key |
| 7 | Screen, audio and camera sessions | TURN server |
| 8 | File transfer | Storage configured |
| 9 | NEXUS Academy, written against the finished interface | none |
| 10 | Security hardening, real-device test pass, signed release | Release keystore |

A phase counts as complete only when it builds in CI **and** has been tested on real devices. Until then the in-app roadmap shows it as Planned.
