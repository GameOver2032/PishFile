package ir.pishfile.app.data.repository

import ir.pishfile.app.data.local.dao.StatusCount
import ir.pishfile.app.data.local.dao.UnitDao
import ir.pishfile.app.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

class UnitRepository(private val dao: UnitDao) {

    fun observeAll(): Flow<List<UnitEntity>> = dao.observeAll()

    fun observeByProject(projectId: String): Flow<List<UnitEntity>> = dao.observeByProject(projectId)

    fun search(query: String): Flow<List<UnitEntity>> = dao.search(query.trim())

    fun observeById(id: String): Flow<UnitEntity?> = dao.observeById(id)

    fun observeCount(): Flow<Int> = dao.observeCount()

    fun observeCountByProject(projectId: String): Flow<Int> = dao.observeCountByProject(projectId)

    fun observeCountByStatus(status: String): Flow<Int> = dao.observeCountByStatus(status)

    fun observeLatestAvailable(): Flow<UnitEntity?> = dao.observeLatestAvailable()

    fun observeStatusCounts(projectId: String? = null): Flow<List<StatusCount>> = dao.observeStatusCounts(projectId)

    fun findAvailable(minArea: Double?, maxArea: Double?, maxPrice: Long?): Flow<List<UnitEntity>> =
        dao.findAvailable(minArea, maxArea, maxPrice)

    fun observeAveragePrice(projectId: String): Flow<Double?> = dao.observeAveragePrice(projectId)

    fun observeTotalArea(projectId: String): Flow<Double?> = dao.observeTotalArea(projectId)

    suspend fun getById(id: String): UnitEntity? = dao.getById(id)

    suspend fun getByProject(projectId: String): List<UnitEntity> = dao.getByProject(projectId)

    suspend fun save(unit: UnitEntity) {
        val existing = dao.getById(unit.id)
        if (existing == null) {
            dao.insert(unit)
        } else {
            dao.update(unit.copy(syncState = "PENDING_UPLOAD", updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun saveAll(units: List<UnitEntity>) = dao.insertAll(units)

    suspend fun updateStatus(id: String, status: String) = dao.updateStatus(id, status)

    suspend fun delete(id: String) = dao.softDelete(id)

    /**
     * ساخت گروهی واحدها — مثلاً «۱۰ واحد در طبقات ۱ تا ۵».
     * یکی از پرکاربردترین کارها: کاربر می‌خواهد سریع یک بلوک کامل بسازد.
     */
    suspend fun createBatch(
        projectId: String,
        block: String?,
        fromFloor: Int,
        toFloor: Int,
        unitsPerFloor: Int,
        baseArea: Double,
        areaStepPerFloor: Double,
        pricePerMeter: Long?,
        unitType: String,
        bedrooms: Int?,
        facilities: String?,
        deliveryDate: String?,
    ): Int {
        val list = mutableListOf<UnitEntity>()
        var counter = 0
        for (floor in fromFloor..toFloor) {
            for (index in 1..unitsPerFloor) {
                counter++
                val area = baseArea + areaStepPerFloor * (floor - fromFloor)
                val number = if (unitsPerFloor >= 10) "${floor}${index.toString().padStart(2, '0')}"
                else "${floor}0$index"
                list += UnitEntity(
                    projectId = projectId,
                    block = block,
                    unitNumber = number,
                    floor = floor,
                    unitType = unitType,
                    bedrooms = bedrooms,
                    grossArea = area,
                    netArea = area - 12,
                    pricePerMeter = pricePerMeter,
                    totalPrice = pricePerMeter?.let { (area * it).toLong() },
                    facilities = facilities,
                    deliveryDate = deliveryDate,
                    status = "AVAILABLE",
                )
            }
        }
        dao.insertAll(list)
        return counter
    }
}
