package com.labirintodosaber.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.labirintodosaber.ui.screen.addstudent.AddStudentScreen
import com.labirintodosaber.ui.screen.dashboard.DashboardScreen
import com.labirintodosaber.ui.screen.forgotpassword.ForgotPasswordScreen
import com.labirintodosaber.ui.screen.login.LoginScreen
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
            )
        }
        composable(AppDestination.Students.route) {
            StudentsScreen(
                onStudentClick = { id ->
                    navController.navigate(AppDestination.StudentProfile.createRoute(id))
                },
                onAddStudentClick = { navController.navigate(AppDestination.AddStudent.route) },
                onHomeClick = { navController.popBackStack() },
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
            arguments = listOf(navArgument("studentId") { type = NavType.IntType }),
        ) {
            StudentProfileScreen(
                onBackClick = { navController.popBackStack() },
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
        fun createRoute(studentId: Int) = "student-profile/$studentId"
    }
}
