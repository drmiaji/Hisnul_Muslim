package com.drmiaji.hisnulmuslim.data.dao

import androidx.room.*
import com.drmiaji.hisnulmuslim.data.entities.DuaName
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaNameDao {
    @Query("SELECT * FROM duanames ORDER BY chap_id, chapname")
    fun getAllDuaNames(): Flow<List<DuaName>>

    @Query("SELECT * FROM duanames WHERE category = :categoryId ORDER BY category")
    fun getDuaNamesByCategory(categoryId: Int): Flow<List<DuaName>>

    @Query("SELECT * FROM duanames WHERE chapname = :globalId")
    suspend fun getDuaNameByGlobalId(globalId: String): DuaName?

    @Query("SELECT * FROM duanames WHERE chapname LIKE '%' || :searchQuery || '%' OR chapname LIKE '%' || :searchQuery || '%' OR tags LIKE '%' || :searchQuery || '%'")
    fun searchDuaNames(searchQuery: String): Flow<List<DuaName>>

    @Query("SELECT DISTINCT chapname FROM duanames ORDER BY chap_id")
    fun getAllChapterNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuaName(duaName: DuaName)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuaNames(duaNames: List<DuaName>)
}