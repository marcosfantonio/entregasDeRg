package com.fantonio.entregarg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fantonio.entregarg.ui.screens.IdentitySearchScreen
import com.fantonio.entregarg.ui.screens.SettingsScreen
import com.fantonio.entregarg.ui.screens.CameraScanScreen
import com.fantonio.entregarg.ui.screens.ScanReviewScreen
import com.fantonio.entregarg.ui.viewmodel.IdentityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: IdentityViewModel = viewModel()

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            IdentitySearchScreen(
                viewModel = viewModel,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onScanClick = { navController.navigate("camera_scan") }
            )
        }
        composable("camera_scan") {
            CameraScanScreen(
                viewModel = viewModel,
                onNavigateToReview = { navController.navigate("scan_review") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("scan_review") {
            ScanReviewScreen(
                viewModel = viewModel,
                onConfirm = { navController.popBackStack("settings", inclusive = false) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
