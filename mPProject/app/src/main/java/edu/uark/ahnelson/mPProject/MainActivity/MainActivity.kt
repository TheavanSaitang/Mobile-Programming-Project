package edu.uark.ahnelson.mPProject.MainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.add
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.R
import androidx.fragment.app.commit


class MainActivity : AppCompatActivity() {

    //This instantiates the viewModel instance
    private val gameListViewModel: GameListViewModel by viewModels {
        GameListViewModelFactory((application as GamesApplication).repository)
    }

    //onCreate override class
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Phillip - Temporary action definitions for buttons, this will work until we get the game
        //data displayed
        val gamesButton = findViewById<Button>(R.id.btnGames)
        gamesButton.setOnClickListener{
           Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        }
        val favoritesButton = findViewById<Button>(R.id.btnFavorites)
        favoritesButton.setOnClickListener{
            Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        }
        val wantToPlayButton = findViewById<Button>(R.id.btnWantToPlay)
        wantToPlayButton.setOnClickListener{
            Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        }
        val addGameButton = findViewById<FloatingActionButton>(R.id.fab)
        addGameButton.setOnClickListener{
            Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        }
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
                    R.id.settings -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
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
    }
}

