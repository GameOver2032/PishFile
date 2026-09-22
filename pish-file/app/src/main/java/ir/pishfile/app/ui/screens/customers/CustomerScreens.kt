package ir.pishfile.app.ui.screens.customers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
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
import ir.pishfile.app.ui.components.FollowUpDialog
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.components.preFileStatusColor
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.CustomerDetailViewModel
import ir.pishfile.app.ui.viewmodel.CustomerEditViewModel
import ir.pishfile.app.ui.viewmodel.CustomerListViewModel

// ---------------------------------------------------------------------------
// فهرست مشتریان
// ---------------------------------------------------------------------------

@Composable
fun CustomerListScreen(
    onOpen: (String) -> Unit,
    viewModel: CustomerListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val preFileRows by viewModel.preFileRows.collectAsStateWithLifecycle()
    val fileByPreFileId = preFileRows.associateBy { it.preFile.id }

    Column(Modifier.fillMaxSize()) {
        SearchField(
            query = query,
            onQueryChange = {
                query = it
                viewModel.setQuery(it)
            },
            placeholder = "جست‌وجوی مشتری: نام، شماره…",
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
                title = "مشتری ثبت نشده",
                subtitle = "با دکمه + یا صفحه‌ی «سریع» یک مشتری جدید ثبت کنید",
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(customers, key = { it.id }) { customer ->
                    val fileRow = customer.preFileId?.let { fileByPreFileId[it] }
                    Card(
                        onClick = { onOpen(customer.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        customer.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        listOfNotNull(
                                            customer.phone,
                                            fileRow?.preFile?.draftNumber,
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { pendingDelete = customer }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            SpacerH(6)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusChip(
                                    Constants.customerRoleLabel(customer.role),
                                    StatusColors.reserved,
                                )
                                StatusChip(
                                    Constants.customerStatusLabel(customer.status),
                                    if (customer.status == Constants.CUSTOMER_ACTIVE) StatusColors.available
                                    else if (customer.status == Constants.CUSTOMER_DONE) StatusColors.paid
                                    else StatusColors.overdue,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { customer ->
        ConfirmDialog(
            title = "حذف مشتری",
            message = "مشتری «${customer.name}» حذف شود؟",
            onConfirm = { viewModel.delete(customer.id) },
            onDismiss = { pendingDelete = null },
        )
    }
}

// ---------------------------------------------------------------------------
// ثبت / ویرایش مشتری
// ---------------------------------------------------------------------------

@Composable
fun CustomerEditScreen(
    customerId: String?,
    initialUnitId: String,
    initialPreFileId: String,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: CustomerEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    if (customerId == null) {
        viewModel.setTarget(initialUnitId, initialPreFileId)
    } else {
        viewModel.load(customerId)
    }

    val form by viewModel.form.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = if (customerId == null) "مشتری جدید" else "ویرایش مشتری",
                subtitle = "خریدار یا طرف‌مذاکره‌ی فایل پیش‌فروش",
            ) {
                FormTextField(
                    value = form.name,
                    onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                    label = "نام و نام خانوادگی *",
                )
                SpacerH(8)
                FormTextField(
                    value = form.phone,
                    onValueChange = { v -> viewModel.update { it.copy(phone = v) } },
                    label = "شماره تماس",
                )
                SpacerH(8)
                DropdownField(
                    label = "نقش",
                    options = Constants.customerRoles.map { Constants.customerRoleLabel(it) },
                    selected = Constants.customerRoleLabel(form.role),
                    onSelect = { label ->
                        val code = Constants.customerRoles.firstOrNull { Constants.customerRoleLabel(it) == label } ?: form.role
                        viewModel.update { it.copy(role = code) }
                    },
                )
                if (customerId != null) {
                    SpacerH(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Constants.customerStatuses.forEach { status ->
                            FilterChip(
                                selected = form.status == status,
                                onClick = { viewModel.update { it.copy(status = status) } },
                                label = { Text(Constants.customerStatusLabel(status)) },
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "اتصال به فایل / واحد", subtitle = "جایی که این مشتری با آن در ارتباط است") {
                when {
                    form.linkedFileLabel != null -> InfoRow("فایل پیش‌فروش", form.linkedFileLabel, emphasize = true)
                    form.linkedUnitLabel != null -> InfoRow("واحد", form.linkedUnitLabel, emphasize = true)
                    else -> Text(
                        "بدون اتصال — بعداً می‌توانید نوت و پیگیری را مستقیم روی مشتری ثبت کنید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(title = "یادداشت") {
                FormTextField(
                    value = form.notes,
                    onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                    label = "توضیحات درباره‌ی مشتری",
                    singleLine = false,
                    minLines = 3,
                )
            }
        }

        item {
            Button(
                onClick = { viewModel.save { id -> onSaved(id) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = form.name.isNotBlank(),
            ) { Text(if (customerId == null) "ثبت مشتری" else "ذخیره تغییرات") }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
        }
    }
}

// ---------------------------------------------------------------------------
// جزئیات مشتری
// ---------------------------------------------------------------------------

@Composable
fun CustomerDetailScreen(
    customerId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenPreFile: (String) -> Unit,
    viewModel: CustomerDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.setCustomerId(customerId)

    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val preFileRow by viewModel.preFileRow.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val followUps by viewModel.followUps.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showDelete by remember { mutableStateOf(false) }
    var showFollowUpDialog by remember { mutableStateOf(false) }

    val c = customer ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // --- سربرگ ---
        item {
            SectionCard(
                title = c.name,
                subtitle = Constants.customerRoleLabel(c.role),
                trailing = {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(Constants.customerStatusLabel(c.status), StatusColors.available)
                    SpacerH(0)
                }
                SpacerH(8)
                InfoRow("شماره تماس", c.phone)
                if (!c.phone.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}")))
                        },
                    ) { Text("📞 تماس گرفتن") }
                }
                if (!c.notes.isNullOrBlank()) {
                    SpacerH(8)
                    InfoRow("یادداشت", c.notes)
                }
            }
        }

        // --- فایل/واحد مرتبط ---
        item {
            val row = preFileRow
            if (row != null) {
                SectionCard(
                    title = "فایل پیش‌فروش مرتبط",
                    trailing = {
                        StatusChip(
                            Constants.preFileStatusLabel(row.preFile.status),
                            preFileStatusColor(row.preFile.status),
                        )
                    },
                ) {
                    InfoRow("شماره فایل", row.preFile.draftNumber, emphasize = true)
                    InfoRow("پروژه", row.projectName)
                    InfoRow("واحد", row.unitTitle)
                    InfoRow("قیمت کل", Formatters.amountWithUnit(row.preFile.displayPrice), emphasize = true)
                    SpacerH(8)
                    OutlinedButton(onClick = { onOpenPreFile(row.preFile.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("مشاهده فایل")
                    }
                }
            } else if (unit != null) {
                SectionCard(title = "واحد مرتبط") {
                    InfoRow("واحد", unit?.displayTitle, emphasize = true)
                }
            }
        }

        // --- پیگیری‌های مشتری (با آلارم) ---
        item {
            SectionCard(
                title = "پیگیری‌های مشتری",
                subtitle = "با تعیین تاریخ و ساعت، آلارم یادآوری تنظیم می‌شود 🔔",
                trailing = {
                    OutlinedButton(onClick = { showFollowUpDialog = true }) { Text("پیگیری جدید") }
                },
            ) {
                if (followUps.isEmpty()) {
                    Text(
                        "پیگیری‌ای برای این مشتری ثبت نشده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        followUps.forEach { followUp ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                followUp.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                listOfNotNull(
                                                    followUp.dueDate?.let { Formatters.toPersianDigits(it) },
                                                    followUp.dueTime?.let { "🔔 $it" },
                                                ).joinToString(" • "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteFollowUp(followUp.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    if (followUp.status == Constants.FOLLOWUP_PENDING) {
                                        SpacerH(6)
                                        OutlinedButton(
                                            onClick = { viewModel.markFollowUpDone(followUp.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                            Text(" انجام شد")
                                        }
                                    } else {
                                        StatusChip(Constants.followUpStatusLabel(followUp.status), StatusColors.paid)
                                    }
                                }
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

    if (showDelete) {
        ConfirmDialog(
            title = "حذف مشتری",
            message = "مشتری «${c.name}» حذف شود؟",
            onConfirm = { viewModel.delete(onBack) },
            onDismiss = { showDelete = false },
        )
    }

    if (showFollowUpDialog) {
        FollowUpDialog(
            preFiles = listOfNotNull(preFileRow?.let { "${it.preFile.draftNumber} — ${it.projectName ?: ""}" }),
            preFileIds = listOfNotNull(preFileRow?.preFile?.id),
            customers = allCustomers.map { it.name },
            customerIds = allCustomers.map { it.id },
            initialCustomerId = c.id,
            onDismiss = { showFollowUpDialog = false },
            onSave = { followUp ->
                // اگر مشتری در دیالوگ انتخاب نشده باشد، پیش‌فرض همین مشتری است
                viewModel.saveFollowUp(
                    if (followUp.customerId == null) followUp.copy(customerId = c.id) else followUp
                ) { showFollowUpDialog = false }
            },
        )
    }
}
