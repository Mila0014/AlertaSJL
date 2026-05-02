package com.example.sjl_alert_v4.actividades

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.SJL_Alert_v4Theme

class MainActivity : ComponentActivity() {

    /**
     * attachBaseContext se ejecuta ANTES que onCreate.
     * Es el punto correcto para aplicar el locale en Android 7+,
     * ya que envuelve el contexto base con la configuración correcta
     * desde el inicio del ciclo de vida de la Activity.
     */
    override fun attachBaseContext(newBase: Context) {
        val prefManager = PreferenceManager(newBase)
        val language = prefManager.getLanguage()
        super.attachBaseContext(LocaleHelper.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SJL_Alert_v4Theme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }

    val startDestination = if (prefManager.haySesionActiva()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginPage(
                onLoginSuccess = {
                    navController.navigate("welcome") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterPage(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("welcome") {
            WelcomePage(
                onContinue = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // ── INICIO: lista de reportes ──────────────────────────────────────────
        composable("home") {
            MisReportesPage(
                onBack = { },
                onLogout = {
                    prefManager.cerrarSesion()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToSettings = { navController.navigate("settings") },
                onNuevoReporte = { navController.navigate("reports") },
                onNavigateToDirectory = { navController.navigate("directory") },
                onVerDetalles = { id ->
                    navController.navigate("reporte_detalles/$id")
                }
            )
        }

        // ── REPORTES: formulario para reportar ────────────────────────────────
        composable("reports") {
            ReportsPage(
                onLogout = {
                    prefManager.cerrarSesion()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToDirectory = { navController.navigate("directory") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToMisReportes = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("report_detail/{tipo}") { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "General"
            ReportDetailPage(
                tipoIncidencia = tipo,
                onBack = { navController.popBackStack() },
                onReportSent = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("reporte_detalles/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ReporteDetallesPage(
                reporteId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable("directory") {
            DirectorioPage(
                onBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToReports = { navController.navigate("reports") },
                onLogout = {
                    prefManager.cerrarSesion()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            AjustesPrefs(
                onBack = { navController.popBackStack() },
                onThemeChanged = {
                    (context as? android.app.Activity)?.recreate()
                }
            )
        }
    }
}