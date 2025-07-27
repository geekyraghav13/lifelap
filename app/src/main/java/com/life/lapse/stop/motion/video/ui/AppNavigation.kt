package com.life.lapse.stop.motion.video.ui // Correct package for the ui folder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.life.lapse.stop.motion.video.ui.camera.CameraScreen
import com.life.lapse.stop.motion.video.ui.editor.EditorScreen
import com.life.lapse.stop.motion.video.ui.editor.EditorViewModel
import com.life.lapse.stop.motion.video.ui.home.HomeScreen
import com.life.lapse.stop.motion.video.ui.home.HomeViewModel
import com.life.lapse.stop.motion.video.ui.settings.SettingsScreen
import java.util.UUID

// Defines the top-level navigation routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Project : Screen("project/{projectId}")
    // The "createRoute" function is no longer needed and has been removed.
}

// Defines the screens within the nested project graph
sealed class ProjectScreen(val route: String) {
    object Editor : ProjectScreen("editor")
    object Camera : ProjectScreen("camera")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNewProject = {
                    val newProjectId = UUID.randomUUID().toString()
                    // Navigate to the camera screen for the new project
                    navController.navigate("project/$newProjectId/camera")
                },
                onNavigateToProject = { projectId ->
                    // For existing projects, navigate to the editor
                    navController.navigate("project/$projectId/editor")
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // The navigation argument is defined on the GRAPH, not an individual screen.
        navigation(
            startDestination = ProjectScreen.Editor.route,
            route = Screen.Project.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) {
            composable(route = ProjectScreen.Editor.route) { backStackEntry ->
                val projectViewModel: EditorViewModel = backStackEntry.sharedViewModel(navController)
                val projectId = backStackEntry.arguments?.getString("projectId")

                LaunchedEffect(projectId) {
                    projectViewModel.loadProject(projectId)
                }

                EditorScreen(
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    editorViewModel = projectViewModel,
                    onNavigateToCamera = { navController.navigate(ProjectScreen.Camera.route) }
                )
            }

            composable(route = ProjectScreen.Camera.route) { backStackEntry ->
                val projectViewModel: EditorViewModel = backStackEntry.sharedViewModel(navController)
                val projectId = backStackEntry.arguments?.getString("projectId")

                // CameraScreen now also loads the project, making the "New Project" flow work.
                LaunchedEffect(projectId) {
                    projectViewModel.loadProject(projectId)
                }

                CameraScreen(
                    onNavigateBack = { navController.popBackStack() },
                    projectViewModel = projectViewModel,
                    onNavigateToEditor = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}