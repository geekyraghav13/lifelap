package com.life.lapse.stop.motion.video.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.life.lapse.stop.motion.video.ui.camera.CameraScreen
import com.life.lapse.stop.motion.video.ui.editor.EditorScreen
import com.life.lapse.stop.motion.video.ui.editor.EditorViewModel
import com.life.lapse.stop.motion.video.ui.home.HomeScreen
import com.life.lapse.stop.motion.video.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Camera : Screen("camera")
    object Editor : Screen("editor")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Create the ViewModel here, so it's shared by CameraScreen and EditorScreen
    val sharedProjectViewModel: EditorViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNewProject = {
                    // Clear any old project data before starting a new one
                    sharedProjectViewModel.clearProject()
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { navController.navigate(Screen.Editor.route) },
                // Pass the shared ViewModel to the CameraScreen
                projectViewModel = sharedProjectViewModel
            )
        }

        composable(Screen.Editor.route) {
            EditorScreen(
                onNavigateBack = { navController.popBackStack() },
                // Pass the SAME shared ViewModel to the EditorScreen
                editorViewModel = sharedProjectViewModel
            )
        }
    }
}
