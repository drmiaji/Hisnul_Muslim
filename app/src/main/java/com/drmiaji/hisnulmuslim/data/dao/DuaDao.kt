package com.drmiaji.hisnulmuslim.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.drmiaji.hisnulmuslim.data.entities.Category
import com.drmiaji.hisnulmuslim.data.entities.DuaDetail
import com.drmiaji.hisnulmuslim.data.entities.DuaName

@Dao
interface DuaDao {
    // Get all categories
    @Query("SELECT * FROM category")
    fun getAllCategories(): List<Category>

    // Get all dua names
    @Query("SELECT * FROM duanames")
    fun getAllDuaNames(): List<DuaName>

    // Get all dua details for a given dua_global_id
    @Query("SELECT * FROM duadetails WHERE dua_global_id = :duaGlobalId")
    fun getDuaDetailsByGlobalId(duaGlobalId: Int): List<DuaDetail>

    // Get all dua names for a given category
    @Query("SELECT * FROM duanames WHERE category = :categoryId")
    fun getDuaNamesByCategory(categoryId: Int): List<DuaName>

    // Example: get a dua name by global id
    @Query("SELECT * FROM duanames WHERE dua_global_id = :duaGlobalId LIMIT 1")
    fun getDuaNameByGlobalId(duaGlobalId: Int): DuaName?
}