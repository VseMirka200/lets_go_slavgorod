/**
 * Управление настройками обновлений приложения
 * 
 * Этот модуль отвечает за:
 * - Сохранение настроек автоматической проверки обновлений
 * - Хранение информации о доступных обновлениях
 * - Управление временем последней проверки
 * - Отслеживание состояния уведомлений об обновлениях
 * 
 * @author VseMirka
 * @version 1.0
 * @since 2024
 */
package com.example.slavgorodbus.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore для хранения настроек обновлений
 * 
 * Создает отдельный DataStore с именем "update_preferences"
 * для изоляции настроек обновлений от других настроек приложения.
 */
val Context.updatePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_preferences")

/**
 * Ключи для доступа к настройкам обновлений в DataStore
 * 
 * Содержит все необходимые ключи для работы с настройками
 * обновлений через DataStore Preferences API.
 */
object UpdatePreferencesKeys {
    /** Ключ для состояния автоматической проверки обновлений */
    val AUTO_UPDATE_CHECK_ENABLED = booleanPreferencesKey("auto_update_check_enabled")
    
    /** Ключ для времени последней проверки обновлений */
    val LAST_UPDATE_CHECK_TIME = longPreferencesKey("last_update_check_time")
    
    /** Ключ для версии доступного обновления */
    val AVAILABLE_UPDATE_VERSION = stringPreferencesKey("available_update_version")
    
    /** Ключ для URL скачивания обновления */
    val AVAILABLE_UPDATE_URL = stringPreferencesKey("available_update_url")
    
    /** Ключ для описания изменений в обновлении */
    val AVAILABLE_UPDATE_NOTES = stringPreferencesKey("available_update_notes")
    
    /** Ключ для состояния показа уведомления об обновлении */
    val UPDATE_NOTIFICATION_SHOWN = booleanPreferencesKey("update_notification_shown")
}

/**
 * Класс для управления настройками обновлений
 * 
 * Предоставляет реактивные потоки данных и методы для работы
 * с настройками обновлений через DataStore Preferences.
 * 
 * @param context Контекст приложения для доступа к DataStore
 */
class UpdatePreferences(private val context: Context) {
    
    /**
     * Поток состояния автоматической проверки обновлений
     * 
     * @return Flow<Boolean> - true если автоматическая проверка включена, false если отключена
     *         По умолчанию возвращает true (автоматическая проверка включена)
     */
    val autoUpdateCheckEnabled: Flow<Boolean> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AUTO_UPDATE_CHECK_ENABLED] ?: true
        }
    
    /**
     * Поток времени последней проверки обновлений
     * 
     * @return Flow<Long> - timestamp последней проверки в миллисекундах, 0L если проверка не выполнялась
     */
    val lastUpdateCheckTime: Flow<Long> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.LAST_UPDATE_CHECK_TIME] ?: 0L
        }
    
    /**
     * Поток версии доступного обновления
     * 
     * @return Flow<String?> - версия доступного обновления или null если обновлений нет
     */
    val availableUpdateVersion: Flow<String?> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_VERSION]
        }
    
    /**
     * Поток URL для скачивания обновления
     * 
     * @return Flow<String?> - URL для скачивания APK файла или null если обновлений нет
     */
    val availableUpdateUrl: Flow<String?> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_URL]
        }
    
    /**
     * Поток описания изменений в обновлении
     * 
     * @return Flow<String?> - описание изменений (release notes) или null если обновлений нет
     */
    val availableUpdateNotes: Flow<String?> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_NOTES]
        }
    
    /**
     * Поток состояния показа уведомления об обновлении
     * 
     * @return Flow<Boolean> - true если уведомление было показано, false если нет
     */
    val updateNotificationShown: Flow<Boolean> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN] ?: false
        }
    
    /**
     * Устанавливает состояние автоматической проверки обновлений
     * 
     * @param enabled true для включения автоматической проверки, false для отключения
     */
    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.AUTO_UPDATE_CHECK_ENABLED] = enabled
        }
    }
    
    /**
     * Устанавливает время последней проверки обновлений
     * 
     * @param time Timestamp последней проверки в миллисекундах
     */
    suspend fun setLastUpdateCheckTime(time: Long) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.LAST_UPDATE_CHECK_TIME] = time
        }
    }
    
    /**
     * Сохраняет информацию о доступном обновлении
     * 
     * Сохраняет все данные об обновлении и сбрасывает флаг показа уведомления.
     * 
     * @param version Версия доступного обновления
     * @param url URL для скачивания APK файла
     * @param notes Описание изменений в обновлении
     */
    suspend fun setAvailableUpdate(version: String, url: String, notes: String) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_VERSION] = version
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_URL] = url
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_NOTES] = notes
            preferences[UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN] = false // Сбрасываем флаг уведомления
        }
    }
    
    /**
     * Очищает информацию о доступном обновлении
     * 
     * Удаляет все данные об обновлении из DataStore, включая
     * версию, URL, описание и флаг показа уведомления.
     */
    suspend fun clearAvailableUpdate() {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences.remove(UpdatePreferencesKeys.AVAILABLE_UPDATE_VERSION)
            preferences.remove(UpdatePreferencesKeys.AVAILABLE_UPDATE_URL)
            preferences.remove(UpdatePreferencesKeys.AVAILABLE_UPDATE_NOTES)
            preferences.remove(UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN)
        }
    }
    
    /**
     * Устанавливает состояние показа уведомления об обновлении
     * 
     * @param shown true если уведомление было показано, false если нет
     */
    suspend fun setUpdateNotificationShown(shown: Boolean) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN] = shown
        }
    }
}
