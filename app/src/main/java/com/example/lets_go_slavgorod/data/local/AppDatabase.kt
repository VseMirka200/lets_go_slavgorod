package com.example.lets_go_slavgorod.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lets_go_slavgorod.data.local.dao.FavoriteTimeDao
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.utils.Constants

/**
 * База данных приложения (Room)
 */
@Database(
    entities = [FavoriteTimeEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters()
abstract class AppDatabase : RoomDatabase() {

    // DAO для избранных времен
    abstract fun favoriteTimeDao(): FavoriteTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Миграция с версии 4 на 5 - добавление полей route_number и route_name
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE favorite_times ADD COLUMN route_number TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE favorite_times ADD COLUMN route_name TEXT NOT NULL DEFAULT ''")
            }
        }

        // Получение экземпляра базы данных (Singleton)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}