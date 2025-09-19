# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# DataStore
-keep class androidx.datastore.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Glance widgets
-keep class androidx.glance.** { *; }

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

# Serialization (if using)
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}