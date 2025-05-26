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

    fun getDuaNamesByCategory(categoryId: Int): Flow<List<DuaName>> =
        duaNameDao.getDuaNamesByCategory(categoryId)

    suspend fun getDuaNameByGlobalId(globalId: String): DuaName? =
        duaNameDao.getDuaNameByGlobalId(globalId)

    fun searchDuaNames(query: String): Flow<List<DuaName>> =
        duaNameDao.searchDuaNames(query)

    fun getAllChapterNames(): Flow<List<String>> = duaNameDao.getAllChapterNames()

    // DuaDetail operations
    fun getDuaDetailsByGlobalId(globalId: String): Flow<List<DuaDetail>> =
        duaDetailDao.getDuaDetailsByGlobalId(globalId)

    suspend fun getDuaDetailById(id: Int): DuaDetail? =
        duaDetailDao.getDuaDetailById(id)

    fun searchDuaDetails(query: String): Flow<List<DuaDetail>> =
        duaDetailDao.searchDuaDetails(query)

    // Combined operations
    suspend fun getDuaWithDetails(globalId: String): Pair<DuaName?, List<DuaDetail>> {
        val duaName = duaNameDao.getDuaNameByGlobalId(globalId)
        val details = duaDetailDao.getDuaDetailsByGlobalId(globalId).first()
        return Pair(duaName, details)
    }
}