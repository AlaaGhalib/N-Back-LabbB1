package mobappdev.example.nback_cimpl.ui.viewmodels

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * The viewmodel manages the state of the game, including the score, the game type, and other game-related data.
 * It has functions to start a game, set a game type, check for matches, and reset the game.
 * It also saves high scores for the user.
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 */
interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int
    val eventHistorySize: Int // Added property to expose event history size

    fun setGameType(gameType: GameType)
    fun startGame()
    fun checkMatch(): Boolean // This function now returns whether the guess was correct
    fun resetGame()
}

class GameVM(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application), GameViewModel {

    private val _gameState = MutableStateFlow(GameState())
    private var mediaPlayer: MediaPlayer? = null

    // Current state of the game
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // Defines how many elements back the user must match in the game
    override val nBack: Int = 2

    private var job: Job? = null
    private val eventInterval: Long = 2000L

    private val nBackHelper = NBackHelper()
    private var events = emptyArray<Int>()
    private val eventHistory = mutableListOf<Int>()

    // Get the current event history size
    override val eventHistorySize: Int
        get() = eventHistory.size

    /**
     * Resets the game state, including score and event history. Releases resources for MediaPlayer.
     */
    override fun resetGame() {
        mediaPlayer?.release()
        job?.cancel()
        _score.value = 0
        eventHistory.clear()
    }

    /**
     * Sets the game type (Audio, Visual, AudioVisual) and initializes the game state accordingly.
     */
    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType, gameEnded = false)
    }

    /**
     * Starts the game by resetting the current state and generating a sequence of events based on nBack.
     */
    override fun startGame() {
        resetGame()
        Log.d("GameVM", "Game type selected: ${gameState.value.gameType}")
        job?.cancel()

        // Generate the sequence of events for the game
        events = nBackHelper.generateNBackString(10, 9, 30, nBack).toList().toTypedArray()
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        // Launch the game sequence based on game type
        job = viewModelScope.launch {
            when (_gameState.value.gameType) {
                GameType.Audio -> {
                    Log.d("GameVM", "Starting Audio game...")
                    runAudioGame(events)
                }
                GameType.Visual -> {
                    Log.d("GameVM", "Starting Visual game...")
                    runVisualGame(events)
                }
                GameType.AudioVisual -> {
                    Log.d("GameVM", "Starting AudioVisual game...")
                    runAudioVisualGame()
                }
            }
            _gameState.value = _gameState.value.copy(gameEnded = true)
            Log.d("GameVM", "Game has ended.")
            saveHighScoreIfBeaten() // Save high score if beaten
        }
    }

    /**
     * Plays a sound based on the provided resource ID.
     */
    private fun playSound(soundResourceId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(getApplication<Application>(), soundResourceId)
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.start()
    }

    /**
     * Retrieves a sound resource ID based on an integer identifier.
     */
    private fun getSoundResourceById(id: Int): Int {
        return when (id) {
            0 -> R.raw.a
            1 -> R.raw.b
            2 -> R.raw.c
            3 -> R.raw.d
            4 -> R.raw.e
            5 -> R.raw.f
            6 -> R.raw.g
            7 -> R.raw.h
            8 -> R.raw.i
            else -> R.raw.a
        }
    }

    /**
     * Checks whether the current event matches the event n-back. Returns true if the guess is correct.
     */
    override fun checkMatch(): Boolean {
        if (eventHistory.size >= nBack) {
            val currentEvent = _gameState.value.eventValue
            val matchEvent = eventHistory[eventHistory.size - nBack]

            if (currentEvent == matchEvent) {
                if (!_gameState.value.pointAwarded) {
                    _score.value += 1
                    Log.d("GameVM", "Match found! Score increased to: ${_score.value}")

                    // Update the game state to indicate that the point has been awarded
                    _gameState.value = _gameState.value.copy(pointAwarded = true)
                    return true
                } else {
                    Log.d("GameVM", "Point already awarded for this match. No score increment.")
                }
            } else {
                _score.value = maxOf(_score.value - 1, 0)
                Log.d("GameVM", "No match found.")
            }
        } else {
            Log.d("GameVM", "Not enough history to check for match.")
        }
        return false
    }

    /**
     * Runs a visual game by iterating through a sequence of events and updating game state.
     */
    private suspend fun runVisualGame(events: Array<Int>) {
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value, pointAwarded = false)
            delay(1500L)  // Increase delay to give enough time for user input
            eventHistory.add(value)
            delay(eventInterval - 1500L)  // The remaining time before moving to the next event
        }
    }

    /**
     * Runs an audio game by playing a sequence of sounds and updating game state.
     */
    private suspend fun runAudioGame(events: Array<Int>) {
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value, pointAwarded = false)  // Reset pointAwarded when new event starts
            playSound(getSoundResourceById(value)) // Play the corresponding sound
            eventHistory.add(value) // Track this event
            delay(eventInterval) // Wait for the next event to play
        }
    }

    /**
     * Runs an audio-visual game. Not yet implemented.
     */
    private fun runAudioVisualGame() { /* To be implemented */ }

    /**
     * Saves the high score if the current score exceeds the saved high score.
     */
    private suspend fun saveHighScoreIfBeaten() {
        if (_score.value > _highscore.value) {
            Log.d("GameVM", "New high score achieved: ${_score.value}")
            userPreferencesRepository.saveHighScore(_score.value)
        }
    }



    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application, application.userPreferencesRespository)
            }
        }
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}


// Class representing the different game types
enum class GameType {
    Audio,
    Visual,
    AudioVisual
}

// Data class to maintain the state of the game
data class GameState(
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the current event
    val gameEnded: Boolean = false,  // Flag indicating if the game has ended
    val pointAwarded: Boolean = false  // Flag to indicate if a point has been awarded for the current check
)
