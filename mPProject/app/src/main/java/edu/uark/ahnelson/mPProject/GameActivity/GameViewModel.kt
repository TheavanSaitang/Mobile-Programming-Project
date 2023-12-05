package edu.uark.ahnelson.mPProject.GameActivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import edu.uark.ahnelson.mPProject.Model.Game
import edu.uark.ahnelson.mPProject.Model.GameRepository
import kotlinx.coroutines.coroutineScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository, private val id:Int) : ViewModel() {

    var curGame: LiveData<Game> = repository.getGame(id).asLiveData()

    fun updateId(id:Int){
        curGame = repository.getGame(id).asLiveData()
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    suspend fun insert(game: Game){
        coroutineScope {
            repository.insert(game)
        }
    }

    /**
     * Launching a new coroutine to Update the data in a non-blocking way
     */
    suspend fun update(game: Game) {
        coroutineScope {
            repository.update(game)
        }
    }

    fun scrapeGameInfo(title: String, id: Int, context: Context) {
        viewModelScope.launch {
            repository.scrapeGameInfo(title, id, context)
        }
    }

}

class GameViewModelFactory(private val repository: GameRepository, private val id:Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository,id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
