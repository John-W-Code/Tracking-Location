# Tracking Location App

## 📌 Overview
Tracking Location is an Android application designed to track the user's location in real-time using a **Foreground Service** and **BroadcastReceiver**. The app ensures location updates even when the app is in the background, making it ideal for tracking, navigation, or location-based services.

## 🚀 Features
- 📍 **Real-time location tracking** using Google Location API.
- 🔄 **Works in the background** via a foreground service.
- 🔔 **Notification support** to inform users about location tracking.
- 📡 **BroadcastReceiver** implementation to detect GPS status changes.
- 🔧 **Optimized for low battery consumption**.
- 📜 **Uses PendingIntent for location updates**.
- ❌ **Prevents location tracking if GPS is disabled** and redirects users to enable GPS.

## 🛠️ Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM (ViewModel, LiveData)
- **Google Location Services API**
- **Foreground Services**
- **BroadcastReceiver**
- **Android Jetpack Components**

## 📂 Project Structure
```bash
📦TrackingLocation
 ┣ 📂app
 ┃ ┣ 📂src/main/java/com/heydar/trackinglocation
 ┃ ┃ ┣ 📂location
 ┃ ┃ ┃ ┣ 📜ForegroundUpdateLocationService.kt
 ┃ ┃ ┃ ┣ 📜LocationBroadcastReceiver.kt
 ┃ ┃ ┃ ┣ 📜GPSBroadcastReceiver.kt
 ┃ ┃ ┣ 📜EnableGPSActivity.kt
 ┃ ┃ ┣ 📜MainActivity.kt
 ┃ ┣ 📂res
 ┃ ┃ ┣ 📂layout
 ┃ ┃ ┣ 📂drawable
 ┃ ┃ ┣ 📂values
```

## 🔧 Setup & Installation
### 1️⃣ Clone the repository
```bash
git clone https://github.com/haedarfarhani/Tracking-Location.git
cd Tracking-Location
```
### 2️⃣ Open in Android Studio
- Open **Android Studio**
- Click **Open an existing project**
- Select the `Tracking-Location` folder
- Sync Gradle

### 3️⃣ Add Google Play Services dependencies (if not included)
Ensure you have the following in your `build.gradle`:
```gradle
dependencies {
    implementation 'com.google.android.gms:play-services-location:21.0.1'
}
```

### 4️⃣ Run the app
Click **Run ▶️** in Android Studio or use the following command:
```bash
./gradlew installDebug
```

## 📌 Permissions
Make sure to request the following permissions in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

## 📲 How It Works
1️⃣ The **Foreground Service** continuously tracks the location.
2️⃣ If **GPS is turned off**, a `BroadcastReceiver` detects it and prompts the user to enable GPS.
3️⃣ Location updates are sent via `PendingIntent` to a `BroadcastReceiver`.
4️⃣ The location data can be observed in `ViewModel` and displayed in the UI.

## 🛠️ Troubleshooting
- **App crashes on startup?** Check if all permissions are granted.
- **Location updates are not working?** Ensure GPS is enabled and the app has location permissions.
- **Foreground service stops unexpectedly?** Verify that battery optimization settings allow background services.

## 👨‍💻 Contributing
Feel free to fork this repository and submit a pull request if you have any improvements!

## 📜 License
This project is licensed under the **MIT License**.

## 📞 Contact
- **GitHub:** [haedarfarhani](https://github.com/haedarfarhani)
- **Email:** haedar.farhani@gmail.com

