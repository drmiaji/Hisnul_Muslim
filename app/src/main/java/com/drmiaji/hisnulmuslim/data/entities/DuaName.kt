package com.drmiaji.hisnulmuslim.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duanames")
data class DuaName(
    @PrimaryKey val ID: String,
    val category: Int?,
    val chap_id: Int?,
    val chapname: String?,
    val dua_global_id: Int, // MUST be not null in both entity and DB
    val dua_id: String?,
    val duaname: String?,
    val tags: String?
)