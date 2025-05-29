package com.drmiaji.hisnulmuslim.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.drmiaji.hisnulmuslim.data.entities.Category
import com.drmiaji.hisnulmuslim.data.entities.DuaName
import kotlinx.coroutines.flow.Flow

@Dao
interface HisnulMuslimDao {
    @Query("SELECT * FROM category ORDER BY id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM duanames ORDER BY chap_id ASC")
    fun getAllChapters(): Flow<List<DuaName>>

    @Query("SELECT * FROM duanames WHERE category = :categoryName ORDER BY chap_id ASC")
    fun getChaptersByCategory(categoryName: String): Flow<List<DuaName>>
}