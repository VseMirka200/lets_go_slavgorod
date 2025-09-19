package com.example.slavgorodbus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.slavgorodbus.data.local.dao.FavoriteTimeDao
import com.example.slavgorodbus.data.local.entity.FavoriteTimeEntity
import kotlinx.coroutines.asExecutor

@Database(
    entities = [FavoriteTimeEntity::class],
    version = 3,
    exportSchema = false,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteTimeDao(): FavoriteTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bus_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .setQueryExecutor(kotlinx.coroutines.Dispatchers.IO.asExecutor())
                    .setTransactionExecutor(kotlinx.coroutines.Dispatchers.IO.asExecutor())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}