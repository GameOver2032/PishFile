package ir.pishfile.app.data.sync

data class PendingSyncSummary(
    val projects: Int = 0,
    val units: Int = 0,
    val preFiles: Int = 0,
    val followUps: Int = 0,
) {
    val total: Int get() = projects + units + preFiles + followUps
}
