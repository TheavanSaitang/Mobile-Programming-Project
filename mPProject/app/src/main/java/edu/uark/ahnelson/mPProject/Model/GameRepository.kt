package edu.uark.ahnelson.mPProject.Model

import android.os.AsyncTask
import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONArray
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

    //connects to Steam API, gets all app id's from a user with a specified userId, then calls a function
    //to parse them
    suspend fun getSteamGames(userId: String) = withContext(Dispatchers.IO) {

        val request = Request.Builder()
            //https://api.steampowered.com/ISteamApps/GetAppList/v2
            .url("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=$APIKey&steamid=$userId&format=json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for ((name, value) in response.headers) {
                println("$name: $value")
            }
            parseSteamGames(response.body!!.string())

        }
    }
    //parses SteamGames JSON for usable individual games, then calls getSteamGameInfo with that
    private suspend fun parseSteamGames(APIReturn: String){
        val gamesArray = JSONObject(APIReturn)
            .getJSONObject("response").getJSONArray("games")
        //getSteamGameInfo(gamesArray.getJSONObject(0).getString("appid"))
        for(i in 0 until gamesArray.length())
        {
            //Log.d("Database", gamesArray.getJSONObject(i).get("appid").toString())
            Log.d("Debug", i.toString())
            Log.d("Debug", gamesArray.length().toString())
            getSteamGameInfo(gamesArray.getJSONObject(i).getString("appid"))
        }
        Log.d("Test", "Made it!")
    }
    //gets appId and then calls steamAPI for more info, calls parseSteamGameInfo to parse it
    private suspend fun getSteamGameInfo(appId:String) {
        val request = Request.Builder()
            .url("https://api.steampowered.com/ICommunityService/GetApps/v1/?key=FCBCDE0D333F3FA53CE2A1AB19FCCE52&appids%5B0%5D=$appId")
            .build()
        client.newCall(request).execute().use {response ->
            if(!response.isSuccessful) throw IOException("Unexpected code $response")
            for((name, value) in response.headers) {
                println("$name: $value")
            }
            parseSteamGameInfo(response.body!!.string())
        }
    }
    //parses game Info which is passed in, then adds the game to the database
    private suspend fun parseSteamGameInfo(APIReturn: String){
        val objectJSON = JSONObject(APIReturn)
            .getJSONObject("response").getJSONArray("apps").getJSONObject(0)
        val title = objectJSON.getString("name")
        val icon = ""
        if(objectJSON.has("icon")) {
            val icon = objectJSON.getString("icon")
        }
        if(!checkGame(title)) {
            insert(Game(null, title, false, 0, "PC", icon, "", "", 0, "", "", 0))
        }
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

    @Suppress
    @WorkerThread
    suspend fun checkGame(title: String): Boolean {
        return gameDao.getIfGameExists(title) != null
    }
}

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

