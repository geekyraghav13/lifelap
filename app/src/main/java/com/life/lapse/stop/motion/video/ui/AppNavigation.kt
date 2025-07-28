package com.life.lapse.stop.motion.video.ui

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

object NavRoutes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PROJECT_GRAPH = "project_graph"
    const val EDITOR = "editor/{projectId}"
    const val CAMERA = "camera/{projectId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToNewProject = {
                    val newProjectId = UUID.randomUUID().toString()
                    navController.navigate("camera/$newProjectId")
                },
                onNavigateToProject = { projectId ->
                    navController.navigate("editor/$projectId")
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        navigation(
            startDestination = NavRoutes.EDITOR,
            route = NavRoutes.PROJECT_GRAPH
        ) {
            composable(
                route = NavRoutes.EDITOR,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectViewModel: EditorViewModel = backStackEntry.sharedViewModel(navController)
                val projectId = backStackEntry.arguments?.getString("projectId")

                LaunchedEffect(projectId) {
                    projectViewModel.loadProject(projectId)
                }

                EditorScreen(
                    onNavigateBack = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
                        }
                    },
                    editorViewModel = projectViewModel,
                    onNavigateToCamera = {
                        val currentProjectId = projectViewModel.uiState.value.project.id
                        navController.navigate("camera/$currentProjectId")
                    }
                )
            }

            composable(
                route = NavRoutes.CAMERA,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectViewModel: EditorViewModel = backStackEntry.sharedViewModel(navController)
                val projectId = backStackEntry.arguments?.getString("projectId")

                LaunchedEffect(projectId) {
                    projectViewModel.loadProject(projectId)
                }

                CameraScreen(
                    onNavigateBack = { navController.popBackStack() },
                    projectViewModel = projectViewModel,
                    // ✅ FIX: This now navigates to the editor and cleans up the back stack.
                    onNavigateToEditor = {
                        val currentProjectId = projectViewModel.uiState.value.project.id
                        navController.navigate("editor/$currentProjectId") {
                            // This ensures pressing "back" from the editor goes home,
                            // not back to the camera.
                            popUpTo(NavRoutes.HOME)
                        }
                    }
                )
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController
): T {
    val navGraphRoute = NavRoutes.PROJECT_GRAPH
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}