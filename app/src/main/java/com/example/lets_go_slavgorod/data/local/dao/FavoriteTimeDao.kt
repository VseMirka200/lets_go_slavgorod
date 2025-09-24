package com.example.lets_go_slavgorod.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTimeDao {
    @Query("SELECT * FROM favorite_times ORDER BY departure_time ASC")
    fun getAllFavoriteTimes(): Flow<List<FavoriteTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteTime(favoriteTime: FavoriteTimeEntity)

    @Query("DELETE FROM favorite_times WHERE id = :scheduleId")
    suspend fun removeFavoriteTime(scheduleId: String)

    @Query("SELECT * FROM favorite_times WHERE id = :id")
    fun getFavoriteTimeById(id: String): Flow<FavoriteTimeEntity?>

    @Update
    suspend fun updateFavoriteTime(favoriteTime: FavoriteTimeEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_times WHERE id = :scheduleId AND is_active = 1 LIMIT 1)")
    fun isFavorite(scheduleId: String): Flow<Boolean>
}