package ir.pishfile.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.BuildConfig
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.GameHeader
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
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val persianDigits by viewModel.persianDigits.collectAsStateWithLifecycle()
    val pending by viewModel.pending.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var restoreTarget by remember { mutableStateOf<RestoreTarget?>(null) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val target = restoreTarget
        restoreTarget = null
        if (uri == null || target == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
        }.getOrNull()
        if (text == null) return@rememberLauncherForActivityResult
        when (target) {
            RestoreTarget.FULL -> viewModel.importBackup(text)
            RestoreTarget.PROJECTS -> viewModel.importProjectsBackup(text)
            RestoreTarget.UNITS -> viewModel.importUnitsBackup(text)
        }
    }

    LaunchedEffect(Unit) { viewModel.refreshPending() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            GameHeader(
                title = "تنظیمات",
                emoji = "⚙️",
                subtitle = "مدیریت برنامه، پشتیبان‌گیری و ظاهر",
            )
        }
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
            SectionCard(title = "اطلاعات مشاور / دفتر املاک") {
                FormTextField(
                    value = officeName,
                    onValueChange = viewModel::setOfficeName,
                    label = "نام دفتر املاک یا آژانس",
                )
                SpacerH(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(
                        value = agentName,
                        onValueChange = viewModel::setAgentName,
                        label = "نام مشاور / کارشناس",
                        modifier = Modifier.weight(1f),
                    )
                    FormTextField(
                        value = agentPhone,
                        onValueChange = viewModel::setAgentPhone,
                        label = "شماره تماس مشاور",
                        modifier = Modifier.weight(1f),
                    )
                }
                SpacerH(8)
                FormTextField(
                    value = defaultCity,
                    onValueChange = viewModel::setDefaultCity,
                    label = "شهر پیش‌فرض",
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
            }
        }

        item {
            SectionCard(title = "پشتیبان‌گیری و خروجی") {
                Text(
                    "داده‌های شما به‌صورت آفلاین و محلی ذخیره می‌شوند. می‌توانید در هر زمان نسخه پشتیبان تهیه کنید یا به اکسل خروجی بگیرید.",
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
                    onClick = {
                        restoreTarget = RestoreTarget.FULL
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("بازیابی کامل از فایل بکاپ") }
                SpacerH(8)
                OutlinedButton(
                    onClick = {
                        viewModel.exportProjectsBackup { intent ->
                            context.startActivity(Intent.createChooser(intent, "خروجی پروژه‌ها"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("خروجی JSON پروژه‌ها (با متراژها)") }
                SpacerH(8)
                OutlinedButton(
                    onClick = {
                        restoreTarget = RestoreTarget.PROJECTS
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("بازیابی پروژه‌ها از JSON") }
                SpacerH(8)
                OutlinedButton(
                    onClick = {
                        viewModel.exportUnitsBackup { intent ->
                            context.startActivity(Intent.createChooser(intent, "خروجی واحدها"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("خروجی JSON واحدها") }
                SpacerH(8)
                OutlinedButton(
                    onClick = {
                        restoreTarget = RestoreTarget.UNITS
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("بازیابی واحدها از JSON") }
                SpacerH(8)
                OutlinedButton(
                    onClick = {
                        viewModel.exportPreFilesCsv { intent ->
                            context.startActivity(Intent.createChooser(intent, "خروجی فایل‌های پیش‌فروش"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("خروجی اکسل پیش‌فروش‌ها (CSV)") }
            }
        }

        item {
            SectionCard(title = "آمار محلی") {
                InfoRow("تعداد پروژه‌ها", Formatters.number(pending.projects))
                InfoRow("تعداد واحدها", Formatters.number(pending.units))
                InfoRow("تعداد فایل‌های پیش‌فروش", Formatters.number(pending.preFiles))
                InfoRow("پیگیری‌ها", Formatters.number(pending.followUps))
            }
        }

        item {
            SectionCard(title = "درباره برنامه") {
                InfoRow("نام", "پیش‌فایل (Pish File)")
                InfoRow("نسخه", Formatters.toPersianDigits(BuildConfig.VERSION_NAME))
                InfoRow("کاربرد", "مدیریت و بایگانی فایل‌های پیش‌فروش مسکونی، تجاری و سهامی جهت ارائه به مشتریان و متقاضیان خرید")
                SoftDivider()
                Text(
                    "سازگار با انواع مدل‌های فروش: واریزی و امتیاز، قیمت متری و سهامی.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** هدف بازیابی — برای تشخیص کدام بخش از فایل JSON بازیابی شود */
private enum class RestoreTarget { FULL, PROJECTS, UNITS }
