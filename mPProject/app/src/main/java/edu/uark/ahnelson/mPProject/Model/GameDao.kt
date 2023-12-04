package edu.uark.ahnelson.mPProject.Model

import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    //Get all games alphabetized
    @Query("SELECT * FROM game_table WHERE title LIKE :keyword ORDER BY title ASC ")
    fun getAlphabetizedGames(keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE title LIKE :keyword ORDER BY title DESC")
    fun getReverseAlphabetizedGames(keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE title LIKE :keyword ORDER BY rating DESC")
    fun getGamesByRating(keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE title LIKE :keyword ORDER BY rating ASC")
    fun getGamesByReverseRating(keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE completed=:isCompleted AND title LIKE :keyword ORDER BY title ASC")
    fun getAlphabetizedCompletedGames(isCompleted:Boolean, keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE completed=:isCompleted AND title LIKE :keyword ORDER BY title DESC")
    fun getReverseAlphabetizedCompletedGames(isCompleted:Boolean, keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE completed=:isCompleted AND title LIKE :keyword ORDER BY rating DESC")
    fun getCompletedGamesByRating(isCompleted:Boolean, keyword:String): Flow<List<Game>>
    @Query("SELECT * FROM game_table WHERE completed=:isCompleted AND title LIKE :keyword ORDER BY rating ASC")
    fun getCompleteGamesByReverseRating(isCompleted:Boolean, keyword:String): Flow<List<Game>>

    //Get a single game with a given id
    @Query("SELECT * FROM game_table WHERE id=:id")
    fun getGame(id:Int): Flow<Game>

    //Get a single game with a given id
    @Query("SELECT * FROM game_table WHERE id=:id")
    fun getGameNotLive(id:Int): Game

    //check if a single game exists with the given name
    @Query("SELECT * FROM game_table WHERE title=:title")
    fun getIfGameExists(title:String): Boolean
    //Insert a single game
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    //Delete all games
    @Query("DELETE FROM game_table")
    suspend fun deleteAll()

    //Delete a single game with a given id
    @Query("DELETE FROM game_table WHERE id=:id")
    suspend fun deleteGame(id:Int?)

    //Update a single game
    @Update
    suspend fun update(game: Game):Int
}