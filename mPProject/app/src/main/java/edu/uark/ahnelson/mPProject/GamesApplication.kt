package edu.uark.ahnelson.mPProject

import android.app.Application
import edu.uark.ahnelson.mPProject.Model.GameRepository
import edu.uark.ahnelson.mPProject.Model.GameRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class GamesApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { GameRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { GameRepository(database.gameDao()) }
}
