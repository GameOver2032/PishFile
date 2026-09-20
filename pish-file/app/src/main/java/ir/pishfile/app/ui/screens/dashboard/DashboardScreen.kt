package ir.pishfile.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FinanceCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatCard
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.navigation.Routes
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.DashboardViewModel

/**
 * داشبورد — خلاصه‌ی وضعیت فروش در یک نگاه:
 * ارزش قراردادها، دریافتی‌ها، مانده‌ها، سررسیدهای نزدیک و کارهای امروز.
 */
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val summary by viewModel.financeSummary.collectAsStateWithLifecycle()
    val projectCount by viewModel.projectCount.collectAsStateWithLifecycle()
    val unitCount by viewModel.unitCount.collectAsStateWithLifecycle()
    val availableUnits by viewModel.availableUnitCount.collectAsStateWithLifecycle()
    val soldUnits by viewModel.soldUnitCount.collectAsStateWithLifecycle()
    val customerCount by viewModel.customerCount.collectAsStateWithLifecycle()
    val preFileCount by viewModel.preFileCount.collectAsStateWithLifecycle()
    val receivables by viewModel.totalReceivables.collectAsStateWithLifecycle()
    val overdueAmount by viewModel.overdueAmount.collectAsStateWithLifecycle()
    val received30 by viewModel.receivedLast30Days.collectAsStateWithLifecycle()
    val upcoming by viewModel.upcomingInstallments.collectAsStateWithLifecycle()
    val overdue by viewModel.overdueInstallments.collectAsStateWithLifecycle()
    val followUps by viewModel.pendingFollowUps.collectAsStateWithLifecycle()
    val latest by viewModel.latestPreFiles.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    ) {
        // --- خلاصه‌ی مالی ---
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FinanceCard(
                    title = "ارزش قراردادهای فعال",
                    amount = Formatters.amountWithUnit(summary?.totalContractValue),
                    subtitle = "${Formatters.number(summary?.contractCount ?: 0)} قرارداد",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.TrendingUp,
                    modifier = Modifier.weight(1f),
                )
                FinanceCard(
                    title = "مانده مطالبات",
                    amount = Formatters.amountWithUnit(receivables),
                    subtitle = "جمع اقساط تسویه‌نشده",
                    color = Color(0xFFB87E1E),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FinanceCard(
                    title = "دریافتی ۳۰ روز گذشته",
                    amount = Formatters.amountWithUnit(received30),
                    color = StatusColors.paid,
                    modifier = Modifier.weight(1f),
                )
                FinanceCard(
                    title = "اقساط معوق",
                    amount = Formatters.amountWithUnit(overdueAmount),
                    color = if ((overdueAmount ?: 0) > 0) StatusColors.overdue else MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = if ((overdueAmount ?: 0) > 0) Icons.Filled.Warning else null,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // --- دسترسی سریع ---
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    StatCard(
                        title = "پیش‌فایل جدید",
                        value = "＋",
                        icon = Icons.Filled.Description,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigate(Routes.preFileNew()) },
                        modifier = Modifier,
                    )
                }
                item {
                    StatCard("اقساط", Formatters.number(upcoming.size), Icons.Filled.Payments, Color(0xFFB87E1E)) {
                        onNavigate(Routes.INSTALLMENTS)
                    }
                }
                item {
                    StatCard("پیگیری‌ها", Formatters.number(followUps.size), Icons.Filled.EventNote, Color(0xFF8E24AA)) {
                        onNavigate(Routes.FOLLOWUPS)
                    }
                }
                item {
                    StatCard("تنظیمات", "⚙", Icons.Filled.Settings, MaterialTheme.colorScheme.onSurfaceVariant) {
                        onNavigate(Routes.SETTINGS)
                    }
                }
            }
        }

        // --- آمار کلی ---
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "پروژه‌ها", value = Formatters.number(projectCount), icon = Icons.Filled.Apartment,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.PROJECTS) },
                )
                StatCard(
                    title = "واحدها", value = Formatters.number(unitCount), icon = Icons.Filled.Apartment,
                    color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f),
                    subtitle = "آزاد: ${Formatters.number(availableUnits)} • فروخته: ${Formatters.number(soldUnits)}",
                    onClick = { onNavigate(Routes.UNITS) },
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "مشتری‌ها", value = Formatters.number(customerCount), icon = Icons.Filled.People,
                    color = Color(0xFF00695C), modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.CUSTOMERS) },
                )
                StatCard(
                    title = "پیش‌فایل‌ها", value = Formatters.number(preFileCount), icon = Icons.Filled.Description,
                    color = Color(0xFF1565C0), modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.PREFILES) },
                )
            }
        }

        // --- سررسیدهای پیش‌رو ---
        item {
            Text(
                "سررسیدهای ۳۰ روز آینده",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (upcoming.isEmpty() && overdue.isEmpty()) {
            item {
                EmptyState(
                    title = "سررسیدی نزدیک نیست",
                    subtitle = "با ثبت پیش‌فایل جدید، اقساط به‌صورت خودکار ساخته می‌شوند",
                )
            }
        } else {
            items(overdue) { installment ->
                InstallmentMiniRow(
                    title = "قسط ${Formatters.number(installment.installmentNumber)} — معوق",
                    amount = installment.amount - installment.paidAmount,
                    dueDate = installment.dueDate,
                    color = StatusColors.overdue,
                    onClick = { onNavigate(Routes.preFile(installment.preFileId)) },
                )
            }
            items(upcoming) { installment ->
                InstallmentMiniRow(
                    title = "قسط ${Formatters.number(installment.installmentNumber)}",
                    amount = installment.amount - installment.paidAmount,
                    dueDate = installment.dueDate,
                    color = StatusColors.reserved,
                    onClick = { onNavigate(Routes.preFile(installment.preFileId)) },
                )
            }
        }

        // --- پیگیری‌های امروز ---
        item {
            Text(
                "پیگیری‌های امروز",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (followUps.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Text(
                        "پیگیری معوق یا امروزی ندارید 👌",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
        } else {
            items(followUps.take(5)) { followUp ->
                Card(
                    onClick = { onNavigate(Routes.FOLLOWUPS) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(followUp.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${followUp.dueDate ?: "بدون تاریخ"} • ${followUp.dueTime ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusChip("انجام شد؟", StatusColors.pending)
                    }
                }
            }
        }

        // --- آخرین پیش‌فایل‌ها ---
        item {
            Text(
                "آخرین پیش‌فایل‌ها",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        items(latest) { row ->
            Card(
                onClick = { onNavigate(Routes.preFile(row.preFile.id)) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${row.preFile.draftNumber} • ${row.customerName ?: "بدون مشتری"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "${row.projectName ?: ""} ${row.unitTitle ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${Formatters.amountShort(row.preFile.effectivePrice)} تومان",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        SpacerH(2)
                        StatusChip(
                            Constants.preFileStatusLabel(row.preFile.status),
                            preFileStatusColor(row.preFile.status),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallmentMiniRow(
    title: String,
    amount: Long,
    dueDate: String,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${Formatters.toPersianDigits(dueDate)} • ${Formatters.relativeJalali(dueDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
            }
            Text(
                "${Formatters.amountShort(amount)} تومان",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

fun preFileStatusColor(status: String): Color = when (status) {
    Constants.PREFILE_DRAFT -> StatusColors.draft
    Constants.PREFILE_RESERVED -> StatusColors.reserved
    Constants.PREFILE_PENDING_PAYMENT -> StatusColors.pending
    Constants.PREFILE_CONFIRMED -> StatusColors.confirmed
    Constants.PREFILE_COMPLETED -> StatusColors.delivered
    Constants.PREFILE_CANCELED -> StatusColors.canceled
    else -> StatusColors.draft
}

fun unitStatusColor(status: String): Color = when (status) {
    Constants.UNIT_AVAILABLE -> StatusColors.available
    Constants.UNIT_RESERVED -> StatusColors.reserved
    Constants.UNIT_SOLD -> StatusColors.sold
    Constants.UNIT_DELIVERED -> StatusColors.delivered
    else -> StatusColors.draft
}

fun installmentStatusColor(status: String): Color = when (status) {
    Constants.INSTALLMENT_PAID -> StatusColors.paid
    Constants.INSTALLMENT_PARTIAL -> StatusColors.partial
    Constants.INSTALLMENT_OVERDUE -> StatusColors.overdue
    else -> StatusColors.pending
}
