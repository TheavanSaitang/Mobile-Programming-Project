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
    val allGamesAlphabetized: LiveData<List<Game>> = repository.allGamesAlphabetized.asLiveData()
    val allGamesReverseAlphabetized: LiveData<List<Game>> = repository.allGamesReverseAlphabetized.asLiveData()
    val allGamesByRating: LiveData<List<Game>> = repository.allGamesByRating.asLiveData()
    val allGamesByReverseRating: LiveData<List<Game>> = repository.allGamesByReverseRating.asLiveData()
    val completedGamesAlphabetized: LiveData<List<Game>> = repository.completedGamesAlphabetized.asLiveData()
    val completedGamesReverseAlphabetized: LiveData<List<Game>> = repository.completedGamesReverseAlphabetized.asLiveData()
    val completedGamesByRating: LiveData<List<Game>> = repository.completedGamesByRating.asLiveData()
    val completedGamesByReverseRating: LiveData<List<Game>> = repository.completedGamesByReverseRating.asLiveData()
    val incompleteGamesAlphabetized: LiveData<List<Game>> = repository.incompleteGamesAlphabetized.asLiveData()
    val incompleteGamesReverseAlphabetized: LiveData<List<Game>> = repository.incompleteGamesReverseAlphabetized.asLiveData()
    val incompleteGamesByRating: LiveData<List<Game>> = repository.incompleteGamesByRating.asLiveData()
    val incompleteGamesByReverseRating: LiveData<List<Game>> = repository.incompleteGamesByReverseRating.asLiveData()

    val loading: MutableLiveData<Boolean> = repository.loading
    fun update(game: Game) {
        viewModelScope.launch {
            repository.update(game)
        }
    }
    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
    fun setSort(mode:Int) {
        viewModelScope.launch {
            repository.setAllGames(mode)
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
