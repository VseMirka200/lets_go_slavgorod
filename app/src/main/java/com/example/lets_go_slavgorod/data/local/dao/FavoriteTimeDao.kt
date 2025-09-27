package com.example.lets_go_slavgorod.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с избранными временами в базе данных
 * 
 * Основные функции:
 * - Получение всех избранных времен
 * - Добавление нового избранного времени
 * - Удаление избранного времени
 * - Обновление избранного времени
 * - Проверка существования избранного времени
 * 
 * @author VseMirka200
 * @version 1.0
 */
@Dao
interface FavoriteTimeDao {
    /**
     * Получает все избранные времена, отсортированные по времени отправления
     * 
     * @return Flow со списком всех избранных времен
     */
    @Query("SELECT * FROM favorite_times ORDER BY departure_time ASC")
    fun getAllFavoriteTimes(): Flow<List<FavoriteTimeEntity>>

    /**
     * Добавляет новое избранное время в базу данных
     * 
     * @param favoriteTime избранное время для добавления
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteTime(favoriteTime: FavoriteTimeEntity)

    /**
     * Удаляет избранное время по его ID
     * 
     * @param scheduleId ID избранного времени для удаления
     */
    @Query("DELETE FROM favorite_times WHERE id = :scheduleId")
    suspend fun removeFavoriteTime(scheduleId: String)

    /**
     * Получает избранное время по его ID
     * 
     * @param id ID избранного времени
     * @return Flow с избранным временем или null
     */
    @Query("SELECT * FROM favorite_times WHERE id = :id")
    fun getFavoriteTimeById(id: String): Flow<FavoriteTimeEntity?>

    /**
     * Обновляет существующее избранное время
     * 
     * @param favoriteTime избранное время для обновления
     */
    @Update
    suspend fun updateFavoriteTime(favoriteTime: FavoriteTimeEntity)

    /**
     * Проверяет, является ли время избранным и активным
     * 
     * @param scheduleId ID времени для проверки
     * @return Flow с результатом проверки (true если избранное и активное)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_times WHERE id = :scheduleId AND is_active = 1 LIMIT 1)")
    fun isFavorite(scheduleId: String): Flow<Boolean>
}