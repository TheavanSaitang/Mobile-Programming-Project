package edu.uark.ahnelson.mPProject.MainActivity
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.add
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.R
import androidx.fragment.app.commit
<<<<<<< HEAD
=======
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import edu.uark.ahnelson.mPProject.GameActivity.EXTRA_ID
import edu.uark.ahnelson.mPProject.GameActivity.GameActivity

>>>>>>> e97d59c (SteamAPI Loading Fragment)
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

<<<<<<< HEAD
        val addGameButton = findViewById<FloatingActionButton>(R.id.fab)
        addGameButton.setOnClickListener{
            Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        }
=======


>>>>>>> e97d59c (SteamAPI Loading Fragment)
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
                    R.id.addGame -> Toast.makeText(this, "Add Game", Toast.LENGTH_SHORT).show()

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
                        val settingsFragment = LoadingFragment()
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

        //TODO make fab sort
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this@MainActivity, NewEditGameActivity::class.java)
//            startNewGameActivity.launch(intent)
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
            }
        }
        gameListViewModel.updating.observe(this) { it ->
            if(it == 1)
            {
                val loadingFragment = LoadingFragment()
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    replace(R.id.fragment_container_view, loadingFragment, "loadingFragment")
                    addToBackStack("loadingFragment")
                }
            }
            else
            {
                supportFragmentManager.popBackStack("steamFragment", POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.popBackStack("loadingFragment", POP_BACK_STACK_INCLUSIVE)
            }
        }
    }
    //called by SteamFragment, initiates any steam stuff
    fun getSteamInfo(userId: String){
        gameListViewModel.getSteamGames("76561198044143028")
    }
    private fun gameItemClicked(id: Int){
        // todo: set up NewEditGameActivity intent when clicking a game tile ( view/edit game )
        Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        // val intent = Intent(this@MainActivity, NewEditGameTaskActivity::class.java)
        // intent.putExtra(EXTRA_ID,id)
        // startNewGameActivity.launch(intent)
    }

    // This is our ActivityResultContracts value that defines the behavior of our application when the MainActivity has finished.
    // Should be called above in gameItemClicked.
    val startNewGameActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK){
            //Note that all we are doing is logging that we completed
            //This means that the other activity is handling updates to the data
            Log.d("MainActivity","Completed")
        }
    }
}

