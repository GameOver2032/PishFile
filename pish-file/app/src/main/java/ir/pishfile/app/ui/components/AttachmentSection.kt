package ir.pishfile.app.ui.components

import android.content.Context
import android.content.Intent
import android.media.MediaController
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import ir.pishfile.app.core.Formatters
import ir.pishfile.app.data.local.entity.AttachmentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * بخش پیوست‌ها (فایل، عکس و ویدیو) — مشترک برای فایل‌های پیش‌فروش و واحدهای آماده.
 *
 * افزودن از طریق انتخابگر فایل سیستم؛ عکس و ویدیو داخل برنامه پیش‌نمایش
 * می‌شوند و بقیه‌ی فایل‌ها با اپلیکیشن مناسب سیستم باز می‌شوند.
 */
@Composable
fun AttachmentSection(
    ownerType: String,
    ownerId: String,
    attachments: List<AttachmentEntity>,
    onAdd: (AttachmentEntity) -> Unit,
    onDelete: (AttachmentEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var preview by remember { mutableStateOf<AttachmentEntity?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            val (name, size, mime) = resolveAttachment(context, uri)
            onAdd(
                AttachmentEntity(
                    ownerType = ownerType,
                    ownerId = ownerId,
                    displayName = name,
                    mimeType = mime,
                    uri = uri.toString(),
                    sizeBytes = size,
                )
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (attachments.isEmpty()) {
            Text(
                "هنوز فایل، عکس یا ویدیویی به این مورد اضافه نشده است",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            attachments.forEach { attachment ->
                AttachmentRow(
                    attachment = attachment,
                    onOpen = { preview = attachment },
                    onDelete = { onDelete(attachment) },
                )
            }
        }
        OutlinedButton(onClick = { pickLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("📎 افزودن فایل / عکس / ویدیو")
        }
    }

    preview?.let { attachment ->
        AttachmentPreviewDialog(
            attachment = attachment,
            onDismiss = { preview = null },
        )
    }
}

/** نام، اندازه و نوع MIME یک Uri + گرفتن مجوز خوانش پایدار */
private fun resolveAttachment(context: Context, uri: Uri): Triple<String, Long?, String> {
    val resolver = context.contentResolver
    var name = uri.lastPathSegment ?: "فایل"
    var size: Long? = null
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex >= 0) name = cursor.getString(nameIndex)
            if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
        }
    }
    val mime = resolver.getType(uri) ?: "application/octet-stream"
    try {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: SecurityException) {
        // برخی منابع مجوز پایدار نمی‌دهند؛ در آن حالت URI به‌همین‌صورت ذخیره می‌شود
    }
    return Triple(name, size, mime)
}

@Composable
private fun AttachmentRow(
    attachment: AttachmentEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                when {
                    attachment.isImage -> Icons.Filled.Image
                    attachment.isVideo -> Icons.Filled.Videocam
                    else -> Icons.Filled.FileOpen
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp),
            )
            SpacerW(10)
            Column(Modifier.weight(1f)) {
                Text(
                    attachment.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    listOfNotNull(
                        when {
                            attachment.isImage -> "عکس"
                            attachment.isVideo -> "ویدیو"
                            else -> "فایل"
                        },
                        attachment.sizeBytes?.let { Formatters.bytesToSize(it) },
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AttachmentPreviewDialog(
    attachment: AttachmentEntity,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                attachment.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        if (attachment.isVideo) {
                            VideoView(context).apply {
                                setMediaController(MediaController(context, this))
                                setVideoURI(Uri.parse(attachment.uri))
                                setOnPreparedListener { it.start() }
                            }
                        } else {
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                setImageURI(Uri.parse(attachment.uri))
                            }
                        }
                    },
                    onRelease = { view ->
                        if (view is VideoView) view.release()
                    },
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } },
    )
}
