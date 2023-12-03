package edu.uark.ahnelson.mPProject.MainActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import edu.uark.ahnelson.mPProject.Model.Game
import edu.uark.ahnelson.mPProject.Model.GameRepository
import kotlinx.coroutines.launch

class GameListViewModel(private val repository: GameRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allGames: LiveData<List<Game>> = repository.allGames.asLiveData()
    val completedGames: LiveData<List<Game>> = repository.completedGames.asLiveData()
    val incompleteGames: LiveData<List<Game>> = repository.incompleteGames.asLiveData()
    val updating: MutableLiveData<Int> = repository.updating
    fun update(game: Game) {
        viewModelScope.launch {
            repository.update(game)
        }
    }
    fun getSteamGames(userId: String) {
        viewModelScope.launch {
            repository.getSteamGames(userId)
        }
    }

}

class GameListViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
