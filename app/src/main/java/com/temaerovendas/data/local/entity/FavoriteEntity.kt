// Caminho: app/src/main/java/com/temaerovendas/data/local/entity/FavoriteEntity.kt
package com.temaerovendas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val aircraftId: String,
    val savedAt: Long = System.currentTimeMillis()
)
