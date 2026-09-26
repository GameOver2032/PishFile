package ir.pishfile.app.ui.screens.projects

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
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.EmptyState
import ir.pishfile.app.ui.components.GameHeader
import ir.pishfile.app.ui.components.SearchField
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.viewmodel.ProjectListViewModel

/**
 * انتخاب پروژه هنگام ثبت «فایل پیش‌فروش»:
 * فایل برای کدام پروژه است؟ — پروژه‌ها همان پروژه‌های تب «پروژه‌ها» هستند.
 */
@Composable
fun ProjectPickScreen(
    onPickProject: (projectId: String) -> Unit,
    onNewProject: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProjectListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        GameHeader(
            title = "فایل پیش‌فروش",
            emoji = "📄",
            subtitle = "فایل برای کدام پروژه است؟",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )

        SearchField(
            query = query,
            onQueryChange = { viewModel.setQuery(it) },
            placeholder = "جست‌وجوی پروژه…",
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (projects.isEmpty()) {
                item {
                    EmptyState(
                        title = "پروژه‌ای ثبت نشده است",
                        subtitle = "اول در تب «پروژه‌ها» کلیات پروژه را ثبت کنید",
                        icon = { Icon(Icons.Filled.Apartment, contentDescription = null) },
                    )
                }
                item {
                    OutlinedButton(onClick = onNewProject, modifier = Modifier.fillMaxWidth()) {
                        Text("ثبت پروژه جدید")
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    ProjectPickRow(project, onClick = { onPickProject(project.id) })
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
private fun ProjectPickRow(
    project: ProjectEntity,
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
                    project.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    listOfNotNull(
                        project.city?.takeIf { it.isNotBlank() },
                        Constants.projectPhaseLabel(project.phase),
                        Constants.projectPricingModelLabel(project.pricingModel),
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.Apartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
