package edu.uark.ahnelson.mPProject.MainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.R

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
        val navButton = findViewById<Button>(R.id.btnNav)
        navButton.setOnClickListener{
            Toast.makeText(this, "Functionality not implemented", Toast.LENGTH_SHORT).show()
        }
    }
}

