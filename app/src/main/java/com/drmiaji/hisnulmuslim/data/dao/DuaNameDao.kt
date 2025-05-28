package com.drmiaji.hisnulmuslim.data.dao

import androidx.room.*
import com.drmiaji.hisnulmuslim.data.entities.DuaName
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaNameDao {
    @Query("SELECT * FROM duanames ORDER BY chap_id, chapname")
    fun getAllDuaNames(): Flow<List<DuaName>>

    // Fix: Use String for category, per schema
    @Query("SELECT * FROM duanames WHERE category = :category ORDER BY chap_id, chapname")
    fun getDuaNamesByCategory(category: String): Flow<List<DuaName>>

    @Query("SELECT * FROM duanames WHERE chapname = :globalId")
    suspend fun getDuaNameByGlobalId(globalId: String): DuaName?

    // Fix: Remove tags reference, only search chapname and category
    @Query("SELECT * FROM duanames WHERE chapname LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%'")
    fun searchDuaNames(searchQuery: String): Flow<List<DuaName>>

    // Fix: Only return chapname column
    @Query("SELECT chapname FROM duanames ORDER BY chap_id")
    fun getAllChapterNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuaName(duaName: DuaName)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuaNames(duaNames: List<DuaName>)
}