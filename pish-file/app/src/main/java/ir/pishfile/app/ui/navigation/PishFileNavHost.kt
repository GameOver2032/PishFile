package ir.pishfile.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.pishfile.app.ui.screens.dashboard.DashboardScreen
import ir.pishfile.app.ui.screens.followups.FollowUpsScreen
import ir.pishfile.app.ui.screens.prefiles.PreFileDetailScreen
import ir.pishfile.app.ui.screens.prefiles.PreFileEditScreen
import ir.pishfile.app.ui.screens.prefiles.PreFileListScreen
import ir.pishfile.app.ui.screens.prefiles.PreFileWizardScreen
import ir.pishfile.app.ui.screens.projects.ProjectDetailScreen
import ir.pishfile.app.ui.screens.projects.ProjectEditScreen
import ir.pishfile.app.ui.screens.projects.ProjectListScreen
import ir.pishfile.app.ui.screens.settings.SettingsScreen
import ir.pishfile.app.ui.screens.units.UnitDetailScreen
import ir.pishfile.app.ui.screens.units.UnitEditScreen
import ir.pishfile.app.ui.screens.units.UnitListScreen

/** مسیرهای برنامه */
object Routes {
    const val DASHBOARD = "dashboard"
    const val PREFILES = "prefiles"
    const val PREFILE_NEW = "prefiles/new?projectId={projectId}&unitId={unitId}"
    const val PREFILE_DETAIL = "prefile/{preFileId}"
    const val PREFILE_EDIT = "prefile/{preFileId}/edit"

    const val UNITS = "units"
    const val UNIT_NEW = "units/new?projectId={projectId}"
    const val UNIT_DETAIL = "unit/{unitId}"
    const val UNIT_EDIT = "unit/{unitId}/edit"

    const val PROJECTS = "projects"
    const val PROJECT_NEW = "projects/new"
    const val PROJECT_DETAIL = "project/{projectId}"
    const val PROJECT_EDIT = "project/{projectId}/edit"

    const val FOLLOWUPS = "followups"
    const val SETTINGS = "settings"

    fun project(id: String) = "project/$id"
    fun projectEdit(id: String) = "project/$id/edit"
    fun unit(id: String) = "unit/$id"
    fun unitEdit(id: String) = "unit/$id/edit"
    fun unitNew(projectId: String? = null) = "units/new?projectId=${projectId ?: ""}"
    fun preFile(id: String) = "prefile/$id"
    fun preFileEdit(id: String) = "prefile/$id/edit"
    fun preFileNew(projectId: String? = null, unitId: String? = null) =
        "prefiles/new?projectId=${projectId ?: ""}&unitId=${unitId ?: ""}"
}

private data class TopLevelDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

/** تب‌های پایینی: پیش‌فروش، واحدها، پروژه‌ها، داشبورد */
private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.DASHBOARD, "داشبورد", Icons.Filled.Dashboard),
    TopLevelDestination(Routes.PREFILES, "پیش‌فروش", Icons.Filled.Description),
    TopLevelDestination(Routes.UNITS, "واحدها", Icons.Filled.Apartment),
    TopLevelDestination(Routes.PROJECTS, "پروژه‌ها", Icons.Filled.Apartment),
)

private fun titleFor(route: String?): String = when {
    route == null -> "پیش‌فایل"
    route.startsWith(Routes.DASHBOARD) -> "پیش‌فایل"
    route.startsWith(Routes.PROJECTS) -> "پروژه‌ها"
    route.startsWith("project/") -> "پروژه"
    route.startsWith("unit/") -> "واحد"
    route.startsWith(Routes.UNITS) -> "واحدها"
    route.startsWith(Routes.PREFILES) -> "پیش‌فروش"
    route.startsWith("prefile/") -> "فایل پیش‌فروش"
    route.startsWith(Routes.FOLLOWUPS) -> "پیگیری‌ها"
    route.startsWith(Routes.SETTINGS) -> "تنظیمات"
    else -> "پیش‌فایل"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleFor(currentRoute), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.FOLLOWUPS) }) {
                        Icon(Icons.Filled.EventNote, contentDescription = "پیگیری‌ها")
                    }
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(Routes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.title) },
                            label = { Text(destination.title, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val fab: (@Composable () -> Unit)? = when (currentRoute) {
                Routes.PREFILES -> {
                    { AddFab("پیش‌فروش جدید") { navController.navigate(Routes.preFileNew()) } }
                }
                Routes.UNITS -> {
                    { AddFab("واحد جدید") { navController.navigate(Routes.unitNew()) } }
                }
                Routes.PROJECTS -> {
                    { AddFab("پروژه جدید") { navController.navigate(Routes.PROJECT_NEW) } }
                }
                Routes.FOLLOWUPS -> {
                    { AddFab("پیگیری جدید") { navController.navigate("${Routes.FOLLOWUPS}?new=1") } }
                }
                else -> null
            }
            fab?.invoke()
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigate = { route -> navController.navigate(route) },
                )
            }

            // ---------- پروژه‌ها ----------
            composable(Routes.PROJECTS) {
                ProjectListScreen(
                    onOpen = { id -> navController.navigate(Routes.project(id)) },
                )
            }
            composable(Routes.PROJECT_NEW) {
                ProjectEditScreen(
                    projectId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { id -> navController.navigate(Routes.project(id)) },
                )
            }
            composable(
                Routes.PROJECT_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            ) { entry ->
                val projectId = entry.arguments?.getString("projectId").orEmpty()
                ProjectDetailScreen(
                    projectId = projectId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.projectEdit(projectId)) },
                    onAddUnit = { navController.navigate(Routes.unitNew(projectId)) },
                    onOpenUnit = { id -> navController.navigate(Routes.unit(id)) },
                    onOpenPreFile = { id -> navController.navigate(Routes.preFile(id)) },
                )
            }
            composable(
                Routes.PROJECT_EDIT,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            ) { entry ->
                ProjectEditScreen(
                    projectId = entry.arguments?.getString("projectId"),
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            // ---------- واحدها ----------
            composable(Routes.UNITS) {
                UnitListScreen(
                    onOpen = { id -> navController.navigate(Routes.unit(id)) },
                )
            }
            composable(
                Routes.UNIT_NEW,
                arguments = listOf(navArgument("projectId") {
                    type = NavType.StringType; defaultValue = ""
                }),
            ) { entry ->
                UnitEditScreen(
                    unitId = null,
                    projectId = entry.arguments?.getString("projectId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSaved = { id -> navController.navigate(Routes.unit(id)) },
                )
            }
            composable(
                Routes.UNIT_DETAIL,
                arguments = listOf(navArgument("unitId") { type = NavType.StringType }),
            ) { entry ->
                val unitId = entry.arguments?.getString("unitId").orEmpty()
                UnitDetailScreen(
                    unitId = unitId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.unitEdit(unitId)) },
                    onNewPreFile = { navController.navigate(Routes.preFileNew(unitId = unitId)) },
                    onOpenPreFile = { id -> navController.navigate(Routes.preFile(id)) },
                )
            }
            composable(
                Routes.UNIT_EDIT,
                arguments = listOf(navArgument("unitId") { type = NavType.StringType }),
            ) { entry ->
                UnitEditScreen(
                    unitId = entry.arguments?.getString("unitId"),
                    projectId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            // ---------- فایل‌های پیش‌فروش ----------
            composable(Routes.PREFILES) {
                PreFileListScreen(onOpen = { id -> navController.navigate(Routes.preFile(id)) })
            }
            composable(
                Routes.PREFILE_NEW,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("unitId") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                PreFileWizardScreen(
                    initialProjectId = entry.arguments?.getString("projectId").orEmpty(),
                    initialUnitId = entry.arguments?.getString("unitId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSaved = { id -> navController.navigate(Routes.preFile(id)) },
                    onOpenProjects = { navController.navigate(Routes.PROJECTS) },
                )
            }
            composable(
                Routes.PREFILE_DETAIL,
                arguments = listOf(navArgument("preFileId") { type = NavType.StringType }),
            ) { entry ->
                val preFileId = entry.arguments?.getString("preFileId").orEmpty()
                PreFileDetailScreen(
                    preFileId = preFileId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.preFileEdit(preFileId)) },
                    onOpenUnit = { id -> navController.navigate(Routes.unit(id)) },
                )
            }
            composable(
                Routes.PREFILE_EDIT,
                arguments = listOf(navArgument("preFileId") { type = NavType.StringType }),
            ) { entry ->
                PreFileEditScreen(
                    preFileId = entry.arguments?.getString("preFileId"),
                    initialProjectId = "",
                    initialUnitId = "",
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            // ---------- پیگیری‌ها و تنظیمات ----------
            composable(
                "followups?new={new}",
                arguments = listOf(navArgument("new") { type = NavType.StringType; defaultValue = "" }),
            ) { entry ->
                FollowUpsScreen(
                    openNewOnStart = entry.arguments?.getString("new") == "1",
                    onOpenPreFile = { id -> navController.navigate(Routes.preFile(id)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun AddFab(label: String, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(Icons.Filled.Add, contentDescription = label)
    }
}
