package ir.pishfile.app.data.repository

import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.dao.PreFileDao
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.dao.ProjectDao
import ir.pishfile.app.data.local.dao.UnitDao
import ir.pishfile.app.data.local.dao.FollowUpDao
import ir.pishfile.app.data.local.entity.PreFileEntity
import kotlinx.coroutines.flow.Flow

/**
 * مدیریت پرونده‌ها و فایل‌های پیش‌فروش.
 */
class PreFileRepository(
    private val preFileDao: PreFileDao,
    private val unitDao: UnitDao,
    private val projectDao: ProjectDao,
    private val followUpDao: FollowUpDao,
) {

    // ---------- خواندن ----------
    fun observeAllRows(): Flow<List<PreFileRow>> = preFileDao.observeAllRows()
    fun observeRowsByStatus(status: String): Flow<List<PreFileRow>> = preFileDao.observeRowsByStatus(status)
    fun observeRowsByProject(projectId: String): Flow<List<PreFileRow>> = preFileDao.observeRowsByProject(projectId)
    fun search(query: String): Flow<List<PreFileRow>> = preFileDao.search(query.trim())
    fun observeById(id: String): Flow<PreFileEntity?> = preFileDao.observeById(id)
    fun observeRowById(id: String): Flow<PreFileRow?> = preFileDao.observeRowById(id)
    fun observeCount(): Flow<Int> = preFileDao.observeCount()
    fun observeStatusCounts() = preFileDao.observeStatusCounts()
    fun observeLatest(): Flow<PreFileRow?> = preFileDao.observeLatest()

    suspend fun getById(id: String): PreFileEntity? = preFileDao.getById(id)
    suspend fun getAll(): List<PreFileEntity> = preFileDao.getAll()

    // ---------- شماره‌گذاری خودکار ----------
    suspend fun nextDraftNumber(): String {
        val year = Formatters.todayJalali().substringBefore('/')
        val count = preFileDao.countForYear(year)
        return "PF-$year-${(count + 1).toString().padStart(4, '0')}"
    }

    // ---------- ذخیره ----------
    suspend fun save(preFile: PreFileEntity) {
        val existing = preFileDao.getById(preFile.id)
        val computedPrice = preFile.computedTotal
        val toSave = preFile.copy(
            updatedAt = System.currentTimeMillis(),
            syncState = "PENDING_UPLOAD",
            totalPrice = if (preFile.totalPrice > 0) preFile.totalPrice else computedPrice,
        )

        if (existing == null) preFileDao.insert(toSave) else preFileDao.update(toSave)

        // به‌روزرسانی وضعیت واحد
        toSave.unitId?.let { unitId ->
            val newUnitStatus = when (toSave.status) {
                Constants.PREFILE_DRAFT -> Constants.UNIT_AVAILABLE
                Constants.PREFILE_URGENT, Constants.PREFILE_NORMAL -> Constants.UNIT_RESERVED
                Constants.PREFILE_WITHDRAWN -> Constants.UNIT_AVAILABLE
                else -> Constants.UNIT_AVAILABLE
            }
            unitDao.updateStatus(unitId, newUnitStatus)
        }
    }

    suspend fun delete(preFileId: String) {
        val preFile = preFileDao.getById(preFileId)
        preFileDao.softDelete(preFileId)
        preFile?.unitId?.let { unitDao.updateStatus(it, Constants.UNIT_AVAILABLE) }
    }
}
