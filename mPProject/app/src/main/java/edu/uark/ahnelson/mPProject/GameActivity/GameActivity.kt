package edu.uark.ahnelson.mPProject.GameActivity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DateFormat
import android.os.Bundle
import android.os.Environment
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
import edu.uark.ahnelson.mPProject.GamesApplication
import edu.uark.ahnelson.mPProject.Model.Game
import edu.uark.ahnelson.mPProject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


const val EXTRA_ID:String = "com.example.assignment2.GameActivity.EXTRA_ID"
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

    private var initialArt: String = ""
    private var initialPhotos: String = ""
    private var art: String = ""
    private var photos: String = ""

    private var photoArtPath: String = ""

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory((application as GamesApplication).repository,-1)
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
                photoArtPath = game.art.toString()
                Log.d("My BAAALS", game.art.toString())
                initialArt = photoArtPath
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
                        imPictures.setImageBitmap(game.photos?.let { it1 -> getPic(it1, imPictures.width, imPictures.height) })
                        initialPhotos = game.photos!!
                    }

                }
            }

            imGame.setOnClickListener {
                var photoPath = takeAPicture()
//                var photoBitmap: Bitmap = getPic(photoPath,imGame.width, imGame.height)
//                imGame.setImageBitmap(photoBitmap)
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
                    if(art == initialArt) {
                        art = ""
                    }
                    if(photos == initialPhotos) {
                        photos = ""
                    }

                    if (id == -1) {
                        gameViewModel.insert(
                            Game(
                                null,
                                etTitle.text.toString(),
                                cbComplete.isChecked,
                                com,
                                etSystem.text.toString(),
                                art,
                                etDescription.text.toString(),
                                etPublisher.text.toString(),
                                pub,
                                etNotes.text.toString(),
                                photos,
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
                            updatedGame.art = art
                            updatedGame.description = etDescription.text.toString()
                            updatedGame.publisher = etPublisher.text.toString()
                            updatedGame.publishDate = pub
                            updatedGame.notes = etNotes.text.toString()
                            updatedGame.photos = photos
                            updatedGame.rating = ratingBar.rating
                            gameViewModel.update(updatedGame)
                        }
                    }
                }

                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Hey", "MyBalls!" + initialArt)
        if(art != "") {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Thread.sleep(200)
                    withContext(Dispatchers.Main){
                        imGame.setImageBitmap(getPic(initialArt, imGame.width, imGame.height))
                    }
                }
            }
        }
    }

    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            var photoBitmap: Bitmap = getPic(photoArtPath,imGame.width, imGame.height)
            imGame.setImageBitmap(photoBitmap)
        }
    }

    private fun takeAPicture():String {
        val picIntent: Intent =  Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if(picIntent.resolveActivity(packageManager) != null){
            val filepath: String = createFilePath()
            val myFile: File = File(filepath)
            val photoUri = FileProvider.getUriForFile(this,"edu.uark.ahnelson.mPProject.fileprovider",myFile)
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            photoArtPath = filepath
            takePictureResultLauncher.launch(picIntent)
            art = filepath
            return filepath
        }
        return ""
    }
    private fun createFilePath(): String {
        // Create an image file name
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intent
        return image.absolutePath
    }
    private fun getPic(photoPath: String, w: Int, h: Int): Bitmap {
        val targetW: Int = w

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val targetH: Int = h
        // Determine how much to scale down the image
        val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        Log.d("Hey", photoPath)
        val bitmap = BitmapFactory.decodeFile(photoPath, bmOptions)
        return bitmap
    }
}
