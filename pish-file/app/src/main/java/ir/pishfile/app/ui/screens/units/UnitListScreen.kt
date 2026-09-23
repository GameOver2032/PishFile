package ir.pishfile.app.ui.screens.units

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.ConfirmDialog
import ir.pishfile.app.ui.components.DropdownField
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.FilterChipsRow
import ir.pishfile.app.ui.components.GameHeader
import ir.pishfile.app.ui.components.GameStat
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.components.StatusChip
import ir.pishfile.app.ui.components.unitStatusColor
import ir.pishfile.app.ui.viewmodel.UnitListViewModel

/**
 * بخش واحدها — فهرست همه‌ی واحدها (به‌ویژه واحدهای آماده) از همه‌ی پروژه‌ها.
 */
@Composable
fun UnitListScreen(
    onOpen: (String) -> Unit,
    viewModel: UnitListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var projectFilter by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<UnitEntity?>(null) }

    val units by viewModel.units.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val projectById = projects.associateBy { it.id }

    val availableCount = units.count { it.status == Constants.UNIT_AVAILABLE }

    Column(Modifier.fillMaxSize()) {
        GameHeader(
            title = "واحدها",
            emoji = "🏠",
            subtitle = "واحدهای آماده و واحدهای همه‌ی پروژه‌ها",
            stats = listOf(
                GameStat(Formatters.number(units.size), "کل واحدها"),
                GameStat(Formatters.number(availableCount), "آماده"),
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        SearchField(
            query = query,
            onQueryChange = {
                query = it
                viewModel.setQuery(it)
            },
            placeholder = "جست‌وجوی واحد: شماره، پروژه، بلوک…",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        FilterChipsRow(
            options = Constants.unitStatuses.map { it to Constants.unitStatusLabel(it) },
            selectedKey = statusFilter,
            onSelect = {
                statusFilter = it
                viewModel.setStatusFilter(it)
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        if (projects.isNotEmpty()) {
            SpacerH(4)
            DropdownField(
                label = "پروژه",
                options = projects.map { it.name },
                selected = projectFilter?.let { projectById[it]?.name },
                onSelect = { name ->
                    val project = projects.firstOrNull { it.name == name }
                    projectFilter = project?.id
                    viewModel.setProjectFilter(project?.id)
                },
                allowEmpty = true,
                emptyLabel = "همه‌ی پروژه‌ها",
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        if (units.isEmpty()) {
            EmptyState(
                title = "واحدی ثبت نشده",
                subtitle = "با دکمه + یک واحد جدید بسازید یا از صفحه‌ی «سریع» شروع کنید",
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(units, key = { it.id }) { unit ->
                    UnitListItem(
                        unit = unit,
                        projectName = projectById[unit.projectId]?.name,
                        onClick = { onOpen(unit.id) },
                        onDelete = { pendingDelete = unit },
                    )
                }
            }
        }
    }

    pendingDelete?.let { unit ->
        ConfirmDialog(
            title = "حذف واحد",
            message = "«${unit.displayTitle}» حذف شود؟",
            onConfirm = { viewModel.delete(unit.id) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun UnitListItem(
    unit: UnitEntity,
    projectName: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
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
                            projectName,
                            unit.grossArea?.let { "${Formatters.number(it.toInt())} م²" },
                            unit.direction,
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            SpacerH(6)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "قیمت کل: ${Formatters.amountWithUnit(unit.finalPrice ?: unit.totalPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                )
                StatusChip(
                    Constants.unitStatusLabel(unit.status),
                    unitStatusColor(unit.status),
                )
            }
        }
    }
}
