package com.drmiaji.hisnulmuslim.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "duadetails",
    foreignKeys = [
        ForeignKey(
            entity = DuaName::class,
            parentColumns = ["dua_global_id"],
            childColumns = ["dua_global_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DuaDetail(
    @PrimaryKey
    val ID: Int,
    val dua_global_id: String,
    val dua_segment_id: Int,
    val top: String?,
    val arabic_diacless: String?,
    val arabic: String?,
    val transliteration: String?,
    val translations: String?,
    val bottom: String?,
    val reference: String?
)