package ir.pishfile.app.ui.screens.fast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.pishfile.app.ui.components.GameButton
import ir.pishfile.app.ui.components.GameHeader
import ir.pishfile.app.ui.components.SpacerH
import androidx.compose.ui.graphics.Color

/**
 * انتخاب نوع فایل جدید — اولین قدم پس از زدن «فایل جدید» در صفحه‌ی سریع:
 *  - واحد آماده: فایل برای واحدی که آماده تحویل است
 *  - پیش‌فروش: فایل برای واحد در حال ساخت / پروژه
 */
@Composable
fun FileChoiceScreen(
    onReadyUnit: () -> Unit,
    onPresale: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SpacerH(8)
        GameHeader(
            title = "فایل جدید",
            emoji = "📄",
            subtitle = "چه نوع فایلی ثبت می‌کنید؟",
        )
        SpacerH(10)
        GameButton(
            emoji = "🏠",
            title = "واحد آماده",
            subtitle = "فایل فروش برای واحد آماده — مستقیم فرم ثبت را پر می‌کنید",
            gradient = listOf(Color(0xFF1B5E20), Color(0xFF43A047)),
            onClick = onReadyUnit,
        )
        SpacerH(10)
        GameButton(
            emoji = "🏗️",
            title = "پیش‌فروش",
            subtitle = "فایل پیش‌فروش — ابتدا پروژه را انتخاب می‌کنید",
            gradient = listOf(Color(0xFF283593), Color(0xFF5C6BC0)),
            onClick = onPresale,
        )
        SpacerH(16)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}
