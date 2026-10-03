# Chirp

Chirp is a Kotlin Multiplatform chat app targeting **Android, iOS, and Desktop (JVM)**.

## Features

- **Auth** — login, registration, registration-success, forgot password; session check on launch
  gates the nav graph.
- **Chat** — adaptive chat list / detail layout, create chat, manage chat (participants, leave),
  realtime messages, send / delete / retry, connection-state handling.
- **Profile** — view/edit profile.
- **Shared design system** — `Chirp*` components, theme, and multiplatform resources under
  `shared/src/commonMain`.

## Tech stack

- Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.1.1
- `compileSdk` / `targetSdk` 37, `minSdk` 24, JVM target 17
- Navigation-Compose (type-safe routes), Koin (DI), Coil 3 + Ktor, kotlinx.serialization /
  datetime / coroutines
- Backend: GitLive Firebase KMP (`firebase-auth`, `firestore`, `storage`, `messaging`) behind
  `core/data/firebase/FirebaseClients`

## Project structure

- `shared/src/commonMain` — everything shared: `App.kt`, `navigation/` (`NavigationRoot`,
  `authGraph`, `chatGraph`), `features/{auth,chat,profile}/` (each: `data/` DTOs +
  `Firestore*Repository`, `domain/` interfaces + models, `presentation/<screen>/`
  Screen/State/Action/Event/ViewModel, `di/*Module.kt`), `core/` (`data`, `di`, `designsystem`,
  `domain`, `platform`, `presentation/util`).
- `shared` source sets — `androidMain`, `jvmMain`, `iosMain`, `nativeMain` for platform code /
  `expect`/`actual` (e.g. `core/platform/requiresEmailVerification`); tests in `commonTest`,
  `androidHostTest`, `jvmTest`, `iosTest`.
- `androidApp/` — `MainActivity` + `ChirpApplication` (calls `initKoin` with `androidContext`).
- `desktopApp/` — `main.kt` + `DesktopFirebase.kt` (calls `initializeFirebaseDesktop()` **before**
  `initKoin()`).
- `iosApp/` — Xcode entry point; shared builds a static `Shared` framework (`iosArm64`,
  `iosSimulatorArm64`). Swift entry calls through `MainViewController.kt`.

## Running the apps

Use IDE run configurations or Gradle:

- Android: `./gradlew :androidApp:assembleDebug` (PowerShell:
  `./gradlew.bat :androidApp:assembleDebug`)
- Desktop standard run: `./gradlew :desktopApp:run`
- Desktop hot reload: `./gradlew :desktopApp:hotRun --auto`
- iOS: open `iosApp/` in Xcode and run from there.
