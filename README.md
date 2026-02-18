# QuizzAI 🎯

An Android quiz application covering 6 programming languages with Firebase Firestore integration, featuring a modern dark-themed UI with gamification elements.

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Java-orange.svg)
![Database](https://img.shields.io/badge/Database-Firebase%20Firestore-yellow.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 📱 Features

### Core Functionality
- **6 Programming Languages**: C, C++, Java, Python, Kotlin, C#
- **3 Difficulty Levels**: Easy, Medium, Hard (15 questions each)
- **270+ Questions**: Comprehensive question bank stored in Firebase Firestore
- **Dynamic Updates**: Add new questions without app updates via Firestore

### User Experience
- 🎨 **Modern Dark Theme**: Space-themed UI with purple/cyan accents
- 🏆 **Achievement System**: Earn badges by completing topics and challenges
- 📊 **Analytics Dashboard**: Track performance across all subjects
- 📈 **Progress Tracking**: View detailed statistics and quiz history
- ⏱️ **Timer Functionality**: Timed quizzes with countdown
- 🎯 **Score System**: Instant feedback and score tracking
- 💬 **AI Chatbot**: Dialogflow-powered assistant for learning support

### Technical Features
- **Firebase Authentication**: Secure user login with Google Sign-In
- **Cloud Firestore**: Real-time NoSQL database for questions
- **Push Notifications**: Daily reminders and motivational quotes
- **Leaderboard**: Compete with other users globally
- **Material Design 3**: Modern Android UI components

## 🛠️ Tech Stack

- **Language**: Java
- **IDE**: Android Studio
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth (Google Sign-In)
- **AI Integration**: Dialogflow API
- **UI Framework**: Material Design 3

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR-USERNAME/QuizzAI.git
cd QuizzAI
```

### 2. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project: "QuizzAI"
3. Add Android app with package: `com.example.quizz`
4. Download `google-services.json` → place in `app/` folder
5. Enable Authentication (Email/Password + Google Sign-In)
6. Create Firestore Database (Production mode)

### 3. Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /questions/{topic}/{difficulty}/{questionId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null 
                         && request.auth.uid == userId;
    }
  }
}
```

### 4. Upload Questions

Temporarily add in `MainActivity.onCreate()`:
```java
new FirestoreUploader(this).uploadAll();
```
Run app once, then remove this line.

### 5. Build & Run
```bash
./gradlew clean build
./gradlew installDebug
```

## 📂 Project Structure

```
QuizzAI/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/quizz/
│   │   │   ├── MainActivity.java
│   │   │   ├── QuizActivity.java
│   │   │   ├── LeaderboardActivity.java
│   │   │   ├── AnalyticsActivity.java
│   │   │   ├── ChatActivity.java
│   │   │   ├── FirestoreQuestionLoader.java
│   │   │   ├── Question.java
│   │   │   ├── BadgeManager.java
│   │   │   └── ...
│   │   ├── res/
│   │   └── assets/ (JSON question files)
│   └── google-services.json
├── .gitignore
└── README.md
```

## 🗄️ Firestore Structure

```
questions/
├── c/
│   ├── easy/ → {question, optionA, optionB, optionC, optionD, answer}
│   ├── medium/
│   └── hard/
├── cpp/, java/, python/, kotlin/, csharp/
    └── (same structure)

users/{userId}/
├── profile: {name, email, photoUrl}
├── scores: {topic, difficulty, score, date}
└── badges: {badgeId, earnedDate}
```

## 📖 Usage Example

```java
FirestoreQuestionLoader loader = new FirestoreQuestionLoader();

loader.loadQuestions("java", "easy", 10, true, 
    new FirestoreQuestionLoader.QuestionLoadCallback() {
        @Override
        public void onQuestionsLoaded(List<Question> questions) {
            displayQuestions(questions);
        }
        
        @Override
        public void onError(String errorMessage) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
);
```

## 🎨 Customization

### Change Theme Colors
Edit `res/values/colors.xml`:
```xml
<color name="primary_purple">#6366f1</color>
<color name="accent_cyan">#06b6d4</color>
<color name="background_dark">#0f1117</color>
```

### Add New Language
1. Create `language_easy.json`, `language_medium.json`, `language_hard.json`
2. Add to `FirestoreUploader.FILE_MAPPINGS`
3. Upload to Firestore
4. Add UI card in MainActivity

## 🐛 Troubleshooting

**App crashes on startup:**
- Ensure `google-services.json` is in `app/` folder
- Verify package name: `com.example.quizz`

**Questions not loading:**
- Check internet connection
- Verify Firestore rules allow read access
- Ensure questions uploaded to Firestore

**Google Sign-In fails:**
- Add SHA-1 fingerprint to Firebase Console:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/AmazingFeature`
3. Commit changes: `git commit -m 'Add AmazingFeature'`
4. Push to branch: `git push origin feature/AmazingFeature`
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👨‍💻 Author

**Akash Singh**
- DIT Student
- Email: as524610@gmail.com
- Specialization: Mobile App Development

## 🙏 Acknowledgments

- Firebase for backend services
- Material Design for UI components
- Dialogflow for AI chatbot
- DIT faculty for guidance

## 🗺️ Roadmap

- [ ] Add JavaScript, Swift, Rust support
- [ ] Multiplayer quiz mode
- [ ] Video explanations for answers
- [ ] Web dashboard for analytics
- [ ] Offline mode with sync
- [ ] Social sharing features

---

⭐ **If you found this helpful, please star the repo!** ⭐

Made with ❤️ by Akash Singh
