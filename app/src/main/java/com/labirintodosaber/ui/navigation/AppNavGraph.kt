package com.labirintodosaber.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.labirintodosaber.ui.screen.forgotpassword.ForgotPasswordScreen
import com.labirintodosaber.ui.screen.home.HomeScreen
import com.labirintodosaber.ui.screen.login.LoginScreen
import com.labirintodosaber.ui.screen.register.RegisterScreen

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
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppDestination.Register.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(AppDestination.ForgotPassword.route)
                },
            )
        }
        composable(AppDestination.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.popBackStack() },
            )
        }
        composable(AppDestination.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Home.route) {
            HomeScreen(
                onNavigate = { destination -> navController.navigate(destination.route) },
            )
        }
    }
}

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Register : AppDestination("register")
    data object ForgotPassword : AppDestination("forgot-password")
    data object Home : AppDestination("home")
}
