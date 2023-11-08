package com.example.mpproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
// TODO: This asLiveData call will hopefully work correctly when the rest of the bind function is operating appropriately.
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mpproject.model.Game
import com.example.mpproject.model.GameRepository

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class GameListViewModel(private val repository: GameRepository) : ViewModel() {

    // Using LiveData and caching what allGames returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allGames: LiveData<List<Game>> = repository.allGames.asLiveData()
    fun update(task: Game) {
        viewModelScope.launch {
            repository.update(task)
        }
    }
}

class GameListViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <G : ViewModel> create(modelClass: Class<G>): G {
        if (modelClass.isAssignableFrom(GameListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameListViewModel(repository) as G
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}