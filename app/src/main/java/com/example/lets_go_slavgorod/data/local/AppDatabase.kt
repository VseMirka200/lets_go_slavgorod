package com.example.lets_go_slavgorod.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lets_go_slavgorod.data.local.dao.FavoriteTimeDao
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.utils.Constants

/**
 * База данных приложения на основе Room
 * 
 * Основные функции:
 * - Хранение избранных времен отправления автобусов
 * - Обеспечение потокобезопасного доступа к данным
 * - Автоматическое управление миграциями
 * 
 * Оптимизации:
 * - Singleton паттерн для единого экземпляра базы данных
 * - Fallback к деструктивной миграции для упрощения
 * - Отключение экспорта схемы для уменьшения размера APK
 * 
 * @author VseMirka200
 * @version 1.1
 * @since 1.0
 */
@Database(
    entities = [FavoriteTimeEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false,
    autoMigrations = []
)
@TypeConverters()
abstract class AppDatabase : RoomDatabase() {

    /**
     * DAO для работы с избранными временами отправления
     * 
     * @return экземпляр FavoriteTimeDao для доступа к данным
     */
    abstract fun favoriteTimeDao(): FavoriteTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Получает экземпляр базы данных (Singleton)
         * 
         * Обеспечивает потокобезопасное создание единого экземпляра базы данных.
         * Использует двойную проверку блокировки для оптимизации производительности.
         * 
         * @param context контекст приложения для инициализации базы данных
         * @return экземпляр AppDatabase
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}