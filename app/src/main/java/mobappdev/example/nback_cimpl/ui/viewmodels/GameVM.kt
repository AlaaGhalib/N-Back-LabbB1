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
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int
    val eventHistorySize: Int // Added property to expose event history size

    fun setGameType(gameType: GameType)
    fun startGame()
    fun checkMatch()
    fun resetGame()
}
class GameVM(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application), GameViewModel {

    private val _gameState = MutableStateFlow(GameState())
    private var mediaPlayer: MediaPlayer? = null
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    override val nBack: Int = 2

    private var job: Job? = null
    private val eventInterval: Long = 2000L

    private val nBackHelper = NBackHelper()
    private var events = emptyArray<Int>()
    private val eventHistory = mutableListOf<Int>()

    override val eventHistorySize: Int
        get() = eventHistory.size

    override fun resetGame() {
        mediaPlayer?.release()
        job?.cancel()
        _score.value = 0
        eventHistory.clear()
    }

    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType, gameEnded = false)
    }


    override fun startGame() {
        resetGame()
        Log.d("GameVM", "Game type selected: ${gameState.value.gameType}")
        job?.cancel()

        events = nBackHelper.generateNBackString(10, 9, 30, nBack).toList().toTypedArray()
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

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
        }
    }


    private fun playSound(soundResourceId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(getApplication<Application>(), soundResourceId)
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.start()
    }

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

    override fun checkMatch() {
        if (eventHistory.size >= nBack) {
            val currentEvent = _gameState.value.eventValue
            val matchEvent = eventHistory[eventHistory.size - 3]

            if (currentEvent == matchEvent) {
                _score.value += 1
                Log.d("GameVM", "Match found! Score increased to: ${_score.value}")
            } else {
                _score.value = maxOf(_score.value - 1, 0)
                Log.d("GameVM", "No match. Score decreased to: ${_score.value}")
            }
        } else {
            Log.d("GameVM", "Not enough history to check for match.")
        }
    }

    private suspend fun runVisualGame(events: Array<Int>) {
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value)
            delay(500L)
            eventHistory.add(value)
            delay(eventInterval - 500L)
        }
    }

    private suspend fun runAudioGame(events: Array<Int>) {
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value)
            playSound(getSoundResourceById(value)) // Play the corresponding sound
            eventHistory.add(value) // Track this event
            delay(eventInterval) // Wait for the next event to play
        }
    }

    private fun runAudioVisualGame() { /* To be implemented */ }

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

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val gameEnded: Boolean = false
)