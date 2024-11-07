package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * Composable that represents the Game Screen.
 *
 * It displays the game elements based on the selected game type, such as Visual or Audio.
 * The screen also shows the user's current score and provides controls for checking if the guess is correct
 * and navigating back to the home screen.
 *
 * @param navController Used to navigate between different screens.
 * @param gameViewModel The ViewModel that holds the game state and logic.
 */
@Composable
fun GameScreen(
    navController: NavController,
    gameViewModel: GameViewModel // Use the GameVM view model
) {
    // Collect the game state and score from the ViewModel
    val gameState by gameViewModel.gameState.collectAsState() // Observe the game state
    val score by gameViewModel.score.collectAsState() // Observe the score
    val gameStarted = gameState.eventValue != -1 // Check if the game has started based on the event value
    val currentEventNumber = gameViewModel.eventHistorySize + 1 // Track the current event number in the sequence

    // Remember SnackbarHostState and CoroutineScope for showing feedback
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Start the game only once when entering the GameScreen
    androidx.compose.runtime.LaunchedEffect(Unit) {
        gameViewModel.startGame()
    }

    // Scaffold to structure the screen layout with a snackbar for messages
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire available size
                .padding(it),
            verticalArrangement = Arrangement.Center, // Center elements vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center elements horizontally
        ) {
            // Display score at the top if the game has started
            if (gameStarted) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.headlineMedium // Display the score prominently
                    )
                }

                // Display the current event number in the sequence
                Text(
                    text = "Current Event: ${currentEventNumber-1}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Display specific UI based on the game type selected by the user
            when (gameState.gameType) {
                GameType.Visual -> {
                    // UI for the Visual game
                    if (gameStarted) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3), // Create a 3x3 grid
                            modifier = Modifier.size(350.dp), // Set the size of the grid
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(9) { index ->
                                Surface(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(100.dp),
                                    color = if (index == gameState.eventValue) Color.Red
                                    else MaterialTheme.colorScheme.primaryContainer, // Highlight the correct square
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    // Placeholder for interaction logic if needed in the future
                                }
                            }
                        }
                    }
                }

                GameType.Audio -> {
                    // UI for the Audio game
                    if (gameStarted) {
                        Text(
                            text = "Listen to the sound sequence and press 'Check Result' when ready.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                else -> Unit
            }

            // "Check Result" button for both visual and audio games
            if (gameStarted) {
                Button(
                    onClick = {
                        val isCorrect = gameViewModel.checkMatch() // Check if the user's guess is correct

                        // Show snackbar message indicating if the guess was correct or not
                        scope.launch {
                            val message = if (isCorrect) {
                                "Correct!"
                            } else {
                                "Not Correct"
                            }
                            snackBarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Check Result")
                }
            }

            // Back to Home Screen Button
            Button(
                onClick = {
                    gameViewModel.resetGame() // Reset the game state before navigating back
                    navController.popBackStack() // Navigate back to the previous screen
                },
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(text = "Back to Home Screen")
            }
        }
    }
}
