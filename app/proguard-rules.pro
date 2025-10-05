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
}

# Samsung Galaxy Store specific rules
-keep class com.samsung.** { *; }
-dontwarn com.samsung.**

# Samsung Galaxy Store SDK compatibility
-keep class com.sec.** { *; }
-dontwarn com.sec.**

# Enhanced security for Samsung devices
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Samsung Galaxy Store analytics compatibility
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Optimize for Samsung Galaxy Store distribution
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Samsung Galaxy Store crash reporting compatibility
-keepattributes *Annotation*,InnerClasses,Signature,Exceptions
