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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lets_go_slavgorod"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")

    // =====================================================================================
    //                              БАЗА ДАННЫХ И ХРАНЕНИЕ
    // =====================================================================================
    
    // Room для локальной базы данных
    implementation("androidx.room:room-runtime:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    
    // DataStore для настроек
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // =====================================================================================
    //                              UI И НАВИГАЦИЯ
    // =====================================================================================
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation-layout")
    
    // Навигация
    implementation("androidx.navigation:navigation-compose:2.6.0")
    
    // =====================================================================================
    //                              УТИЛИТЫ И ИНСТРУМЕНТЫ
    // =====================================================================================
    
    // Timber для улучшенного логирования
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // WebView для открытия ссылок внутри приложения
    implementation("androidx.webkit:webkit:1.6.1")
    
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