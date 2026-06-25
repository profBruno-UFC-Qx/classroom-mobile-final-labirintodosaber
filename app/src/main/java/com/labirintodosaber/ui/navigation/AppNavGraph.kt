package com.labirintodosaber.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.labirintodosaber.ui.screen.activities.ActivitiesScreen
import com.labirintodosaber.ui.screen.activityanswer.ActivityAnswerScreen
import com.labirintodosaber.ui.screen.addstudent.AddStudentScreen
import com.labirintodosaber.ui.screen.createactivity.CreateActivityScreen
import com.labirintodosaber.ui.screen.createnotebook.CreateNotebookScreen
import com.labirintodosaber.ui.screen.createtaskgroup.CreateTaskGroupScreen
import com.labirintodosaber.ui.screen.dashboard.DashboardScreen
import com.labirintodosaber.ui.screen.forgotpassword.ForgotPasswordScreen
import com.labirintodosaber.ui.screen.groupdetail.GroupDetailScreen
import com.labirintodosaber.ui.screen.login.LoginScreen
import com.labirintodosaber.ui.screen.notebookdetail.NotebookDetailScreen
import com.labirintodosaber.ui.screen.register.RegisterScreen
import com.labirintodosaber.ui.screen.studentprofile.StudentProfileScreen
import com.labirintodosaber.ui.screen.students.StudentsScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
        modifier = modifier,
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
                    }
                },
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
        ) {
            StudentProfileScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Activities.route) {
            ActivitiesScreen(
                onHomeClick = {
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Dashboard.route) { inclusive = false }
                    }
                },
                onStudentsClick = { navController.navigate(AppDestination.Students.route) },
                onReportsClick = { },
                onCreateActivityClick = { navController.navigate(AppDestination.CreateActivity.route) },
                onCreateNotebookClick = { navController.navigate(AppDestination.CreateNotebook.route) },
                onCreateGroupClick = { navController.navigate(AppDestination.CreateTaskGroup.route) },
                onNotebookClick = { id -> navController.navigate(AppDestination.NotebookDetail.createRoute(id)) },
                onGroupClick = { id -> navController.navigate(AppDestination.GroupDetail.createRoute(id)) },
                onTaskClick = { id -> navController.navigate(AppDestination.ActivityAnswer.createRoute(id)) },
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
}
