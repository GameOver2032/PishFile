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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.pishfile.app.data.local.entity.InstallmentEntity
import ir.pishfile.app.data.local.entity.PreFileEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FilterChipsRow
import ir.pishfile.app.ui.components.FinanceCard
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.MoneyField
import ir.pishfile.app.ui.components.NumberField
import ir.pishfile.app.ui.components.PaymentProgress
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SoftDivider
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.screens.dashboard.installmentStatusColor
import ir.pishfile.app.ui.screens.dashboard.preFileStatusColor
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.PreFileDetailViewModel
import ir.pishfile.app.ui.viewmodel.PreFileEditViewModel
import ir.pishfile.app.ui.viewmodel.PreFileForm
import ir.pishfile.app.ui.viewmodel.PreFileListViewModel

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
            placeholder = "شماره پیش‌فایل، مشتری، پروژه، واحد…",
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
                title = "پیش‌فایلی ثبت نشده",
                subtitle = "با دکمه + یک پیش‌فایل جدید بسازید؛ شماره‌ی پیش‌فایل و جدول اقساط خودکار ساخته می‌شود",
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
            title = "حذف پیش‌فایل",
            message = "پیش‌فایل ${preFile.draftNumber} و اقساط آن حذف می‌شوند.",
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
                        "${preFile.draftNumber} • ${row.customerName ?: "بدون مشتری"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listOfNotNull(row.projectName, row.unitTitle, Formatters.toPersianDigits(preFile.draftDate))
                            .joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(Constants.preFileStatusLabel(preFile.status), preFileStatusColor(preFile.status))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            SpacerH(8)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "مبلغ کل: ${Formatters.amountShort(preFile.effectivePrice)} تومان",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    "مانده: ${Formatters.amountShort(preFile.dueAmount)} تومان",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (preFile.dueAmount > 0) StatusColors.reserved else StatusColors.paid,
                )
            }
            SpacerH(6)
            PaymentProgress(paid = preFile.paidAmount, total = preFile.effectivePrice)
        }
    }
}

@Composable
fun PreFileEditScreen(
    preFileId: String?,
    initialProjectId: String,
    initialUnitId: String,
    initialCustomerId: String,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: PreFileEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    if (preFileId == null) {
        viewModel.startNew(initialProjectId, initialUnitId, initialCustomerId)
    } else {
        viewModel.load(preFileId)
    }

    val form = viewModel.form
    val projects = viewModel.projects
    val customers = viewModel.customers
    val units = viewModel.availableUnits
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = "شناسه پیش‌فایل",
                subtitle = "شماره به‌صورت خودکار ساخته می‌شود",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = Formatters.toPersianDigits(form.draftNumber),
                        onValueChange = { v -> viewModel.update { it.copy(draftNumber = Formatters.toLatinDigits(v)) } },
                        label = "شماره پیش‌فایل",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.trackingCode,
                        onValueChange = { v -> viewModel.update { it.copy(trackingCode = v) } },
                        label = "کد رزرو",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                JalaliDateField(
                    value = form.draftDate,
                    onValueChange = { v -> viewModel.update { it.copy(draftDate = v) } },
                    label = "تاریخ تنظیم",
                    quickMonths = emptyList(),
                )
            }
        }

        item {
            SectionCard(title = "پروژه و واحد") {
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
                    options = units.map { "${it.displayTitle} — ${Formatters.amountShort(it.finalPrice ?: it.totalPrice)} تومان" },
                    selected = units.firstOrNull { it.id == form.unitId }
                        ?.let { "${it.displayTitle} — ${Formatters.amountShort(it.finalPrice ?: it.totalPrice)} تومان" },
                    onSelect = { label ->
                        units.firstOrNull {
                            "${it.displayTitle} — ${Formatters.amountShort(it.finalPrice ?: it.totalPrice)} تومان" == label
                        }?.let { viewModel.selectUnit(it.id) }
                    },
                    emptyLabel = "بدون واحد",
                    allowEmpty = true,
                )
                SpacerH(6)
                if (units.isEmpty()) {
                    Text(
                        "برای این پروژه واحدی ثبت نشده — ابتدا واحدها را بسازید",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        item {
            SectionCard(title = "مشتری") {
                DropdownField(
                    label = "مشتری *",
                    options = customers.map { "${it.firstName} ${it.lastName} — ${it.phonePrimary ?: ""}" },
                    selected = customers.firstOrNull { it.id == form.customerId }
                        ?.let { "${it.firstName} ${it.lastName} — ${it.phonePrimary ?: ""}" },
                    onSelect = { label ->
                        customers.firstOrNull {
                            "${it.firstName} ${it.lastName} — ${it.phonePrimary ?: ""}" == label
                        }?.let { viewModel.update { f -> f.copy(customerId = it.id) } }
                    },
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.salesAgentName,
                        onValueChange = { v -> viewModel.update { it.copy(salesAgentName = v) } },
                        label = "کارشناس فروش",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.salesAgentPhone,
                        onValueChange = { v -> viewModel.update { it.copy(salesAgentPhone = v) } },
                        label = "تلفن کارشناس",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            SectionCard(title = "شرایط مالی") {
                MoneyField(
                    value = form.pricePerMeter,
                    onValueChange = { v -> viewModel.update { it.copy(pricePerMeter = v) } },
                    label = "قیمت هر مترمربع",
                )
                SpacerH(8)
                MoneyField(
                    value = form.totalPrice,
                    onValueChange = { v -> viewModel.update { it.copy(totalPrice = v) } },
                    label = "مبلغ کل",
                )
                SpacerH(8)
                MoneyField(
                    value = form.discount,
                    onValueChange = { v -> viewModel.update { it.copy(discount = v) } },
                    label = "تخفیف",
                )
                SpacerH(8)
                MoneyField(
                    value = form.prepayment,
                    onValueChange = { v -> viewModel.update { it.copy(prepayment = v) } },
                    label = "پیش‌پرداخت",
                )
                SpacerH(10)
                FinanceCard(
                    title = "مبلغ نهایی پس از تخفیف",
                    amount = Formatters.amountWithUnit(form.finalPriceValue),
                    color = MaterialTheme.colorScheme.primary,
                )
                SpacerH(8)
                DropdownField(
                    label = "نوع پرداخت",
                    options = PreFileForm.paymentTypeOptions(),
                    selected = form.paymentType,
                    onSelect = { v -> viewModel.update { it.copy(paymentType = v) } },
                )
            }
        }

        item {
            SectionCard(title = "قسط‌بندی", subtitle = "با ذخیره، جدول اقساط به‌صورت خودکار ساخته می‌شود") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.installmentCount,
                        onValueChange = { v -> viewModel.update { it.copy(installmentCount = v) } },
                        label = "تعداد اقساط",
                        modifier = Modifier.weight(1f),
                    )
                    DropdownField(
                        label = "دوره پرداخت",
                        options = PreFileForm.installmentPeriods,
                        selected = form.installmentPeriod,
                        onSelect = { v -> viewModel.update { it.copy(installmentPeriod = v) } },
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                MoneyField(
                    value = form.installmentAmount.ifBlank { form.suggestedInstallment().toString() },
                    onValueChange = { v -> viewModel.update { it.copy(installmentAmount = v) } },
                    label = "مبلغ هر قسط",
                    helperText = "اگر خالی بماند: (مبلغ نهایی − پیش‌پرداخت) ÷ تعداد اقساط = ${Formatters.amountShort(form.suggestedInstallment())} تومان",
                )
                SpacerH(8)
                JalaliDateField(
                    value = form.installmentStartDate,
                    onValueChange = { v -> viewModel.update { it.copy(installmentStartDate = v) } },
                    label = "تاریخ شروع اقساط",
                )
                SpacerH(6)
                Text(
                    "مبلغ قابل قسط‌بندی: ${Formatters.amountWithUnit(form.remainingValue)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SpacerH(6)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(
                        checked = viewModel.autoGenerateInstallments,
                        onCheckedChange = { viewModel.setAutoGenerate(it) },
                    )
                    Text("ساخت خودکار جدول اقساط هنگام ذخیره", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            SectionCard(title = "تعهدات و شرایط") {
                FormTextField(
                    value = form.sellerCommitment,
                    onValueChange = { v -> viewModel.update { it.copy(sellerCommitment = v) } },
                    label = "تعهد فروشنده",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(8)
                FormTextField(
                    value = form.buyerCommitment,
                    onValueChange = { v -> viewModel.update { it.copy(buyerCommitment = v) } },
                    label = "تعهد خریدار",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(8)
                FormTextField(
                    value = form.penaltyClause,
                    onValueChange = { v -> viewModel.update { it.copy(penaltyClause = v) } },
                    label = "جریمه عدم انجام تعهد",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(8)
                FormTextField(
                    value = form.cancellationTerms,
                    onValueChange = { v -> viewModel.update { it.copy(cancellationTerms = v) } },
                    label = "شرایط فسخ / انصراف",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(8)
                DropdownField(
                    label = "نوع ضمانت",
                    options = listOf("بدون ضمانت", "چک", "سفته", "ضامن", "چک + ضامن", "تهاتر"),
                    selected = form.guaranteeType.takeIf { it.isNotBlank() },
                    onSelect = { v -> viewModel.update { it.copy(guaranteeType = v) } },
                    allowEmpty = true,
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = form.chequeCount,
                        onValueChange = { v -> viewModel.update { it.copy(chequeCount = v) } },
                        label = "تعداد چک",
                        modifier = Modifier.weight(1f),
                    )
                    MoneyField(
                        value = form.chequeAmount,
                        onValueChange = { v -> viewModel.update { it.copy(chequeAmount = v) } },
                        label = "مبلغ چک‌ها",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            SectionCard(title = "تنظیم سند و تحویل") {
                JalaliDateField(
                    value = form.deedDate,
                    onValueChange = { v -> viewModel.update { it.copy(deedDate = v) } },
                    label = "تاریخ تنظیم سند رسمی",
                    quickMonths = listOf(6, 12),
                )
                SpacerH(8)
                FormTextField(
                    value = form.deedOffice,
                    onValueChange = { v -> viewModel.update { it.copy(deedOffice = v) } },
                    label = "دفترخانه / محل تنظیم سند",
                )
                SpacerH(8)
                JalaliDateField(
                    value = form.deliveryDate,
                    onValueChange = { v -> viewModel.update { it.copy(deliveryDate = v) } },
                    label = "تاریخ تحویل واحد",
                    quickMonths = listOf(6, 12),
                )
                SpacerH(8)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(
                        checked = form.isUnitMortgaged,
                        onCheckedChange = { v -> viewModel.update { it.copy(isUnitMortgaged = v) } },
                    )
                    Text("واحد در رهن/بازداشت است", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            SectionCard(title = "وضعیت و توضیحات") {
                DropdownField(
                    label = "وضعیت پیش‌فایل",
                    options = Constants.preFileStatuses.map { Constants.preFileStatusLabel(it) },
                    selected = Constants.preFileStatusLabel(form.status),
                    onSelect = { label ->
                        val code = Constants.preFileStatuses.firstOrNull { Constants.preFileStatusLabel(it) == label } ?: form.status
                        viewModel.update { it.copy(status = code) }
                    },
                )
                SpacerH(8)
                FormTextField(
                    value = form.exchangeDetails,
                    onValueChange = { v -> viewModel.update { it.copy(exchangeDetails = v) } },
                    label = "جزئیات تهاتر / تسهیلات",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(8)
                FormTextField(
                    value = form.notes,
                    onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                    label = "یادداشت",
                    singleLine = false,
                    minLines = 3,
                )
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.save { id -> onSaved(id) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (preFileId == null) "ثبت پیش‌فایل" else "ذخیره تغییرات") }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
        }
    }
}

@Composable
fun PreFileDetailScreen(
    preFileId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenCustomer: (String) -> Unit,
    viewModel: PreFileDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.setPreFileId(preFileId)

    val row by viewModel.row.collectAsStateWithLifecycle()
    val installments by viewModel.installments.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val customer by viewModel.customer.collectAsStateWithLifecycle()

    var showDelete by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var payingInstallment by remember { mutableStateOf<InstallmentEntity?>(null) }

    val currentRow = row ?: return
    val preFile = currentRow.preFile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = "${preFile.draftNumber} • ${currentRow.customerName ?: "بدون مشتری"}",
                subtitle = "${currentRow.projectName ?: ""} • ${currentRow.unitTitle ?: ""}",
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
                    StatusChip(Constants.preFileStatusLabel(preFile.status), preFileStatusColor(preFile.status))
                    SpacerH(0)
                    Text(
                        "  ${Formatters.toPersianDigits(preFile.draftDate)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SpacerH(10)
                PaymentProgress(paid = preFile.paidAmount, total = preFile.effectivePrice)
                SpacerH(6)
                InfoRow("کارشناس فروش", preFile.salesAgentName)
                InfoRow("تاریخ تحویل", preFile.deliveryDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("کد رزرو", preFile.trackingCode)
            }
        }

        item {
            SectionCard(title = "شرایط مالی") {
                InfoRow("قیمت هر متر", Formatters.amountWithUnit(preFile.pricePerMeter))
                InfoRow("مبلغ کل", Formatters.amountWithUnit(preFile.totalPrice))
                InfoRow("تخفیف", Formatters.amountWithUnit(preFile.discount))
                InfoRow("مبلغ نهایی", Formatters.amountWithUnit(preFile.finalPrice), emphasize = true)
                InfoRow("پیش‌پرداخت", Formatters.amountWithUnit(preFile.prepayment))
                InfoRow("دریافتی", Formatters.amountWithUnit(preFile.paidAmount))
                InfoRow("مانده", Formatters.amountWithUnit(preFile.remainingAmount), emphasize = true)
                InfoRow("نوع پرداخت", Constants.paymentTypeLabel(preFile.paymentType))
                InfoRow(
                    "قسط",
                    preFile.installmentAmount?.let {
                        "${Formatters.amountShort(it)} تومان × ${Formatters.number(preFile.installmentCount ?: 0)} (${preFile.installmentPeriod ?: "ماهانه"})"
                    },
                )
                InfoRow("شروع اقساط", preFile.installmentStartDate?.let { Formatters.toPersianDigits(it) })
            }
        }

        item {
            SectionCard(title = "اقساط و سررسیدها (${Formatters.number(installments.size)})") {
                if (installments.isEmpty()) {
                    Text(
                        "قسطی ثبت نشده — با ویرایش پیش‌فایل و تنظیم تعداد اقساط، جدول ساخته می‌شود",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    installments.forEach { installment ->
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        installment.title ?: "قسط ${installment.installmentNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        "سررسید ${Formatters.toPersianDigits(installment.dueDate)} • ${Formatters.relativeJalali(installment.dueDate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${Formatters.amountShort(installment.amount)} تومان",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    StatusChip(
                                        Constants.installmentStatusLabel(installment.status),
                                        installmentStatusColor(installment.status),
                                    )
                                }
                            }
                            SpacerH(6)
                            if (installment.status == Constants.INSTALLMENT_PAID) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "پرداخت‌شده در ${installment.paidDate?.let { Formatters.toPersianDigits(it) } ?: "—"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusColors.paid,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(onClick = { viewModel.unpayInstallment(installment.id) }) {
                                        Text("لغو پرداخت")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { payingInstallment = installment },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("ثبت پرداخت این قسط") }
                            }
                        }
                        SoftDivider()
                    }
                }
            }
        }

        item {
            SectionCard(title = "تعهدات و سند") {
                InfoRow("تعهد فروشنده", preFile.sellerCommitment)
                InfoRow("تعهد خریدار", preFile.buyerCommitment)
                InfoRow("جریمه", preFile.penaltyClause)
                InfoRow("شرایط فسخ", preFile.cancellationTerms)
                InfoRow("تاریخ سند", preFile.deedDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("دفترخانه", preFile.deedOffice)
                InfoRow("نوع ضمانت", preFile.guaranteeType)
                InfoRow("چک‌ها", preFile.chequeCount?.let { "${Formatters.number(it)} فقره — ${Formatters.amountWithUnit(preFile.chequeAmount)}" })
                InfoRow("تهاتر", preFile.exchangeDetails)
                InfoRow("یادداشت", preFile.notes)
            }
        }

        if (unit != null || customer != null) {
            item {
                SectionCard(title = "دسترسی سریع") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        unit?.let { u ->
                            OutlinedButton(onClick = { onOpenUnit(u.id) }, modifier = Modifier.weight(1f)) {
                                Text("مشاهده واحد ${u.unitNumber}")
                            }
                        }
                        customer?.let { c ->
                            OutlinedButton(onClick = { onOpenCustomer(c.id) }, modifier = Modifier.weight(1f)) {
                                Text("پرونده ${c.firstName}")
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
        }
    }

    payingInstallment?.let { installment ->
        InstallmentPayDialog(
            installment = installment,
            onDismiss = { payingInstallment = null },
            onConfirm = { amount, method, reference ->
                viewModel.payInstallment(installment.id, amount, method, reference)
                payingInstallment = null
            },
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "حذف پیش‌فایل",
            message = "پیش‌فایل ${preFile.draftNumber} و اقساط آن حذف شوند؟",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }
}

/** دیالوگ ثبت پرداخت قسط — با پیش‌فرض مبلغ کامل و امکان پرداخت جزئی */
@Composable
fun InstallmentPayDialog(
    installment: InstallmentEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, method: String?, reference: String?) -> Unit,
) {
    var amountText by remember { mutableStateOf(Formatters.amount(installment.amount - installment.paidAmount)) }
    var method by remember { mutableStateOf("کارت‌به‌کارت") }
    var reference by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(installment.title ?: "قسط ${installment.installmentNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "مبلغ قسط: ${Formatters.amountWithUnit(installment.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                MoneyField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "مبلغ پرداختی",
                )
                DropdownField(
                    label = "روش پرداخت",
                    options = listOf("کارت‌به‌کارت", "نقدی", "چک", "حواله", "تسویه بانکی"),
                    selected = method,
                    onSelect = { method = it },
                )
                FormTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = "شماره پیگیری / چک",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = Formatters.parseLong(amountText) ?: 0L
                if (amount > 0) onConfirm(amount, method, reference.ifBlank { null })
            }) { Text("ثبت پرداخت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
