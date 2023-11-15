package edu.uark.ahnelson.mPProject.Model

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Game::class], version = 1, exportSchema = false)
abstract class GameRoomDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    private class GameDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d("Database","Beginning Initialization...")

            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis += 60000

            INSTANCE?.let { database ->
                scope.launch {
                    val gameDao = database.gameDao()

                    Log.d("Database","Attempting to create games...")
                    // Add sample games.
                    val game = Game(
                        null,
                        "Portal",
                        false,
                        null,
                        "PC",
                        null,
                        null,
                        "Valve",
                        1191974400,
                        "my first FPS - just use 'sv_cheats 1' and 'impulse 101'",
                        null,
                        5)
                    gameDao.insert(game)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GameRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): GameRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameRoomDatabase::class.java,
                    "game_database"
                )
                    .addCallback(GameDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}