package mobappdev.example.nback_cimpl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.screens.GameScreen
import mobappdev.example.nback_cimpl.ui.screens.HomeScreen
import mobappdev.example.nback_cimpl.ui.theme.NBack_CImplTheme
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBack_CImplTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up the NavController for navigation
                    val navController = rememberNavController()
                    val gameViewModel: GameVM = viewModel(factory = GameVM.Factory)

                    // Define the NavHost with routes for "home" and "game"
                    NavHost(navController = navController, startDestination = "home") {
                        // Home screen route
                        composable("home") {
                            HomeScreen(
                                vm = gameViewModel,
                                navController = navController
                            )
                        }
                        // Game screen route
                        composable("game") {
                            GameScreen(navController = navController, gameViewModel = gameViewModel)
                        }
                    }
                }
            }
        }
    }
}
