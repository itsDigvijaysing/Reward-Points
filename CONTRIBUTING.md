# Contributing to Stat Up

Thank you for your interest in contributing to Stat Up! This document provides guidelines for contributing to this privacy-focused, offline reward tracking app.

## 🤝 Ways to Contribute

- 🐛 **Report Bugs** - Help us identify and fix issues
- 💡 **Suggest Features** - Propose new functionality
- 🔧 **Submit Code** - Implement features or fix bugs
- 📖 **Improve Documentation** - Enhance README, comments, or guides
- 🌍 **Add Translations** - Help make the app accessible worldwide
- 🧪 **Testing** - Test on different devices and Android versions

## 📋 Before You Start

### Code of Conduct
- Be respectful and inclusive
- Focus on constructive feedback
- Help create a welcoming environment for all contributors

### Project Principles
- **Privacy First** - No tracking, analytics, or data collection
- **Offline First** - Core gamification works fully offline. Two network surfaces exist, both opt-in: Todoist sync (user-supplied API token) and the Gemini AI Coach (user-supplied API key). The app makes no network calls until the user adds credentials in Settings.
- **FOSS Dependencies** - Only use Free and Open Source libraries
- **API 26+ Support** - Maintain compatibility with Android 8.0+
- **Material 3 / Compose** - Jetpack Compose with Material 3 design system

## 🚀 Getting Started

### 1. Fork and Clone
```bash
# Fork the repository on GitHub
git clone https://github.com/yourusername/Stat-Up.git
cd Stat-Up
```

### 2. Set Up Development Environment
- **Android Studio** - Iguana or newer (Compose + Kotlin 2.1 support); or just Gradle + the Android cmdline tools.
- **JDK** - Version 17 (Gradle 8.13 will not run on JDK 24+)
- **Android SDK** - minSdk 26, targetSdk 35, **compileSdk 36** (install `platforms;android-35`, `platforms;android-36`, `build-tools;35.0.0`, `build-tools;36.0.0`, and `platform-tools` via `sdkmanager`)
- **Git** - For version control

### 3. Build the Project
```bash
# Clean build
./gradlew clean

# Build debug version
./gradlew assembleDebug

# Run tests
./gradlew test
```

## 💻 Development Guidelines

### Code Style
- Follow Android development best practices
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise
- Use proper Android lifecycle methods

### Architecture Guidelines
- Follow MVVM (Screen → ViewModel → Repository → Room/DataStore/Ktor)
- All wiring goes through Koin in `di/AppModule.kt`
- Use Compose state holders; avoid leaking ViewModel scopes into composables
- Keep RPG logic (point math, decay, rank transitions) inside the engines in `rpg/`

### Testing Requirements
- Test on Android 8.0+ (API 26+)
- Test on different screen sizes
- Verify offline functionality (toggle airplane mode)
- Check data persistence across reboots
- Validate UI responsiveness

## 🔧 Submitting Changes

### 1. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes
- Write clear, focused commits
- Test thoroughly on multiple devices
- Ensure code builds without warnings
- Update documentation if needed

### 3. Commit Guidelines
```bash
# Good commit messages
git commit -m "feat: Add daily streak counter to missions"
git commit -m "fix: Resolve JSON parsing crash on corrupted data"
git commit -m "docs: Update installation instructions"
```

### 4. Submit Pull Request
- Provide clear description of changes
- Reference any related issues
- Include screenshots for UI changes
- Ensure CI builds pass

## 🐛 Reporting Bugs

### Before Reporting
- Check existing issues to avoid duplicates
- Test on latest version
- Gather device/Android version info

### Bug Report Template
```markdown
**Device Info:**
- Device: [e.g. Pixel 6]
- Android Version: [e.g. Android 12]
- App Version: [e.g. 1.5.0]

**Bug Description:**
Clear description of what happened

**Steps to Reproduce:**
1. Open app
2. Navigate to...
3. Tap on...
4. Observe error

**Expected Behavior:**
What should have happened

**Screenshots:**
If applicable, add screenshots
```

## 💡 Feature Requests

When suggesting features, consider:
- **Privacy Impact** - Will it compromise offline/privacy principles?
- **FOSS Compatibility** - Can it be implemented with open source libraries?
- **User Value** - Does it solve a real user problem?
- **Complexity** - Is it worth the development and maintenance cost?

### Feature Request Template
```markdown
**Feature Description:**
Clear description of the proposed feature

**Problem it Solves:**
What user problem does this address?

**Proposed Solution:**
How should this feature work?

**Alternatives Considered:**
Other ways to solve this problem

**Privacy Considerations:**
How does this maintain offline/privacy principles?
```

## 🌍 Translation Guidelines

### Adding a New Language
1. Create language-specific resource folders
2. Translate all user-facing strings
3. Test UI layout with translated text
4. Consider cultural context and conventions

### Translation Files
- Copy `res/values/strings.xml` to `res/values-[language]/strings.xml`
- Translate all string values
- Maintain proper XML formatting
- Test for text overflow issues

## 📝 Documentation

### Code Documentation
- Add JavaDoc comments for public methods
- Explain complex algorithms or business logic
- Document any workarounds or hacks
- Keep comments up to date with code changes

### README Updates
- Update feature lists when adding functionality
- Keep build instructions current
- Update screenshots if UI changes significantly
- Maintain accurate technical specifications

## 🔒 Security Considerations

- The `INTERNET` permission exists for the two opt-in integrations: Todoist sync and the Gemini AI Coach. Do not add new network calls without discussion.
- Never log sensitive user data (Todoist tokens, Gemini API keys, chat transcripts, future API keys).
- Use AndroidX Security (`EncryptedSharedPreferences` / Tink) via `SecretStorage` for any new secret storage. The existing `KEY_TODOIST_TOKEN` and `KEY_GEMINI_API_KEY` are the model — never store secrets in plain DataStore.
- Follow secure coding practices.
- Report security issues privately (see SECURITY.md)

## 📞 Getting Help

- **GitHub Issues** - For bugs and feature requests
- **GitHub Discussions** - For questions and general discussion
- **Code Reviews** - Learn from feedback on pull requests

Thank you for contributing to Stat Up and helping make it better for everyone! 🎉
