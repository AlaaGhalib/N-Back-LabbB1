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

@Composable
fun GameScreen(
    navController: NavController,
    gameViewModel: GameViewModel // Use the GameVM view model
) {
    // Collect the game state and score from the ViewModel
    val gameState by gameViewModel.gameState.collectAsState()
    val score by gameViewModel.score.collectAsState()
    val gameStarted = gameState.eventValue != -1

    // Remember SnackbarHostState and CoroutineScope for showing feedback
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Start the game only once when entering the GameScreen
    androidx.compose.runtime.LaunchedEffect(Unit) {
        gameViewModel.startGame()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Display specific UI based on the game type
            when (gameState.gameType) {
                GameType.Visual -> {
                    // Visual Game UI
                    if (gameStarted) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.size(350.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(9) { index ->
                                Surface(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(100.dp),
                                    color = if (index == gameState.eventValue) Color.Red
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    // Placeholder for interaction logic
                                }
                            }
                        }
                    }
                }

                GameType.Audio -> {
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
                        gameViewModel.checkMatch() // Call the checkMatch function

                        // Show snackbar message indicating if the guess was correct or not
                        scope.launch {
                            val message = if (gameViewModel.eventHistorySize >= gameViewModel.nBack &&
                                gameState.eventValue == gameViewModel.gameState.value.eventValue
                            ) {
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
                    navController.popBackStack()
                },
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(text = "Back to Home Screen")
            }
        }
    }
}
