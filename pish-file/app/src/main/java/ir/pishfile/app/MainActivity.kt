package ir.pishfile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pishfile.app.ui.AppViewModelProvider
import ir.pishfile.app.ui.navigation.MainScreen
import ir.pishfile.app.ui.theme.PishFileTheme
import ir.pishfile.app.ui.viewmodel.SettingsViewModel

/**
 * تنها Activity برنامه — همه‌ی صفحه‌ها با Compose و Navigation ساخته می‌شوند.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val themeMode by settingsViewModel.themeMode.collectAsState()

            PishFileTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}
