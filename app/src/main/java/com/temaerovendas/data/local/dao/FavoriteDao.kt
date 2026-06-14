// Caminho: app/src/main/java/com/temaerovendas/data/local/dao/FavoriteDao.kt
package com.temaerovendas.data.local.dao

import androidx.room.*
import com.temaerovendas.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY savedAt DESC")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND aircraftId = :aircraftId)")
    suspend fun isFavorite(userId: String, aircraftId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND aircraftId = :aircraftId")
    suspend fun deleteFavorite(userId: String, aircraftId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearFavoritesForUser(userId: String)
}
