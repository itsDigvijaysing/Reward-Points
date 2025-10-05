# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep all model classes
-keep class com.rewardpoints.app.models.** { *; }

# Keep all constants
-keep class com.rewardpoints.app.constants.** { *; }

# Keep PreferencesManager
-keep class com.rewardpoints.app.utils.PreferencesManager { *; }

# Keep Activity classes
-keep class com.rewardpoints.app.MainActivity { *; }
-keep class com.rewardpoints.app.PointsHistoryActivity { *; }

# Android specific rules
-keep class androidx.** { *; }
-keep class android.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# F-Droid specific rules - Open Source App Store
# F-Droid builds from source, so we need robust obfuscation rules

# FOSS compatibility - keep open source components
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.lang.model.element.Modifier

# F-Droid privacy optimization - remove analytics and tracking
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# F-Droid compatibility for reproducible builds
-keepattributes *Annotation*,InnerClasses,Signature,SourceFile,LineNumberTable

# F-Droid security optimization
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# F-Droid Parcelable compatibility (better than Samsung version)
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# F-Droid build optimization - more aggressive than commercial stores
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 3
-allowaccessmodification
-dontpreverify

# F-Droid crash reporting compatibility (FOSS crash reporting)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
