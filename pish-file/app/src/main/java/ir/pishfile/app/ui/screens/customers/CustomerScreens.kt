package ir.pishfile.app.ui.screens.customers

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import ir.pishfile.app.data.local.entity.CustomerEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FilterChipsRow
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.MoneyField
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SoftDivider
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.screens.dashboard.preFileStatusColor
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.CustomerDetailViewModel
import ir.pishfile.app.ui.viewmodel.CustomerEditViewModel
import ir.pishfile.app.ui.viewmodel.CustomerForm
import ir.pishfile.app.ui.viewmodel.CustomerListViewModel

@Composable
fun CustomerListScreen(
    onOpen: (String) -> Unit,
    viewModel: CustomerListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val customers by viewModel.filtered.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        SearchField(
            query = query,
            onQueryChange = {
                query = it
                viewModel.setQuery(it)
            },
            placeholder = "نام، کد ملی، شماره تماس…",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        FilterChipsRow(
            options = Constants.customerStatuses.map { it to Constants.customerStatusLabel(it) },
            selectedKey = statusFilter,
            onSelect = {
                statusFilter = it
                viewModel.setStatusFilter(it)
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        if (customers.isEmpty()) {
            EmptyState(
                title = "مشتری‌ای ثبت نشده",
                subtitle = "مشتری‌ها، سرنخ‌ها و خریداران را این‌جا مدیریت کنید",
                icon = { Icon(Icons.Filled.People, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onOpen(customer.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(customer) },
                        onDelete = { pendingDelete = customer },
                    )
                }
            }
        }
    }

    pendingDelete?.let { customer ->
        ConfirmDialog(
            title = "حذف مشتری",
            message = "«${customer.firstName} ${customer.lastName}» حذف شود؟ پیش‌فایل‌های مرتبط نیز حذف خواهند شد.",
            onConfirm = { viewModel.delete(customer.id) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
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
                        "${customer.title.orEmpty()} ${customer.firstName} ${customer.lastName}".trim(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listOfNotNull(customer.phonePrimary, customer.city, Constants.customerSourceLabel(customer.source))
                            .joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (customer.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "علاقه‌مندی",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            SpacerH(6)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusChip(Constants.customerStatusLabel(customer.status), customerStatusColor(customer.status))
                customer.creditScore?.let {
                    StatusChip("امتیاز ${Formatters.number(it)}", MaterialTheme.colorScheme.tertiary)
                }
                if (customer.hasBouncedCheque) {
                    StatusChip("چک برگشتی", StatusColors.overdue)
                }
            }
        }
    }
}

private fun customerStatusColor(status: String) = when (status) {
    Constants.CUSTOMER_LEAD -> StatusColors.reserved
    Constants.CUSTOMER_ACTIVE -> StatusColors.available
    Constants.CUSTOMER_INSTALLMENT -> StatusColors.sold
    Constants.CUSTOMER_SETTLED -> StatusColors.paid
    else -> StatusColors.draft
}

@Composable
fun CustomerEditScreen(
    customerId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: CustomerEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    if (customerId == null) viewModel.markNew() else viewModel.load(customerId)
    val form = viewModel.form

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(title = "نوع مشتری") {
                DropdownField(
                    label = "شخص",
                    options = listOf("حقیقی", "حقوقی"),
                    selected = form.entityType,
                    onSelect = { v -> viewModel.update { it.copy(entityType = v) } },
                )
            }
        }

        item {
            SectionCard(title = "اطلاعات هویتی") {
                if (form.isLegalEntity) {
                    FormTextField(
                        value = form.companyName,
                        onValueChange = { v -> viewModel.update { it.copy(companyName = v) } },
                        label = "نام شرکت *",
                    )
                    SpacerH(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormTextField(
                            value = form.registrationNumber,
                            onValueChange = { v -> viewModel.update { it.copy(registrationNumber = v) } },
                            label = "شماره ثبت",
                            modifier = Modifier.weight(1f),
                        )
                        FormTextField(
                            value = form.economicCode,
                            onValueChange = { v -> viewModel.update { it.copy(economicCode = v) } },
                            label = "کد اقتصادی",
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownField(
                            label = "عنوان",
                            options = listOf("آقای", "خانم", "جناب آقای", "سرکار خانم"),
                            selected = form.title,
                            onSelect = { v -> viewModel.update { it.copy(title = v) } },
                            modifier = Modifier.weight(1f),
                        )
                        FormTextField(
                            value = form.firstName,
                            onValueChange = { v -> viewModel.update { it.copy(firstName = v) } },
                            label = "نام *",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    SpacerH(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormTextField(
                            value = form.lastName,
                            onValueChange = { v -> viewModel.update { it.copy(lastName = v) } },
                            label = "نام خانوادگی *",
                            modifier = Modifier.weight(1f),
                        )
                        FormTextField(
                            value = form.fatherName,
                            onValueChange = { v -> viewModel.update { it.copy(fatherName = v) } },
                            label = "نام پدر",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.nationalId,
                        onValueChange = { v -> viewModel.update { it.copy(nationalId = v.filter { c -> c.isDigit() }) } },
                        label = if (form.isLegalEntity) "شناسه ملی" else "کد ملی",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.idNumber,
                        onValueChange = { v -> viewModel.update { it.copy(idNumber = v) } },
                        label = "شماره شناسنامه",
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!form.isLegalEntity) {
                    SpacerH(8)
                    JalaliDateField(
                        value = form.birthDate,
                        onValueChange = { v -> viewModel.update { it.copy(birthDate = v) } },
                        label = "تاریخ تولد",
                        quickMonths = emptyList(),
                    )
                }
            }
        }

        item {
            SectionCard(title = "راه‌های تماس") {
                FormTextField(
                    value = form.phonePrimary,
                    onValueChange = { v -> viewModel.update { it.copy(phonePrimary = v.filter { c -> c.isDigit() || c == '+' }) } },
                    label = "موبایل اصلی *",
                )
                SpacerH(8)
                FormTextField(
                    value = form.phoneSecondary,
                    onValueChange = { v -> viewModel.update { it.copy(phoneSecondary = v.filter { c -> c.isDigit() || c == '+' }) } },
                    label = "شماره دوم / تلفن ثابت",
                )
                SpacerH(8)
                FormTextField(
                    value = form.whatsapp,
                    onValueChange = { v -> viewModel.update { it.copy(whatsapp = v.filter { c -> c.isDigit() || c == '+' }) } },
                    label = "واتس‌اپ",
                )
                SpacerH(8)
                FormTextField(
                    value = form.email,
                    onValueChange = { v -> viewModel.update { it.copy(email = v) } },
                    label = "ایمیل",
                )
            }
        }

        item {
            SectionCard(title = "نشانی و شغل") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.province,
                        onValueChange = { v -> viewModel.update { it.copy(province = v) } },
                        label = "استان",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.city,
                        onValueChange = { v -> viewModel.update { it.copy(city = v) } },
                        label = "شهر",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                FormTextField(
                    value = form.address,
                    onValueChange = { v -> viewModel.update { it.copy(address = v) } },
                    label = "نشانی",
                    singleLine = false,
                    minLines = 2,
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.job,
                        onValueChange = { v -> viewModel.update { it.copy(job = v) } },
                        label = "شغل",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.workPhone,
                        onValueChange = { v -> viewModel.update { it.copy(workPhone = v) } },
                        label = "تلفن محل کار",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            SectionCard(title = "وضعیت و اعتبارسنجی") {
                DropdownField(
                    label = "وضعیت مشتری",
                    options = Constants.customerStatuses.map { Constants.customerStatusLabel(it) },
                    selected = Constants.customerStatusLabel(form.status),
                    onSelect = { label ->
                        val code = Constants.customerStatuses.firstOrNull { Constants.customerStatusLabel(it) == label } ?: form.status
                        viewModel.update { it.copy(status = code) }
                    },
                )
                SpacerH(8)
                DropdownField(
                    label = "منبع آشنایی",
                    options = CustomerForm.sourceOptions(),
                    selected = form.source,
                    onSelect = { v -> viewModel.update { it.copy(source = v) } },
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = form.referredBy,
                        onValueChange = { v -> viewModel.update { it.copy(referredBy = v) } },
                        label = "معرفی‌کننده",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = form.creditScore,
                        onValueChange = { v -> viewModel.update { it.copy(creditScore = v.filter { c -> c.isDigit() }) } },
                        label = "امتیاز اعتباری (۱-۱۰۰)",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                MoneyField(
                    value = form.creditLimit,
                    onValueChange = { v -> viewModel.update { it.copy(creditLimit = v) } },
                    label = "سقف اعتبار",
                )
                SpacerH(8)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(
                        checked = form.isReturningCustomer,
                        onCheckedChange = { v -> viewModel.update { it.copy(isReturningCustomer = v) } },
                    )
                    SpacerH(0)
                    Text("مشتری قدیمی است", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(
                        checked = form.hasBouncedCheque,
                        onCheckedChange = { v -> viewModel.update { it.copy(hasBouncedCheque = v) } },
                    )
                    Text("سابقه چک برگشتی", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            SectionCard(title = "یادداشت") {
                FormTextField(
                    value = form.notes,
                    onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                    label = "یادداشت‌ها",
                    singleLine = false,
                    minLines = 3,
                )
            }
        }

        item {
            Button(
                onClick = { viewModel.save { id -> onSaved(id) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (customerId == null) "ثبت مشتری" else "ذخیره تغییرات") }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
        }
    }
}

@Composable
fun CustomerDetailScreen(
    customerId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewPreFile: () -> Unit,
    onOpenPreFile: (String) -> Unit,
    viewModel: CustomerDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.setCustomerId(customerId)

    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val preFiles by viewModel.preFiles.collectAsStateWithLifecycle()
    val followUps by viewModel.followUps.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    val current = customer ?: return
    val totalValue = preFiles.sumOf { it.preFile.effectivePrice }
    val totalPaid = preFiles.sumOf { it.preFile.paidAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = "${current.title.orEmpty()} ${current.firstName} ${current.lastName}".trim(),
                subtitle = listOfNotNull(current.job, current.city).joinToString(" • "),
                trailing = {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            ) {
                StatusChip(Constants.customerStatusLabel(current.status), customerStatusColor(current.status))
                SpacerH(10)
                InfoRow("موبایل", current.phonePrimary?.let { Formatters.toPersianDigits(it) })
                InfoRow("تلفن دوم", current.phoneSecondary?.let { Formatters.toPersianDigits(it) })
                InfoRow("کد ملی", current.nationalId?.let { Formatters.toPersianDigits(it) })
                InfoRow("نام پدر", current.fatherName)
                InfoRow("تاریخ تولد", current.birthDate?.let { Formatters.toPersianDigits(it) })
                InfoRow("شهر", current.city)
                InfoRow("نشانی", current.address)
                InfoRow("شغل", current.job)
                InfoRow("منبع آشنایی", Constants.customerSourceLabel(current.source))
                InfoRow("معرفی‌کننده", current.referredBy)
                InfoRow("امتیاز اعتباری", current.creditScore?.let { Formatters.number(it) })
                InfoRow("سقف اعتبار", Formatters.amountWithUnit(current.creditLimit))
                InfoRow("یادداشت", current.notes)
                if (!current.phonePrimary.isNullOrBlank()) {
                    SpacerH(10)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {}, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Call, contentDescription = null)
                            SpacerH(0)
                            Text(" تماس")
                        }
                        OutlinedButton(onClick = onNewPreFile, modifier = Modifier.weight(1f)) {
                            Text("پیش‌فایل جدید")
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "خلاصه مالی مشتری") {
                InfoRow("تعداد قرارداد", Formatters.number(preFiles.size))
                InfoRow("ارزش کل قراردادها", Formatters.amountWithUnit(totalValue), emphasize = true)
                InfoRow("جمع دریافتی", Formatters.amountWithUnit(totalPaid))
                InfoRow(
                    "مانده بدهی",
                    Formatters.amountWithUnit((totalValue - totalPaid).coerceAtLeast(0)),
                    emphasize = true,
                    valueColor = if (totalValue - totalPaid > 0) StatusColors.overdue else StatusColors.paid,
                )
            }
        }

        item {
            SectionCard(title = "پیش‌فایل‌ها") {
                if (preFiles.isEmpty()) {
                    Text("پیش‌فایلی برای این مشتری ثبت نشده", style = MaterialTheme.typography.bodySmall)
                } else {
                    preFiles.forEach { row ->
                        Card(
                            onClick = { onOpenPreFile(row.preFile.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.preFile.draftNumber, style = MaterialTheme.typography.bodyMedium)
                                    StatusChip(
                                        Constants.preFileStatusLabel(row.preFile.status),
                                        preFileStatusColor(row.preFile.status),
                                    )
                                }
                                Text(
                                    "${row.projectName ?: ""} • ${row.unitTitle ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "${Formatters.amountShort(row.preFile.effectivePrice)} تومان • مانده ${Formatters.amountShort(row.preFile.dueAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "پیگیری‌ها (${Formatters.number(followUps.size)})") {
                if (followUps.isEmpty()) {
                    Text("پیگیری‌ای ثبت نشده", style = MaterialTheme.typography.bodySmall)
                } else {
                    followUps.take(8).forEach { followUp ->
                        Column(Modifier.padding(vertical = 6.dp)) {
                            Text(followUp.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${followUp.dueDate ?: "—"} • ${Constants.followUpStatusLabel(followUp.status)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        SoftDivider()
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
            title = "حذف مشتری",
            message = "«${current.firstName} ${current.lastName}» و پیش‌فایل‌های مرتبط حذف شوند؟",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }
}
