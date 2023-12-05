package edu.uark.ahnelson.mPProject.Model

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import edu.uark.ahnelson.mPProject.GameActivity.GameActivity
import edu.uark.ahnelson.mPProject.MainActivity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class GameRepository(private val gameDao: GameDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    var loading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var playerTitle: MutableLiveData<String> = MutableLiveData<String>()
    var playerIcon: MutableLiveData<String> = MutableLiveData<String>()
    var playerId: MutableLiveData<String> = MutableLiveData<String>()
    //TODO remove this after merge
    //I got rid of transactionComplete entirely, after finding a better way to make steamFragment
    //close, feel free to remove this in merge
    var transactionComplete: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun getFlow(sort: Int, filter: Int, keyword: String): Flow<List<Game>> {
        when (sort) {
            0 -> return if (filter == 0)
                gameDao.getAlphabetizedGames("%$keyword%")
            else
                gameDao.getAlphabetizedCompletedGames(filter == 1, "%$keyword%")

            1 -> return if (filter == 0)
                gameDao.getReverseAlphabetizedGames("%$keyword%")
            else
                gameDao.getReverseAlphabetizedCompletedGames(filter == 1, "%$keyword%")

            2 -> return if (filter == 0)
                gameDao.getGamesByRating("%$keyword%")
            else
                gameDao.getCompletedGamesByRating(filter == 1, "%$keyword%")

            3 -> return if (filter == 0)
                gameDao.getGamesByReverseRating("%$keyword%")
            else
                gameDao.getCompleteGamesByReverseRating(filter == 1, "%$keyword%")
        }
        return gameDao.getAlphabetizedGames("%$keyword%")
    }

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getGame(id: Int): Flow<Game> {
        return gameDao.getGame(id)
    }

    fun getGameNotLive(id: Int): Game {
        return gameDao.getGameNotLive(id)
    }

    // API work
    private val client = OkHttpClient()
    private val apiKey = "FCBCDE0D333F3FA53CE2A1AB19FCCE52"
    suspend fun getSteamUser(userId: String): String = withContext(Dispatchers.IO) {
        transactionComplete.postValue(false)
        val request = Request.Builder()
            .url("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=$apiKey&steamids=$userId")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for ((name, value) in response.headers) {
                Log.d("HTTPRequest", "$name: $value")
            }
            val apiReturn = response.body!!.string()
            try {
                playerTitle.postValue(
                    JSONObject(apiReturn).getJSONObject("response").getJSONArray("players")
                        .getJSONObject(0).getString("personaname")
                )
                playerIcon.postValue(
                    JSONObject(apiReturn).getJSONObject("response").getJSONArray("players")
                        .getJSONObject(0).getString("avatarfull")
                )
                playerId.postValue(
                    JSONObject(apiReturn).getJSONObject("response").getJSONArray("players")
                        .getJSONObject(0).getString("steamid")
                )

            } catch (e: Exception) {
                playerTitle.postValue("")
                playerIcon.postValue("")
                playerId.postValue("")
            }
            transactionComplete.postValue(true)
        }
        return@withContext ""
    }

    //connects to Steam API, gets all app id's from a user with a specified userId, then calls a function
    //to parse them
    suspend fun getSteamGames() = withContext(Dispatchers.IO) {
        //flags loading to true

        loading.postValue(true)
        val request = Request.Builder()
            //https://api.steampowered.com/ISteamApps/GetAppList/v2
            .url("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=$apiKey&steamid=${playerId.value}&format=json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for ((name, value) in response.headers) {
                Log.d("HTTPRequest", "$name: $value")
            }
            parseSteamGames(response.body!!.string())

        }
    }

    //parses SteamGames JSON for usable individual games, then calls getSteamGameInfo with that,
    //puts the return into an ArrayList, and then adds those games to the repository
    private suspend fun parseSteamGames(apiReturn: String) {
        val gamesArray = JSONObject(apiReturn)
            .getJSONObject("response").getJSONArray("games")
        val gamesToAdd = arrayListOf<String>()
        for (i in 0 until gamesArray.length()) {
            gamesToAdd.add(gamesArray.getJSONObject(i).getString("appid"))
        }
        parseAndInsertSteamGamesInfo(getSteamGamesInfo(gamesToAdd.toTypedArray()))
        loading.postValue(false)
        Log.d("Test", "Made it!")
    }

    //gets appId and then calls steamAPI for more info, calls parseSteamGameInfo to parse it
    private fun getSteamGamesInfo(appIds: Array<String>): String {
        var url = "https://api.steampowered.com/ICommunityService/GetApps/v1/?key=$apiKey"
        for ((i, id) in appIds.withIndex()) {
            url += "&appids%5B$i%5D=$id"
        }
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for ((name, value) in response.headers) {
                Log.d("HTTPRequest", "$name: $value")
            }
            return response.body!!.string()//parseSteamGameInfo(response.body!!.string())
        }
    }

    //parses JSONArray off appid's with associated title and icon
    //inserts each value pair into the database with placeholder values for all unfilled fields
    private suspend fun parseAndInsertSteamGamesInfo(apiReturn: String) {
        val objectJSON = JSONObject(apiReturn).getJSONObject("response").getJSONArray("apps")
        var title: String
        var icon: String
        for (i in 0 until objectJSON.length()) {
            title = objectJSON.getJSONObject(i).getString("name")

            icon = if (objectJSON.getJSONObject(i).has("icon"))
                objectJSON.getJSONObject(i).getString("icon")
            else
                ""
            if (!checkGame(title))
                insert(Game(null, title, false, 0L, "PC", icon, "", "", 0L, "", "", 0F))
        }
    }

    private val twitchAPIkey = "8tewxx9su460oqwdf9pu9kwvpy1lut"
    private val twitchClientID = "ea004to7ydnqixv12xvi2cq88nvkbo"
    private val twitchAccessToken = "h6m4f0dm1yxgnrwxtjdpxtkd4z04qv"

    val mediaType = "text/plain".toMediaType()
    suspend fun scrapeGameInfo(title: String, id: Int) = withContext(Dispatchers.IO) {
        Log.d("IGDB", "Starting call thread...")
        // If your scraping isnt working, try uncommenting this and check logcat for new twitchAccessToken. Copy/Paste that token above in twitchAccessToken.
        //        val tatrec = Request.Builder()
        //        .url("https://id.twitch.tv/oauth2/token?client_id=$twitchClientID&client_secret=$twitchAPIkey&grant_type=client_credentials")
        //            .post("".toRequestBody(mediaType))
        //            .build()
        //        client.newCall(tatrec).execute().use { response ->
        //            if (!response.isSuccessful) throw IOException("Unexpected code $response")
        //            Log.d("GameRepository", "Request successful!")
        //            response.body?.let { Log.d("GameRepository", it.string()) }
        //        }

        val body =
            "fields id,name,involved_companies.company.name,first_release_date,cover.url,summary;\nwhere name = \"$title\";\nsort rating desc;".toRequestBody(
                mediaType
            )
        val request = Request.Builder()
            .url("https://api.igdb.com/v4/games/")
            .post(body)
            .addHeader("Client-ID", twitchClientID)
            .addHeader("Content-Type", "text/plain")
            .addHeader("Authorization", "Bearer $twitchAccessToken")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            Log.d("IGDB", "Request successful!")
//            Log.d("IGDB",(response.body!!.string()))

            val arrayJSON = JSONArray(response.body!!.string())
//            for (i in 0 until arrayJSON.length()){
            // TODO: set up fragment to choose which game
            val igdbTitle = arrayJSON.getJSONObject(0).getString("name") ?: title
            val igdbID = arrayJSON.getJSONObject(0).getString("id")
            val date = arrayJSON.getJSONObject(0).getString("first_release_date")
            val dev = arrayJSON.getJSONObject(0).getJSONArray("involved_companies").getJSONObject(0).getJSONObject("company").getString("name")
            val cover = "https:" + arrayJSON.getJSONObject(0).getJSONObject("cover").getString("url")
            val desc = arrayJSON.getJSONObject(0).getString("summary")

            Log.d("IGDB", "ID: $igdbID NAME: $igdbTitle DEV: $dev DATE: $date COVER: $cover DESC: $desc")
            var gameData = getGameNotLive(id)
            gameData.title = igdbTitle
            gameData.publishDate = date.toLong()
            gameData.publisher = dev
            gameData.art = cover
            gameData.description = desc

            update(gameData)
        }
    }



    private fun makeUri(appid: String, icon: String): String {
        if (appid != "" && icon != "") {
            return "https://media.steampowered.com/steamcommunity/public/images/apps/$appid/$icon.jpg"
        }
        return ""
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



