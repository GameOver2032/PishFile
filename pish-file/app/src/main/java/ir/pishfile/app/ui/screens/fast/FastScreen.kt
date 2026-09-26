package ir.pishfile.app.ui.screens.fast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import ir.pishfile.app.core.Constants
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.FollowUpEntity
import ir.pishfile.app.data.local.entity.NoteEntity
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.components.GameButton
import ir.pishfile.app.ui.components.NoteDialog
import ir.pishfile.app.ui.components.NoteTimelineCard
import ir.pishfile.app.ui.components.NotificationPermissionHint
import ir.pishfile.app.ui.components.SectionCard
import ir.pishfile.app.ui.components.SpacerH
import ir.pishfile.app.ui.theme.StatusColors
import ir.pishfile.app.ui.viewmodel.FastViewModel

/**
 * صفحه‌ی «سریع» — خانه‌ی برنامه با طراحی بازی‌گونه:
 * دکمه‌های بزرگ «فایل جدید» و «مشتری جدید»، آمار، اقدامات سریع و پیگیری‌های امروز.
 */
@Composable
fun FastScreen(
    onNewFile: () -> Unit,
    onNewCustomer: () -> Unit,
    onNewFollowUp: () -> Unit,
    onOpenFollowUps: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenPreFile: (String) -> Unit,
    onOpenCustomer: (String) -> Unit,
    viewModel: FastViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val preFileCount by viewModel.preFileCount.collectAsStateWithLifecycle()
    val customerCount by viewModel.customerCount.collectAsStateWithLifecycle()
    val todayFollowUps by viewModel.todayFollowUps.collectAsStateWithLifecycle()
    val recentNotes by viewModel.recentNotes.collectAsStateWithLifecycle()
    val preFileRows by viewModel.preFileRows.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val fileByPreFileId = preFileRows.associateBy { it.preFile.id }
    val customerById = customers.associateBy { it.id }

    var showNoteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
    ) {
        // --- سلام و تاریخ امروز ---
        item {
            Column {
                Text(
                    "سلام! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "امروز ${Formatters.epochToJalaliLong(System.currentTimeMillis())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // --- آمار (HUD) ---
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameStatCard(
                    count = preFileCount,
                    label = "فایل",
                    icon = Icons.Filled.Description,
                    color = StatusColors.available,
                    modifier = Modifier.weight(1f),
                )
                GameStatCard(
                    count = customerCount,
                    label = "مشتری",
                    icon = Icons.Filled.Person,
                    color = StatusColors.reserved,
                    modifier = Modifier.weight(1f),
                )
                GameStatCard(
                    count = todayFollowUps.size,
                    label = "پیگیری امروز",
                    icon = Icons.Filled.EventNote,
                    color = StatusColors.overdue,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // --- دکمه‌های بزرگ بازی‌گونه ---
        item {
            GameButton(
                emoji = "📄",
                title = "فایل جدید",
                subtitle = "ثبت سریع فایل جدید (واحد آماده یا پیش‌فروش)",
                gradient = listOf(Color(0xFF1B5E20), Color(0xFF43A047)),
                onClick = onNewFile,
            )
        }
        item {
            GameButton(
                emoji = "👥",
                title = "مشتری جدید",
                subtitle = "ثبت مشتری / خریدار برای یک فایل",
                gradient = listOf(Color(0xFF283593), Color(0xFF5C6BC0)),
                onClick = onNewCustomer,
            )
        }

        // --- اقدامات سریع ---
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip("✍️ ثبت مکالمه", onClick = { showNoteDialog = true }, modifier = Modifier.weight(1f))
                QuickActionChip("📌 پیگیری جدید", onClick = onNewFollowUp, modifier = Modifier.weight(1f))
            }
        }

        // --- پیگیری‌های امروز ---
        item {
            SectionCard(
                title = "پیگیری‌های امروز",
                subtitle = "${Formatters.number(todayFollowUps.size)} مورد در انتظار",
                trailing = {
                    OutlinedButton(onClick = onOpenFollowUps) { Text("همه") }
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
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayFollowUps.forEach { followUp ->
                            FastFollowUpRow(
                                followUp = followUp,
                                onDone = { viewModel.completeFollowUp(followUp.id) },
                                onOpen = onOpenFollowUps,
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "آخرین مکالمات",
                trailing = {
                    OutlinedButton(onClick = onOpenNotes) { Text("همه") }
                },
            ) {
                if (recentNotes.isEmpty()) {
                    Text(
                        "مکالمه‌ای ثبت نشده — نتیجه‌ی تماس با مشتری را با «ثبت مکالمه» بنویسید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentNotes.forEach { note ->
                            FastNoteRow(
                                note = note,
                                contextLine = listOfNotNull(
                                    note.preFileId?.let { fileByPreFileId[it] }?.let {
                                        "${it.preFile.draftNumber} — ${it.projectName ?: ""}"
                                    },
                                    note.customerId?.let { customerById[it] }?.let { "مشتری: ${it.name}" },
                                ).joinToString(" • ").ifBlank { null },
                                onOpen = note.preFileId?.let { id -> ({ onOpenPreFile(id) }) }
                                    ?: note.customerId?.let { id -> ({ onOpenCustomer(id) }) },
                            )
                        }
                    }
                }
            }
        }

        item {
            NotificationPermissionHint()
        }


        item {
            Text(
                "💡 برای شروع، یکی از دکمه‌های بزرگ را بزنید؛ فقط فیلدهای خالی از شما پرسیده می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showNoteDialog) {
        NoteDialog(
            preFiles = preFileRows.map { "${it.preFile.draftNumber} — ${it.projectName ?: ""}" },
            preFileIds = preFileRows.map { it.preFile.id },
            customers = customers.map { it.name },
            customerIds = customers.map { it.id },
            onDismiss = { showNoteDialog = false },
            onSave = { note ->
                viewModel.saveNote(note) { showNoteDialog = false }
            },
        )
    }
}

/** ردیف فشرده‌ی مکالمه برای صفحه‌ی سریع */
@Composable
private fun FastNoteRow(
    note: NoteEntity,
    contextLine: String?,
    onOpen: (() -> Unit)?,
) {
    Card(
        onClick = { onOpen?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Formatters.toPersianDigits(note.noteDate),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "  ${Constants.noteTypeLabel(note.type)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                if (!contextLine.isNullOrBlank()) {
                    Text(
                        contextLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            SpacerH(4)
            Text(
                note.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            if (!note.outcome.isNullOrBlank()) {
                Text(
                    "نتیجه: ${note.outcome}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** کارت آمار کوچک (سبک HUD بازی) */
@Composable
private fun GameStatCard(
    count: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            SpacerH(4)
            Text(
                Formatters.number(count),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color,
            )
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** چیپ اقدام سریع */
@Composable
private fun QuickActionChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/** ردیف پیگیری روز با دکمه‌ی «انجام شد» */
@Composable
private fun FastFollowUpRow(
    followUp: FollowUpEntity,
    onDone: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    followUp.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    listOfNotNull(followUp.dueTime?.let { "🔔 $it" }, followUp.contactPhone)
                        .joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDone) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "انجام شد",
                    tint = StatusColors.paid,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}
