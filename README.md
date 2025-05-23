## 📂 Project Info

- 📅 Semester: S1 2025 – Monash FIT5046
- 👨‍💻 Developers: Haowen Wang, Tao Wan, Yufei Bai, Zeyu Gong
- 🧠 Tech Stack: Kotlin, Jetpack Compose, Firebase Auth, Room DB, WorkManager, OpenRouter AI API
- 📁 Folder Structure:
  - `app/` – main application code
  - `screens/` – composables for each screen
  - `components/` – shared UI components
  - `services/` – API, DB, and background logic
- ▶️ To Run:
  1. Clone this repo
  2. Open in Android Studio Arctic Fox+
  3. Run on Emulator (API 24+ recommended)

## 📚 BookBuddy – AI-powered Learning App for Kids

**BookBuddy** is a Kotlin-based Android app that helps children aged 10–15 improve reading, science, and math skills through AI-generated content and interactive quizzes. Designed with a child-friendly UI, the app combines Firebase Authentication, Jetpack Compose, and OpenRouter API to deliver an educational and engaging experience.

## ✨ Features

### 1. 🔐 Login & Register
- Firebase Authentication integration
- Email/password-based login
- "Confirm Password" validation for registration
- Eye icon to toggle password visibility
- Error prompts for invalid email/short passwords
- Child-friendly prompts like: _"Back again? Tap to log in!"_

### 2. 🧠 AI Reading Challenge (English)
- Users enter a keyword to generate a short paragraph
- Paragraph is generated using an AI API via OpenRouter
- Automatically generates 3 comprehension quiz questions
- Users select answers and get immediate feedback
- Quiz results are stored in local Room database

### 3. 🔬 Science & ➗ Math Quiz
- Dropdown topic selection (e.g., Animals, Fractions)
- AI generates 3 multiple-choice questions
- Users submit answers and receive scores
- Daily quiz stats are saved locally for tracking

### 4. 🏠 Homepage
- Four animated subject buttons
- Colorful gradient background and child-friendly icons
- Navigation to all modules via Bottom Navigation Bar

### 5. 🗂 Room Database + 📊 Daily Stats
- Track each user's quiz performance by date and category
- Stored locally using Room, linked to Firebase user ID
- Display and testing enabled via "Try the Database" screen

### 6. 🕒 WorkManager Background Cleanup
- A weekly job runs every Sunday at midnight
- Automatically clears old quiz stats to start a new week
- Manual trigger button provided for testing
