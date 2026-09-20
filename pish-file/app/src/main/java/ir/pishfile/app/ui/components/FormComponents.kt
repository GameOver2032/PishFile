package ir.pishfile.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.pishfile.app.core.Formatters

/** کارت آماری کوچک داشبورد */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(19.dp))
                }
                SpacerW(8)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SpacerH(8)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** کارت بزرگ مالی */
@Composable
fun FinanceCard(
    title: String,
    amount: String,
    subtitle: String? = null,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    SpacerW(6)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SpacerH(6)
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** دیالوگ تأیید حذف */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "حذف",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) { Text(confirmLabel, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}

/**
 * فیلد تاریخ شمسی — با دکمه‌های میان‌بر «امروز» و «+۱ ماه»
 * (کاربر می‌تواند دستی هم 1405/06/29 بنویسد؛ رقم فارسی و لاتین هر دو پذیرفته می‌شود)
 */
@Composable
fun JalaliDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    quickMonths: List<Int> = listOf(1, 12),
) {
    Column(modifier.fillMaxWidth()) {
        FormTextField(
            value = Formatters.toPersianDigits(value),
            onValueChange = { input ->
                onValueChange(Formatters.toLatinDigits(input).filter { it.isDigit() || it == '/' || it == '-' })
            },
            label = label,
            placeholder = "1405/06/29",
        )
        SpacerH(6)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            QuickDateChip("امروز") { onValueChange(Formatters.todayJalali()) }
            quickMonths.forEach { months ->
                QuickDateChip("+${Formatters.toPersianDigits(months.toString())} ماه") {
                    onValueChange(Formatters.addJalaliMonths(value.ifBlank { Formatters.todayJalali() }, months))
                }
            }
            if (value.isNotBlank()) {
                val relative = Formatters.daysFromToday(value)
                if (relative != null) {
                    Text(
                        text = Formatters.relativeJalali(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDateChip(label: String, onClick: () -> Unit) {
    androidx.compose.material3.AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        shape = RoundedCornerShape(16.dp),
    )
}

/** انتخاب چندگانه (امکانات واحد، ویژگی‌ها…) */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MultiSelectChips(
    label: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SpacerH(6)
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option in selected,
                    onClick = { onToggle(option) },
                    label = { Text(option, style = MaterialTheme.typography.labelMedium) },
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }
    }
}

/** بخش مالی خلاصه — «مبلغ کل، پرداختی، مانده» با نوار پیشرفت */
@Composable
fun PaymentProgress(
    paid: Long,
    total: Long,
    modifier: Modifier = Modifier,
) {
    val percent = if (total > 0) ((paid.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 0
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("پرداخت‌شده", style = MaterialTheme.typography.labelMedium)
            Text(
                "${Formatters.percent(percent)} از ${Formatters.amountShort(total)} تومان",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        SpacerH(6)
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(6.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(percent / 100f)
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
            )
        }
        SpacerH(6)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "دریافتی: ${Formatters.amountShort(paid)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "مانده: ${Formatters.amountShort((total - paid).coerceAtLeast(0))} تومان",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** ردیف دکمه‌های عملیات (ویرایش، حذف، …) */
@Composable
fun ActionRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/** عنوان بخش‌ها با فاصله‌ی استاندارد */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 6.dp),
        textAlign = TextAlign.Start,
    )
}
