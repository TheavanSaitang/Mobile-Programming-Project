package edu.uark.ahnelson.mPProject.MainActivity
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.R
import androidx.fragment.app.commit
import edu.uark.ahnelson.mPProject.GameActivity.EXTRA_ID
import edu.uark.ahnelson.mPProject.GameActivity.GameActivity
import edu.uark.ahnelson.mPProject.Model.Game

class MainActivity : AppCompatActivity(){

    //This instantiates the viewModel instance
    private val gameListViewModel: GameListViewModel by viewModels {
        GameListViewModelFactory((application as GamesApplication).repository)
    }
    private var filterMode: Int = 0
    //onCreate override class
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //creates a popupMenu on click of navButton
        //popupMenu has three options, "Add Game", "Import Steam Library", and "Settings"
        val navButton = findViewById<Button>(R.id.btnNav)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        var sortMode = 0
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

                    //very basic settings fragment
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

                    R.id.clearDatabase -> {
                        val permissionFragment = PermissionFragment()
                        supportFragmentManager.commit {
                            setCustomAnimations(
                                R.anim.fade_in,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.fade_out
                            )
                            replace(R.id.fragment_container_view, permissionFragment, "permissionFragment]")
                            addToBackStack("permissionFragment")
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

        // Set up RecyclerView for games list
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = GameListAdapter(this::gameItemClicked)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        //MASSIVE observer function, used all over the MainActivity, unfortunately it has to be defined
        //up here as to make everything else work

        fun observe(){
            when(filterMode){
                //all games filter
                0->{
                    when(sortMode){
                        //alphabetical descending sort
                        0->{
                            fab.setImageResource(R.drawable.ic_sort_alphabetical_descending)
                            gameListViewModel.allGamesAlphabetized.observe( this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //alphabetical ascending sort
                        1->{
                            fab.setImageResource(R.drawable.ic_sort_alphabetical_ascending)
                            gameListViewModel.allGamesReverseAlphabetized.observe( this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //rating descending sort
                        2->{
                            fab.setImageResource(R.drawable.ic_sort_rating_ascending)
                            gameListViewModel.allGamesByRating.observe( this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //rating ascending sort
                        3->{
                            fab.setImageResource(R.drawable.ic_sort_rating_descending)
                            gameListViewModel.allGamesByReverseRating.observe( this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                    }
                }

                //completed games filter
                1-> {
                    when (sortMode) {
                        //alphabetical descending sort
                        0 -> {
                            Log.d("Debug", "Made it")
                            fab.setImageResource(R.drawable.ic_sort_alphabetical_descending)
                            gameListViewModel.completedGamesAlphabetized.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //alphabetical ascending sort
                        1 -> {
                            fab.setImageResource(R.drawable.ic_sort_alphabetical_ascending)
                            gameListViewModel.completedGamesReverseAlphabetized.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //rating descending sort
                        2 -> {
                            fab.setImageResource(R.drawable.ic_sort_rating_ascending)
                            gameListViewModel.completedGamesByRating.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //rating ascending sort
                        3 -> {
                            fab.setImageResource(R.drawable.ic_sort_rating_descending)
                            gameListViewModel.completedGamesByReverseRating.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                    }
                }

                //incomplete games filter
                2-> {
                    when (sortMode) {
                        //alphabetical descending sort
                        0 -> {
                            Log.d("Debug", "Made it")
                            fab.setImageResource(R.drawable.ic_sort_alphabetical_descending)
                            gameListViewModel.incompleteGamesAlphabetized.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //alphabetical ascending sort
                        1 -> {
                            fab.setImageResource(R.drawable.ic_sort_alphabetical_ascending)
                            gameListViewModel.incompleteGamesReverseAlphabetized.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //rating descending sort
                        2 -> {
                            fab.setImageResource(R.drawable.ic_sort_rating_ascending)
                            gameListViewModel.incompleteGamesByRating.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                        //rating ascending sort
                        3 -> {
                            fab.setImageResource(R.drawable.ic_sort_rating_descending)
                            gameListViewModel.incompleteGamesByReverseRating.observe(this) { words ->
                                // Update the cached copy of the words in the adapter.
                                words.let {
                                    adapter.submitList(it)
                                }
                            }
                        }
                    }
                }
            }
        }
        observe()
        //fab button cycles through sorting modes


        fab.setOnClickListener {
            sortMode++
            sortMode %= 4
            Log.d("Sort", sortMode.toString())
            observe()
        }

        //filter buttons
        //data displayed
        val gamesButton = findViewById<Button>(R.id.btnGames)
        gamesButton.setOnClickListener{
            filterMode = 0
            observe()
        }
        val completeButton = findViewById<Button>(R.id.btnCompleted)
        completeButton.setOnClickListener{
            filterMode = 1
            observe()
        }
        val incompleteButton = findViewById<Button>(R.id.btnIncomplete)
        incompleteButton.setOnClickListener{
            filterMode = 2
            observe()
        }
        //if "loading" is true, use loadingFragment
        //else, close steamFragment & loadingFragment
        gameListViewModel.loading.observe(this) { loading ->
            if(loading)
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
    fun deleteAll(){
        gameListViewModel.deleteAll()
    }
    private fun gameItemClicked(id: Int){
        val intent = Intent(this@MainActivity, GameActivity::class.java)
        intent.putExtra(EXTRA_ID,id)
        startGameActivity.launch(intent)
    }

    // This is our ActivityResultContracts value that defines the behavior of our application when the MainActivity has finished.
    // Should be called above in gameItemClicked.
    private val startGameActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK){
            //Note that all we are doing is logging that we completed
            //This means that the other activity is handling updates to the data
            Log.d("MainActivity","Completed")
        }
    }

    fun compare(p0: Game, p1: Game): Int {
        val result = p0.title.compareTo(p1.title)
        return if(result > 0)
            1;
        else if(result == 0)
            0;
        else
            -1
    }
}
