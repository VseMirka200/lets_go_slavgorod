/**
 * Конфигурация сборки приложения "Поехали! Славгород"
 * 
 * Оптимизированная конфигурация для:
 * - Быстрого запуска приложения
 * - Минимального размера APK
 * - Максимальной производительности
 * - Совместимости с современными версиями Android
 * 
 * @author VseMirka200
 * @version 1.1
 * @since 1.0
 */
@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinSymbolProcessingKsp)
}

android {
    namespace = "com.example.lets_go_slavgorod"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lets_go_slavgorod"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 10005
        versionName = "v1.05"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // =====================================================================================
        //                              ОПТИМИЗАЦИИ ДЛЯ БЫСТРОГО ЗАПУСКА
        // =====================================================================================
        
        // Multidex для поддержки core library desugaring
        multiDexEnabled = true
        
        // Фильтрация архитектур для уменьшения размера APK
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        
        // Оптимизация ресурсов
        resConfigs("ru", "en") // Только необходимые языки
    }

    // =====================================================================================
    //                              КОНФИГУРАЦИЯ СБОРКИ
    // =====================================================================================
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            isZipAlignEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    // =====================================================================================
    //                              КОМПИЛЯЦИЯ И ОПТИМИЗАЦИЯ
    // =====================================================================================
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompilerVersion.get()
    }
    
    // =====================================================================================
    //                              УПАКОВКА И РЕСУРСЫ
    // =====================================================================================
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

// =====================================================================================
//                              ЗАВИСИМОСТИ
// =====================================================================================

dependencies {
    // =====================================================================================
    //                              ОСНОВНЫЕ ЗАВИСИМОСТИ
    // =====================================================================================
    
    // Multidex для поддержки core library desugaring
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Kotlin Coroutines для асинхронного программирования
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // =====================================================================================
    //                              БАЗА ДАННЫХ И ХРАНЕНИЕ
    // =====================================================================================
    
    // Room для локальной базы данных
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // DataStore для настроек
    implementation(libs.androidx.datastore.preferences)
    
    // =====================================================================================
    //                              UI И НАВИГАЦИЯ
    // =====================================================================================
    
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation.layout)
    
    // Навигация
    implementation(libs.androidx.navigation.compose)
    
    // =====================================================================================
    //                              УТИЛИТЫ И ИНСТРУМЕНТЫ
    // =====================================================================================
    
    // Timber для улучшенного логирования
    implementation(libs.timber)
    
    // WebView для открытия ссылок внутри приложения
    implementation(libs.androidx.webkit)
    
    // =====================================================================================
    //                              ПЛАТЕЖИ И ЮКАССА
    // =====================================================================================
    
    // ЮКасса SDK для приема платежей (стабильная версия)
    implementation("ru.yoomoney.sdk.kassa.payments:yookassa-android-sdk:6.8.0")
    
    // Дополнительные зависимости для ЮКассы
    implementation("com.vk:android-sdk:4.0.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("org.slf4j:slf4j-android:1.7.36")
    
    // =====================================================================================
    //                              ТЕСТИРОВАНИЕ
    // =====================================================================================
    
    // Unit тесты
    testImplementation(libs.junit)
    
    // Android тесты
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug инструменты
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // =====================================================================================
    //                              ДЕСУГАРИНГ
    // =====================================================================================
    
    // Core Library Desugaring для поддержки новых API на старых версиях Android
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}