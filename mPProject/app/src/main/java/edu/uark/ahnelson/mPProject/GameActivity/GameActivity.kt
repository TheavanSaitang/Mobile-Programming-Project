package edu.uark.ahnelson.mPProject.GameActivity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.loader.content.CursorLoader
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.Model.Game
import edu.uark.ahnelson.mPProject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


const val EXTRA_ID:String = "edu.uark.ahnelson.mPProject.GameActivity.EXTRA_ID"
class GameActivity : AppCompatActivity() {

    private lateinit var imGame: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etSystem: EditText
    private lateinit var etPublisher: EditText
    private lateinit var etReleaseDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var etCompleteDate: EditText
    private lateinit var cbComplete: CheckBox
    private lateinit var ratingBar: RatingBar
    private lateinit var etNotes: EditText
    private lateinit var imPictures: ImageView

    var photoPathArt: String = ""
    var photoPathPics: String = ""

    val myExecutor = Executors.newSingleThreadExecutor()
    val myHandler = Handler(Looper.getMainLooper())

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory((application as GamesApplication).repository,-1)
    }

    fun scrapeGame(title: String){
            gameViewModel.scrapeGameInfo(title)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        imGame = findViewById(R.id.imageGamePicture)
        etTitle = findViewById(R.id.textTitle)
        etSystem = findViewById(R.id.textSystem)
        etPublisher = findViewById(R.id.textPublisher)
        etReleaseDate = findViewById(R.id.datePublished)
        etDescription = findViewById(R.id.textDescription)
        etCompleteDate = findViewById(R.id.dateCompletion)
        cbComplete = findViewById(R.id.checkCompletion)
        ratingBar = findViewById(R.id.ratingBarShow)
        etNotes = findViewById(R.id.textNotes)
        imPictures = findViewById(R.id.imagePictures)

        val id = intent.getIntExtra(EXTRA_ID, -1)
        if (id != -1) {
            gameViewModel.updateId(id)
        }
        gameViewModel.curGame.observe(this) { game ->
            game?.let {
                if(game.art != "") {
                    //imGame.setImageBitmap(game.art?.let { it1 -> getPic(it1, imGame.width, imGame.height) })
                    initialArt = game.art!!
                }
                etTitle.setText(game.title)
                etSystem.setText(game.system)
                etPublisher.setText(game.publisher)
                if (game.publishDate != null) {
                    etReleaseDate.setText(
                        java.text.DateFormat.getDateInstance(
                            DateFormat.SHORT
                        ).format(game.publishDate)
                    )

                    etDescription.setText(game.description)
                    if (game.completedDate != null) {
                        etCompleteDate.setText(
                            java.text.DateFormat.getDateInstance(
                                DateFormat.SHORT
                            ).format(game.completedDate)
                        )
                    }
                    cbComplete.setChecked(game.completed)
                    ratingBar.rating = game.rating!!
                    etNotes.setText(game.notes)
                    if(game.photos != "") {
                        //imPictures.setImageBitmap(game.photos?.let { it1 -> getPic(it1, imPictures.width, imPictures.height) })
                        initialPhotos = game.photos!!
                    }

                    }
                photoPathPics = game.photos.toString()
                if(photoPathPics != ""){
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            Thread.sleep(200)
                            withContext(Dispatchers.Main){
                                setPicPhotos()
                            }
                        }
                    }
                }
            }
        }

        imGame.setOnClickListener {
            takeAPicture()
        }

        imPictures.setOnClickListener {
            takeAPicturePhotos()
        }



            val scrapeButton = findViewById<Button>(R.id.scrapeButton)
            scrapeButton.setOnClickListener{
                gameViewModel.scrapeGameInfo(etTitle.text.toString())
            }

            val saveButton = findViewById<Button>(R.id.buttonSave)
            saveButton.setOnClickListener {
                CoroutineScope(SupervisorJob()).launch {
                    var com: Long?
                    var pub: Long?

                    if (etCompleteDate.text.toString() != "") {
                        com = java.text.DateFormat.getDateInstance(DateFormat.SHORT)
                            .parse(etCompleteDate.text.toString())?.time
                    } else {
                        com = null
                    }
                    if (etReleaseDate.text.toString() != "") {
                        pub = java.text.DateFormat.getDateInstance(DateFormat.SHORT)
                            .parse(etReleaseDate.text.toString())?.time
                    } else {
                        pub = null
                    }


                    if (id == -1) {
                        gameViewModel.insert(
                            Game(
                                null,
                                etTitle.text.toString(),
                                cbComplete.isChecked,
                                com,
                                etSystem.text.toString(),
                                photoPathArt,
                                etDescription.text.toString(),
                                etPublisher.text.toString(),
                                pub,
                                etNotes.text.toString(),
                                photoPathPics,
                                ratingBar.rating
                            )
                        )
                    } else {
                        val updatedGame = gameViewModel.curGame.value
                        if (updatedGame != null) {
                            updatedGame.title = etTitle.text.toString()
                            updatedGame.completed = cbComplete.isChecked
                            updatedGame.completedDate = com
                            updatedGame.system = etSystem.text.toString()
                            updatedGame.art = photoPathArt
                            updatedGame.description = etDescription.text.toString()
                            updatedGame.publisher = etPublisher.text.toString()
                            updatedGame.publishDate = pub
                            updatedGame.notes = etNotes.text.toString()
                            updatedGame.photos = photoPathPics
                            updatedGame.rating = ratingBar.rating
                            gameViewModel.update(updatedGame)
                        }
                    }
                }

                setResult(RESULT_OK)
                finish()
            }
        }

    override fun onResume() {
        super.onResume()
    }

    /*private fun getPic(photoPath: String, w: Int, h: Int): Bitmap {
        val targetW: Int = w

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoPathArt, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val photoRatio:Double = (photoH.toDouble())/(photoW.toDouble())
        val targetH: Int = imGame.getHeight()
        // Determine how much to scale down the image
        val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))


        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val bitmap = BitmapFactory.decodeFile(photoPath, bmOptions)
        return bitmap
    }*/
}


