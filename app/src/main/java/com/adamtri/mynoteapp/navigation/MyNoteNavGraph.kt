package com.adamtri.mynoteapp.navigation



import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adamtri.mynoteapp.ui.screens.DashboardScreen
import com.adamtri.mynoteapp.ui.screens.EditorScreen
import com.adamtri.mynoteapp.ui.screens.SettingsScreen
import com.adamtri.mynoteapp.ui.screens.AboutScreen
import com.adamtri.mynoteapp.viewmodel.NoteViewModel
import com.adamtri.mynoteapp.viewmodel.SettingsViewModel

/**
 * NavGraph = "peta jalan" aplikasi: layar apa saja yang ada
 * dan bagaimana cara berpindah di antaranya.
 */
@Composable
fun MyNoteNavGraph() {
    val navController = rememberNavController()

    // ViewModel dibuat DI SINI (level NavGraph), lalu dibagikan ke kedua layar.
    // Dengan begitu Dashboard dan Editor melihat DATA YANG SAMA.
    // Analogi: satu papan tulis di ruang guru yang dilihat semua kelas.
    val noteViewModel: NoteViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        // ── Layar 1: Dashboard ─────────────────────────────────────────
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = noteViewModel,
                // Event navigasi DIHOIST ke sini: layar tidak tahu-menahu
                // soal navController → layar mudah di-preview & di-test.
                onAddNote = {
                    navController.navigate(Screen.Editor.buildRoute())
                },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.Editor.buildRoute(noteId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ── Layar 2: Editor (Create & Edit) ────────────────────────────
        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument(Screen.Editor.ARG_NOTE_ID) {
                    type = NavType.LongType
                    defaultValue = Screen.Editor.NO_ID // tanpa argumen = mode CREATE
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments
                ?.getLong(Screen.Editor.ARG_NOTE_ID) ?: Screen.Editor.NO_ID

            EditorScreen(
                viewModel = noteViewModel,
                noteId = noteId,
                // popBackStack() = "tekan tombol back secara programatik".
                // Dashboard TIDAK dibuat ulang — ia masih ada di back stack
                // dan otomatis menampilkan data terbaru dari StateFlow.
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Layar 3: Settings ──────────────────────────────────────────
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        // ── Layar 4: About ─────────────────────────────────────────────
        composable(route = Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
