package ir.pishfile.app.ui.screens.prefiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FilterChipsRow
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.MoneyField
import ir.pishfile.app.ui.components.NumberField
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.screens.dashboard.preFileStatusColor
import ir.pishfile.app.ui.viewmodel.PreFileDetailViewModel
import ir.pishfile.app.ui.viewmodel.PreFileEditViewModel
import ir.pishfile.app.ui.viewmodel.PreFileListViewModel

/**
 * فهرست فایل‌های پیش‌فروش
 */
@Composable
fun PreFileListScreen(
    onOpen: (String) -> Unit,
    viewModel: PreFileListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<PreFileEntity?>(null) }

    val preFiles by viewModel.preFiles.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        SearchField(
            query = query,
            onQueryChange = {
                query = it
                viewModel.setQuery(it)
            },
            placeholder = "جست‌وجوی پیش‌فروش: شماره فایل، مالک، پروژه، واحد…",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        FilterChipsRow(
            options = Constants.preFileStatuses.map { it to Constants.preFileStatusLabel(it) },
            selectedKey = statusFilter,
            onSelect = {
                statusFilter = it
                viewModel.setStatusFilter(it)
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        if (preFiles.isEmpty()) {
            EmptyState(
                title = "فایل پیش‌فروشی ثبت نشده",
                subtitle = "با دکمه + یک فایل پیش‌فروش جدید اضافه کنید",
                icon = { Icon(Icons.Filled.Description, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(preFiles, key = { it.preFile.id }) { row ->
                    PreFileCard(
                        row = row,
                        onClick = { onOpen(row.preFile.id) },
                        onDelete = { pendingDelete = row.preFile },
                    )
                }
            }
        }
    }

    pendingDelete?.let { preFile ->
        ConfirmDialog(
            title = "حذف فایل پیش‌فروش",
            message = "فایل ${preFile.draftNumber} حذف شود؟",
            onConfirm = { viewModel.delete(preFile.id) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun PreFileCard(
    row: ir.pishfile.app.data.local.dao.PreFileRow,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val preFile = row.preFile
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${preFile.draftNumber} • ${row.projectName ?: "پروژه"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listOfNotNull(
                            row.unitTitle,
                            preFile.ownerName?.let { "مالک: $it" },
                            Formatters.toPersianDigits(preFile.draftDate)
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(Constants.preFileStatusLabel(preFile.status), preFileStatusColor(preFile.status))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }

            SpacerH(6)

            // نمایش بر اساس مدل قیمت‌گذاری
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                when (preFile.pricingModel) {
                    Constants.PRICING_DEPOSIT_BONUS -> {
                        Text(
                            "واریزی: ${Formatters.amountShort(preFile.depositAmount)} | امتیاز: ${Formatters.amountShort(preFile.bonusAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            "مجموع: ${Formatters.amountShort(preFile.computedTotal)} تومان",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Constants.PRICING_SHARE -> {
                        Text(
                            "${Formatters.number(preFile.shareCount)} سهم (${Formatters.number(preFile.shareMeterArea?.toInt())} متری)",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            "کل: ${Formatters.amountShort(preFile.computedTotal)} تومان",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    else -> { // METER
                        Text(
                            "متراژ: ${Formatters.number(preFile.meterArea?.toInt())} م² • متری ${Formatters.amountShort(preFile.pricePerMeter)}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            "قیمت: ${Formatters.amountShort(preFile.displayPrice)} تومان",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            SpacerH(4)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "شرایط: ${preFile.saleConditionsSummary}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (preFile.hasRanking && !preFile.ranking.isNullOrBlank()) {
                    Text(
                        "رتبه: ${preFile.ranking}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

/**
 * فرم ثبت / ویرایش فایل پیش‌فروش
 */
@Composable
fun PreFileEditScreen(
    preFileId: String?,
    initialProjectId: String,
    initialUnitId: String,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: PreFileEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    if (preFileId == null) {
        viewModel.startNew(initialProjectId, initialUnitId)
    } else {
        viewModel.load(preFileId)
    }

    val form = viewModel.form
    val projects = viewModel.projects
    val units = viewModel.availableUnits

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // --- ۱) پروژه و واحد ---
        item {
            SectionCard(
                title = "پروژه و واحد",
                subtitle = "با انتخاب پروژه، شرایط اختصاصی آن به‌صورت خودکار پر می‌شود",
            ) {
                DropdownField(
                    label = "پروژه *",
                    options = projects.map { it.name },
                    selected = projects.firstOrNull { it.id == form.projectId }?.name,
                    onSelect = { name ->
                        projects.firstOrNull { it.name == name }?.let { viewModel.selectProject(it.id) }
                    },
                )
                SpacerH(8)
                DropdownField(
                    label = "واحد",
                    options = units.map { it.displayTitle },
                    selected = units.firstOrNull { it.id == form.unitId }?.displayTitle,
                    onSelect = { title ->
                        units.firstOrNull { it.displayTitle == title }?.let { viewModel.selectUnit(it.id) }
                    },
                    emptyLabel = "بدون واحد مشخص",
                    allowEmpty = true,
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.draftNumber,
                        onValueChange = { v -> viewModel.update { it.copy(draftNumber = v) } },
                        label = "شماره فایل",
                        modifier = Modifier.weight(1f),
                    )
                    JalaliDateField(
                        value = form.draftDate,
                        onValueChange = { v -> viewModel.update { it.copy(draftDate = v) } },
                        label = "تاریخ ثبت",
                        quickMonths = emptyList(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // --- ۲) سپارنده / مالک فایل ---
        item {
            SectionCard(title = "مشخصات مالک / سپارنده فایل") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.ownerName,
                        onValueChange = { v -> viewModel.update { it.copy(ownerName = v) } },
                        label = "نام مالک / سپارنده",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.ownerPhone,
                        onValueChange = { v -> viewModel.update { it.copy(ownerPhone = v) } },
                        label = "شماره تماس",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // --- ۳) مدل قیمت‌گذاری و ارقام مالی ---
        item {
            SectionCard(
                title = "مدل قیمت‌گذاری و قیمت فایل",
                subtitle = "یکی از ۳ مدل زیر را انتخاب کنید:",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = form.pricingModel == Constants.PRICING_DEPOSIT_BONUS,
                        onClick = { viewModel.update { it.copy(pricingModel = Constants.PRICING_DEPOSIT_BONUS) } },
                        label = { Text("واریزی و امتیاز") },
                    )
                    FilterChip(
                        selected = form.pricingModel == Constants.PRICING_METER,
                        onClick = { viewModel.update { it.copy(pricingModel = Constants.PRICING_METER) } },
                        label = { Text("قیمت متری") },
                    )
                    FilterChip(
                        selected = form.pricingModel == Constants.PRICING_SHARE,
                        onClick = { viewModel.update { it.copy(pricingModel = Constants.PRICING_SHARE) } },
                        label = { Text("سهامی") },
                    )
                }

                SpacerH(10)

                when (form.pricingModel) {
                    Constants.PRICING_DEPOSIT_BONUS -> {
                        MoneyField(
                            value = form.depositAmount,
                            onValueChange = { v -> viewModel.update { it.copy(depositAmount = v) } },
                            label = "مبلغ واریزی پروژه تا امروز",
                        )
                        SpacerH(8)
                        MoneyField(
                            value = form.bonusAmount,
                            onValueChange = { v -> viewModel.update { it.copy(bonusAmount = v) } },
                            label = "مبلغ امتیاز پروژه (سود پروژه)",
                        )
                        SpacerH(8)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "مجموع پرداختی خریدار (واریزی + امتیاز):",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    "${Formatters.amountWithUnit(form.computedTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    Constants.PRICING_SHARE -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberField(
                                value = form.shareMeterArea,
                                onValueChange = { v -> viewModel.update { it.copy(shareMeterArea = v) } },
                                label = "متراژ هر سهم",
                                suffix = "م²",
                                decimal = true,
                                modifier = Modifier.weight(1f),
                            )
                            NumberField(
                                value = form.shareCount,
                                onValueChange = { v -> viewModel.update { it.copy(shareCount = v) } },
                                label = "تعداد سهم",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        SpacerH(8)
                        MoneyField(
                            value = form.sharePrice,
                            onValueChange = { v -> viewModel.update { it.copy(sharePrice = v) } },
                            label = "قیمت هر سهم",
                        )
                        SpacerH(8)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "مبلغ کل سهام:",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    "${Formatters.amountWithUnit(form.computedTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    else -> { // METER
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberField(
                                value = form.meterArea,
                                onValueChange = { v -> viewModel.update { it.copy(meterArea = v) } },
                                label = "متراژ واحد",
                                suffix = "م²",
                                decimal = true,
                                modifier = Modifier.weight(1f),
                            )
                            MoneyField(
                                value = form.pricePerMeter,
                                onValueChange = { v -> viewModel.update { it.copy(pricePerMeter = v) } },
                                label = "قیمت هر متر مربع",
                                modifier = Modifier.weight(1.5f),
                            )
                        }
                        SpacerH(8)
                        MoneyField(
                            value = form.totalPrice.ifBlank { form.computedTotal.toString() },
                            onValueChange = { v -> viewModel.update { it.copy(totalPrice = v) } },
                            label = "مبلغ کل فایل",
                            helperText = "از حاصل‌ضرب متراژ در قیمت هر متر محاسبه می‌شود",
                        )
                    }
                }
            }
        }

        // --- ۴) رتبه‌بندی فایل ---
        item {
            SectionCard(title = "رتبه‌بندی فایل در پروژه") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = form.hasRanking,
                        onCheckedChange = { v -> viewModel.update { it.copy(hasRanking = v) } },
                    )
                    Text(" این فایل دارای رتبه در پروژه است", style = MaterialTheme.typography.bodySmall)
                }
                if (form.hasRanking) {
                    SpacerH(8)
                    FormTextField(
                        value = form.ranking,
                        onValueChange = { v -> viewModel.update { it.copy(ranking = v) } },
                        label = "رتبه فایل (مثلاً: رتبه ۱۲، اولویت الف)",
                    )
                }
            }
        }

        // --- ۵) شرایط فروش ---
        item {
            SectionCard(title = "شرایط فروش") {
                Text("نوع پرداخت و تسویه:", style = MaterialTheme.typography.bodySmall)
                SpacerH(4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = form.saleConditionCash,
                            onCheckedChange = { v -> viewModel.update { it.copy(saleConditionCash = v) } },
                        )
                        Text("نقد", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = form.saleConditionInstallment,
                            onCheckedChange = { v -> viewModel.update { it.copy(saleConditionInstallment = v) } },
                        )
                        Text("شرایطی", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = form.saleConditionExchange,
                            onCheckedChange = { v -> viewModel.update { it.copy(saleConditionExchange = v) } },
                        )
                        Text("تهاتر", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                SpacerH(8)
                FormTextField(
                    value = form.saleConditionNotes,
                    onValueChange = { v -> viewModel.update { it.copy(saleConditionNotes = v) } },
                    label = "توضیحات شرایط فروش (مثلاً نوع خودرو یا ملک جهت تهاتر)",
                    singleLine = false,
                    minLines = 2,
                )
            }
        }

        // --- ۶) اطلاعات اقساط پرونده ---
        item {
            SectionCard(
                title = "اقساط پروژه",
                subtitle = "مشخصات اقساط این فایل برای ارائه به خریدار",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.installmentCount,
                        onValueChange = { v -> viewModel.update { it.copy(installmentCount = v) } },
                        label = "تعداد اقساط",
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        value = form.remainingInstallmentsCount,
                        onValueChange = { v -> viewModel.update { it.copy(remainingInstallmentsCount = v) } },
                        label = "تعداد اقساط مانده",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoneyField(
                        value = form.installmentAmount,
                        onValueChange = { v -> viewModel.update { it.copy(installmentAmount = v) } },
                        label = "مبلغ هر قسط",
                        modifier = Modifier.weight(1.2f),
                    )
                    DropdownField(
                        label = "دوره پرداخت",
                        options = listOf("ماهانه", "دو ماهه", "سه ماهه / فصلی", "شش ماهه", "سالانه"),
                        selected = form.installmentPeriod,
                        onSelect = { v -> viewModel.update { it.copy(installmentPeriod = v) } },
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                JalaliDateField(
                    value = form.nextInstallmentDueDate,
                    onValueChange = { v -> viewModel.update { it.copy(nextInstallmentDueDate = v) } },
                    label = "تاریخ سررسید قسط پیش‌رو",
                    quickMonths = listOf(1, 2, 3),
                )
            }
        }

        // --- ۷) وضعیت و تحویل ---
        item {
            SectionCard(title = "وضعیت فایل و تحویل") {
                DropdownField(
                    label = "وضعیت پیش‌فروش",
                    options = Constants.preFileStatuses.map { Constants.preFileStatusLabel(it) },
                    selected = Constants.preFileStatusLabel(form.status),
                    onSelect = { label ->
                        val code = Constants.preFileStatuses.firstOrNull { Constants.preFileStatusLabel(it) == label } ?: form.status
                        viewModel.update { it.copy(status = code) }
                    },
                )
                SpacerH(8)
                JalaliDateField(
                    value = form.deliveryDate,
                    onValueChange = { v -> viewModel.update { it.copy(deliveryDate = v) } },
                    label = "تاریخ تقریبی تحویل واحد",
                    quickMonths = listOf(6, 12),
                )
                SpacerH(8)
                FormTextField(
                    value = form.notes,
                    onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                    label = "توضیحات و یادداشت تکمیلی",
                    singleLine = false,
                    minLines = 3,
                )
            }
        }

        item {
            Button(
                onClick = { viewModel.save { id -> onSaved(id) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (preFileId == null) "ثبت فایل پیش‌فروش" else "ذخیره تغییرات") }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
        }
    }
}

/**
 * نمایش کامل مشخصات فایل پیش‌فروش جهت ارائه به خریدار
 */
@Composable
fun PreFileDetailScreen(
    preFileId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenUnit: (String) -> Unit,
    viewModel: PreFileDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.setPreFileId(preFileId)

    val row by viewModel.row.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()

    var showDelete by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val currentRow = row ?: return
    val pf = currentRow.preFile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = "${pf.draftNumber} • ${currentRow.projectName ?: "پروژه"}",
                subtitle = currentRow.unitTitle ?: "بدون واحد مشخص",
                trailing = {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
                        IconButton(onClick = { showStatusMenu = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "تغییر وضعیت")
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                        DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                            Constants.preFileStatuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(Constants.preFileStatusLabel(status)) },
                                    onClick = {
                                        viewModel.changeStatus(status)
                                        showStatusMenu = false
                                    },
                                )
                            }
                        }
                    }
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(Constants.preFileStatusLabel(pf.status), preFileStatusColor(pf.status))
                    SpacerH(0)
                    Text(
                        "  تاریخ ثبت: ${Formatters.toPersianDigits(pf.draftDate)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SpacerH(8)
                InfoRow("مالک / سپارنده", pf.ownerName)
                InfoRow("شماره تماس", pf.ownerPhone)
                InfoRow("مدل پروژه", Constants.projectPricingModelLabel(pf.pricingModel))
                if (pf.hasRanking && !pf.ranking.isNullOrBlank()) {
                    InfoRow("رتبه در پروژه", pf.ranking, emphasize = true)
                }
            }
        }

        item {
            SectionCard(title = "اطلاعات مالی فایل") {
                when (pf.pricingModel) {
                    Constants.PRICING_DEPOSIT_BONUS -> {
                        InfoRow("مبلغ واریزی تا امروز", Formatters.amountWithUnit(pf.depositAmount))
                        InfoRow("مبلغ امتیاز (سود پروژه)", Formatters.amountWithUnit(pf.bonusAmount))
                        InfoRow("مجموع پرداختی خرید (واریزی + امتیاز)", Formatters.amountWithUnit(pf.computedTotal), emphasize = true)
                    }
                    Constants.PRICING_SHARE -> {
                        InfoRow("متراژ هر سهم", pf.shareMeterArea?.let { "${Formatters.number(it.toInt())} م²" })
                        InfoRow("تعداد سهم", Formatters.number(pf.shareCount))
                        InfoRow("قیمت هر سهم", Formatters.amountWithUnit(pf.sharePrice))
                        InfoRow("مبلغ کل سهام", Formatters.amountWithUnit(pf.computedTotal), emphasize = true)
                    }
                    else -> { // METER
                        InfoRow("متراژ", pf.meterArea?.let { "${Formatters.number(it.toInt())} م²" })
                        InfoRow("قیمت هر مترمربع", Formatters.amountWithUnit(pf.pricePerMeter))
                        InfoRow("مبلغ کل فایل", Formatters.amountWithUnit(pf.displayPrice), emphasize = true)
                    }
                }
            }
        }

        item {
            SectionCard(title = "شرایط فروش") {
                InfoRow("نوع پرداخت", pf.saleConditionsSummary, emphasize = true)
                InfoRow("توضیحات شرایط و تهاتر", pf.saleConditionNotes)
            }
        }

        item {
            SectionCard(title = "اطلاعات اقساط") {
                InfoRow("تعداد کل اقساط", Formatters.number(pf.installmentCount))
                InfoRow("تعداد اقساط مانده", Formatters.number(pf.remainingInstallmentsCount), emphasize = true)
                InfoRow("مبلغ هر قسط", Formatters.amountWithUnit(pf.installmentAmount))
                InfoRow("دوره پرداخت", pf.installmentPeriod)
                InfoRow("تاریخ سررسید قسط پیش‌رو", pf.nextInstallmentDueDate?.let { Formatters.toPersianDigits(it) }, emphasize = true)
            }
        }

        item {
            SectionCard(title = "تحویل و یادداشت‌ها") {
                InfoRow("تاریخ تقریبی تحویل", pf.deliveryDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("یادداشت", pf.notes)
            }
        }

        if (unit != null) {
            item {
                SectionCard(title = "مشخصات واحد مربوطه") {
                    unit?.let { u ->
                        InfoRow("عنوان واحد", u.displayTitle)
                        InfoRow("متراژ ناخالص", u.grossArea?.let { "${Formatters.number(it.toInt())} م²" })
                        InfoRow("جهت", u.direction)
                        SpacerH(6)
                        OutlinedButton(onClick = { onOpenUnit(u.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("مشاهده جزئیات کامل واحد")
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "حذف فایل پیش‌فروش",
            message = "فایل ${pf.draftNumber} حذف شود؟",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }
}
