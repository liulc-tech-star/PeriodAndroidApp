# 月期知纪 · Period-Mobile

Jetpack Compose Android application for private menstrual-cycle tracking, prediction, and insight. The app is optimized for fully offline use, keeps data on-device, and adds a first-launch privacy agreement to ensure transparent data handling.

## ✨ Features
- **Flexible record management**: tap “添加记录” to log the start/end dates with calendar pickers; remove the latest entry with one tap.
- **Cycle analytics**: automatic calculation of cycle length plus period, follicular, ovulation, and luteal phases with continuous timelines.
- **Modern Compose UI**: gradient backgrounds, phase cards, warning indicators, and localized strings (Chinese).
- **Persistent storage**: Room database (schema v3) with auto-increment IDs and Kotlin `LocalDate` converters.
- **Strict privacy**: data never leaves the device; a dedicated `privacy.html` dialog must be accepted on first launch before any tracking features unlock.

## 🧱 Tech Stack
- Kotlin + Jetpack Compose Material 3
- Room Database 2.7.0
- Android SDK 35 / Gradle 8.11.1
- Java 21 (HotSpot) toolchain

## 🚀 Getting Started
1. Install Android Studio (Hedgehog or newer) with Android SDK 35.
2. Install JDK 21 and point `JAVA_HOME` to it (HotSpot build verified).
3. Clone the repo and build:

```powershell
git clone https://github.com/liulc-tech-star/Period-Mobile.git
cd Period-Mobile
$env:JAVA_HOME="C:\Program Files (x86)\Java\jdk-21.0.9.10-hotspot"
.\gradlew assembleDebug
```

The resulting APK is emitted to `app\build\outputs\apk\debug\app-debug.apk`. Import the project into Android Studio if you prefer IDE-based builds or to run on an emulator/physical device.

## 📱 Usage
1. Launch the app and review the privacy modal (content from `app/src/main/assets/privacy.html`). Accept to proceed.
2. On the landing page:
	- Tap **添加记录** to open the dialog, pick start/end dates, and save.
	- Tap **删除最近一条记录** to undo the latest entry.
3. Review the “上次月经日期” card and the cycle analysis cards for predicted phases.
4. Dates stay in local storage; uninstalling the app removes them.

## 🔐 Privacy & Data Handling
- App is offline-only: no networking code, no external sync.
- Room DB lives entirely on the device; users can delete entries at any time.
- First-launch dialog references `privacy.html`, ensuring informed consent before data entry.

## 📁 Project Structure (excerpt)
```
Period-Mobile/
├── app/
│   ├── src/main/java/com/example/period_app_01/
│   │   ├── MainActivity.kt
│   │   ├── EnterDate.kt
│   │   └── data/* (Room entities, DAO, repository)
│   ├── src/main/assets/privacy.html
│   └── src/main/res/* (themes, icons, strings)
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

## 📄 License
See `LICENSE` for the full license text.
