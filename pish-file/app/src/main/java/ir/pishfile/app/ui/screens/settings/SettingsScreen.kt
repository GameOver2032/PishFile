package ir.pishfile.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SoftDivider
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    val officeName by viewModel.officeName.collectAsStateWithLifecycle()
    val agentName by viewModel.agentName.collectAsStateWithLifecycle()
    val agentPhone by viewModel.agentPhone.collectAsStateWithLifecycle()
    val defaultCity by viewModel.defaultCity.collectAsStateWithLifecycle()
    val defaultInstallmentCount by viewModel.defaultInstallmentCount.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val persianDigits by viewModel.persianDigits.collectAsStateWithLifecycle()
    val autoGenerateInst by viewModel.autoGenerateInstallments.collectAsStateWithLifecycle()
    val lastSyncAt by viewModel.lastSyncAt.collectAsStateWithLifecycle()
    val pending by viewModel.pending.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    LaunchedEffect(Unit) { viewModel.refreshPending() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        message?.let { text ->
            item {
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        androidx.compose.material3.TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("بستن")
                        }
                    },
                ) { Text(text) }
            }
        }

        item {
            SectionCard(title = "اطلاعات دفتر / شعبه") {
                FormTextField(
                    value = officeName,
                    onValueChange = viewModel::setOfficeName,
                    label = "نام دفتر یا شرکت",
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = agentName,
                        onValueChange = viewModel::setAgentName,
                        label = "نام کارشناس فروش",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = agentPhone,
                        onValueChange = viewModel::setAgentPhone,
                        label = "تلفن کارشناس",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                FormTextField(
                    value = defaultCity,
                    onValueChange = viewModel::setDefaultCity,
                    label = "شهر پیش‌فرض",
                )
                SpacerH(8)
                FormTextField(
                    value = defaultInstallmentCount,
                    onValueChange = viewModel::setDefaultInstallmentCount,
                    label = "تعداد پیش‌فرض اقساط",
                )
            }
        }

        item {
            SectionCard(title = "ظاهر برنامه") {
                Text("حالت نمایش", style = MaterialTheme.typography.bodySmall)
                SpacerH(6)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = themeMode == "system",
                        onClick = { viewModel.setThemeMode("system") },
                        label = { Text("سیستم") },
                    )
                    FilterChip(
                        selected = themeMode == "light",
                        onClick = { viewModel.setThemeMode("light") },
                        label = { Text("روشن") },
                    )
                    FilterChip(
                        selected = themeMode == "dark",
                        onClick = { viewModel.setThemeMode("dark") },
                        label = { Text("تاریک") },
                    )
                }
                SpacerH(8)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = persianDigits, onCheckedChange = viewModel::setPersianDigits)
                    Text("نمایش اعداد به فارسی (۱۲۳)", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = autoGenerateInst,
                        onCheckedChange = viewModel::setAutoGenerateInstallments,
                    )
                    Text("ساخت خودکار جدول اقساط", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            SectionCard(title = "پشتیبان‌گیری و خروجی") {
                Text(
                    "داده‌ها کاملاً روی همین گوشی ذخیره می‌شوند. برای اطمینان، هر چند وقت یک‌بار بکاپ بگیرید و در جای امن (ایمیل، تلگرام، درایو) نگه دارید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SpacerH(10)
                Button(
                    onClick = {
                        viewModel.exportBackup { intent ->
                            context.startActivity(Intent.createChooser(intent, "ذخیره بکاپ"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("پشتیبان‌گیری کامل (JSON)") }
                SpacerH(8)
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("بازیابی از فایل بکاپ") }
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.exportPreFilesCsv { intent ->
                                context.startActivity(Intent.createChooser(intent, "خروجی پیش‌فایل‌ها"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("خروجی اکسل پیش‌فایل‌ها") }
                    OutlinedButton(
                        onClick = {
                            viewModel.exportInstallmentsCsv { intent ->
                                context.startActivity(Intent.createChooser(intent, "خروجی اقساط"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("خروجی اکسل اقساط") }
                }
            }
        }

        item {
            SectionCard(title = "همگام‌سازی (آماده برای آینده)") {
                Text(
                    "این نسخه کاملاً آفلاین کار می‌کند. هر تغییری که می‌دهید با برچسب «منتظر ارسال» ذخیره می‌شود تا وقتی سرور اضافه شد، همه‌چیز بدون از دست رفتن داده همگام شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SpacerH(10)
                InfoRow("پروژه‌ها", Formatters.number(pending.projects))
                InfoRow("واحدها", Formatters.number(pending.units))
                InfoRow("مشتری‌ها", Formatters.number(pending.customers))
                InfoRow("پیش‌فایل‌ها", Formatters.number(pending.preFiles))
                InfoRow("اقساط", Formatters.number(pending.installments))
                InfoRow("پیگیری‌ها", Formatters.number(pending.followUps))
                SoftDivider()
                InfoRow("جمع تغییرات معلق", Formatters.number(pending.total), emphasize = true)
                InfoRow("آخرین همگام‌سازی", lastSyncAt.ifBlank { "—" })
                SpacerH(10)
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth(0.15f))
                } else {
                    OutlinedButton(
                        onClick = { viewModel.syncNow() },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("تلاش برای همگام‌سازی") }
                }
            }
        }

        item {
            SectionCard(title = "درباره برنامه") {
                InfoRow("نام", "پیش‌فایل (Pish File)")
                InfoRow("نسخه", "۰.۱.۰")
                InfoRow("کارکرد", "ساماندهی پروژه‌ها، واحدها، مشتریان و پیش‌فایل‌های پیش‌فروش املاک")
                SoftDivider()
                Text(
                    "برای گزارش خطا یا درخواست قابلیت جدید، یک Issue در مخزن گیت‌هاب پروژه ثبت کنید.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
