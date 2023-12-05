package edu.uark.ahnelson.mPProject.MainActivity
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import java.security.Key
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    //This instantiates the viewModel instance
    val gameListViewModel: GameListViewModel by viewModels {
        GameListViewModelFactory((application as GamesApplication).repository)
    }
    private var filterMode: Int = 0
    val steamFragment = SteamFragment()
    //onCreate override class
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var sortMode = 0
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val searchBar = findViewById<EditText>(R.id.searchBar)
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
                            replace(R.id.fragment_container_view, permissionFragment, "permissionFragment")
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

        //observer function, switches the flow which the function is currently observing to that of
        //another flow, defined by parameters passed in
        //sort 0-3 are described in when(sortMode)
        //filter 0 = allGames
        //filter 1 = completedGames
        //filter 2 = incompleteGames
        fun observe(sort:Int, filter:Int, keyword:String){
            when(sortMode){
                0->fab.setImageResource(R.drawable.ic_sort_alphabetical_descending)
                1->fab.setImageResource(R.drawable.ic_sort_alphabetical_ascending)
                2->fab.setImageResource(R.drawable.ic_sort_rating_ascending)
                3->fab.setImageResource(R.drawable.ic_sort_rating_descending)
            }
            gameListViewModel.getFlow(sort, filter, keyword).observe(this) { words ->
                words.let{
                    adapter.submitList(it)
                }
            }
        }
        observe(0, filterMode, searchBar.text.toString())

        //fab button cycles through sorting modes
        fab.setOnClickListener {
            sortMode++
            sortMode %= 4
            Log.d("Sort", sortMode.toString())
            //must re-observe in order to see changes
            observe(sortMode, filterMode, searchBar.text.toString())
        }

        //re-observes the database every time searchbar text is changed
        //allows user to dynamically see search query as it is typed
        searchBar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun afterTextChanged(p0: Editable?) {
                observe(sortMode, filterMode, searchBar.text.toString())
                return
            }

        })
        //filter buttons
        val gamesButton = findViewById<Button>(R.id.btnGames)
        gamesButton.setOnClickListener{
            filterMode = 0
            //must re-observe in order to see changes
            observe(sortMode, filterMode, searchBar.text.toString())
        }
        val completeButton = findViewById<Button>(R.id.btnCompleted)
        completeButton.setOnClickListener{
            filterMode = 1
            //must re-observe in order to see changes
            observe(sortMode, filterMode, searchBar.text.toString())
        }
        val incompleteButton = findViewById<Button>(R.id.btnIncomplete)
        incompleteButton.setOnClickListener{
            filterMode = 2
            //must re-observe in order to see changes
            observe(sortMode, filterMode, searchBar.text.toString())
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
        //TODO remove this after merge
        //I got rid of transactionComplete entirely, after finding a better way to make steamFragment
        //close, feel free to remove this in merge
    }
    //called by SteamFragment, initiates any steam stuff
    fun getSteamUser(userId: String){
        gameListViewModel.getSteamUser("76561198044143028")
        //76561198044143028
    }
    fun getSteamGames(){
        gameListViewModel.getSteamGames()
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

}
