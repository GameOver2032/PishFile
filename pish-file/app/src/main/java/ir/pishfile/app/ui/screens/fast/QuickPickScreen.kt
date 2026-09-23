package ir.pishfile.app.ui.screens.fast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.GameHeader
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SectionTitle
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.components.preFileStatusColor
import ir.pishfile.app.ui.components.unitStatusColor
import ir.pishfile.app.ui.viewmodel.QuickPickViewModel

/** حالت انتخاب: فایل واحد آماده، فایل پیش‌فروش، یا ثبت مشتری */
enum class QuickPickMode { READY_UNIT, PRESALE, CUSTOMER }

/**
 * «از کجا شروع کنیم؟» — اولین قدم فرایند ثبت:
 * انتخاب واحد آماده یا واحد پیش‌فروشی (و برای مشتری، فایل پیش‌فروش).
 */
@Composable
fun QuickPickScreen(
    mode: QuickPickMode,
    allowSkip: Boolean,
    onSkip: () -> Unit,
    onPickUnit: (unitId: String, projectId: String) -> Unit,
    onPickPreFile: (preFileId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: QuickPickViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val units by viewModel.units.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val preFileRows by viewModel.preFileRows.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    val projectById = projects.associateBy { it.id }
    val q = query.trim()

    val statusAllowed = when (mode) {
        QuickPickMode.READY_UNIT -> { unit: UnitEntity -> unit.status == Constants.UNIT_AVAILABLE }
        else -> { _: UnitEntity -> true }
    }
    val visibleUnits = (if (q.isBlank()) units else units.filter {
        it.displayTitle.contains(q, true) || projectById[it.projectId]?.name?.contains(q, true) == true
    })
        .filter { statusAllowed(it) }
        .sortedWith(compareBy({ it.status != Constants.UNIT_AVAILABLE }, { it.id }))

    val visiblePreFiles = if (mode != QuickPickMode.CUSTOMER) emptyList()
    else (
        if (q.isBlank()) preFileRows
        else preFileRows.filter {
            it.preFile.draftNumber.contains(q, true) ||
                (it.preFile.ownerName ?: "").contains(q, true) ||
                (it.projectName ?: "").contains(q, true)
        }
    )

    val hasAny = visibleUnits.isNotEmpty() || visiblePreFiles.isNotEmpty()

    Column(Modifier.fillMaxSize()) {
        GameHeader(
            title = when (mode) {
                QuickPickMode.READY_UNIT -> "فایل واحد آماده"
                QuickPickMode.PRESALE -> "فایل پیش‌فروش"
                QuickPickMode.CUSTOMER -> "مشتری جدید"
            },
            emoji = when (mode) {
                QuickPickMode.READY_UNIT -> "🏠"
                QuickPickMode.PRESALE -> "📄"
                QuickPickMode.CUSTOMER -> "👤"
            },
            subtitle = when (mode) {
                QuickPickMode.READY_UNIT -> "یک واحد آماده انتخاب کن — شرایط پروژه‌اش خودکار پر می‌شود"
                QuickPickMode.PRESALE -> "اول واحد را انتخاب کن — شرایط پروژه‌اش خودکار پر می‌شود"
                QuickPickMode.CUSTOMER -> "مشتری برای کدام واحد یا فایل پیش‌فروش است؟"
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )

        SearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = "جست‌وجوی واحد یا پروژه…",
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (allowSkip) {
                item {
                    OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                        Text("ادامه بدون انتخاب (بعداً وصل می‌کنیم)")
                    }
                }
            }

            val available = visibleUnits.filter { it.status == Constants.UNIT_AVAILABLE }
            val reserved = visibleUnits.filter { it.status == Constants.UNIT_RESERVED }

            if (available.isNotEmpty()) {
                item { SectionTitle("واحدهای آماده") }
                items(available, key = { it.id }) { unit ->
                    UnitPickRow(unit, projectById[unit.projectId]?.name, onClick = { onPickUnit(unit.id, unit.projectId) })
                }
            }
            if (reserved.isNotEmpty() && mode != QuickPickMode.READY_UNIT) {
                item { SectionTitle("واحدهای پیش‌فروشی") }
                items(reserved, key = { it.id }) { unit ->
                    UnitPickRow(unit, projectById[unit.projectId]?.name, onClick = { onPickUnit(unit.id, unit.projectId) })
                }
            }
            if (visiblePreFiles.isNotEmpty()) {
                item { SectionTitle("فایل‌های پیش‌فروش") }
                items(visiblePreFiles, key = { it.preFile.id }) { row ->
                    PreFilePickRow(row, onClick = { onPickPreFile(row.preFile.id) })
                }
            }

            if (!hasAny) {
                item {
                    EmptyState(
                        title = "چیزی پیدا نشد",
                        subtitle = "واحد یا پروژه‌ای با این مشخصات ثبت نشده است",
                        icon = { Icon(Icons.Filled.Apartment, contentDescription = null) },
                    )
                }
            }

            item {
                SpacerH(4)
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
            }
        }
    }
}

@Composable
private fun UnitPickRow(
    unit: UnitEntity,
    projectName: String?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
                    "${projectName.orEmpty()} • ${unit.displayTitle}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    listOfNotNull(
                        unit.grossArea?.let { "${Formatters.number(it.toInt())} م²" },
                        unit.pricePerMeter?.let { "متری ${Formatters.amountShort(it)}" },
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(
                Constants.unitStatusLabel(unit.status),
                unitStatusColor(unit.status),
            )
        }
    }
}

@Composable
private fun PreFilePickRow(row: PreFileRow, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
                    "${row.preFile.draftNumber} • ${row.projectName.orEmpty()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    listOfNotNull(row.unitTitle, row.preFile.ownerName?.let { "مالک: $it" })
                        .joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(
                Constants.preFileStatusLabel(row.preFile.status),
                preFileStatusColor(row.preFile.status),
            )
        }
    }
}
