package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * Composable that represents the Home Screen.
 *
 * This screen is the main entry point for the game, displaying high scores, game information, and buttons
 * to start a visual or audio game. It also provides easy navigation to the actual game screen.
 *
 * @param vm The ViewModel that holds game-related data.
 * @param navController Used to navigate between different screens.
 */
@Composable
fun HomeScreen(
    vm: GameViewModel,
    navController: NavController
) {
    // Observe the highscore from the ViewModel
    val highscore by vm.highscore.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() } // Remember snackbar host state for showing messages

    // Main scaffold structure of the HomeScreen
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) } // Provide the snackbar for showing messages
    ) {
        // Column to arrange all elements vertically and centered
        Column(
            modifier = Modifier
                .fillMaxSize() // Take up the whole screen size
                .padding(it), // Padding to accommodate system UI such as the navigation bar
            verticalArrangement = Arrangement.Center, // Center all elements vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center all elements horizontally
        ) {
            // Display High Score
            Text(
                modifier = Modifier.padding(32.dp), // Add padding around the text
                text = "High-Score = $highscore", // Display the current high score
                style = MaterialTheme.typography.headlineLarge // Use a headline style for prominence
            )

            // Display Game Information
            Text(
                modifier = Modifier.padding(16.dp), // Padding to separate it from other elements
                text = "n = 2, values 10, 20% correct, 3 seconds between interval", // Game settings information
                style = MaterialTheme.typography.bodyLarge // Style as body text
            )

            // Button to Start Visual Game
            Button(
                onClick = {
                    vm.setGameType(GameType.Visual) // Set game type to Visual before navigating
                    navController.navigate("game") // Navigate to the GameScreen
                },
                modifier = Modifier.padding(16.dp) // Add padding around the button
            ) {
                Text(text = "Start Visual Game") // Button label
            }

            // Button to Start Audio Game
            Button(
                onClick = {
                    vm.setGameType(GameType.Audio) // Set game type to Audio before navigating
                    navController.navigate("game") // Navigate to the GameScreen
                },
                modifier = Modifier.padding(16.dp) // Add padding around the button
            ) {
                Text(text = "Start Audio Game") // Button label
            }
        }
    }
}
