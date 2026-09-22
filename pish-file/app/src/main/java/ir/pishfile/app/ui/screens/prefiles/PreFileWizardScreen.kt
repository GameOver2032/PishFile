package ir.pishfile.app.ui.screens.prefiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.core.Constants
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FormTextField
import ir.pishfile.app.ui.components.InfoRow
import ir.pishfile.app.ui.components.JalaliDateField
import ir.pishfile.app.ui.components.MoneyField
import ir.pishfile.app.ui.components.NumberField
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.SpacerW
import ir.pishfile.app.ui.viewmodel.PreFileForm
import ir.pishfile.app.ui.viewmodel.PreFileWizardStep
import ir.pishfile.app.ui.viewmodel.PreFileWizardViewModel

/**
 * ثبت سریع فایل پیش‌فروش — قدم‌به‌قدم.
 *
 * پروژه یک‌بار کامل در بخش «پروژه‌ها» تعریف می‌شود (مدل قیمت‌گذاری، واریزی تا امروز،
 * امتیاز، رتبه، شرایط فروش، اقساط…) و در این صفحه برنامه فقط فیلدهای خالی را
 * می‌پرسد. «قیمت کل پیشنهادی مالک» (= واریزی + امتیاز) همیشه به‌عنوان یک فیلد
 * مشخص نشان داده و به‌روز می‌شود.
 */
@Composable
fun PreFileWizardScreen(
    initialProjectId: String,
    initialUnitId: String,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onOpenProjects: () -> Unit,
    viewModel: PreFileWizardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.startNew(
        initialProjectId.takeIf { it.isNotBlank() },
        initialUnitId.takeIf { it.isNotBlank() },
    )

    if (!viewModel.isLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("در حال آماده‌سازی…", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    if (viewModel.projects.isEmpty()) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyState(
                title = "اول یک پروژه کامل بسازید",
                subtitle = "برای ثبت سریع فایل، ابتدا پروژه را با همه‌ی شرایطش (واریزی، امتیاز، رتبه، شرایط فروش، اقساط) اضافه کنید. بعد از آن، ثبت هر فایل فقط چند سؤال کوتاه است.",
                icon = { Icon(Icons.Filled.Apartment, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )
            SpacerH(8)
            Button(onClick = onOpenProjects, modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("رفتن به پروژه‌ها")
            }
        }
        return
    }

    val form = viewModel.form
    val steps = viewModel.steps(viewModel.selectedProject())
    val step = viewModel.currentStep()
    val idx = steps.indexOf(step)
    val isLast = idx == steps.size - 1
    val optional = viewModel.isOptionalStep(step)
    val project = viewModel.selectedProject()
    val unit = viewModel.availableUnits.firstOrNull { it.id == form.unitId }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // --- نوار خلاصه: شماره فایل + پروژه/واحد + قیمت کل زنده ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        form.draftNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listOfNotNull(
                            project?.name,
                            unit?.displayTitle ?: "بدون واحد مشخص",
                            form.ownerName.takeIf { it.isNotBlank() }?.let { "مالک: $it" },
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (form.computedTotal > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "قیمت کل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            Formatters.amountWithUnit(form.computedTotal),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        // --- هدر قدم ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "قدم ${Formatters.toPersianDigits((idx + 1).toString())} از ${Formatters.toPersianDigits(steps.size.toString())}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            SpacerW(8)
            Text(step.title, style = MaterialTheme.typography.titleSmall)
        }
        LinearProgressIndicator(
            progress = { (idx + 1) / steps.size.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        SpacerH(4)

        // --- کارت قدم جاری ---
        SectionCard(
            title = step.title,
            subtitle = stepSubtitle(step, project),
        ) {
            when (step) {
                PreFileWizardStep.PROJECT -> WizardStepProject(viewModel, form)
                PreFileWizardStep.OWNER -> WizardStepOwner(viewModel, form)
                PreFileWizardStep.PRICE -> WizardStepPrice(viewModel, form)
                PreFileWizardStep.RANK -> WizardStepRank(viewModel, form)
                PreFileWizardStep.SALE -> WizardStepSale(viewModel, form)
                PreFileWizardStep.INSTALLMENT -> WizardStepInstallment(viewModel, form)
                PreFileWizardStep.STATUS -> WizardStepStatus(viewModel, form)
            }
        }

        viewModel.stepError?.let { error ->
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            SpacerH(2)
        }

        // --- دکمه‌های حرکت بین قدم‌ها ---
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (idx > 0) {
                TextButton(onClick = { viewModel.back() }) { Text("قبلی") }
            }
            if (optional) {
                OutlinedButton(
                    onClick = {
                        if (isLast) viewModel.next { id -> onSaved(id) }
                        else viewModel.goTo(idx + 1)
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(if (isLast) "ثبت" else "رد کردن") }
            }
            Button(
                onClick = { viewModel.next { id -> onSaved(id) } },
                modifier = Modifier.weight(if (optional) 1f else 2f),
            ) { Text(if (isLast) "ثبت فایل پیش‌فروش" else "بعدی") }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("انصراف") }
    }
}

/** توضیح کوتاه هر قدم — چه چیزی از پروژه آمده و چه چیزی باید وارد شود */
private fun stepSubtitle(step: PreFileWizardStep, project: ProjectEntity?): String? = when (step) {
    PreFileWizardStep.PROJECT ->
        "با انتخاب پروژه، شرایط اختصاصی آن به‌صورت خودکار پر می‌شود"
    PreFileWizardStep.OWNER ->
        "فقط اطلاعات مالک این فایل مورد نیاز است"
    PreFileWizardStep.PRICE ->
        when (project?.pricingModel) {
            Constants.PRICING_DEPOSIT_BONUS -> "واریزی از پروژه آمده است؛ فقط امتیاز (یا قیمت کل) را وارد کنید"
            Constants.PRICING_SHARE -> "قیمت هر سهم از پروژه آمده است؛ فقط تعداد سهم را مشخص کنید"
            else -> "متراژ و قیمت هر متر از پروژه/واحد آمده است؛ در صورت نیاز اصلاح کنید"
        }
    PreFileWizardStep.RANK ->
        "این پروژه دارای رتبه است؛ رتبه این مالک را بنویسید"
    PreFileWizardStep.SALE ->
        "با پیش‌فرض‌های پروژه پر شده؛ در صورت تفاوت تغییر دهید"
    PreFileWizardStep.INSTALLMENT ->
        "اقساط از پروژه آمده است؛ اگر برای این مالک متفاوت است تغییر دهید"
    PreFileWizardStep.STATUS ->
        "وضعیت را انتخاب کنید؛ بقیه با پیش‌فرض‌های پروژه است"
}

// ---------------------------------------------------------------------------
// قدم ۱: پروژه و واحد
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepProject(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    val projects = viewModel.projects
    val units = viewModel.availableUnits
    DropdownField(
        label = "پروژه *",
        options = projects.map { it.name },
        selected = projects.firstOrNull { it.id == form.projectId }?.name,
        onSelect = { name ->
            projects.firstOrNull { it.name == name }?.let { viewModel.selectProject(it.id) }
        },
    )
    SpacerH(8)
    DropdownField(
        label = "واحد (اختیاری)",
        options = units.map { it.displayTitle },
        selected = units.firstOrNull { it.id == form.unitId }?.displayTitle,
        onSelect = { title ->
            units.firstOrNull { it.displayTitle == title }?.let { viewModel.selectUnit(it.id) }
        },
        emptyLabel = "بدون واحد مشخص",
        allowEmpty = true,
    )
    SpacerH(8)
    InfoRow("شماره فایل (خودکار)", form.draftNumber)
    InfoRow("تاریخ ثبت", Formatters.toPersianDigits(form.draftDate))
}

// ---------------------------------------------------------------------------
// قدم ۲: مالک / سپارنده فایل
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepOwner(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FormTextField(
            value = form.ownerName,
            onValueChange = { v -> viewModel.update { it.copy(ownerName = v) } },
            label = "نام مالک / سپارنده *",
            modifier = Modifier.weight(1f),
        )
        FormTextField(
            value = form.ownerPhone,
            onValueChange = { v -> viewModel.update { it.copy(ownerPhone = v) } },
            label = "شماره تماس",
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---------------------------------------------------------------------------
// قدم ۳: قیمت فایل — قیمت کل همیشه به‌عنوان فیلد مشخص
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepPrice(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    when (form.pricingModel) {
        Constants.PRICING_DEPOSIT_BONUS -> {
            MoneyField(
                value = form.depositAmount,
                onValueChange = viewModel::onDepositChange,
                label = "واریزی تا امروز",
                helperText = "مقدار پروژه؛ در صورت به‌روزرسانی تغییر دهید",
            )
            SpacerH(8)
            MoneyField(
                value = form.bonusAmount,
                onValueChange = viewModel::onBonusChange,
                label = "مبلغ امتیاز (سود پروژه) *",
                helperText = "فقط این فیلد (یا قیمت کل) را وارد کنید",
            )
            SpacerH(8)
            TotalPriceCard(form.computedTotal, "واریزی + امتیاز")
            SpacerH(8)
            MoneyField(
                value = form.totalPrice.ifBlank {
                    if (form.computedTotal > 0) Formatters.amount(form.computedTotal) else ""
                },
                onValueChange = viewModel::onTotalChange,
                label = "قیمت کل پیشنهادی مالک *",
                helperText = "به‌صورت خودکار = واریزی + امتیاز؛ اگر دستی تغییر دهید، امتیاز تنظیم می‌شود",
            )
        }

        Constants.PRICING_SHARE -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    value = form.shareMeterArea,
                    onValueChange = { v -> viewModel.update { it.copy(shareMeterArea = v) } },
                    label = "متراژ هر سهم",
                    suffix = "م²",
                    decimal = true,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = form.shareCount,
                    onValueChange = { v -> viewModel.update { it.copy(shareCount = v) } },
                    label = "تعداد سهم *",
                    modifier = Modifier.weight(1f),
                )
            }
            SpacerH(8)
            MoneyField(
                value = form.sharePrice,
                onValueChange = { v -> viewModel.update { it.copy(sharePrice = v) } },
                label = "قیمت هر سهم",
                helperText = "از پروژه آمده؛ در صورت نیاز تغییر دهید",
            )
            SpacerH(8)
            TotalPriceCard(form.computedTotal, "تعداد سهم × قیمت هر سهم")
        }

        else -> { // METER
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    value = form.meterArea,
                    onValueChange = { v -> viewModel.update { it.copy(meterArea = v) } },
                    label = "متراژ واحد",
                    suffix = "م²",
                    decimal = true,
                    modifier = Modifier.weight(1f),
                )
                MoneyField(
                    value = form.pricePerMeter,
                    onValueChange = { v -> viewModel.update { it.copy(pricePerMeter = v) } },
                    label = "قیمت هر متر مربع",
                    modifier = Modifier.weight(1.4f),
                )
            }
            SpacerH(8)
            TotalPriceCard(form.computedTotal, "متراژ × قیمت هر متر")
            SpacerH(8)
            MoneyField(
                value = form.totalPrice.ifBlank {
                    if (form.computedTotal > 0) Formatters.amount(form.computedTotal) else ""
                },
                onValueChange = { v -> viewModel.update { it.copy(totalPrice = v) } },
                label = "قیمت کل فایل *",
                helperText = "در صورت خالی ماندن، از متراژ × قیمت هر متر محاسبه می‌شود",
            )
        }
    }
}

/** کارت برجسته‌ی «قیمت کل پیشنهادی مالک» */
@Composable
private fun TotalPriceCard(total: Long, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "قیمت کل پیشنهادی مالک",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                if (total > 0) Formatters.amountWithUnit(total) else "—",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// قدم ۴: رتبه در پروژه (فقط اگر پروژه رتبه‌بندی داشته باشد)
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepRank(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    FormTextField(
        value = form.ranking,
        onValueChange = { v -> viewModel.update { it.copy(ranking = v) } },
        label = "رتبه فایل در پروژه (مثلاً: رتبه ۱۲، اولویت الف) *",
    )
}

// ---------------------------------------------------------------------------
// قدم ۵: شرایط فروش (پیش‌فرض از پروژه)
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepSale(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    Text("نوع پرداخت و تسویه:", style = MaterialTheme.typography.bodySmall)
    SpacerH(4)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = form.saleConditionCash,
                onCheckedChange = { v -> viewModel.update { it.copy(saleConditionCash = v) } },
            )
            Text("نقد", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = form.saleConditionInstallment,
                onCheckedChange = { v -> viewModel.update { it.copy(saleConditionInstallment = v) } },
            )
            Text("شرایطی", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = form.saleConditionExchange,
                onCheckedChange = { v -> viewModel.update { it.copy(saleConditionExchange = v) } },
            )
            Text("تهاتر", style = MaterialTheme.typography.bodyMedium)
        }
    }
    SpacerH(8)
    FormTextField(
        value = form.saleConditionNotes,
        onValueChange = { v -> viewModel.update { it.copy(saleConditionNotes = v) } },
        label = "توضیحات شرایط فروش (مثلاً نوع خودرو جهت تهاتر)",
        singleLine = false,
        minLines = 2,
    )
}

// ---------------------------------------------------------------------------
// قدم ۶: اقساط (پیش‌فرض از پروژه)
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepInstallment(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField(
            value = form.installmentCount,
            onValueChange = { v -> viewModel.update { it.copy(installmentCount = v) } },
            label = "تعداد کل اقساط",
            modifier = Modifier.weight(1f),
        )
        NumberField(
            value = form.remainingInstallmentsCount,
            onValueChange = { v -> viewModel.update { it.copy(remainingInstallmentsCount = v) } },
            label = "اقساط باقی‌مانده",
            modifier = Modifier.weight(1f),
        )
    }
    SpacerH(8)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MoneyField(
            value = form.installmentAmount,
            onValueChange = { v -> viewModel.update { it.copy(installmentAmount = v) } },
            label = "مبلغ هر قسط",
            modifier = Modifier.weight(1.2f),
        )
        DropdownField(
            label = "دوره پرداخت",
            options = listOf("ماهانه", "دو ماهه", "سه ماهه / فصلی", "شش ماهه", "سالانه"),
            selected = form.installmentPeriod,
            onSelect = { v -> viewModel.update { it.copy(installmentPeriod = v) } },
            modifier = Modifier.weight(1f),
        )
    }
    SpacerH(8)
    JalaliDateField(
        value = form.nextInstallmentDueDate,
        onValueChange = { v -> viewModel.update { it.copy(nextInstallmentDueDate = v) } },
        label = "تاریخ سررسید قسط پیش‌رو",
        quickMonths = listOf(1, 2, 3),
    )
}

// ---------------------------------------------------------------------------
// قدم ۷: وضعیت و تحویل
// ---------------------------------------------------------------------------
@Composable
private fun WizardStepStatus(viewModel: PreFileWizardViewModel, form: PreFileForm) {
    DropdownField(
        label = "وضعیت پیش‌فروش",
        options = Constants.preFileStatuses.map { Constants.preFileStatusLabel(it) },
        selected = Constants.preFileStatusLabel(form.status),
        onSelect = { label ->
            val code = Constants.preFileStatuses.firstOrNull { Constants.preFileStatusLabel(it) == label } ?: form.status
            viewModel.update { it.copy(status = code) }
        },
    )
    SpacerH(8)
    JalaliDateField(
        value = form.deliveryDate,
        onValueChange = { v -> viewModel.update { it.copy(deliveryDate = v) } },
        label = "تاریخ تقریبی تحویل واحد",
        quickMonths = listOf(6, 12),
    )
    SpacerH(8)
    FormTextField(
        value = form.notes,
        onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
        label = "توضیحات و یادداشت تکمیلی",
        singleLine = false,
        minLines = 2,
    )
}
