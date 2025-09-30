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
 * База данных приложения (Room)
 */
@Database(
    entities = [FavoriteTimeEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false,
    autoMigrations = []
)
@TypeConverters()
abstract class AppDatabase : RoomDatabase() {

    // DAO для избранных времен
    abstract fun favoriteTimeDao(): FavoriteTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Получение экземпляра базы данных (Singleton)
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