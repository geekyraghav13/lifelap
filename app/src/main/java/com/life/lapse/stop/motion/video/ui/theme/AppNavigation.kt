package com.life.lapse.stop.motion.video.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
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
    // A nested graph for everything related to a single project
    object Project : Screen("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
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
                    navController.navigate(Screen.Project.createRoute(newProjectId))
                },
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.Project.createRoute(projectId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // This function defines the nested graph for a project
        projectGraph(navController)
    }
}

// FIX: This is the corrected way to define a nested navigation graph.
// The `arguments` parameter has been removed from the `navigation()` call.
fun NavGraphBuilder.projectGraph(navController: NavController) {
    navigation(
        startDestination = ProjectScreen.Editor.route,
        route = Screen.Project.route
    ) {
        composable(
            route = ProjectScreen.Editor.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectViewModel: EditorViewModel = backStackEntry.sharedViewModel(navController = navController)
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
        composable(ProjectScreen.Camera.route) { backStackEntry ->
            val projectViewModel: EditorViewModel = backStackEntry.sharedViewModel(navController = navController)

            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                projectViewModel = projectViewModel,
                onNavigateToEditor = { navController.popBackStack() }
            )
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}
