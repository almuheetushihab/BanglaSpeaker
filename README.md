# 🎙️ BanglaSpeaker - AI-Powered Bangla to English Speech Learning & Translation App

**BanglaSpeaker** is an advanced Android application engineered with Artificial Intelligence (Google Gemini AI) and on-device offline capabilities. It is designed to empower Bengali speakers to learn, translate, and speak fluent, natural English through effortless voice interaction.

---

## 📱 Application Screens Overview

The application features **4 primary screens** accessible via a floating translucent bottom navigation bar:

### 1. 🎙️ **Speaker Screen (Voice Translation)** - Route: `speak`
* **Primary Purpose**: Dictate speech in natural Bangla and get instant, grammatically accurate English translations with native voice playback.
* **Key Features**:
  * **Voice Dictation**: Tap or hold the mic button to speak in Bangla. Includes real-time RMS audio waveform visualizer bars and a countdown timer.
  * **AI Speech Correction**: Automatically detects speech recognition errors or phonetic mistakes and silently fixes the source Bangla transcript.
  * **Multi-Style Translation**: Generates 3 distinct style variations for every sentence:
    1. **Natural**: Fluent, conversational, everyday English.
    2. **Formal**: Polite, professional, or official phrasing.
    3. **Casual**: Informal speech suitable for friends and casual settings.
  * **Intent Classification & Grammar Tips**: Identifies sentence intent (question, answer, statement, request, command, greeting) and provides 1-2 line grammar/vocabulary tips along with key translated phrases.
  * **Text-To-Speech (TTS)**: Sentence-by-sentence natural voice playback in American (US) or British (UK) accents with controllable speech rates.
  * **Manual Editing & Auto-Translate**: Edit recognized Bangla text prior to translation and toggle auto-translation upon speech completion.

---

### 2. 📜 **History Screen (Conversation Log & Bookmarks)** - Route: `history`
* **Primary Purpose**: Store, search, and review all previous translations offline.
* **Key Features**:
  * **Room SQLite Persistence**: Automatically saves all translation history locally on the device.
  * **Live Search**: Instant text filtering across Bangla inputs and English translations.
  * **Tab Filtering (All / Bookmarks)**: Bookmark important translations and filter saved items instantly.
  * **Quick Play & Practice Redirect**: Listen to translations on demand or jump straight into the Practice screen using a selected sentence as the target.
  * **History Cleanup**: Delete individual items or clear all history via confirmation dialogs.

---

### 3. 🎯 **Practice Screen (Interactive Learning Hub)** - Route: `practice`
* **Primary Purpose**: Comprehensive speaking practice hub featuring **3 dedicated sub-tabs**:

  1. 🔄 **Repeat After Me**:
     * **Function**: Listen to a target English sentence, speak it aloud into the mic, and evaluate pronunciation.
     * **Feature**: Performs word-by-word string alignment and Levenshtein distance analysis, highlighting correct words (green) and incorrect words (red) with an overall accuracy percentage score (%).

  2. 🤖 **AI Roleplay**:
     * **Function**: Practice interactive English conversations in real-life scenarios (Job Interview, Shopping, Travel, Office Meeting).
     * **Feature**: Dynamic context-aware AI dialogue designed to build conversational confidence.

  3. 📇 **Vocabulary Notebook (Spaced Repetition)**:
     * **Function**: Review key phrases and vocabulary extracted during translation sessions.
     * **Feature**: Interactive 3D flip flashcards showing Bangla/English definitions with `Easy`, `Medium`, and `Hard` difficulty ratings that schedule future review intervals using a Spaced Repetition System (SRS).

---

### 4. ⚙️ **Settings Screen (Preferences & Configuration)** - Route: `settings`
* **Primary Purpose**: Manage app behavior, theme, voice options, and offline AI models.
* **Key Features**:
  * **Scenario Context**: Select conversation scenarios (Casual Chat, Office, Job Interview, Travel, Shopping, Study) to tailor AI translation phrasing.
  * **Voice Accent & Speech Rate**: Choose between US and UK accents, and adjust playback speed (0.5x to 1.2x).
  * **App Theme**: Toggle between Light Mode, Dark Mode, or System Default dynamic theme.
  * **Interface Language**: Switch full application UI localization between English, Bangla, or System language.
  * **API & Offline Model Monitor**: Verify Google Gemini API Key configuration and monitor on-device ML Kit translation model download status.

---

## 🛠️ Key Technical Highlights & Architecture

| Feature | Description |
| :--- | :--- |
| **Dual Engine Translation** | High-performance Google Gemini 2.5 Flash API (online AI) with Google ML Kit Bengali-to-English (on-device offline fallback). |
| **Real-Time Speech Recognition** | Custom cold-flow wrapper (`SpeechRecognizerManager`) emitting live RMS volume levels for real-time visualizer animation. |
| **Sentence-Level TTS** | Integrated `TtsManager` splitting long text for smooth, natural audio playback with accent and speed adjustments. |
| **Pronunciation Accuracy Scoring** | Levenshtein distance and word-matching algorithm providing visual color feedback and percentage accuracy scores. |
| **Spaced Repetition System (SRS)** | Vocabulary cards auto-generated from translation key phrases with difficulty-based review interval scheduling. |
| **Local Offline Persistence** | Complete Room Database integration (`AppDatabase`, `ConversationDao`, `VocabularyDao`, `PracticeDao`). |

---

## 🏗️ Code Structure & Directory Layout

The project adheres strictly to **MVVM (Model-View-ViewModel)** architecture and **Jetpack Compose** best practices:

```
com.banglaenglish.speaker/
│
├── MainActivity.kt                     # Single Activity UI host & Compose Navigation Graph
│
├── data/                               # Data Layer (Models, Repositories, API Clients, Native Drivers)
│   ├── Models.kt                       # Domain models, Enums (ConversationItem, Scenario, Intent, Style)
│   ├── GeminiTranslationEngine.kt     # Google Gemini 2.5 Flash REST API Client
│   ├── MlKitTranslationEngine.kt      # Offline Google ML Kit On-Device Translator Engine
│   ├── TranslationRepository.kt        # Repository managing engine selection (Gemini vs ML Kit)
│   ├── SpeechRecognizerManager.kt      # Native SpeechRecognizer Flow wrapper with audio levels
│   ├── TtsManager.kt                   # Native TextToSpeech manager with speed & accent control
│   ├── UserPreferencesRepository.kt    # SharedPreferences storage for user settings
│   │
│   └── local/                          # Room Database Layer
│       ├── AppDatabase.kt              # Room Database definition (v1)
│       ├── ConversationEntity.kt / Dao # Translation log schema and DAO queries
│       ├── VocabularyEntity.kt / Dao   # Spaced repetition flashcard schema and SRS queries
│       └── PracticeSessionEntity.kt / Dao # Practice statistics and streak counter DAO
│
└── ui/                                 # Presentation Layer (Jetpack Compose)
    ├── UiText.kt                       # Localization string resources wrapper
    │
    ├── components/                     # Reusable Material 3 Composables
    │   ├── BottomNavigationBar.kt      # Floating Translucent Pill Navigation Bar
    │   ├── MicButton.kt                # Animated pulsing dictation mic button
    │   ├── AudioVisualizerBars.kt      # Live audio frequency visualizer
    │   ├── TopBarHeader.kt             # Header bar with daily practice streak counter
    │   ├── ModelBanner.kt              # Offline translation model status indicator
    │   ├── AppChip.kt, GradientButton.kt, ShimmerLoadingCard.kt, ScreenBackground.kt
    │
    ├── screen/                         # ViewModels & Screen Views
    │   ├── speaker/                    # Voice Translation (`SpeakerScreen.kt`, `SpeakerViewModel.kt`)
    │   ├── history/                    # Conversation History (`HistoryScreen.kt`, `HistoryViewModel.kt`)
    │   ├── practice/                   # Learning Hub (`PracticeScreen.kt`, `PracticeViewModel.kt`)
    │   │   └── components/             # (`RepeatAfterMeSection.kt`, `RoleplaySection.kt`, `VocabNotebookSection.kt`)
    │   └── settings/                   # App Settings (`SettingsScreen.kt`, `SettingsViewModel.kt`)
    │
    └── theme/                          # Design System & Styling
        ├── Color.kt, Shape.kt, Type.kt, Theme.kt, ExtendedTheme.kt
```

---

## 🧰 Tech Stack & Libraries Used

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Architecture**: MVVM, Clean Architecture, Repository Pattern
* **Navigation**: Navigation Compose
* **Concurrency**: Kotlin Coroutines & StateFlow
* **AI & Machine Learning**: Google Gemini 2.5 Flash API, Google ML Kit Translation
* **Database**: Room Database (SQLite)
* **Audio & Speech**: Android SpeechRecognizer API, Native TextToSpeech
* **Networking & Serialization**: OkHttp3, Kotlinx Serialization
* **Preferences**: SharedPreferences, AppCompatDelegate (App Locale Management)

---

## 🚀 Getting Started

1. Clone the repository and open it in **Android Studio (Giraffe, Ladybug, or 2024+)**.
2. Add your **Gemini API Key** in `local.properties`:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   *(If omitted, the app gracefully falls back to the on-device ML Kit engine).*
3. Perform a Gradle Sync and run the application on an Android device or emulator.
