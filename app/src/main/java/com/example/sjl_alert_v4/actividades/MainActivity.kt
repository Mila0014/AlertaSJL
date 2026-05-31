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
import androidx.work.*
import com.example.sjl_alert_v4.actividades.recuperacion.RecuperarContraPage
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.SJL_Alert_v4Theme
import com.example.sjl_alert_v4.workers.SyncWorker
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefManager = PreferenceManager(newBase)
        val language = prefManager.getLanguage()
        super.attachBaseContext(LocaleHelper.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fuerza apertura de la DB para que App Inspection pueda conectarse
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(this@MainActivity).openHelper.readableDatabase
        }

        // ── WorkManager: sincronizar incidencias cuando haya internet ──────
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_incidencias",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

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

    // Si el usuario no eligió mantener sesión, cerrarla automáticamente al abrir
    prefManager.verificarSesionAlAbrir()
    val startDestination = if (prefManager.haySesionActiva()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginPage(
                onLoginSuccess = {
                    navController.navigate("welcome") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("recuperar") }
            )
        }

        composable("recuperar") {
            RecuperarContraPage(
                onBack = { navController.popBackStack() },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("recuperar") { inclusive = true }
                    }
                }
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

        composable("home") {
            MisReportesPage(
                onBack = { },
                onLogout = {
                    prefManager.cerrarSesion()
                    navController.navigate("cerrando_sesion") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToSettings = { navController.navigate("settings") },
                onNuevoReporte = { navController.navigate("reports") },
                onNavigateToDirectory = { navController.navigate("directory") },
                onVerDetalles = { id ->
                    navController.navigate("reporte_detalles/$id")
                },
                onNavigateToCrud = { navController.navigate("crud") }
            )
        }

        composable("reports") {
            ReportsPage(
                onLogout = {
                    prefManager.cerrarSesion()
                    navController.navigate("cerrando_sesion") { popUpTo(0) { inclusive = true } }
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
                    navController.navigate("cerrando_sesion") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            AjustesPrefs(
                onBack = { navController.popBackStack() },
                onLogout = {
                    prefManager.cerrarSesion()
                    navController.navigate("cerrando_sesion") { popUpTo(0) { inclusive = true } }
                },
                onThemeChanged = {
                    (context as? android.app.Activity)?.recreate()
                }
            )
        }

        composable("cerrando_sesion") {
            CerrandoSesionPage(
                onFinished = {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("crud") {
            CrudIncidenciasPage(onBack = { navController.popBackStack() })
        }
    }
}