package ir.pishfile.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.ui.viewmodel.FollowUpsViewModel

/**
 * دیالوگ «پیگیری جدید» — مشترک بین تب پیگیری‌ها و صفحه‌ی جزئیات مشتری.
 * پیگیری‌ها که تاریخ داشته باشند، آلارم یادآوری برایشان تنظیم می‌شود.
 */
@Composable
fun FollowUpDialog(
    preFiles: List<String>,
    preFileIds: List<String>,
    customers: List<String>,
    customerIds: List<String>,
    onDismiss: () -> Unit,
    onSave: (FollowUpEntity) -> Unit,
    initialCustomerId: String? = null,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("تماس تلفنی") }
    var priority by remember { mutableStateOf("معمولی") }
    var dueDate by remember { mutableStateOf(Formatters.todayJalali()) }
    var dueTime by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var preFile by remember { mutableStateOf<String?>(null) }
    var customer by remember {
        mutableStateOf<String?>(initialCustomerId?.let { id ->
            customers.getOrNull(customerIds.indexOf(id))
        })
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = "ساعت (۱۰:۳۰) — آلارم",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = "شماره تماس",
                        modifier = Modifier.weight(1f),
                    )
                }
                if (preFiles.isNotEmpty()) {
                    DropdownField(
                        label = "فایل پیش‌فروش مربوطه",
                        options = preFiles,
                        selected = preFile,
                        onSelect = { preFile = it },
                        allowEmpty = true,
                        emptyLabel = "بدون فایل",
                    )
                }
                if (customers.isNotEmpty()) {
                    DropdownField(
                        label = "مشتری مربوطه",
                        options = customers,
                        selected = customer,
                        onSelect = { customer = it },
                        allowEmpty = true,
                        emptyLabel = "بدون مشتری",
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        FollowUpEntity(
                            type = toFollowUpTypeCode(type),
                            priority = toFollowUpPriorityCode(priority),
                            title = title.trim(),
                            description = description.ifBlank { null },
                            preFileId = preFile?.let { label -> preFileIds.getOrNull(preFiles.indexOf(label)) },
                            customerId = customer?.let { label -> customerIds.getOrNull(customers.indexOf(label)) },
                            dueDate = dueDate.ifBlank { null },
                            dueTime = dueTime.ifBlank { null },
                            contactPhone = contactPhone.ifBlank { null },
                            status = Constants.FOLLOWUP_PENDING,
                        )
                    )
                }
            }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

internal fun toFollowUpTypeCode(label: String): String =
    FollowUpsViewModel.types.firstOrNull { it.second == label }?.first ?: "CALL"

internal fun toFollowUpPriorityCode(label: String): String =
    FollowUpsViewModel.priorities.firstOrNull { it.second == label }?.first ?: "NORMAL"
