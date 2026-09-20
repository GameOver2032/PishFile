package ir.pishfile.app.ui.screens.installments

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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FilterChipsRow
import ir.pishfile.app.ui.components.FinanceCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.screens.dashboard.installmentStatusColor
import ir.pishfile.app.ui.screens.prefiles.InstallmentPayDialog
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.InstallmentsViewModel

/**
 * مدیریت اقساط و سررسیدها: چه چیزی نزدیک است، چه چیزی معوق شده و ثبت پرداخت.
 */
@Composable
fun InstallmentsScreen(
    onOpenPreFile: (String) -> Unit,
    viewModel: InstallmentsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var filter by remember { mutableStateOf(InstallmentsViewModel.FILTER_UPCOMING) }
    var paying by remember { mutableStateOf<InstallmentEntity?>(null) }

    val list by viewModel.current.collectAsStateWithLifecycle()
    val overdueTotal by viewModel.overdueTotal.collectAsStateWithLifecycle()
    val upcomingTotal by viewModel.upcomingTotal.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FinanceCard(
                title = "سررسید ۳۰ روز آینده",
                amount = Formatters.amountWithUnit(upcomingTotal),
                color = StatusColors.reserved,
                modifier = Modifier.weight(1f),
            )
            FinanceCard(
                title = "مجموع معوق",
                amount = Formatters.amountWithUnit(overdueTotal),
                color = if ((overdueTotal ?: 0) > 0) StatusColors.overdue else StatusColors.paid,
                modifier = Modifier.weight(1f),
            )
        }

        FilterChipsRow(
            options = listOf(
                InstallmentsViewModel.FILTER_UPCOMING to "پیش‌رو (۳۰ روز)",
                InstallmentsViewModel.FILTER_OVERDUE to "معوق",
                InstallmentsViewModel.FILTER_ALL to "همه",
            ),
            selectedKey = filter,
            onSelect = { selected ->
                filter = selected ?: InstallmentsViewModel.FILTER_UPCOMING
                viewModel.setFilter(filter)
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        SpacerH(6)

        if (list.isEmpty()) {
            EmptyState(
                title = "قسطی در این بازه نیست",
                subtitle = "با ثبت پیش‌فایل و تعیین تعداد اقساط، سررسیدها خودکار ساخته می‌شوند",
                icon = { Icon(Icons.Filled.Payments, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list, key = { it.id }) { installment ->
                    InstallmentFullCard(
                        installment = installment,
                        onPay = { paying = installment },
                        onUnpay = { viewModel.markUnpaid(installment.id) },
                        onOpen = { onOpenPreFile(installment.preFileId) },
                    )
                }
            }
        }
    }

    paying?.let { installment ->
        InstallmentPayDialog(
            installment = installment,
            onDismiss = { paying = null },
            onConfirm = { amount, method, reference ->
                viewModel.markPaid(installment.id, amount, method, reference)
                paying = null
            },
        )
    }
}

@Composable
private fun InstallmentFullCard(
    installment: InstallmentEntity,
    onPay: () -> Unit,
    onUnpay: () -> Unit,
    onOpen: () -> Unit,
) {
    val remaining = installment.amount - installment.paidAmount
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        installment.title ?: "قسط ${installment.installmentNumber}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "سررسید ${Formatters.toPersianDigits(installment.dueDate)} • ${Formatters.relativeJalali(installment.dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (installment.status == Constants.INSTALLMENT_OVERDUE) {
                            StatusColors.overdue
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                StatusChip(
                    Constants.installmentStatusLabel(installment.status),
                    installmentStatusColor(installment.status),
                )
            }
            SpacerH(8)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("مبلغ قسط: ${Formatters.amountWithUnit(installment.amount)}", style = MaterialTheme.typography.labelMedium)
                if (installment.paidAmount > 0 && remaining > 0) {
                    Text("پرداختی: ${Formatters.amountShort(installment.paidAmount)}", style = MaterialTheme.typography.labelSmall)
                }
            }
            SpacerH(8)
            if (installment.status == Constants.INSTALLMENT_PAID) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = StatusColors.paid)
                    Text(
                        " پرداخت‌شده در ${installment.paidDate?.let { Formatters.toPersianDigits(it) } ?: "—"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusColors.paid,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onUnpay) { Text("لغو") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPay, modifier = Modifier.weight(1f)) {
                        Text("ثبت پرداخت (${Formatters.amountShort(remaining)})")
                    }
                    OutlinedButton(onClick = onOpen) { Text("پیش‌فایل") }
                }
            }
        }
    }
}
