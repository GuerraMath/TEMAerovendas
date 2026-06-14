// Caminho: app/src/main/java/com/temaerovendas/navigation/NavGraph.kt
package com.temaerovendas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.temaerovendas.ui.screens.admin.AdminReportsScreen
import com.temaerovendas.ui.screens.detail.AircraftDetailScreen
import com.temaerovendas.ui.screens.hangar.HangarDetailScreen
import com.temaerovendas.ui.screens.hangar.HangarRegisterScreen
import com.temaerovendas.ui.screens.login.LoginScreen
import com.temaerovendas.ui.screens.main.MainScreen
import com.temaerovendas.ui.screens.register.AircraftRegisterScreen
import com.temaerovendas.ui.screens.signup.SignUpScreen
import com.temaerovendas.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")

    /**
     * Tela principal pós-login, com bottom navigation (Comprar/Vender/Perfil).
     * Substitui a antiga rota direta para a lista de aeronaves.
     */
    object Main : Screen("main")

    object AircraftDetail : Screen("aircraft_detail/{aircraftId}") {
        fun createRoute(aircraftId: String) = "aircraft_detail/$aircraftId"
    }

    object AircraftEdit : Screen("aircraft_edit/{aircraftId}") {
        fun createRoute(aircraftId: String) = "aircraft_edit/$aircraftId"
    }

    /** Painel de administração de denúncias (Política de UGC) — acesso restrito a isAdmin. */
    object AdminReports : Screen("admin_reports")

    object HangarDetail : Screen("hangar_detail/{hangarId}") {
        fun createRoute(hangarId: String) = "hangar_detail/$hangarId"
    }

    object HangarEdit : Screen("hangar_edit/{hangarId}") {
        fun createRoute(hangarId: String) = "hangar_edit/$hangarId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToList = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onAircraftClick = { aircraftId ->
                    navController.navigate(Screen.AircraftDetail.createRoute(aircraftId))
                },
                onHangarClick = { hangarId ->
                    navController.navigate(Screen.HangarDetail.createRoute(hangarId))
                },
                onAdminReportsClick = {
                    navController.navigate(Screen.AdminReports.route)
                },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AdminReports.route) {
            AdminReportsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AircraftDetail.route,
            arguments = listOf(navArgument("aircraftId") { type = NavType.StringType })
        ) { backStackEntry ->
            val aircraftId = backStackEntry.arguments?.getString("aircraftId") ?: return@composable
            AircraftDetailScreen(
                aircraftId = aircraftId,
                onBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Screen.AircraftEdit.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AircraftEdit.route,
            arguments = listOf(navArgument("aircraftId") { type = NavType.StringType })
        ) {
            // O aircraftId é lido automaticamente pelo ViewModel via SavedStateHandle
            // (injetado pelo Hilt a partir dos argumentos desta rota de navegação).
            AircraftRegisterScreen(
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HangarDetail.route,
            arguments = listOf(navArgument("hangarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hangarId = backStackEntry.arguments?.getString("hangarId") ?: return@composable
            HangarDetailScreen(
                hangarId = hangarId,
                onBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Screen.HangarEdit.createRoute(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HangarEdit.route,
            arguments = listOf(navArgument("hangarId") { type = NavType.StringType })
        ) {
            // O hangarId é lido automaticamente pelo ViewModel via SavedStateHandle
            // (injetado pelo Hilt a partir dos argumentos desta rota de navegação).
            HangarRegisterScreen(
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }
    }
}
