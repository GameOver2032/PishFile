package ir.pishfile.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * پیوست (فایل، عکس یا ویدیو) متصل به یک فایل پیش‌فروش یا یک واحد آماده.
 *
 * URI در این جدول ذخیره می‌شود و مجوز خوانش پایدار (persistable) آن
 * هنگام افزودن گرفته می‌شود.
 */
@Entity(
    tableName = "attachments",
    indices = [Index("ownerType"), Index("ownerId"), Index(value = ["ownerType", "ownerId"])]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** PREFILE یا UNIT */
    val ownerType: String,

    val ownerId: String,

    /** نام نمایشی فایل */
    val displayName: String,

    /** نوع MIME (image/*، video/*، application/pdf و…) */
    val mimeType: String,

    /** content:// URI فایل */
    val uri: String,

    val sizeBytes: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isVideo: Boolean get() = mimeType.startsWith("video/")
}
