package com.drmiaji.hisnulmuslim.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
    @PrimaryKey val id: Int,    // non-nullable Int
    val name: String            // non-nullable String
)
