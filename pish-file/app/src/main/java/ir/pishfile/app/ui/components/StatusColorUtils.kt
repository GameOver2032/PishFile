package ir.pishfile.app.ui.components

import androidx.compose.ui.graphics.Color
import ir.pishfile.app.core.Constants
import ir.pishfile.app.ui.theme.StatusColors

/** رنگ وضعیت فایل پیش‌فروش */
fun preFileStatusColor(status: String): Color = when (status) {
    Constants.PREFILE_DRAFT -> StatusColors.draft
    Constants.PREFILE_URGENT -> StatusColors.overdue // قرمز / فوریت
    Constants.PREFILE_NORMAL -> StatusColors.available // سبز / عادی
    Constants.PREFILE_WITHDRAWN -> Color(0xFF757575) // خاکستری
    else -> StatusColors.draft
}

/** رنگ وضعیت واحد */
fun unitStatusColor(status: String): Color = when (status) {
    Constants.UNIT_AVAILABLE -> StatusColors.available
    Constants.UNIT_RESERVED -> StatusColors.reserved
    Constants.UNIT_SOLD -> StatusColors.sold
    Constants.UNIT_DELIVERED -> StatusColors.delivered
    else -> StatusColors.draft
}
