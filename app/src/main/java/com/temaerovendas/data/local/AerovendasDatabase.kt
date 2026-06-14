// Caminho: app/src/main/java/com/temaerovendas/data/local/AerovendasDatabase.kt
package com.temaerovendas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.temaerovendas.data.local.dao.FavoriteDao
import com.temaerovendas.data.local.entity.FavoriteEntity

@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AerovendasDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
