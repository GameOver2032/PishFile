package ir.pishfile.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import ir.pishfile.app.data.local.PishFileDatabase
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * پشتیبان‌گیری و بازیابی داده‌های برنامه.
 */
class BackupManager(
    private val context: Context,
    private val database: PishFileDatabase,
) {

    // ---------------- پشتیبان‌گیری کامل (JSON) ----------------
    suspend fun exportFullBackup(): File {
        val root = JSONObject()
        root.put("app", "PishFile")
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("projects", JSONArray(database.projectDao().getAll().map { projectToJson(it) }))
        root.put("units", JSONArray(database.unitDao().getAll().map { unitToJson(it) }))
        root.put("preFiles", JSONArray(database.preFileDao().getAll().map { preFileToJson(it) }))
        root.put("followUps", JSONArray(database.followUpDao().getAll().map { followUpToJson(it) }))

        val file = File(exportDir(), "pishfile-backup-${timestamp()}.json")
        file.writeText(root.toString(2), Charsets.UTF_8)
        return file
    }

    // ---------------- خروجی CSV پیش‌فروش‌ها ----------------
    suspend fun exportPreFilesCsv(): File {
        val sb = StringBuilder()
        sb.append('\uFEFF') // BOM جهت نمایش صحیح فارسی در اکسل
        sb.append(
            listOf(
                "شماره فایل", "تاریخ", "پروژه", "واحد", "مالک", "شماره تماس",
                "مدل قیمت", "مبلغ واریزی", "مبلغ امتیاز", "قیمت متری", "متراژ",
                "قیمت کل", "رتبه", "شرایط فروش", "تعداد اقساط", "اقساط مانده",
                "مبلغ قسط", "سررسید قسط پیش‌رو", "وضعیت", "توضیحات"
            ).joinToString(",") { "\"$it\"" }
        ).append('\n')

        val projects = database.projectDao().getAll().associateBy { it.id }
        val units = database.unitDao().getAll().associateBy { it.id }

        for (p in database.preFileDao().getAll()) {
            val project = projects[p.projectId]?.name.orEmpty()
            val unit = units[p.unitId]?.displayTitle.orEmpty()

            sb.append(
                listOf(
                    p.draftNumber,
                    p.draftDate,
                    project,
                    unit,
                    p.ownerName.orEmpty(),
                    p.ownerPhone.orEmpty(),
                    Constants.projectPricingModelLabel(p.pricingModel),
                    (p.depositAmount ?: 0).toString(),
                    (p.bonusAmount ?: 0).toString(),
                    (p.pricePerMeter ?: 0).toString(),
                    (p.meterArea ?: 0.0).toString(),
                    p.computedTotal.toString(),
                    if (p.hasRanking) p.ranking.orEmpty() else "",
                    p.saleConditionsSummary,
                    (p.installmentCount ?: 0).toString(),
                    (p.remainingInstallmentsCount ?: 0).toString(),
                    (p.installmentAmount ?: 0).toString(),
                    p.nextInstallmentDueDate.orEmpty(),
                    Constants.preFileStatusLabel(p.status),
                    (p.notes ?: "").replace("\n", " "),
                ).joinToString(",") { "\"${it.replace("\"", "''")}\"" }
            ).append('\n')
        }

        val file = File(exportDir(), "pishfile-prefiles-${timestamp()}.csv")
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    // ---------------- بازیابی (JSON) ----------------
    suspend fun importFullBackup(json: String): ImportSummary {
        val root = JSONObject(json)
        var projects = 0
        var units = 0
        var preFiles = 0
        var followUps = 0

        root.optJSONArray("projects")?.let { array ->
            for (i in 0 until array.length()) {
                database.projectDao().insert(jsonToProject(array.getJSONObject(i)))
                projects++
            }
        }
        root.optJSONArray("units")?.let { array ->
            for (i in 0 until array.length()) {
                database.unitDao().insert(jsonToUnit(array.getJSONObject(i)))
                units++
            }
        }
        root.optJSONArray("preFiles")?.let { array ->
            for (i in 0 until array.length()) {
                database.preFileDao().insert(jsonToPreFile(array.getJSONObject(i)))
                preFiles++
            }
        }
        root.optJSONArray("followUps")?.let { array ->
            for (i in 0 until array.length()) {
                database.followUpDao().insert(jsonToFollowUp(array.getJSONObject(i)))
                followUps++
            }
        }

        return ImportSummary(projects, units, preFiles, followUps)
    }

    data class ImportSummary(
        val projects: Int,
        val units: Int,
        val preFiles: Int,
        val followUps: Int,
    ) {
        val total: Int get() = projects + units + preFiles + followUps
    }

    fun shareFile(file: File, mimeType: String = "application/octet-stream"): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportDir(): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())

    private fun projectToJson(p: ProjectEntity) = JSONObject().apply {
        put("id", p.id); put("name", p.name); put("code", p.code); put("projectType", p.projectType)
        put("pricingModel", p.pricingModel)
        put("province", p.province); put("city", p.city); put("district", p.district)
        put("address", p.address)
        put("salePricePerMeter", p.salePricePerMeter); put("defaultDepositAmount", p.defaultDepositAmount)
        put("shareMeterArea", p.shareMeterArea); put("sharePrice", p.sharePrice)
        put("phase", p.phase); put("progressPercent", p.progressPercent)
        put("deliveryDate", p.deliveryDate); put("facilities", p.facilities); put("description", p.description)
        put("createdAt", p.createdAt); put("updatedAt", p.updatedAt)
    }

    private fun unitToJson(u: UnitEntity) = JSONObject().apply {
        put("id", u.id); put("projectId", u.projectId); put("block", u.block)
        put("unitNumber", u.unitNumber); put("floor", u.floor); put("unitType", u.unitType)
        put("grossArea", u.grossArea); put("direction", u.direction)
        put("pricePerMeter", u.pricePerMeter); put("totalPrice", u.totalPrice); put("finalPrice", u.finalPrice)
        put("status", u.status); put("deliveryDate", u.deliveryDate)
        put("createdAt", u.createdAt); put("updatedAt", u.updatedAt)
    }

    private fun preFileToJson(p: PreFileEntity) = JSONObject().apply {
        put("id", p.id); put("draftNumber", p.draftNumber); put("draftDate", p.draftDate)
        put("projectId", p.projectId); put("unitId", p.unitId)
        put("ownerName", p.ownerName); put("ownerPhone", p.ownerPhone)
        put("pricingModel", p.pricingModel)
        put("depositAmount", p.depositAmount); put("bonusAmount", p.bonusAmount)
        put("pricePerMeter", p.pricePerMeter); put("meterArea", p.meterArea)
        put("shareMeterArea", p.shareMeterArea); put("shareCount", p.shareCount); put("sharePrice", p.sharePrice)
        put("totalPrice", p.totalPrice)
        put("hasRanking", p.hasRanking); put("ranking", p.ranking)
        put("saleConditionCash", p.saleConditionCash); put("saleConditionInstallment", p.saleConditionInstallment)
        put("saleConditionExchange", p.saleConditionExchange); put("saleConditionNotes", p.saleConditionNotes)
        put("installmentCount", p.installmentCount); put("remainingInstallmentsCount", p.remainingInstallmentsCount)
        put("installmentAmount", p.installmentAmount); put("installmentPeriod", p.installmentPeriod)
        put("nextInstallmentDueDate", p.nextInstallmentDueDate)
        put("status", p.status); put("deliveryDate", p.deliveryDate); put("notes", p.notes)
        put("createdAt", p.createdAt); put("updatedAt", p.updatedAt)
    }

    private fun followUpToJson(f: FollowUpEntity) = JSONObject().apply {
        put("id", f.id); put("type", f.type); put("priority", f.priority)
        put("title", f.title); put("description", f.description); put("outcome", f.outcome)
        put("preFileId", f.preFileId); put("projectId", f.projectId)
        put("dueDate", f.dueDate); put("dueTime", f.dueTime); put("status", f.status)
        put("contactPhone", f.contactPhone)
        put("createdAt", f.createdAt); put("updatedAt", f.updatedAt)
    }

    private fun JSONObject.text(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() && it != "null" }
    private fun JSONObject.long(key: String): Long? = if (isNull(key)) null else optLong(key)
    private fun JSONObject.int(key: String): Int? = if (isNull(key)) null else optInt(key)
    private fun JSONObject.double(key: String): Double? = if (isNull(key)) null else optDouble(key)

    private fun jsonToProject(o: JSONObject) = ProjectEntity(
        id = o.getString("id"),
        name = o.getString("name"),
        code = o.text("code"),
        projectType = o.text("projectType"),
        pricingModel = o.optString("pricingModel", "METER"),
        province = o.text("province"),
        city = o.text("city"),
        district = o.text("district"),
        address = o.text("address"),
        salePricePerMeter = o.long("salePricePerMeter"),
        defaultDepositAmount = o.long("defaultDepositAmount"),
        shareMeterArea = o.double("shareMeterArea"),
        sharePrice = o.long("sharePrice"),
        phase = o.optString("phase", "PLANNING"),
        progressPercent = o.optInt("progressPercent", 0),
        deliveryDate = o.text("deliveryDate"),
        facilities = o.text("facilities"),
        description = o.text("description"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )

    private fun jsonToUnit(o: JSONObject) = UnitEntity(
        id = o.getString("id"),
        projectId = o.getString("projectId"),
        block = o.text("block"),
        unitNumber = o.optString("unitNumber", ""),
        floor = o.int("floor"),
        unitType = o.optString("unitType", "APARTMENT"),
        grossArea = o.double("grossArea"),
        direction = o.text("direction"),
        pricePerMeter = o.long("pricePerMeter"),
        totalPrice = o.long("totalPrice"),
        finalPrice = o.long("finalPrice"),
        status = o.optString("status", "AVAILABLE"),
        deliveryDate = o.text("deliveryDate"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )

    private fun jsonToPreFile(o: JSONObject) = PreFileEntity(
        id = o.getString("id"),
        draftNumber = o.optString("draftNumber", ""),
        draftDate = o.optString("draftDate", Formatters.todayJalali()),
        projectId = o.getString("projectId"),
        unitId = o.text("unitId"),
        ownerName = o.text("ownerName"),
        ownerPhone = o.text("ownerPhone"),
        pricingModel = o.optString("pricingModel", "METER"),
        depositAmount = o.long("depositAmount"),
        bonusAmount = o.long("bonusAmount"),
        pricePerMeter = o.long("pricePerMeter"),
        meterArea = o.double("meterArea"),
        shareMeterArea = o.double("shareMeterArea"),
        shareCount = o.int("shareCount"),
        sharePrice = o.long("sharePrice"),
        totalPrice = o.optLong("totalPrice", 0L),
        hasRanking = o.optBoolean("hasRanking", false),
        ranking = o.text("ranking"),
        saleConditionCash = o.optBoolean("saleConditionCash", true),
        saleConditionInstallment = o.optBoolean("saleConditionInstallment", false),
        saleConditionExchange = o.optBoolean("saleConditionExchange", false),
        saleConditionNotes = o.text("saleConditionNotes"),
        installmentCount = o.int("installmentCount"),
        remainingInstallmentsCount = o.int("remainingInstallmentsCount"),
        installmentAmount = o.long("installmentAmount"),
        installmentPeriod = o.text("installmentPeriod"),
        nextInstallmentDueDate = o.text("nextInstallmentDueDate"),
        status = o.optString("status", "NORMAL"),
        deliveryDate = o.text("deliveryDate"),
        notes = o.text("notes"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )

    private fun jsonToFollowUp(o: JSONObject) = FollowUpEntity(
        id = o.getString("id"),
        type = o.optString("type", "CALL"),
        priority = o.optString("priority", "NORMAL"),
        title = o.optString("title", ""),
        description = o.text("description"),
        outcome = o.text("outcome"),
        preFileId = o.text("preFileId"),
        projectId = o.text("projectId"),
        dueDate = o.text("dueDate"),
        dueTime = o.text("dueTime"),
        status = o.optString("status", "PENDING"),
        contactPhone = o.text("contactPhone"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )
}
