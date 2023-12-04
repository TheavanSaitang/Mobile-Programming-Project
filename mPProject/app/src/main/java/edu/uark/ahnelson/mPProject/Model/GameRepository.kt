package edu.uark.ahnelson.mPProject.Model

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class GameRepository(private val gameDao: GameDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    var loading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    fun getFlow(sort:Int, filter:Int, keyword:String):Flow<List<Game>> {
        when(sort){
            0-> return if(filter==0)
                    gameDao.getAlphabetizedGames("%$keyword%")
                else
                    gameDao.getAlphabetizedCompletedGames(filter==1, "%$keyword%")
            1-> return if(filter==0)
                    gameDao.getReverseAlphabetizedGames("%$keyword%")
                else
                    gameDao.getReverseAlphabetizedCompletedGames(filter==1, "%$keyword%")
            2-> return if(filter==0)
                    gameDao.getGamesByRating("%$keyword%")
                else
                    gameDao.getCompletedGamesByRating(filter==1, "%$keyword%")
            3-> return if(filter==0)
                    gameDao.getGamesByReverseRating("%$keyword%")
                else
                    gameDao.getCompleteGamesByReverseRating(filter==1, "%$keyword%")
        }
        return gameDao.getAlphabetizedGames("%$keyword%")
    }
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getGame(id:Int):Flow<Game>{
        return gameDao.getGame(id)
    }

    fun getGameNotLive(id:Int):Game{
        return gameDao.getGameNotLive(id)
    }
    private val client = OkHttpClient()
    private val apiKey = "FCBCDE0D333F3FA53CE2A1AB19FCCE52"
    //connects to Steam API, gets all app id's from a user with a specified userId, then calls a function
    //to parse them
    suspend fun getSteamGames(userId: String) = withContext(Dispatchers.IO) {
        //flags loading to true
        loading.postValue(true)
        val request = Request.Builder()
            //https://api.steampowered.com/ISteamApps/GetAppList/v2
            .url("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=$apiKey&steamid=$userId&format=json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for ((name, value) in response.headers) {
                println("$name: $value")
            }
            parseSteamGames(response.body!!.string())

        }
    }
    //parses SteamGames JSON for usable individual games, then calls getSteamGameInfo with that,
    //puts the return into an ArrayList, and then adds those games to the repository
    private suspend fun parseSteamGames(apiReturn: String){
        val gamesArray = JSONObject(apiReturn)
            .getJSONObject("response").getJSONArray("games")
        val gamesToAdd = arrayListOf<Game>()
        Log.d("Made", "It")
        //getSteamGameInfo(gamesArray.getJSONObject(0).getString("appid"))
        for(i in 0 until gamesArray.length())
        {
            //Log.d("Database", gamesArray.getJSONObject(i).get("appid").toString())
            getSteamGameInfo(gamesArray.getJSONObject(i).getString("appid"))?.let {
                gamesToAdd.add(
                    it
                )
            }
        }
        for(i in 0 until gamesToAdd.size)
        {
            insert(gamesToAdd[i])
        }
        loading.postValue(false)
        Log.d("Test", "Made it!")
    }

    //gets appId and then calls steamAPI for more info, calls parseSteamGameInfo to parse it
    private suspend fun getSteamGameInfo(appId:String): Game? {
        val request = Request.Builder()
            .url("https://api.steampowered.com/ICommunityService/GetApps/v1/?key=FCBCDE0D333F3FA53CE2A1AB19FCCE52&appids%5B0%5D=$appId")
            .build()
        client.newCall(request).execute().use {response ->
            if(!response.isSuccessful) throw IOException("Unexpected code $response")
            for((name, value) in response.headers) {
                println("$name: $value")
            }
            return parseSteamGameInfo(response.body!!.string())
        }
    }

    //parses game Info which is passed in, then adds the game to the database
    private suspend fun parseSteamGameInfo(apiReturn: String): Game? {
        val objectJSON = JSONObject(apiReturn)
            .getJSONObject("response").getJSONArray("apps").getJSONObject(0)
        val title = objectJSON.getString("name")
        var icon = ""
        if(objectJSON.has("icon")) {
            icon = objectJSON.getString("icon")
        }
        if(!checkGame(title)) {
            return Game(null, title, false, 0, "PC", icon, "", "", 0, "", "", 0F)
        }
        return null
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
    @WorkerThread
    suspend fun update(game: Game) {
        gameDao.update(game)
    }

    @WorkerThread
    suspend fun deleteAll() {
        gameDao.deleteAll()
    }

    @WorkerThread
    suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game.id)
    }

    @WorkerThread
    suspend fun checkGame(title: String): Boolean {
        return gameDao.getIfGameExists(title)
    }
}

