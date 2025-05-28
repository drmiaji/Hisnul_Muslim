package com.drmiaji.hisnulmuslim.data.repository

import com.drmiaji.hisnulmuslim.data.dao.CategoryDao
import com.drmiaji.hisnulmuslim.data.dao.DuaDetailDao
import com.drmiaji.hisnulmuslim.data.dao.DuaNameDao
import com.drmiaji.hisnulmuslim.data.entities.Category
import com.drmiaji.hisnulmuslim.data.entities.DuaDetail
import com.drmiaji.hisnulmuslim.data.entities.DuaName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HisnulMuslimRepository(
    private val categoryDao: CategoryDao,
    private val duaNameDao: DuaNameDao,
    private val duaDetailDao: DuaDetailDao
) {

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun getCategoryById(id: Int): Category? = categoryDao.getCategoryById(id)

    // DuaName operations
    fun getAllDuaNames(): Flow<List<DuaName>> = duaNameDao.getAllDuaNames()

    // category is now String, not Int!
    fun getDuaNamesByCategory(category: String): Flow<List<DuaName>> =
        duaNameDao.getDuaNamesByCategory(category)

    suspend fun getDuaNameByGlobalId(globalId: String): DuaName? =
        duaNameDao.getDuaNameByGlobalId(globalId)

    fun searchDuaNames(query: String): Flow<List<DuaName>> =
        duaNameDao.searchDuaNames(query)

    fun getAllChapterNames(): Flow<List<String>> = duaNameDao.getAllChapterNames()

    // DuaDetail operations: dua_global_id is Int!
    fun getDuaDetailsByGlobalId(globalId: Int): Flow<List<DuaDetail>> =
        duaDetailDao.getDuaDetailsByGlobalId(globalId)

    suspend fun getDuaDetailById(id: Int): DuaDetail? =
        duaDetailDao.getDuaDetailById(id)

    fun searchDuaDetails(query: String): Flow<List<DuaDetail>> =
        duaDetailDao.searchDuaDetails(query)

    // Combined operations
    suspend fun getDuaWithDetails(globalId: Int): Pair<DuaName?, List<DuaDetail>> {
        // In this context, globalId is likely a chapter name or identifier, but your schema
        // links dua_details by dua_global_id (Int). If you want to fetch DuaName by chap_id, use getDuaNameByChapId
        // If you want to fetch DuaName by chapname, use getDuaNameByGlobalId
        val details = duaDetailDao.getDuaDetailsByGlobalId(globalId).first()
        // If you want to fetch the DuaName by chap_id == globalId
        val duaName = duaNameDao.getDuaNameByGlobalId(globalId.toString())
        return Pair(duaName, details)
    }
}