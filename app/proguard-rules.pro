# Reward Points v3.0 ProGuard Rules
# Kotlin + Compose + Room + Ktor + Koin

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rewardpoints.app.**$$serializer { *; }
-keepclassmembers class com.rewardpoints.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.rewardpoints.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room entities and DAOs
-keep class com.rewardpoints.app.data.local.db.entity.** { *; }
-keep class com.rewardpoints.app.data.local.db.dao.** { *; }
-keep class com.rewardpoints.app.data.local.db.AppDatabase { *; }

# Keep domain models
-keep class com.rewardpoints.app.domain.model.** { *; }

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Koin
-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * {
    public <init>(...);
}

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# General Android
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
