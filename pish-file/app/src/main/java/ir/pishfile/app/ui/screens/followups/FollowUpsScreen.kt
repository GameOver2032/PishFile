package ir.pishfile.app.ui.screens.followups

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.FollowUpsViewModel

@Composable
fun FollowUpsScreen(
    openNewOnStart: Boolean,
    onOpenCustomer: (String) -> Unit,
    viewModel: FollowUpsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var showDone by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(openNewOnStart) }

    val followUps by viewModel.followUps.collectAsStateWithLifecycle()
    val customerNames by viewModel.customerNames.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val preFileRows by viewModel.preFileRows.collectAsStateWithLifecycle()

    LaunchedEffect(openNewOnStart) {
        if (openNewOnStart) showAddDialog = true
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = showDone, onCheckedChange = {
                showDone = it
                viewModel.setShowDone(it)
            })
            Text("نمایش انجام‌شده‌ها", style = MaterialTheme.typography.bodySmall)
        }

        if (followUps.isEmpty()) {
            EmptyState(
                title = "پیگیری‌ای ثبت نشده",
                subtitle = "تماس، بازدید و جلسه‌های پیش‌رو را این‌جا برنامه‌ریزی کنید",
                icon = { Icon(Icons.Filled.EventNote, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(followUps, key = { it.id }) { followUp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        followUp.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        listOfNotNull(
                                            followUp.customerId?.let { customerNames[it] },
                                            followUp.dueDate?.let { Formatters.toPersianDigits(it) },
                                            followUp.dueTime,
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                StatusChip(
                                    FollowUpsViewModel.typeLabel(followUp.type),
                                    MaterialTheme.colorScheme.tertiary,
                                )
                                SpacerH(0)
                                IconButton(onClick = { viewModel.delete(followUp.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            followUp.description?.let {
                                SpacerH(4)
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            if (followUp.status == Constants.FOLLOWUP_PENDING) {
                                SpacerH(8)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.markDone(followUp.id) },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                        Text(" انجام شد")
                                    }
                                    followUp.customerId?.let { id ->
                                        OutlinedButton(onClick = { onOpenCustomer(id) }) { Text("پرونده مشتری") }
                                    }
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

    if (showAddDialog) {
        FollowUpDialog(
            customers = customers.map { "${it.firstName} ${it.lastName}" },
            preFiles = preFileRows.map { "${it.preFile.draftNumber} — ${it.customerName ?: ""}" },
            customerIds = customers.map { it.id },
            preFileIds = preFileRows.map { it.preFile.id },
            onDismiss = { showAddDialog = false },
            onSave = { followUp ->
                viewModel.save(followUp) { showAddDialog = false }
            },
        )
    }
}

@Composable
private fun FollowUpDialog(
    customers: List<String>,
    preFiles: List<String>,
    customerIds: List<String>,
    preFileIds: List<String>,
    onDismiss: () -> Unit,
    onSave: (FollowUpEntity) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("تماس") }
    var priority by remember { mutableStateOf("معمولی") }
    var dueDate by remember { mutableStateOf(Formatters.todayJalali()) }
    var dueTime by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf<String?>(null) }
    var preFile by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پیگیری جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(value = title, onValueChange = { title = it }, label = "موضوع *")
                FormTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات",
                    singleLine = false,
                    minLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownField(
                        label = "نوع",
                        options = FollowUpsViewModel.types.map { it.second },
                        selected = type,
                        onSelect = { type = it },
                        modifier = Modifier.weight(1f),
                    )
                    DropdownField(
                        label = "اولویت",
                        options = FollowUpsViewModel.priorities.map { it.second },
                        selected = priority,
                        onSelect = { priority = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                JalaliDateField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = "تاریخ پیگیری",
                    quickMonths = emptyList(),
                )
                FormTextField(value = dueTime, onValueChange = { dueTime = it }, label = "ساعت (مثلاً 10:30)")
                if (customers.isNotEmpty()) {
                    DropdownField(
                        label = "مشتری",
                        options = customers,
                        selected = customer,
                        onSelect = { customer = it },
                        allowEmpty = true,
                        emptyLabel = "بدون مشتری",
                    )
                }
                if (preFiles.isNotEmpty()) {
                    DropdownField(
                        label = "پیش‌فایل",
                        options = preFiles,
                        selected = preFile,
                        onSelect = { preFile = it },
                        allowEmpty = true,
                        emptyLabel = "بدون پیش‌فایل",
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        FollowUpEntity(
                            type = FollowUpViews.toCode(type),
                            priority = FollowUpViews.toPriorityCode(priority),
                            title = title.trim(),
                            description = description.ifBlank { null },
                            customerId = customer?.let { name -> customerIds.getOrNull(customers.indexOf(name)) },
                            preFileId = preFile?.let { label -> preFileIds.getOrNull(preFiles.indexOf(label)) },
                            dueDate = dueDate.ifBlank { null },
                            dueTime = dueTime.ifBlank { null },
                            status = Constants.FOLLOWUP_PENDING,
                        )
                    )
                }
            }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

/** تبدیل برچسب به کد */
private object FollowUpViews {
    fun toCode(label: String): String =
        FollowUpsViewModel.types.firstOrNull { it.second == label }?.first ?: "CALL"

    fun toPriorityCode(label: String): String =
        FollowUpsViewModel.priorities.firstOrNull { it.second == label }?.first ?: "NORMAL"
}
