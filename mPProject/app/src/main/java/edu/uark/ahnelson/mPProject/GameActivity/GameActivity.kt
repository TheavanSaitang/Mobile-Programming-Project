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
                photoPathArt = game.art.toString()
                if(game.art.toString() != "" && game.art.toString().substring(0, 1) == "h") {
                    myExecutor.execute {
                        var image: Bitmap? = fetchIcon(game.art.toString())
                        myHandler.post {
                            if (image != null) {
                                saveImage(image)
                            }
                        }
                    }
                }
                if(photoPathArt != ""){
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            Thread.sleep(200)
                            withContext(Dispatchers.Main){
                                setPic()
                                Log.d("Hey", photoPathArt)
                            }
                        }
                    }
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

    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            setPic()
        }
    }

    val takePictureResultLauncherPics = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            setPicPhotos()
        }
    }

    private fun takeAPicture() {
        val picIntent: Intent =  Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if(picIntent.resolveActivity(packageManager) != null){
            val filepath: String = createFilePath()
            val myFile: File = File(filepath)
            photoPathArt = filepath
            val photoUri = FileProvider.getUriForFile(this,"edu.uark.ahnelson.mPProject.fileprovider",myFile)
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            takePictureResultLauncher.launch(picIntent)
        }
    }

    private fun takeAPicturePhotos() {
        val picIntent: Intent =  Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if(picIntent.resolveActivity(packageManager) != null){
            val filepath: String = createFilePath()
            val myFile: File = File(filepath)
            photoPathPics = filepath
            val photoUri = FileProvider.getUriForFile(this,"edu.uark.ahnelson.mPProject.fileprovider",myFile)
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            takePictureResultLauncherPics.launch(picIntent)
        }
    }

    private fun createFilePath(): String {
        // Create an image file name
        val imageFileName = "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intent
        return image.absolutePath
    }

    private fun setPic() {
        val targetW: Int = imGame.getWidth()

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
        val bitmap = BitmapFactory.decodeFile(photoPathArt, bmOptions)
        imGame.setImageBitmap(bitmap)
    }

    private fun setPicPhotos() {
        val targetW: Int = imPictures.getWidth()

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoPathPics, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val photoRatio:Double = (photoH.toDouble())/(photoW.toDouble())
        val targetH: Int = imPictures.getHeight()


        bmOptions.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(photoPathPics, bmOptions)
        imPictures.setImageBitmap(bitmap)
    }

    private fun fetchIcon(webPath: String): Bitmap? {
        val url: URL = stringToURL(webPath)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }

    private fun stringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (ex: MalformedURLException) {
            ex.printStackTrace()
        }
        return null
    }

    private fun saveImage(bitmap: Bitmap?) {
        val filename = "INTERNET" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + "_"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
                if (imageUri != null) {
                    photoPathArt = getRealPathFromURI(imageUri).toString()
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            photoPathArt = image.absolutePath
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(this, contentUri, proj, null, null, null)
        val cursor: Cursor? = loader.loadInBackground()
        val columnIndex: Int = cursor
            ?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            ?: return null
        cursor.moveToFirst()
        val result: String? = cursor.getString(columnIndex)
        cursor.close()
        return result
    }
}


