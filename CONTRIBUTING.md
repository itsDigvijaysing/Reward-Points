# Contributing to Reward Points

Thank you for your interest in contributing to Reward Points! This document provides guidelines for contributing to this privacy-focused, offline reward tracking app.

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
- **Offline Only** - App must work completely offline
- **FOSS Dependencies** - Only use Free and Open Source libraries
- **API 21+ Support** - Maintain compatibility with Android 5.0+
- **Material Design** - Follow Google's Material Design principles

## 🚀 Getting Started

### 1. Fork and Clone
```bash
# Fork the repository on GitHub
git clone https://github.com/yourusername/Reward-Points.git
cd Reward-Points
```

### 2. Set Up Development Environment
- **Android Studio** - Arctic Fox or newer
- **JDK** - Version 8 or newer
- **Android SDK** - API levels 21-34
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
- Follow MVVM pattern where applicable
- Use AndroidX libraries consistently
- Implement proper error handling
- Avoid memory leaks with weak references
- Use efficient RecyclerView patterns

### Testing Requirements
- Test on multiple Android versions (API 21+)
- Test on different screen sizes
- Verify offline functionality
- Check data persistence and recovery
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

- Never add network permissions or internet access
- Avoid logging sensitive user data
- Use AndroidX Security for encryption
- Follow secure coding practices
- Report security issues privately (see SECURITY.md)

## 📞 Getting Help

- **GitHub Issues** - For bugs and feature requests
- **GitHub Discussions** - For questions and general discussion
- **Code Reviews** - Learn from feedback on pull requests

Thank you for contributing to Reward Points and helping make it better for everyone! 🎉
