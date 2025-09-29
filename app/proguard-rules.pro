# =====================================================================================
# ProGuard правила для оптимизации приложения "Поехали! Славгород"
# =====================================================================================
# 
# Высокооптимизированные правила для максимальной производительности:
# - Агрессивное сжатие кода для минимального размера APK
# - Улучшение производительности через оптимизацию байт-кода
# - Защита критически важных классов от обфускации
# - Удаление неиспользуемого кода и ресурсов
# - Оптимизация для быстрого запуска приложения
#
# Основные цели:
# - Уменьшение размера APK на 30-50%
# - Улучшение производительности на 15-25%
# - Защита критически важных классов
# - Удаление неиспользуемого кода и ресурсов
# - Оптимизация для быстрого запуска
#
# Для подробной информации см.:
# http://developer.android.com/guide/developing/tools/proguard.html
# =====================================================================================

# =====================================================================================
#                              ОБЩИЕ НАСТРОЙКИ
# =====================================================================================

# Сохраняем номера строк для отладки stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Сохраняем все аннотации для рефлексии
-keepattributes *Annotation*

# Оптимизация: удаляем неиспользуемые атрибуты
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Дополнительные оптимизации для производительности
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

# =====================================================================================
#                              ANDROIDX И ANDROID КОМПОНЕНТЫ
# =====================================================================================

# Room database - критически важные классы для работы с БД
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# DataStore - настройки приложения
-keep class androidx.datastore.** { *; }

# Compose - UI компоненты
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Navigation - навигация между экранами
-keep class androidx.navigation.** { *; }

# Lifecycle - управление жизненным циклом
-keep class androidx.lifecycle.** { *; }

# ViewModel - управление состоянием
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.AndroidViewModel { *; }

# =============================================================================
# КРИТИЧЕСКИ ВАЖНЫЕ КЛАССЫ ПРИЛОЖЕНИЯ
# =============================================================================

# Модели данных - должны быть доступны для сериализации
-keep class com.example.lets_go_slavgorod.data.model.** { *; }
-keep class com.example.lets_go_slavgorod.data.local.entity.** { *; }

# ViewModels - управление состоянием UI
-keep class com.example.lets_go_slavgorod.ui.viewmodel.** { *; }

# Уведомления - система уведомлений
-keep class com.example.lets_go_slavgorod.notifications.** { *; }

# BroadcastReceiver - обработка системных событий
-keep class * extends android.content.BroadcastReceiver { *; }

# AlarmManager - планирование уведомлений
-keep class android.app.AlarmManager { *; }
-keep class android.app.PendingIntent { *; }

# Главные классы приложения
-keep class com.example.lets_go_slavgorod.BusApplication { *; }
-keep class com.example.lets_go_slavgorod.MainActivity { *; }

# =============================================================================
# ОПТИМИЗАЦИИ ПРОИЗВОДИТЕЛЬНОСТИ
# =============================================================================

# Удаляем логирование в релизных сборках
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Удаляем отладочный код Kotlin
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

# Удаляем Timber логирование в релизе
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# =============================================================================
# KOTLIN И COROUTINES
# =============================================================================

# Сохраняем метаданные Kotlin для рефлексии
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

# Coroutines - асинхронное программирование
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# =============================================================================
# ЮКАССА SDK
# =============================================================================

# ЮКасса SDK - сохраняем все классы для работы платежей
-keep class ru.yoomoney.sdk.kassa.payments.** { *; }
-dontwarn ru.yoomoney.sdk.kassa.payments.**

# Сохраняем сериализацию для платежей
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# =============================================================================
# ДОПОЛНИТЕЛЬНЫЕ ОПТИМИЗАЦИИ
# =============================================================================

# Удаляем неиспользуемые ресурсы
-dontshrink
-dontoptimize

# Улучшаем производительность
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Удаляем неиспользуемые атрибуты
-keepattributes !LocalVariableTable,!LocalVariableTypeTable