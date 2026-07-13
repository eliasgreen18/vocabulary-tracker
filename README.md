# 📚 VocabularyTracker

**VocabularyTracker** is a high-performance Android application designed for language learners and avid readers. It seamlessly integrates a digital book reader with an advanced vocabulary tracking system, powered by AI and offline translation tools.

## 🚀 Key Features

*   **📖 Smart Reading Companion**: Integrated EPUB and PDF reader with sentence-level context extraction.
*   **🧠 SRS Vocabulary Learning**: Spaced Repetition System (SRS) based on encounter frequency and mastery levels to ensure long-term retention.
*   **✨ AI Tutor (Gemini)**: Get deep linguistic insights, nuances, and natural usage examples for any word.
*   **🌍 Offline Translations**: Instant full-sentence translations using Google ML Kit (completely offline).
*   **📷 OCR Camera Scanner**: Add words and context from physical books using your camera.
*   **⏱️ Reading Analytics**: Persistent session tracking, reading streaks, and detailed activity heatmaps.
*   **☁️ Cloud Sync**: Automatic and manual backups to Google Drive.
*   **🖼️ Word of the Day Widget**: Stay motivated with a Glance-based home screen widget.

## 🛠 Architecture & Tech Stack

The project follows **Modern Android Development (MAD)** practices and **Clean Architecture** principles:

*   **UI**: 100% Jetpack Compose with a focus on high-density, minimalist design.
*   **Pattern**: MVVM (Model-View-ViewModel) + Mappers for data transformation.
*   **Dependency Injection**: Hilt (Dagger) for robust and testable code.
*   **Local Database**: Room with strategic indexing and denormalization for instant loading of large datasets.
*   **Navigation**: Modularized Feature Graphs with state restoration.
*   **Concurrency**: Kotlin Coroutines & Flow for reactive data streams.
*   **Networking**: Retrofit & OkHttp.
*   **Image Loading**: Coil.
*   **Services**: WorkManager for background synchronization and translation tasks.

## 📦 Project Structure

```text
app/
 ├── data/           # Database, DAOs, Entities, and Repository Implementations
 ├── domain/         # Domain Models, Repository Interfaces, and Use Cases
 ├── ui/             # Compose Screens, ViewModels, and Theme
 └── util/           # Logging, Helpers, and Extension functions
```

## ⚙️ Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 17
*   A Gemini AI API Key (optional, for AI features)

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/VocabularyTracker.git
    ```
2.  Open the project in Android Studio.
3.  (Optional) Add your Gemini API Key in the app settings to enable AI Tutor features.
4.  Build and run the `:app` module.

## 🧪 Testing
The project includes a suite of Unit Tests focused on core logic:
```bash
./gradlew test
```

## 📜 License
This project is licensed under the MIT License - see the LICENSE file for details.
