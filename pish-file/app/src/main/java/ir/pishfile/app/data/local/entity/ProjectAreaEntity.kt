package ir.pishfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * متراژ پروژه — برخی پروژه‌ها متراژهای مختلف دارند و هر متراژ
 * شرایط مالی مخصوص به خودش (واریزی، امتیاز، قیمت کل، اقساط) را دارد.
 */
@Entity(
    tableName = "project_areas",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class ProjectAreaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "projectId")
    val projectId: String,

    /** برچسب نمایشی (مثلاً «متراژ ۸۵») */
    val label: String,

    /** متراژ (مترمربع) */
    val areaValue: Double? = null,

    /** قیمت کل این متراژ */
    val totalPrice: Long? = null,

    /** واریزی تا امروز مخصوص این متراژ */
    val depositAmount: Long? = null,

    /** مبلغ امتیاز مخصوص این متراژ */
    val bonusAmount: Long? = null,

    /** تعداد اقساط مخصوص این متراژ */
    val installmentCount: Int? = null,

    /** مبلغ هر قسط مخصوص این متراژ */
    val installmentAmount: Long? = null,

    /** دوره‌ی پرداخت اقساط (ماهانه، فصلی و…) */
    val installmentPeriod: String? = null,

    /** ترتیب نمایش */
    val sortIndex: Int = 0,
)
