# Reward Points - Personal Achievement Tracker

<div align="center">

![Version](https://img.shields.io/badge/version-1.5.0-blue.svg)
![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-GPL--3.0-orange.svg)
![F-Droid](https://img.shields.io/badge/F--Droid-Ready-green.svg)

**A simple, privacy-focused personal reward system that helps you track achievements and motivate yourself through a points-based system.**

[Download](#download) • [Features](#features) • [Privacy](#privacy) • [Building](#building) • [Contributing](#contributing)

</div>

## 📱 Overview

Reward Points is a completely **offline**, **open-source** personal motivation app that gamifies your daily achievements. Track your mood, complete missions, set goals, and reward yourself with points - all while keeping your data 100% private and local.

Perfect for anyone who wants to build better habits, stay motivated, and maintain privacy without sacrificing functionality.

## ✨ Features

### 🎯 **Core Functionality**
- **Daily Mood Tracking** - Log how you're feeling and earn points
- **Custom Rewards** - Create personalized rewards to redeem with points
- **Daily Missions** - Set and complete daily tasks for consistent motivation
- **Long-term Goals** - Track bigger achievements and milestones
- **Transaction History** - View detailed history of all points earned and spent
- **Achievement System** - Unlock achievements as you progress

### 🛡️ **Privacy & Security**
- **100% Offline** - No internet connection required
- **Local Storage Only** - All data stays on your device
- **No Tracking** - Zero analytics, ads, or data collection
- **No Permissions** - Doesn't request unnecessary Android permissions
- **Open Source** - Full transparency with source code review

### 🎨 **User Experience**
- **Material Design 3** - Modern, clean interface
- **Dark/Light Theme** - Automatic theme switching support
- **Intuitive Navigation** - Easy-to-use interface for all ages
- **Responsive Design** - Works on phones and tablets
- **Accessibility** - Designed with accessibility in mind

## 📥 Download

### F-Droid (Recommended)
*Coming Soon* - The app will be available on F-Droid, the privacy-focused app store.

### Direct APK
Download the latest release APK from the [Releases](../../releases) section.

### Build from Source
See the [Building](#building) section below.

## 🚀 Quick Start

1. **Install the app** from F-Droid or download the APK
2. **Set your username** in the welcome screen
3. **Log your daily mood** to start earning points
4. **Create custom rewards** for things you enjoy
5. **Set daily missions** to build consistent habits
6. **Redeem rewards** when you've earned enough points!

## 🏗️ Building

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 8 or newer
- Android SDK with API level 21+

### Build Steps
```bash
# Clone the repository
git clone https://github.com/yourusername/Reward-Points.git
cd Reward-Points

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/`

## 🔒 Privacy

Your privacy is our top priority:

- **No Network Access** - The app doesn't connect to the internet
- **Local Data Only** - Everything is stored on your device using encrypted SharedPreferences
- **No Analytics** - We don't collect usage statistics or crash reports
- **No Ads** - Completely ad-free experience
- **No Accounts** - No registration or login required
- **Open Source** - Code is fully auditable and transparent

## 🛠️ Technical Details

### Architecture
- **Native Android** - Built with Java and AndroidX libraries
- **Material Design 3** - Modern UI components and theming
- **Local Storage** - Uses encrypted SharedPreferences for data persistence
- **MVVM Pattern** - Clean architecture with proper separation of concerns

### Dependencies
- AndroidX libraries (AppCompat, Material, etc.)
- Gson for JSON serialization
- AndroidX Security for encrypted storage
- Only FOSS-compatible dependencies

### Compatibility
- **Minimum Android Version:** 5.0 (API 21)
- **Target Android Version:** 14 (API 34)
- **Architecture:** ARM, ARM64, x86, x86_64
- **Storage:** ~10MB installation size

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Ways to Contribute
- 🐛 **Report bugs** via GitHub Issues
- 💡 **Suggest features** for future versions
- 🔧 **Submit pull requests** with improvements
- 📖 **Improve documentation** and help others
- 🌍 **Translate the app** to other languages

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes and test thoroughly
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to your branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

### Code Guidelines
- Follow Android development best practices
- Maintain API 21+ compatibility
- Keep dependencies FOSS-compatible
- Add appropriate comments and documentation
- Test on multiple device sizes and Android versions

## 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

### What this means:
- ✅ **Use** the app for any purpose
- ✅ **Study** and modify the source code
- ✅ **Share** the app with others
- ✅ **Distribute** modified versions
- ⚠️ **Must** keep the same license for derivatives
- ⚠️ **Must** provide source code for modifications

## 🙏 Acknowledgments

- **Material Design** - Google's design system
- **AndroidX** - Modern Android development libraries
- **F-Droid Community** - For promoting FOSS apps
- **Contributors** - Everyone who helps improve the app

## 📞 Support

- **Issues:** Report bugs and request features via [GitHub Issues](../../issues)
- **Discussions:** Join conversations in [GitHub Discussions](../../discussions)
- **Security:** Report security issues privately via email (see SECURITY.md)

---

<div align="center">

**Made with ❤️ for privacy-conscious users who value local data control**

[⬆ Back to Top](#reward-points---personal-achievement-tracker)

</div>
