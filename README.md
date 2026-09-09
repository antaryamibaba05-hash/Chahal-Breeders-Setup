# High Flyer Pro Tracker

A professional, offline-first native Android application for managing, tracking, and analyzing high flyer pigeon lofts and competitive flight sessions.

[![Build Debug APK](https://github.com/owner/high-flyer-pro-tracker/actions/workflows/build-apk.yml/badge.svg)](https://github.com/owner/high-flyer-pro-tracker/actions/workflows/build-apk.yml)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Offline--First%20MVVM-00C853)
![Database](https://img.shields.io/badge/Storage-Room%20(SQLite)-00599C?logo=sqlite&logoColor=white)

---

## 🕊️ Application Identity

- **Application Name**: High Flyer Pro Tracker
- **Application ID**: `com.highflyerpro.tracker`
- **Namespace**: `com.highflyerpro.tracker`
- **Min SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 36 (Android 16)
- **Compile SDK**: API 36

---

## 🚀 Key Features

- **Permanent Digital Pigeon Profiles**:
  - Unique permanent internal ID (`PIGEON-YYYY-XXXXXX`) for lifetime data continuity.
  - Ring number tracking, strain/breed, color, eye color, sex, and status management.
  - Lineage tracking (Sire & Dam relationship validation with cycle protection).
- **High Flyer Flight Tracking & Live Timer**:
  - Real timestamp tracking (`System.currentTimeMillis() - releaseTimestamp`).
  - Timer remains 100% accurate across screen lock, backgrounding, process recreation, and device reboots.
  - Active session auto-recovery upon application launch.
- **Precision Landing System**:
  - Instant one-tap landing capture with sequential position assignment (1st, 2nd, 3rd, etc.).
  - Confirmation mode to prevent accidental taps outdoors.
  - Manual landing time correction with automatic duration and ranking recalculation.
- **Performance Engine & Analytics**:
  - Automatic calculation of lifetime flight hours, average flight time, and personal best duration.
  - Custom event point calculation with customizable duration thresholds.
  - Leaderboards categorized by total hours, longest flight, average duration, and point scoring.
- **Photo Management & EXIF Orientation**:
  - Multi-category gallery (Profile, Eye, Full Body, Wing Spread, Pedigree, Awards).
  - Built-in Camera capture via CameraX and internal FileProvider.
  - Hardware EXIF orientation correction and optimized thumbnail generation.
- **Loft Groups & Roster Management**:
  - Organize pigeons into customized squads (High Flyers, Young Birds, Breeders, Tournament Team, etc.).
- **100% Offline Architecture & Data Safety**:
  - Pure local SQLite persistence via Android Room.
  - No accounts, no logins, no cloud dependencies, and zero telemetry.
  - Complete JSON backup and restore with embedded metadata and verification.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| --- | --- |
| **Platform** | Native Android (100% Kotlin) |
| **UI Framework** | Jetpack Compose, Material Design 3 (M3) |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Architecture |
| **Local Persistence** | Room Database (SQLite), AndroidX DataStore (Preferences) |
| **Asynchronous Engine** | Kotlin Coroutines & StateFlow |
| **Navigation** | Navigation Compose |
| **Camera & Media** | CameraX, Coil (Offline Image Rendering), ExifInterface |
| **Serialization** | Moshi Kotlin (Offline Backup & Restore) |
| **Testing** | JUnit 4, Robolectric, Roborazzi Screenshot Testing, AndroidX Test |
| **Build Tooling** | Gradle 9.3.1 (Kotlin DSL), KSP, Android Gradle Plugin 9.1.1 |

---

## 📦 Build Instructions

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or Version 21 (Eclipse Temurin recommended)
- **Android SDK**: Build Tools 36.0.0, Platform API 36
- **Operating System**: Linux (Ubuntu 22.04+ / GitHub Actions), macOS, or Windows

### Building the Debug APK

Run the included Gradle Wrapper:

```bash
./gradlew assembleDebug
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Running Tests

Execute local JVM unit and Robolectric tests:

```bash
./gradlew testDebugUnitTest
```

### Clean Rebuild

```bash
./gradlew clean assembleDebug
```

---

## 🔒 Release Signing Setup

Debug builds use the standard Android debug keystore mechanism automatically and require no configuration.

For release builds, supply the following environment variables or Gradle properties (`-P` or `~/.gradle/gradle.properties`):

- `KEYSTORE_PATH`: Absolute or relative path to your `.jks` file
- `STORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password

If these variables are not provided, release builds will build without signing, preventing build failures in CI/CD environments.

---

## 🌐 Offline & Privacy Architecture

High Flyer Pro Tracker was architected from the ground up to operate completely offline:

1. **No External Network Dependencies**: The application does not require Firebase, Supabase, Google Play Services authentication, or any remote server.
2. **Local Data Ownership**: All records, flight sessions, timestamps, and images reside strictly within the device's internal storage and SQLite database (`high_flyer_pro.db`).
3. **Field Reliability**: Pigeon flyers often operate in remote lofts or rural areas with poor or no cellular connectivity. The app functions with full capabilities regardless of network connectivity.

---

## 📄 License

Proprietary software developed for competitive High Flyer pigeon loft operations.
