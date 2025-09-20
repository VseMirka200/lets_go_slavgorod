# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all annotations
-keepattributes *Annotation*

# Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# DataStore
-keep class androidx.datastore.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep data classes and entities
-keep class com.example.slavgorodbus.data.model.** { *; }
-keep class com.example.slavgorodbus.data.local.entity.** { *; }

# Keep ViewModels
-keep class com.example.slavgorodbus.ui.viewmodel.** { *; }

# Keep notification classes
-keep class com.example.slavgorodbus.notifications.** { *; }

# Keep BroadcastReceiver classes
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep AlarmManager related classes
-keep class android.app.AlarmManager { *; }
-keep class android.app.PendingIntent { *; }

# Keep Application class
-keep class com.example.slavgorodbus.BusApplication { *; }

# Keep MainActivity
-keep class com.example.slavgorodbus.MainActivity { *; }

# Optimize: Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Optimize: Remove debug code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(...);
    static void checkNotNullParameter(...);
    static void checkReturnedValueIsNotNull(...);
    static void checkNotNullReturnValue(...);
    static void checkFieldIsNotNull(...);
    static void checkNotNullField(...);
    static void checkExpressionValueIsNotNull(...);
    static void checkNotNullExpressionValue(...);
}

# Optimize: Remove Kotlin metadata
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class ** {
    @kotlin.Metadata *;
}