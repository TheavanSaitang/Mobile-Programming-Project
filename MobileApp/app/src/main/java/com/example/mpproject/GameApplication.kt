package com.example.mpproject

import android.app.Application
import com.example.mpproject.model.GameRepository
import com.example.mpproject.model.GameRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class GameApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { GameRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { GameRepository(database.gameDao()) }
}