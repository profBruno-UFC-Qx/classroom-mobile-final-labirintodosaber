package com.labirintodosaber.ui.navigation

import android.net.Uri
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.labirintodosaber.ui.components.AppMenuDrawer
import com.labirintodosaber.ui.components.ProfileMenuBottomSheet
import com.labirintodosaber.ui.screen.activities.ActivitiesScreen
import com.labirintodosaber.ui.screen.activityanswer.ActivityAnswerScreen
import com.labirintodosaber.ui.screen.addstudent.AddStudentScreen
import com.labirintodosaber.ui.screen.anamnese.AnamneseScreen
import com.labirintodosaber.ui.screen.createactivity.CreateActivityScreen
import com.labirintodosaber.ui.screen.createnotebook.CreateNotebookScreen
import com.labirintodosaber.ui.screen.createtaskgroup.CreateTaskGroupScreen
import com.labirintodosaber.ui.screen.dashboard.DashboardScreen
import com.labirintodosaber.ui.screen.forgotpassword.ForgotPasswordScreen
import com.labirintodosaber.ui.screen.groupdetail.GroupDetailScreen
import com.labirintodosaber.ui.screen.login.LoginScreen
import com.labirintodosaber.ui.screen.notebookdetail.NotebookDetailScreen
import com.labirintodosaber.ui.screen.privacy.PrivacyScreen
import com.labirintodosaber.ui.screen.register.RegisterScreen
import com.labirintodosaber.ui.screen.reportpdf.ReportPdfScreen
import com.labirintodosaber.ui.screen.reports.ReportsScreen
import com.labirintodosaber.ui.screen.sessionconfigure.SessionConfigureScreen
import com.labirintodosaber.ui.screen.sessionreport.SessionReportScreen
import com.labirintodosaber.ui.screen.sessionrun.SessionRunScreen
import com.labirintodosaber.ui.screen.sessionselectstudent.SessionSelectStudentScreen
import com.labirintodosaber.ui.screen.studentprofile.StudentProfileScreen
import com.labirintodosaber.ui.screen.students.StudentsScreen
import com.labirintodosaber.ui.screen.support.SupportScreen
import com.labirintodosaber.ui.screen.userprofile.UserProfileScreen
import com.labirintodosaber.ui.screen.userprofile.UserProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showProfileSheet by rememberSaveable { mutableStateOf(false) }
    val profileViewModel: UserProfileViewModel = hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestination.Login.route

    // Routes where the drawer / profile sheet are accessible
    val mainRoutes = setOf(
        AppDestination.Dashboard.route,
        AppDestination.Activities.route,
        AppDestination.Students.route,
        AppDestination.Reports.route,
    )
    val drawerEnabled = currentRoute in mainRoutes

    // Carrega o educador quando chega numa tela principal já autenticado (o init do VM roda antes do login).
    LaunchedEffect(currentRoute) {
        if (currentRoute in mainRoutes && profileUiState.displayName.isBlank()) {
            profileViewModel.refresh()
        }
    }

    val onMenuClick: () -> Unit = {
        if (drawerEnabled) scope.launch { drawerState.open() }
    }
    val onProfileClick: () -> Unit = { showProfileSheet = true }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerEnabled && !showProfileSheet,
        drawerContent = {
            AppMenuDrawer(
                currentRoute = currentRoute,
                userName = profileUiState.displayName,
                userRole = profileUiState.role,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onClose = { scope.launch { drawerState.close() } },
            )
        },
        modifier = modifier,
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestination.Login.route,
        ) {
            composable(AppDestination.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(AppDestination.Register.route) },
                    onForgotPasswordClick = { navController.navigate(AppDestination.ForgotPassword.route) },
                )
            }
            composable(AppDestination.Register.route) {
                RegisterScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginClick = { navController.popBackStack() },
                )
            }
            composable(AppDestination.ForgotPassword.route) {
                ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
            }
            composable(AppDestination.Dashboard.route) {
                DashboardScreen(
                    onStudentsClick = { navController.navigate(AppDestination.Students.route) },
                    onActivitiesClick = { navController.navigate(AppDestination.Activities.route) },
                    onReportsClick = { navController.navigate(AppDestination.Reports.route) },
                    onStartSessionClick = { navController.navigate(AppDestination.SessionSelectStudent.route) },
                    onMenuClick = onMenuClick,
                    onProfileClick = onProfileClick,
                )
            }
            composable(AppDestination.SessionSelectStudent.route) {
                SessionSelectStudentScreen(
                    onBack = { navController.popBackStack() },
                    onNextStep = { studentId ->
                        navController.navigate(AppDestination.SessionConfigure.createRoute(studentId))
                    },
                )
            }
            composable(
                route = AppDestination.SessionConfigure.route,
                arguments = listOf(navArgument("studentId") { type = NavType.StringType }),
            ) {
                SessionConfigureScreen(
                    onBack = { navController.popBackStack() },
                    onStartSession = { studentId, contentIds, sessionName ->
                        navController.navigate(
                            AppDestination.SessionRun.createRoute(studentId, contentIds, sessionName)
                        )
                    },
                )
            }
            composable(
                route = AppDestination.SessionRun.route,
                arguments = listOf(
                    navArgument("studentId") { type = NavType.StringType },
                    navArgument("contentIds") { type = NavType.StringType },
                    navArgument("sessionName") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val sessionStudentId = backStackEntry.arguments?.getString("studentId").orEmpty()
                SessionRunScreen(
                    onBack = { navController.popBackStack() },
                    onFinishSession = { sessionId ->
                        navController.navigate(
                            AppDestination.SessionReport.createRoute(sessionId, sessionStudentId)
                        ) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                        }
                    },
                )
            }
            composable(
                route = AppDestination.SessionReport.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType },
                    navArgument("studentId") { type = NavType.StringType },
                ),
            ) {
                SessionReportScreen(
                    onHomeClick = {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppDestination.Reports.route) {
                ReportsScreen(
                    onHomeClick = {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onActivitiesClick = {
                        navController.navigate(AppDestination.Activities.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onStudentsClick = { navController.navigate(AppDestination.Students.route) },
                    onMenuClick = onMenuClick,
                    onProfileClick = onProfileClick,
                    onPdfGenerated = { path ->
                        navController.navigate(AppDestination.ReportPdf.createRoute(path)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppDestination.Students.route) {
                StudentsScreen(
                    onStudentClick = { id ->
                        navController.navigate(AppDestination.StudentProfile.createRoute(id))
                    },
                    onAddStudentClick = { navController.navigate(AppDestination.AddStudent.route) },
                    onHomeClick = { navController.popBackStack() },
                    onActivitiesClick = {
                        navController.navigate(AppDestination.Activities.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onReportsClick = {
                        navController.navigate(AppDestination.Reports.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onMenuClick = onMenuClick,
                    onProfileClick = onProfileClick,
                )
            }
            composable(AppDestination.AddStudent.route) {
                AddStudentScreen(
                    onCancelClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                )
            }
            composable(
                route = AppDestination.StudentProfile.route,
                arguments = listOf(navArgument("studentId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val profileStudentId = backStackEntry.arguments?.getString("studentId").orEmpty()
                StudentProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onSessionClick = { sessionId ->
                        navController.navigate(
                            AppDestination.SessionReport.createRoute(sessionId, profileStudentId)
                        )
                    },
                )
            }
            composable(AppDestination.Activities.route) {
                ActivitiesScreen(
                    onHomeClick = {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onStudentsClick = { navController.navigate(AppDestination.Students.route) },
                    onReportsClick = {
                        navController.navigate(AppDestination.Reports.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onCreateActivityClick = { navController.navigate(AppDestination.CreateActivity.route) },
                    onCreateNotebookClick = { navController.navigate(AppDestination.CreateNotebook.route) },
                    onCreateGroupClick = { navController.navigate(AppDestination.CreateTaskGroup.route) },
                    onNotebookClick = { id -> navController.navigate(AppDestination.NotebookDetail.createRoute(id)) },
                    onGroupClick = { id -> navController.navigate(AppDestination.GroupDetail.createRoute(id)) },
                    onTaskClick = { id -> navController.navigate(AppDestination.ActivityAnswer.createRoute(id)) },
                    onMenuClick = onMenuClick,
                    onProfileClick = onProfileClick,
                )
            }
            composable(
                route = AppDestination.GroupDetail.route,
                arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
            ) {
                GroupDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onTaskClick = { id -> navController.navigate(AppDestination.ActivityAnswer.createRoute(id)) },
                )
            }
            composable(
                route = AppDestination.NotebookDetail.route,
                arguments = listOf(navArgument("notebookId") { type = NavType.StringType }),
            ) {
                NotebookDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onTaskClick = { id -> navController.navigate(AppDestination.ActivityAnswer.createRoute(id)) },
                )
            }
            composable(
                route = AppDestination.ActivityAnswer.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
            ) {
                ActivityAnswerScreen(
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable(AppDestination.CreateActivity.route) {
                CreateActivityScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                )
            }
            composable(AppDestination.CreateNotebook.route) {
                CreateNotebookScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                )
            }
            composable(AppDestination.CreateTaskGroup.route) {
                CreateTaskGroupScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                )
            }
            composable(
                route = AppDestination.ReportPdf.route,
                arguments = listOf(navArgument("filePath") { type = NavType.StringType }),
            ) { backStackEntry ->
                ReportPdfScreen(
                    filePath = backStackEntry.arguments?.getString("filePath").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.UserProfile.route) {
                UserProfileScreen(
                    onBack = { navController.popBackStack() },
                    onProfileClick = onProfileClick,
                )
            }
            composable(AppDestination.Privacy.route) {
                PrivacyScreen(
                    onBack = { navController.popBackStack() },
                    onProfileClick = onProfileClick,
                )
            }
            composable(AppDestination.Support.route) {
                SupportScreen(
                    onBack = { navController.popBackStack() },
                    onProfileClick = onProfileClick,
                )
            }
            composable(AppDestination.Anamnese.route) {
                AnamneseScreen(
                    onBack = { navController.popBackStack() },
                    onProfileClick = onProfileClick,
                )
            }
        }

        if (showProfileSheet) {
            ProfileMenuBottomSheet(
                userName = profileUiState.displayName,
                userRole = profileUiState.role,
                onDismiss = { showProfileSheet = false },
                onMeuPerfil = {
                    showProfileSheet = false
                    navController.navigate(AppDestination.UserProfile.route) { launchSingleTop = true }
                },
                onAnamnese = {
                    showProfileSheet = false
                    navController.navigate(AppDestination.Anamnese.route) { launchSingleTop = true }
                },
                onPrivacidade = {
                    showProfileSheet = false
                    navController.navigate(AppDestination.Privacy.route) { launchSingleTop = true }
                },
                onSuporte = {
                    showProfileSheet = false
                    navController.navigate(AppDestination.Support.route) { launchSingleTop = true }
                },
                onLogout = {
                    showProfileSheet = false
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Register : AppDestination("register")
    data object ForgotPassword : AppDestination("forgot-password")
    data object Dashboard : AppDestination("dashboard")
    data object Students : AppDestination("students")
    data object AddStudent : AppDestination("add-student")
    data object StudentProfile : AppDestination("student-profile/{studentId}") {
        fun createRoute(studentId: String) = "student-profile/$studentId"
    }
    data object Activities : AppDestination("activities")
    data object CreateActivity : AppDestination("create-activity")
    data object CreateNotebook : AppDestination("create-notebook")
    data object CreateTaskGroup : AppDestination("create-task-group")
    data object NotebookDetail : AppDestination("notebook-detail/{notebookId}") {
        fun createRoute(notebookId: String) = "notebook-detail/$notebookId"
    }
    data object GroupDetail : AppDestination("group-detail/{groupId}") {
        fun createRoute(groupId: String) = "group-detail/$groupId"
    }
    data object ActivityAnswer : AppDestination("activity-answer/{taskId}") {
        fun createRoute(taskId: String) = "activity-answer/$taskId"
    }
    data object Reports : AppDestination("reports")
    data object SessionReport : AppDestination("session-report/{sessionId}/{studentId}") {
        fun createRoute(sessionId: String, studentId: String) = "session-report/$sessionId/$studentId"
    }
    data object SessionSelectStudent : AppDestination("session/select-student")
    data object SessionConfigure : AppDestination("session/configure/{studentId}") {
        fun createRoute(studentId: String) = "session/configure/$studentId"
    }
    data object SessionRun : AppDestination("session/run/{studentId}/{contentIds}/{sessionName}") {
        fun createRoute(
            studentId: String,
            contentIds: String,
            sessionName: String,
        ) = "session/run/$studentId/${Uri.encode(contentIds)}/${Uri.encode(sessionName)}"
    }
    data object ReportPdf : AppDestination("report-pdf/{filePath}") {
        fun createRoute(filePath: String) = "report-pdf/${Uri.encode(filePath)}"
    }
    data object UserProfile : AppDestination("user-profile")
    data object Privacy : AppDestination("privacy")
    data object Support : AppDestination("support")
    data object Anamnese : AppDestination("anamnese")
}
