package com.drmiaji.hisnulmuslim.data.dao

import androidx.room.*
import com.drmiaji.hisnulmuslim.data.entities.DuaDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDetailDao {
    @Query("SELECT * FROM duadetails WHERE dua_global_id = :globalId ORDER BY dua_segment_id")
    fun getDuaDetailsByGlobalId(globalId: String): Flow<List<DuaDetail>>

    @Query("SELECT * FROM duadetails WHERE ID = :id")
    suspend fun getDuaDetailById(id: Int): DuaDetail?

    @Query("SELECT * FROM duadetails WHERE arabic LIKE '%' || :searchQuery || '%' OR transliteration LIKE '%' || :searchQuery || '%' OR translations LIKE '%' || :searchQuery || '%'")
    fun searchDuaDetails(searchQuery: String): Flow<List<DuaDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuaDetail(duaDetail: DuaDetail)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuaDetails(duaDetails: List<DuaDetail>)
}