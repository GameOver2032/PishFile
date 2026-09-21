package ir.pishfile.app.data.sync

import ir.pishfile.app.data.local.PishFileDatabase

class SyncRepository(
    private val database: PishFileDatabase
) {
    suspend fun getPendingSummary(): PendingSyncSummary {
        val projects = database.projectDao().getAll().size
        val units = database.unitDao().getAll().size
        val preFiles = database.preFileDao().getAll().size
        val followUps = database.followUpDao().getAll().size
        return PendingSyncSummary(
            projects = projects,
            units = units,
            preFiles = preFiles,
            followUps = followUps,
        )
    }
}
