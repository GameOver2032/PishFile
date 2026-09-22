package ir.pishfile.app.ui.screens.dashboard

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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
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
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.navigation.Routes
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.DashboardViewModel

/**
 * داشبورد:
 * نمایش دقیق سه بخش اصلی طبق خواست کاربر:
 *  1) پیگیری‌های امروز
 *  2) آخرین فایل پیش‌فروش ثبت‌شده
 *  3) آخرین واحد آماده ثبت‌شده
 */
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val todayFollowUps by viewModel.todayFollowUps.collectAsStateWithLifecycle()
    val latestPreFile by viewModel.latestPreFile.collectAsStateWithLifecycle()
    val latestUnit by viewModel.latestAvailableUnit.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    ) {
        // --- ۱) پیگیری‌های امروز ---
        item {
            SectionCard(
                title = "پیگیری‌های امروز",
                subtitle = "${Formatters.number(todayFollowUps.size)} مورد در انتظار",
                trailing = {
                    IconButton(onClick = { onNavigate("${Routes.FOLLOWUPS}?new=1") }) {
                        Icon(Icons.Filled.Add, contentDescription = "پیگیری جدید", tint = MaterialTheme.colorScheme.primary)
                    }
                },
            ) {
                if (todayFollowUps.isEmpty()) {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Text(
                            "برای امروز پیگیری ثبت نشده است 👌",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayFollowUps.forEach { followUp ->
                            Card(
                                onClick = { onNavigate(Routes.FOLLOWUPS) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(followUp.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            listOfNotNull(followUp.dueTime, followUp.contactPhone)
                                                .joinToString(" • "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(onClick = { viewModel.completeFollowUp(followUp.id) }) {
                                        Icon(Icons.Filled.Check, contentDescription = "انجام شد", tint = StatusColors.paid)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ۲) آخرین فایل پیش‌فروش ---
        item {
            SectionCard(
                title = "آخرین فایل پیش‌فروش",
                subtitle = "سریع‌ترین دسترسی به تازه‌ترین فایل سپرده‌شده",
                trailing = {
                    IconButton(onClick = { onNavigate(Routes.preFileNew()) }) {
                        Icon(Icons.Filled.Add, contentDescription = "فایل پیش‌فروش جدید", tint = MaterialTheme.colorScheme.primary)
                    }
                },
            ) {
                val row = latestPreFile
                if (row == null) {
                    EmptyState(
                        title = "فایل پیش‌فروشی ثبت نشده",
                        subtitle = "برای ثبت اولین فایل، دکمه + را بزنید",
                        icon = { Icon(Icons.Filled.Description, contentDescription = null) },
                    )
                } else {
                    val pf = row.preFile
                    Card(
                        onClick = { onNavigate(Routes.preFile(pf.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${row.projectName ?: "پروژه"} • ${row.unitTitle ?: "بدون واحد"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        listOfNotNull(
                                            pf.ownerName?.let { "مالک: $it" },
                                            pf.ownerPhone,
                                            Constants.projectPricingModelLabel(pf.pricingModel)
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                StatusChip(
                                    Constants.preFileStatusLabel(pf.status),
                                    preFileStatusColor(pf.status),
                                )
                            }

                            SpacerH(8)

                            // نمایش مالی بر اساس مدل قیمت‌گذاری
                            when (pf.pricingModel) {
                                Constants.PRICING_DEPOSIT_BONUS -> {
                                    InfoRow("واریزی پروژه تا امروز", Formatters.amountWithUnit(pf.depositAmount))
                                    InfoRow("مبلغ امتیاز (سود)", Formatters.amountWithUnit(pf.bonusAmount))
                                    InfoRow("قیمت کل (واریزی + امتیاز)", Formatters.amountWithUnit(pf.displayPrice), emphasize = true)
                                }
                                Constants.PRICING_SHARE -> {
                                    InfoRow("متراژ هر سهم", pf.shareMeterArea?.let { "${Formatters.number(it.toInt())} م²" })
                                    InfoRow("تعداد سهم", Formatters.number(pf.shareCount))
                                    InfoRow("قیمت هر سهم", Formatters.amountWithUnit(pf.sharePrice))
                                    InfoRow("قیمت کل (مبلغ سهام)", Formatters.amountWithUnit(pf.displayPrice), emphasize = true)
                                }
                                else -> { // METER
                                    InfoRow("متراژ", pf.meterArea?.let { "${Formatters.number(it.toInt())} م²" })
                                    InfoRow("قیمت هر متر", Formatters.amountWithUnit(pf.pricePerMeter))
                                    InfoRow("قیمت کل", Formatters.amountWithUnit(pf.displayPrice), emphasize = true)
                                }
                            }

                            if (pf.hasRanking && !pf.ranking.isNullOrBlank()) {
                                InfoRow("رتبه فایل در پروژه", pf.ranking)
                            }

                            InfoRow("شرایط فروش", pf.saleConditionsSummary)

                            if (pf.installmentCount != null || pf.remainingInstallmentsCount != null) {
                                InfoRow(
                                    "اقساط",
                                    "تعداد: ${Formatters.number(pf.installmentCount)} | مانده: ${Formatters.number(pf.remainingInstallmentsCount)} قسط"
                                )
                            }
                            if (!pf.nextInstallmentDueDate.isNullOrBlank()) {
                                InfoRow("تاریخ سررسید قسط پیش‌رو", Formatters.toPersianDigits(pf.nextInstallmentDueDate))
                            }
                        }
                    }
                }
            }
        }

        // --- ۳) آخرین واحد آماده ثبت‌شده ---
        item {
            SectionCard(
                title = "آخرین واحد آماده ثبت‌شده",
                subtitle = "واحدهای آماده تحویل و قابل بازدید",
                trailing = {
                    IconButton(onClick = { onNavigate(Routes.unitNew()) }) {
                        Icon(Icons.Filled.Add, contentDescription = "واحد جدید", tint = MaterialTheme.colorScheme.primary)
                    }
                },
            ) {
                val unit = latestUnit
                if (unit == null) {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Text(
                            "هنوز واحد آماده‌ای ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                } else {
                    Card(
                        onClick = { onNavigate(Routes.unit(unit.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        unit.displayTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        listOfNotNull(
                                            unit.grossArea?.let { "${Formatters.number(it.toInt())} م²" },
                                            Constants.unitTypeLabel(unit.unitType),
                                            unit.direction,
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                StatusChip("آماده", StatusColors.available)
                            }
                            SpacerH(6)
                            InfoRow("قیمت هر متر", Formatters.amountWithUnit(unit.pricePerMeter))
                            InfoRow("قیمت کل", Formatters.amountWithUnit(unit.finalPrice ?: unit.totalPrice), emphasize = true)
                        }
                    }
                }
            }
        }
    }
}

fun preFileStatusColor(status: String): Color = when (status) {
    Constants.PREFILE_DRAFT -> StatusColors.draft
    Constants.PREFILE_URGENT -> StatusColors.overdue // قرمز / فوریت
    Constants.PREFILE_NORMAL -> StatusColors.available // سبز / عادی
    Constants.PREFILE_WITHDRAWN -> Color(0xFF757575) // خاکستری
    else -> StatusColors.draft
}

fun unitStatusColor(status: String): Color = when (status) {
    Constants.UNIT_AVAILABLE -> StatusColors.available
    Constants.UNIT_RESERVED -> StatusColors.reserved
    Constants.UNIT_SOLD -> StatusColors.sold
    Constants.UNIT_DELIVERED -> StatusColors.delivered
    else -> StatusColors.draft
}
