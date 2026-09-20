package ir.pishfile.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import ir.pishfile.app.data.local.PishFileDatabase
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.InstallmentEntity
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
 * پشتیبان‌گیری و بازیابی — چون داده‌ها روی گوشی است، بکاپ حیاتی است.
 *
 * قالب‌ها:
 *  - JSON: بکاپ کامل (همه‌ی جداول) — قابل بازیابی در برنامه
 *  - CSV : خروجی اکسل‌پسند از پیش‌فایل‌ها و اقساط
 */
class BackupManager(
    private val context: Context,
    private val database: PishFileDatabase,
) {

    // ---------------- پشتیبان‌گیری کامل (JSON) ----------------
    suspend fun exportFullBackup(): File {
        val root = JSONObject()
        root.put("app", "PishFile")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("projects", JSONArray(database.projectDao().getAll().map { projectToJson(it) }))
        root.put("units", JSONArray(database.unitDao().getAll().map { unitToJson(it) }))
        root.put("customers", JSONArray(database.customerDao().getAll().map { customerToJson(it) }))
        root.put("preFiles", JSONArray(database.preFileDao().getAll().map { preFileToJson(it) }))

        val installments = database.preFileDao().getAll().flatMap {
            database.installmentDao().getByPreFile(it.id)
        }
        root.put("installments", JSONArray(installments.map { installmentToJson(it) }))
        root.put("followUps", JSONArray(database.followUpDao().getAll().map { followUpToJson(it) }))

        val file = File(exportDir(), "pishfile-backup-${timestamp()}.json")
        file.writeText(root.toString(2), Charsets.UTF_8)
        return file
    }

    // ---------------- خروجی CSV ----------------
    suspend fun exportPreFilesCsv(): File {
        val sb = StringBuilder()
        sb.append('\uFEFF') // BOM تا اکسل فارسی را درست نشان دهد
        sb.append(
            listOf(
                "شماره پیش‌فایل", "تاریخ", "پروژه", "واحد", "مشتری", "موبایل",
                "مبلغ کل", "تخفیف", "مبلغ نهایی", "پیش‌پرداخت", "پرداختی", "مانده",
                "تعداد اقساط", "مبلغ قسط", "وضعیت", "توضیحات"
            ).joinToString(",") { "\"$it\"" }
        ).append('\n')

        val projects = database.projectDao().getAll().associateBy { it.id }
        val units = database.unitDao().getAll().associateBy { it.id }
        val customers = database.customerDao().getAll().associateBy { it.id }

        for (p in database.preFileDao().getAll()) {
            val project = projects[p.projectId]?.name.orEmpty()
            val unit = units[p.unitId]?.let { "واحد ${it.unitNumber}" }.orEmpty()
            val customer = customers[p.customerId]?.let { "${it.firstName} ${it.lastName}" }.orEmpty()
            val phone = customers[p.customerId]?.phonePrimary.orEmpty()

            sb.append(
                listOf(
                    p.draftNumber,
                    p.draftDate,
                    project,
                    unit,
                    customer,
                    phone,
                    p.totalPrice.toString(),
                    (p.discount ?: 0).toString(),
                    p.effectivePrice.toString(),
                    (p.prepayment ?: 0).toString(),
                    p.paidAmount.toString(),
                    p.dueAmount.toString(),
                    (p.installmentCount ?: 0).toString(),
                    (p.installmentAmount ?: 0).toString(),
                    Constants.preFileStatusLabel(p.status),
                    (p.notes ?: "").replace("\n", " "),
                ).joinToString(",") { "\"${it.replace("\"", "''")}\"" }
            ).append('\n')
        }

        val file = File(exportDir(), "pishfile-prefiles-${timestamp()}.csv")
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    suspend fun exportInstallmentsCsv(): File {
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.append(
            listOf(
                "پیش‌فایل", "قسط", "عنوان", "مبلغ", "سررسید", "وضعیت",
                "پرداختی", "تاریخ پرداخت", "روش پرداخت", "شماره پیگیری"
            ).joinToString(",") { "\"$it\"" }
        ).append('\n')

        for (p in database.preFileDao().getAll()) {
            for (i in database.installmentDao().getByPreFile(p.id)) {
                sb.append(
                    listOf(
                        p.draftNumber,
                        i.installmentNumber.toString(),
                        i.title ?: "قسط ${i.installmentNumber}",
                        i.amount.toString(),
                        i.dueDate,
                        Constants.installmentStatusLabel(i.status),
                        i.paidAmount.toString(),
                        i.paidDate.orEmpty(),
                        i.paymentMethod.orEmpty(),
                        i.referenceNumber.orEmpty(),
                    ).joinToString(",") { "\"$it\"" }
                ).append('\n')
            }
        }

        val file = File(exportDir(), "pishfile-installments-${timestamp()}.csv")
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    // ---------------- بازیابی (JSON) ----------------
    /** بازیابی از فایل بکاپ — رکوردهای موجود بازنویسی می‌شوند */
    suspend fun importFullBackup(json: String): ImportSummary {
        val root = JSONObject(json)
        var projects = 0
        var units = 0
        var customers = 0
        var preFiles = 0
        var installments = 0
        var followUps = 0

        // ترتیب مهم است: والدها قبل از فرزندان
        root.optJSONArray("projects")?.let { array ->
            for (i in 0 until array.length()) {
                database.projectDao().insert(jsonToProject(array.getJSONObject(i)))
                projects++
            }
        }
        root.optJSONArray("customers")?.let { array ->
            for (i in 0 until array.length()) {
                database.customerDao().insert(jsonToCustomer(array.getJSONObject(i)))
                customers++
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
        root.optJSONArray("installments")?.let { array ->
            for (i in 0 until array.length()) {
                database.installmentDao().insert(jsonToInstallment(array.getJSONObject(i)))
                installments++
            }
        }
        root.optJSONArray("followUps")?.let { array ->
            for (i in 0 until array.length()) {
                database.followUpDao().insert(jsonToFollowUp(array.getJSONObject(i)))
                followUps++
            }
        }

        return ImportSummary(projects, units, customers, preFiles, installments, followUps)
    }

    data class ImportSummary(
        val projects: Int,
        val units: Int,
        val customers: Int,
        val preFiles: Int,
        val installments: Int,
        val followUps: Int,
    ) {
        val total: Int get() = projects + units + customers + preFiles + installments + followUps
    }

    // ---------------- اشتراک‌گذاری فایل ----------------
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

    // ---------------- ابزارهای داخلی ----------------
    private fun exportDir(): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())

    // --- نگاشت به JSON ---
    private fun projectToJson(p: ProjectEntity) = JSONObject().apply {
        put("id", p.id); put("name", p.name); put("code", p.code); put("projectType", p.projectType)
        put("province", p.province); put("city", p.city); put("district", p.district)
        put("address", p.address); put("postalCode", p.postalCode)
        put("latitude", p.latitude); put("longitude", p.longitude)
        put("landArea", p.landArea); put("totalBuiltArea", p.totalBuiltArea)
        put("blockCount", p.blockCount); put("floorCount", p.floorCount)
        put("unitCount", p.unitCount); put("unitsPerFloor", p.unitsPerFloor)
        put("parkingCount", p.parkingCount); put("elevatorCount", p.elevatorCount)
        put("structureType", p.structureType); put("heatingSystem", p.heatingSystem)
        put("permitNumber", p.permitNumber); put("permitIssueDate", p.permitIssueDate)
        put("permitExpiryDate", p.permitExpiryDate); put("landDeedNumber", p.landDeedNumber)
        put("costPerMeter", p.costPerMeter); put("salePricePerMeter", p.salePricePerMeter)
        put("totalBudget", p.totalBudget); put("phase", p.phase)
        put("progressPercent", p.progressPercent); put("startDate", p.startDate)
        put("deliveryDate", p.deliveryDate); put("deliveryFrom", p.deliveryFrom)
        put("contractorName", p.contractorName); put("contractorPhone", p.contractorPhone)
        put("supervisorName", p.supervisorName); put("supervisorPhone", p.supervisorPhone)
        put("developerName", p.developerName); put("salesManagerPhone", p.salesManagerPhone)
        put("iban", p.iban); put("bankName", p.bankName)
        put("facilities", p.facilities); put("description", p.description); put("tags", p.tags)
        put("isFavorite", p.isFavorite); put("isArchived", p.isArchived)
        put("createdAt", p.createdAt); put("updatedAt", p.updatedAt)
    }

    private fun unitToJson(u: UnitEntity) = JSONObject().apply {
        put("id", u.id); put("projectId", u.projectId); put("block", u.block)
        put("unitNumber", u.unitNumber); put("floor", u.floor); put("unitType", u.unitType)
        put("bedrooms", u.bedrooms); put("bathrooms", u.bathrooms); put("kitchens", u.kitchens)
        put("grossArea", u.grossArea); put("netArea", u.netArea); put("balconyArea", u.balconyArea)
        put("commonAreaShare", u.commonAreaShare); put("ceilingHeight", u.ceilingHeight)
        put("direction", u.direction); put("lighting", u.lighting); put("view", u.view)
        put("positionType", u.positionType); put("facilities", u.facilities)
        put("parkingCount", u.parkingCount); put("parkingNumber", u.parkingNumber)
        put("storageCount", u.storageCount); put("storageNumber", u.storageNumber)
        put("pricePerMeter", u.pricePerMeter); put("totalPrice", u.totalPrice)
        put("finalPrice", u.finalPrice); put("extraCosts", u.extraCosts)
        put("discount", u.discount); put("vatAmount", u.vatAmount)
        put("prepaymentSuggestion", u.prepaymentSuggestion)
        put("suggestedInstallment", u.suggestedInstallment)
        put("suggestedInstallmentCount", u.suggestedInstallmentCount)
        put("costPrice", u.costPrice); put("status", u.status)
        put("deliveryDate", u.deliveryDate); put("deliveryStatus", u.deliveryStatus)
        put("technicalNotes", u.technicalNotes); put("description", u.description)
        put("createdAt", u.createdAt); put("updatedAt", u.updatedAt)
    }

    private fun customerToJson(c: CustomerEntity) = JSONObject().apply {
        put("id", c.id); put("firstName", c.firstName); put("lastName", c.lastName)
        put("fatherName", c.fatherName); put("title", c.title); put("companyName", c.companyName)
        put("nationalId", c.nationalId); put("idNumber", c.idNumber); put("birthDate", c.birthDate)
        put("registrationNumber", c.registrationNumber)
        put("phonePrimary", c.phonePrimary); put("phoneSecondary", c.phoneSecondary)
        put("whatsapp", c.whatsapp); put("email", c.email)
        put("province", c.province); put("city", c.city); put("address", c.address)
        put("postalCode", c.postalCode); put("job", c.job); put("workAddress", c.workAddress)
        put("workPhone", c.workPhone); put("iban", c.iban); put("bankName", c.bankName)
        put("status", c.status); put("source", c.source); put("referredBy", c.referredBy)
        put("creditScore", c.creditScore); put("creditLimit", c.creditLimit)
        put("hasBouncedCheque", c.hasBouncedCheque); put("isReturningCustomer", c.isReturningCustomer)
        put("totalPurchases", c.totalPurchases); put("entityType", c.entityType)
        put("economicCode", c.economicCode); put("notes", c.notes); put("tags", c.tags)
        put("isFavorite", c.isFavorite)
        put("createdAt", c.createdAt); put("updatedAt", c.updatedAt)
    }

    private fun preFileToJson(p: PreFileEntity) = JSONObject().apply {
        put("id", p.id); put("draftNumber", p.draftNumber); put("draftDate", p.draftDate)
        put("projectId", p.projectId); put("unitId", p.unitId); put("customerId", p.customerId)
        put("salesAgentName", p.salesAgentName); put("salesAgentPhone", p.salesAgentPhone)
        put("trackingCode", p.trackingCode)
        put("unitBlock", p.unitBlock); put("unitNumber", p.unitNumber); put("unitFloor", p.unitFloor)
        put("unitArea", p.unitArea)
        put("totalPrice", p.totalPrice); put("pricePerMeter", p.pricePerMeter)
        put("discount", p.discount); put("finalPrice", p.finalPrice)
        put("prepayment", p.prepayment); put("paidAmount", p.paidAmount)
        put("remainingAmount", p.remainingAmount); put("installmentCount", p.installmentCount)
        put("installmentAmount", p.installmentAmount); put("installmentPeriod", p.installmentPeriod)
        put("installmentStartDate", p.installmentStartDate); put("paymentType", p.paymentType)
        put("sellerCommitment", p.sellerCommitment); put("buyerCommitment", p.buyerCommitment)
        put("penaltyClause", p.penaltyClause); put("cancellationTerms", p.cancellationTerms)
        put("deedDate", p.deedDate); put("deedOffice", p.deedOffice)
        put("deliveryDate", p.deliveryDate); put("isUnitMortgaged", p.isUnitMortgaged)
        put("guaranteeType", p.guaranteeType); put("chequeCount", p.chequeCount)
        put("chequeAmount", p.chequeAmount); put("status", p.status)
        put("confirmedDate", p.confirmedDate); put("cancelDate", p.cancelDate)
        put("cancelReason", p.cancelReason); put("cancelPenaltyAmount", p.cancelPenaltyAmount)
        put("exchangeDetails", p.exchangeDetails); put("notes", p.notes)
        put("documentPaths", p.documentPaths); put("contractPhotoPath", p.contractPhotoPath)
        put("isFavorite", p.isFavorite)
        put("createdAt", p.createdAt); put("updatedAt", p.updatedAt)
    }

    private fun installmentToJson(i: InstallmentEntity) = JSONObject().apply {
        put("id", i.id); put("preFileId", i.preFileId)
        put("installmentNumber", i.installmentNumber); put("title", i.title)
        put("amount", i.amount); put("dueDate", i.dueDate); put("status", i.status)
        put("paidAmount", i.paidAmount); put("paidDate", i.paidDate)
        put("paymentMethod", i.paymentMethod); put("referenceNumber", i.referenceNumber)
        put("bankName", i.bankName); put("chequeOwner", i.chequeOwner)
        put("chequeDate", i.chequeDate); put("latePenalty", i.latePenalty)
        put("notes", i.notes); put("reminderEnabled", i.reminderEnabled)
        put("reminderDate", i.reminderDate)
        put("createdAt", i.createdAt); put("updatedAt", i.updatedAt)
    }

    private fun followUpToJson(f: FollowUpEntity) = JSONObject().apply {
        put("id", f.id); put("type", f.type); put("priority", f.priority)
        put("title", f.title); put("description", f.description); put("outcome", f.outcome)
        put("result", f.result); put("customerId", f.customerId); put("preFileId", f.preFileId)
        put("projectId", f.projectId); put("dueDate", f.dueDate); put("dueTime", f.dueTime)
        put("durationMinutes", f.durationMinutes); put("status", f.status)
        put("completedDate", f.completedDate); put("assignee", f.assignee)
        put("contactPhone", f.contactPhone); put("remindDaysBefore", f.remindDaysBefore)
        put("createdAt", f.createdAt); put("updatedAt", f.updatedAt)
    }

    // --- نگاشت از JSON ---
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
        province = o.text("province"),
        city = o.text("city"),
        district = o.text("district"),
        address = o.text("address"),
        postalCode = o.text("postalCode"),
        latitude = o.double("latitude"),
        longitude = o.double("longitude"),
        landArea = o.double("landArea"),
        totalBuiltArea = o.double("totalBuiltArea"),
        blockCount = o.int("blockCount"),
        floorCount = o.int("floorCount"),
        unitCount = o.int("unitCount"),
        unitsPerFloor = o.int("unitsPerFloor"),
        parkingCount = o.int("parkingCount"),
        elevatorCount = o.int("elevatorCount"),
        structureType = o.text("structureType"),
        heatingSystem = o.text("heatingSystem"),
        permitNumber = o.text("permitNumber"),
        permitIssueDate = o.text("permitIssueDate"),
        permitExpiryDate = o.text("permitExpiryDate"),
        landDeedNumber = o.text("landDeedNumber"),
        costPerMeter = o.long("costPerMeter"),
        salePricePerMeter = o.long("salePricePerMeter"),
        totalBudget = o.long("totalBudget"),
        phase = o.optString("phase", "PLANNING"),
        progressPercent = o.optInt("progressPercent", 0),
        startDate = o.text("startDate"),
        deliveryDate = o.text("deliveryDate"),
        deliveryFrom = o.text("deliveryFrom"),
        contractorName = o.text("contractorName"),
        contractorPhone = o.text("contractorPhone"),
        supervisorName = o.text("supervisorName"),
        supervisorPhone = o.text("supervisorPhone"),
        developerName = o.text("developerName"),
        salesManagerPhone = o.text("salesManagerPhone"),
        iban = o.text("iban"),
        bankName = o.text("bankName"),
        facilities = o.text("facilities"),
        description = o.text("description"),
        tags = o.text("tags"),
        isFavorite = o.optBoolean("isFavorite", false),
        isArchived = o.optBoolean("isArchived", false),
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
        bedrooms = o.int("bedrooms"),
        bathrooms = o.int("bathrooms"),
        kitchens = o.int("kitchens"),
        grossArea = o.double("grossArea"),
        netArea = o.double("netArea"),
        balconyArea = o.double("balconyArea"),
        commonAreaShare = o.double("commonAreaShare"),
        ceilingHeight = o.int("ceilingHeight"),
        direction = o.text("direction"),
        lighting = o.text("lighting"),
        view = o.text("view"),
        positionType = o.text("positionType"),
        facilities = o.text("facilities"),
        parkingCount = o.optInt("parkingCount", 0),
        parkingNumber = o.text("parkingNumber"),
        storageCount = o.optInt("storageCount", 0),
        storageNumber = o.text("storageNumber"),
        pricePerMeter = o.long("pricePerMeter"),
        totalPrice = o.long("totalPrice"),
        finalPrice = o.long("finalPrice"),
        extraCosts = o.long("extraCosts"),
        discount = o.long("discount"),
        vatAmount = o.long("vatAmount"),
        prepaymentSuggestion = o.long("prepaymentSuggestion"),
        suggestedInstallment = o.long("suggestedInstallment"),
        suggestedInstallmentCount = o.int("suggestedInstallmentCount"),
        costPrice = o.long("costPrice"),
        status = o.optString("status", "AVAILABLE"),
        deliveryDate = o.text("deliveryDate"),
        deliveryStatus = o.text("deliveryStatus"),
        technicalNotes = o.text("technicalNotes"),
        description = o.text("description"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )

    private fun jsonToCustomer(o: JSONObject) = CustomerEntity(
        id = o.getString("id"),
        firstName = o.optString("firstName", ""),
        lastName = o.optString("lastName", ""),
        fatherName = o.text("fatherName"),
        title = o.text("title"),
        companyName = o.text("companyName"),
        nationalId = o.text("nationalId"),
        idNumber = o.text("idNumber"),
        birthDate = o.text("birthDate"),
        registrationNumber = o.text("registrationNumber"),
        phonePrimary = o.text("phonePrimary"),
        phoneSecondary = o.text("phoneSecondary"),
        whatsapp = o.text("whatsapp"),
        email = o.text("email"),
        province = o.text("province"),
        city = o.text("city"),
        address = o.text("address"),
        postalCode = o.text("postalCode"),
        job = o.text("job"),
        workAddress = o.text("workAddress"),
        workPhone = o.text("workPhone"),
        iban = o.text("iban"),
        bankName = o.text("bankName"),
        status = o.optString("status", "LEAD"),
        source = o.text("source"),
        referredBy = o.text("referredBy"),
        creditScore = o.int("creditScore"),
        creditLimit = o.long("creditLimit"),
        hasBouncedCheque = o.optBoolean("hasBouncedCheque", false),
        isReturningCustomer = o.optBoolean("isReturningCustomer", false),
        totalPurchases = o.long("totalPurchases"),
        entityType = o.optString("entityType", "INDIVIDUAL"),
        economicCode = o.text("economicCode"),
        notes = o.text("notes"),
        tags = o.text("tags"),
        isFavorite = o.optBoolean("isFavorite", false),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )

    private fun jsonToPreFile(o: JSONObject) = PreFileEntity(
        id = o.getString("id"),
        draftNumber = o.optString("draftNumber", ""),
        draftDate = o.optString("draftDate", Formatters.todayJalali()),
        projectId = o.getString("projectId"),
        unitId = o.text("unitId"),
        customerId = o.text("customerId"),
        salesAgentName = o.text("salesAgentName"),
        salesAgentPhone = o.text("salesAgentPhone"),
        trackingCode = o.text("trackingCode"),
        unitBlock = o.text("unitBlock"),
        unitNumber = o.text("unitNumber"),
        unitFloor = o.int("unitFloor"),
        unitArea = o.double("unitArea"),
        totalPrice = o.optLong("totalPrice", 0L),
        pricePerMeter = o.long("pricePerMeter"),
        discount = o.long("discount"),
        finalPrice = o.long("finalPrice"),
        prepayment = o.long("prepayment"),
        paidAmount = o.optLong("paidAmount", 0L),
        remainingAmount = o.long("remainingAmount"),
        installmentCount = o.int("installmentCount"),
        installmentAmount = o.long("installmentAmount"),
        installmentPeriod = o.text("installmentPeriod"),
        installmentStartDate = o.text("installmentStartDate"),
        paymentType = o.optString("paymentType", "INSTALLMENT"),
        sellerCommitment = o.text("sellerCommitment"),
        buyerCommitment = o.text("buyerCommitment"),
        penaltyClause = o.text("penaltyClause"),
        cancellationTerms = o.text("cancellationTerms"),
        deedDate = o.text("deedDate"),
        deedOffice = o.text("deedOffice"),
        deliveryDate = o.text("deliveryDate"),
        isUnitMortgaged = o.optBoolean("isUnitMortgaged", false),
        guaranteeType = o.text("guaranteeType"),
        chequeCount = o.int("chequeCount"),
        chequeAmount = o.long("chequeAmount"),
        status = o.optString("status", "DRAFT"),
        confirmedDate = o.text("confirmedDate"),
        cancelDate = o.text("cancelDate"),
        cancelReason = o.text("cancelReason"),
        cancelPenaltyAmount = o.long("cancelPenaltyAmount"),
        exchangeDetails = o.text("exchangeDetails"),
        notes = o.text("notes"),
        documentPaths = o.text("documentPaths"),
        contractPhotoPath = o.text("contractPhotoPath"),
        isFavorite = o.optBoolean("isFavorite", false),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )

    private fun jsonToInstallment(o: JSONObject) = InstallmentEntity(
        id = o.getString("id"),
        preFileId = o.getString("preFileId"),
        installmentNumber = o.optInt("installmentNumber", 1),
        title = o.text("title"),
        amount = o.optLong("amount", 0L),
        dueDate = o.optString("dueDate", Formatters.todayJalali()),
        status = o.optString("status", "UNPAID"),
        paidAmount = o.optLong("paidAmount", 0L),
        paidDate = o.text("paidDate"),
        paymentMethod = o.text("paymentMethod"),
        referenceNumber = o.text("referenceNumber"),
        bankName = o.text("bankName"),
        chequeOwner = o.text("chequeOwner"),
        chequeDate = o.text("chequeDate"),
        latePenalty = o.long("latePenalty"),
        notes = o.text("notes"),
        reminderEnabled = o.optBoolean("reminderEnabled", true),
        reminderDate = o.text("reminderDate"),
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
        result = o.text("result"),
        customerId = o.text("customerId"),
        preFileId = o.text("preFileId"),
        projectId = o.text("projectId"),
        dueDate = o.text("dueDate"),
        dueTime = o.text("dueTime"),
        durationMinutes = o.int("durationMinutes"),
        status = o.optString("status", "PENDING"),
        completedDate = o.text("completedDate"),
        assignee = o.text("assignee"),
        contactPhone = o.text("contactPhone"),
        remindDaysBefore = o.optInt("remindDaysBefore", 0),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    )
}
