package edu.uark.ahnelson.mPProject.Model

import android.os.AsyncTask
import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class GameRepository(private val gameDao: GameDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allGames: Flow<List<Game>> = gameDao.getAlphabetizedGames()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getGame(id:Int):Flow<Game>{
        return gameDao.getGame(id)
    }

    fun getGameNotLive(id:Int):Game{
        return gameDao.getGameNotLive(id)
    }
    private val client = OkHttpClient()
    private val APIKey = "FCBCDE0D333F3FA53CE2A1AB19FCCE52"
    //connects to Steam API, gets all app id's from a user with a specified userId
    fun getSteamGames(userId:String){

        doAsync {
            Log.d("Test", "This is being called")
            val request = Request.Builder()
                //https://api.steampowered.com/ISteamApps/GetAppList/v2
                .url("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=$APIKey&steamid=$userId&format=json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                for ((name, value) in response.headers) {
                    println("$name: $value")
                }
                val dataInJSON = JSONObject(response.body!!.string())
                val games = dataInJSON.getJSONArray("games")

                Log.d("DatabaseGame", dataInJSON.toString())
                Log.d("Database", response.body!!.string())
            }
        }.execute()
    }
    fun getSteamGame(appId:String){
        doAsync {
            val request = Request.Builder()
                .url("https://api.steampowered.com/ICommunityService/GetApps/v1/?key=FCBCDE0D333F3FA53CE2A1AB19FCCE52&appids%5B0%5D=2247630")
                .build()
            client.newCall(request).execute().use {response ->
                if(!response.isSuccessful) throw IOException("Unexpected code $response")
                for((name, value) in response.headers) {
                    println("$name: $value")
                }

            }
        }.execute()
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(game: Game) {
        gameDao.insert(game)
        Log.d("GameRepository", "Here is the ID of the inserted game: " + game.id)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(game: Game) {
        gameDao.update(game)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        gameDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game.id)
    }
}

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

