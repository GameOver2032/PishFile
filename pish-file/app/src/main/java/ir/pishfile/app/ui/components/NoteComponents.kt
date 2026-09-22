package ir.pishfile.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.NoteEntity

/**
 * دیالوگ «ثبت مکالمه / نوت» — مشترک بین صفحه‌ی فایل، مشتری، تب پیگیری‌ها و صفحه‌ی سریع.
 *
 * اگر [fixedPreFileId] یا [fixedCustomerId] داده شود، نوت به همان موضوع وصل می‌شود
 * و لیست‌ها برای انتخاب موضوع نمایش داده نمی‌شوند.
 */
@Composable
fun NoteDialog(
    preFiles: List<String>,
    preFileIds: List<String>,
    customers: List<String>,
    customerIds: List<String>,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    fixedPreFileId: String? = null,
    fixedCustomerId: String? = null,
) {
    var noteDate by remember { mutableStateOf(Formatters.todayJalali()) }
    var type by remember { mutableStateOf(Constants.NOTE_CALL) }
    var text by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var preFile by remember { mutableStateOf<String?>(null) }
    var customer by remember { mutableStateOf<String?>(null) }

    val showLinks = fixedPreFileId == null && fixedCustomerId == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مکالمه / نوت جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showLinks) {
                    if (preFiles.isNotEmpty()) {
                        DropdownField(
                            label = "فایل پیش‌فروش",
                            options = preFiles,
                            selected = preFile,
                            onSelect = { preFile = it },
                            allowEmpty = true,
                            emptyLabel = "بدون فایل",
                        )
                    }
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
                }
                JalaliDateField(
                    value = noteDate,
                    onValueChange = { noteDate = it },
                    label = "تاریخ مکالمه",
                    quickMonths = emptyList(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Constants.noteTypes.forEach { code ->
                        FilterChip(
                            selected = type == code,
                            onClick = { type = code },
                            label = { Text(Constants.noteTypeLabel(code)) },
                        )
                    }
                }
                FormTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "شرح مکالمه / پیگیری *",
                    singleLine = false,
                    minLines = 3,
                )
                FormTextField(
                    value = outcome,
                    onValueChange = { outcome = it },
                    label = "نتیجه (مثلاً: قرار بازدید، اعلام قیمت، انصراف…)",
                    singleLine = false,
                    minLines = 1,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = {
                    onSave(
                        NoteEntity(
                            preFileId = fixedPreFileId ?: preFile?.let { label -> preFileIds.getOrNull(preFiles.indexOf(label)) },
                            customerId = fixedCustomerId ?: customer?.let { label -> customerIds.getOrNull(customers.indexOf(label)) },
                            type = type,
                            text = text.trim(),
                            outcome = outcome.ifBlank { null },
                            noteDate = noteDate.ifBlank { Formatters.todayJalali() },
                        )
                    )
                },
            ) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

/**
 * یک ردیف از تایم‌لاین مکالمات: تاریخ + نوع، متن، نتیجه و موضوع مرتبط.
 */
@Composable
fun NoteTimelineCard(
    note: NoteEntity,
    contextLine: String?,
    onDelete: () -> Unit,
    onOpen: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onOpen?.invoke() },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Formatters.toPersianDigits(note.noteDate),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "  ${Formatters.relativeJalali(note.noteDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                StatusChip(Constants.noteTypeLabel(note.type), MaterialTheme.colorScheme.tertiary)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.padding(top = 6.dp))
            Text(note.text, style = MaterialTheme.typography.bodyMedium)
            if (!note.outcome.isNullOrBlank()) {
                Spacer(Modifier.padding(top = 4.dp))
                Text(
                    "نتیجه: ${note.outcome}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            if (!contextLine.isNullOrBlank()) {
                Spacer(Modifier.padding(top = 4.dp))
                Text(
                    contextLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
