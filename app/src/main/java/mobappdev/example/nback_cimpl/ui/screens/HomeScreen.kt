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

@Composable
fun HomeScreen(
    vm: GameViewModel,
    navController: NavController
) {
    val highscore by vm.highscore.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

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
            // Display High Score
            Text(
                modifier = Modifier.padding(32.dp),
                text = "High-Score = $highscore",
                style = MaterialTheme.typography.headlineLarge
            )

            // Button to Start Visual Game
            Button(
                onClick = {
                    vm.setGameType(GameType.Visual) // Set game type before navigating
                    navController.navigate("game") // Navigate to GameScreen
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Start Visual Game")
            }

            // Button to Start Audio Game
            Button(
                onClick = {
                    vm.setGameType(GameType.Audio) // Set game type before navigating
                    navController.navigate("game") // Navigate to GameScreen
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Start Audio Game")
            }
        }
    }
}
