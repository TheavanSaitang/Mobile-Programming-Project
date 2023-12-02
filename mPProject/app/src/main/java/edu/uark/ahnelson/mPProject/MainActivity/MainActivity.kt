package edu.uark.ahnelson.mPProject.MainActivity
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.add
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.R
import androidx.fragment.app.commit
import edu.uark.ahnelson.mPProject.GameActivity.EXTRA_ID
import edu.uark.ahnelson.mPProject.GameActivity.GameActivity

// import edu.uark.ahnelson.mPProject.NewEditGameActivity.NewEditGameActivity.Companion.EXTRA_ID


class MainActivity : AppCompatActivity() {

    //This instantiates the viewModel instance
    private val gameListViewModel: GameListViewModel by viewModels {
        GameListViewModelFactory((application as GamesApplication).repository)
    }

    //onCreate override class
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //creates a popupMenu on click of navButton
        //popupMenu has three options, "Add Game", "Import Steam Library", and "Settings"
        val navButton = findViewById<Button>(R.id.btnNav)
        navButton.setOnClickListener{
            val popupMenu = PopupMenu(this@MainActivity, navButton)

            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                //selects specific menuItem option
                when(menuItem.itemId) {
                    //TODO MAKE THIS OPEN NEW GAME ACTIVITY
                    R.id.addGame -> {
                        val intent = Intent(this@MainActivity, GameActivity::class.java)
                        startGameActivity.launch(intent)
                    }

                    R.id.importLibrary -> {
                            val steamFragment = SteamFragment()
                            supportFragmentManager.commit {
                                setCustomAnimations(
                                    R.anim.fade_in,
                                    R.anim.fade_out
                                )
                                replace(R.id.fragment_container_view, steamFragment, "steamFragment")
                                addToBackStack("steamFragment")
                            }
                    }

                    //TODO ADD VERY VERY BASIC SETTINGS FRAGMENT
                    R.id.settings -> {
                        val settingsFragment = SettingsFragment()
                        supportFragmentManager.commit {
                            setCustomAnimations(
                                R.anim.fade_in,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.fade_out
                            )
                            replace(R.id.fragment_container_view, settingsFragment, "settingsFragment")
                            addToBackStack("settingsFragment")
                        }
                    }
                    else -> {
                        //prints error if user somehow clicks unknown popup menu option
                        //SHOULD NEVER HAPPEN
                        Log.d("ERROR", "Invalid popup menu option selected")
                    }
                }
                true
            }

            popupMenu.show()
        }

        // todo: set up NewEditGameActivity intent for floating action button ( add a game )
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            startGameActivity.launch(intent)
        }

        // Set up RecyclerView for games list
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = GameListAdapter(this::gameItemClicked)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        gameListViewModel.allGames.observe( this) { words ->
            // Update the cached copy of the words in the adapter.
            words.let {
                adapter.submitList(it)
            }
        }

        //Phillip - Temporary action definitions for buttons, this will work until we get the game
        //data displayed
        val gamesButton = findViewById<Button>(R.id.btnGames)
        gamesButton.setOnClickListener{
            gameListViewModel.allGames.observe( this) { words ->
                // Update the cached copy of the words in the adapter.
                words.let {
                    adapter.submitList(it)
                }
            }
        }
        val favoritesButton = findViewById<Button>(R.id.btnFavorites)
        favoritesButton.setOnClickListener{
            gameListViewModel.completedGames.observe( this) { words ->
                // Update the cached copy of the words in the adapter.
                words.let {
                    adapter.submitList(it)
                }
            }
        }
        val wantToPlayButton = findViewById<Button>(R.id.btnWantToPlay)
        wantToPlayButton.setOnClickListener{
            gameListViewModel.incompleteGames.observe( this) { words ->
                // Update the cached copy of the words in the adapter.
                words.let {
                    adapter.submitList(it)
                }
            }        }
    }
    //called by SteamFragment, initiates any steam stuff
    fun getSteamInfo(userId: String){
        gameListViewModel.getSteamGames("76561198301339924")
    }
    private fun gameItemClicked(id: Int){
        val intent = Intent(this@MainActivity, GameActivity::class.java)
        intent.putExtra(EXTRA_ID,id)
        startGameActivity.launch(intent)
    }

    // This is our ActivityResultContracts value that defines the behavior of our application when the MainActivity has finished.
    // Should be called above in gameItemClicked.
    val startGameActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK){
            //Note that all we are doing is logging that we completed
            //This means that the other activity is handling updates to the data
            Log.d("MainActivity","Completed")
        }
    }
}


