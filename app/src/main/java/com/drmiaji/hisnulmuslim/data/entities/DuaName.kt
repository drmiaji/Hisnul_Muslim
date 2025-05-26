package com.drmiaji.hisnulmuslim.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "duanames",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["dua_global_id"], unique = true) // ✅ Add this line
    ]
)
data class DuaName(
    @PrimaryKey
    val ID: Int,
    val dua_global_id: String,
    val chap_id: Int,
    val dua_id: Int,
    val chapname: String,
    val duaname: String,
    val tags: String?,
    val category: Int
)