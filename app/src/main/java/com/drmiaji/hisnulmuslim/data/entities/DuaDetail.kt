package com.drmiaji.hisnulmuslim.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duadetails")
data class DuaDetail(
    @PrimaryKey val id: Int,
    val dua_global_id: Int,
    val top: String?,
    val arabic_diacless: String?,
    val arabic: String?,
    val transliteration: String?,
    val translations: String?,
    val bottom: String?,
    val reference: String?
)