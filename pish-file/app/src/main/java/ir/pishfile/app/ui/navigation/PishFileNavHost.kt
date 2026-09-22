package ir.pishfile.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
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
import ir.pishfile.app.ui.screens.customers.CustomerDetailScreen
import ir.pishfile.app.ui.screens.customers.CustomerEditScreen
import ir.pishfile.app.ui.screens.customers.CustomerListScreen
import ir.pishfile.app.ui.screens.fast.FastScreen
import ir.pishfile.app.ui.screens.fast.QuickPickMode
import ir.pishfile.app.ui.screens.fast.QuickPickScreen
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

/** مسیرهای برنامه */
object Routes {
    const val FAST = "fast"

    const val PREFILES = "prefiles"
    const val PREFILE_NEW = "prefiles/new?projectId={projectId}&unitId={unitId}"
    const val PREFILE_DETAIL = "prefile/{preFileId}"
    const val PREFILE_EDIT = "prefile/{preFileId}/edit"

    /** «از کجا شروع کنیم؟» — انتخاب واحد/فایل قبل از ثبت */
    const val QUICK_PICK = "quickpick?mode={mode}&allowSkip={allowSkip}"

    const val CUSTOMERS = "customers"
    const val CUSTOMER_NEW = "customers/new?unitId={unitId}&preFileId={preFileId}"
    const val CUSTOMER_DETAIL = "customer/{customerId}"
    const val CUSTOMER_EDIT = "customer/{customerId}/edit"

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
    fun quickPick(mode: String = "file", allowSkip: Boolean = false) =
        "quickpick?mode=$mode&allowSkip=$allowSkip"
    fun customerNew(unitId: String? = null, preFileId: String? = null) =
        "customers/new?unitId=${unitId ?: ""}&preFileId=${preFileId ?: ""}"
    fun customer(id: String) = "customer/$id"
    fun customerEdit(id: String) = "customer/$id/edit"
}

private data class TopLevelDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

/** تب‌های پایینی: سریع (خانه)، فایل‌ها، پروژه‌ها، مشتری‌ها، پیگیری‌ها */
private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.FAST, "سریع", Icons.Filled.Bolt),
    TopLevelDestination(Routes.PREFILES, "فایل‌ها", Icons.Filled.Description),
    TopLevelDestination(Routes.PROJECTS, "پروژه‌ها", Icons.Filled.Apartment),
    TopLevelDestination(Routes.CUSTOMERS, "مشتری‌ها", Icons.Filled.Person),
    TopLevelDestination(Routes.FOLLOWUPS, "پیگیری‌ها", Icons.Filled.EventNote),
)

private fun titleFor(route: String?): String = when {
    route == null -> "پیش‌فایل"
    route.startsWith(Routes.FAST) -> "پیش‌فایل"
    route.startsWith(Routes.QUICK_PICK) -> "از کجا شروع کنیم؟"
    route.startsWith(Routes.CUSTOMERS) -> "مشتری‌ها"
    route.startsWith("customer/") -> "مشتری"
    route.startsWith(Routes.PROJECTS) -> "پروژه‌ها"
    route.startsWith("project/") -> "پروژه"
    route.startsWith("unit/") -> "واحد"
    route.startsWith(Routes.PREFILES) -> "فایل‌ها"
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
                                        popUpTo(Routes.FAST) { saveState = true }
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
                    { AddFab("فایل جدید") { navController.navigate(Routes.quickPick("file")) } }
                }
                Routes.CUSTOMERS -> {
                    { AddFab("مشتری جدید") { navController.navigate(Routes.quickPick("customer", allowSkip = true)) } }
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
            startDestination = Routes.FAST,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ---------- صفحه‌ی سریع (خانه) ----------
            composable(Routes.FAST) {
                FastScreen(
                    onNewFile = { navController.navigate(Routes.quickPick("file")) },
                    onNewCustomer = { navController.navigate(Routes.quickPick("customer", allowSkip = true)) },
                    onNewFollowUp = { navController.navigate("${Routes.FOLLOWUPS}?new=1") },
                    onOpenFollowUps = { navController.navigate(Routes.FOLLOWUPS) },
                    onOpenNotes = { navController.navigate("followups?notes=1") },
                    onOpenPreFile = { id -> navController.navigate(Routes.preFile(id)) },
                    onOpenCustomer = { id -> navController.navigate(Routes.customer(id)) },
                )
            }

            // ---------- انتخاب واحد/فایل قبل از ثبت ----------
            composable(
                Routes.QUICK_PICK,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType; defaultValue = "file" },
                    navArgument("allowSkip") { type = NavType.StringType; defaultValue = "false" },
                ),
            ) { entry ->
                val mode = if (entry.arguments?.getString("mode") == "customer") {
                    QuickPickMode.CUSTOMER
                } else {
                    QuickPickMode.FILE
                }
                val allowSkip = entry.arguments?.getString("allowSkip") == "true"
                QuickPickScreen(
                    mode = mode,
                    allowSkip = allowSkip,
                    onSkip = { navController.navigate(Routes.customerNew()) },
                    onPickUnit = { unitId, projectId ->
                        if (mode == QuickPickMode.FILE) {
                            navController.navigate(Routes.preFileNew(projectId = projectId, unitId = unitId))
                        } else {
                            navController.navigate(Routes.customerNew(unitId = unitId))
                        }
                    },
                    onPickPreFile = { preFileId ->
                        navController.navigate(Routes.customerNew(preFileId = preFileId))
                    },
                    onBack = { navController.popBackStack() },
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
                    onOpenCustomer = { id -> navController.navigate(Routes.customer(id)) },
                    onNewCustomer = { navController.navigate(Routes.customerNew()) },
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

            // ---------- مشتری‌ها ----------
            composable(Routes.CUSTOMERS) {
                CustomerListScreen(
                    onOpen = { id -> navController.navigate(Routes.customer(id)) },
                )
            }
            composable(
                Routes.CUSTOMER_NEW,
                arguments = listOf(
                    navArgument("unitId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("preFileId") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                CustomerEditScreen(
                    customerId = null,
                    initialUnitId = entry.arguments?.getString("unitId").orEmpty(),
                    initialPreFileId = entry.arguments?.getString("preFileId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSaved = { id -> navController.navigate(Routes.customer(id)) },
                )
            }
            composable(
                Routes.CUSTOMER_DETAIL,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType }),
            ) { entry ->
                val customerId = entry.arguments?.getString("customerId").orEmpty()
                CustomerDetailScreen(
                    customerId = customerId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.customerEdit(customerId)) },
                    onOpenPreFile = { id -> navController.navigate(Routes.preFile(id)) },
                )
            }
            composable(
                Routes.CUSTOMER_EDIT,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType }),
            ) { entry ->
                CustomerEditScreen(
                    customerId = entry.arguments?.getString("customerId"),
                    initialUnitId = "",
                    initialPreFileId = "",
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            // ---------- واحدها (از داخل پروژه‌ها و فایل‌ها) ----------
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

            // ---------- پیگیری‌ها و تنظیمات ----------
            composable(
                "followups?new={new}&notes={notes}",
                arguments = listOf(
                    navArgument("new") { type = NavType.StringType; defaultValue = "" },
                    navArgument("notes") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                FollowUpsScreen(
                    openNewOnStart = entry.arguments?.getString("new") == "1",
                    onOpenPreFile = { id -> navController.navigate(Routes.preFile(id)) },
                    onOpenCustomer = { id -> navController.navigate(Routes.customer(id)) },
                    initialMode = if (entry.arguments?.getString("notes") == "1") "NOTES" else "FOLLOWUPS",
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
