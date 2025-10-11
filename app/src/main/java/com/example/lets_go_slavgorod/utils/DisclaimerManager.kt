package com.example.lets_go_slavgorod.utils

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * Менеджер для управления показом диалога с предупреждением
 * 
 * Основные функции:
 * - Проверяет, нужно ли показать диалог новому пользователю
 * - Запоминает выбор пользователя
 * - Управляет состоянием показа диалога
 * 
 * @author VseMirka200
 * @version 1.0
 * @since 1.0
 */
object DisclaimerManager {
    
    private const val PREFS_NAME = "disclaimer_prefs"
    private const val KEY_DISCLAIMER_SHOWN = "disclaimer_shown"
    private const val KEY_DISCLAIMER_DONT_SHOW = "disclaimer_dont_show"
    
    /**
     * Проверяет, нужно ли показать диалог с предупреждением
     * 
     * @param context контекст приложения
     * @return true если нужно показать диалог, false если нет
     */
    fun shouldShowDisclaimer(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        val disclaimerShown = prefs.getBoolean(KEY_DISCLAIMER_SHOWN, false)
        val dontShowAgain = prefs.getBoolean(KEY_DISCLAIMER_DONT_SHOW, false)
        
        Timber.d("Disclaimer check: shown=$disclaimerShown, dontShow=$dontShowAgain")
        
        // Показываем диалог если:
        // 1. Пользователь еще не видел его ИЛИ
        // 2. Пользователь не выбрал "Не показывать снова"
        return !disclaimerShown || !dontShowAgain
    }
    
    /**
     * Отмечает, что пользователь принял условия
     * 
     * @param context контекст приложения
     */
    fun markDisclaimerAccepted(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putBoolean(KEY_DISCLAIMER_SHOWN, true)
            .apply()
        
        Timber.d("Disclaimer accepted by user")
    }
    
    /**
     * Отмечает, что пользователь выбрал "Не показывать снова"
     * 
     * @param context контекст приложения
     */
    fun markDisclaimerDontShowAgain(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putBoolean(KEY_DISCLAIMER_SHOWN, true)
            .putBoolean(KEY_DISCLAIMER_DONT_SHOW, true)
            .apply()
        
        Timber.d("Disclaimer marked as 'don't show again'")
    }
    
    /**
     * Сбрасывает настройки диалога (для тестирования)
     * 
     * @param context контекст приложения
     */
    fun resetDisclaimerSettings(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .clear()
            .apply()
        
        Timber.d("Disclaimer settings reset")
    }
    
    /**
     * Получает SharedPreferences для хранения настроек диалога
     * 
     * @param context контекст приложения
     * @return экземпляр SharedPreferences
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
