package com.katana.memo.memo.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.ShareActionProvider
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.katana.memo.memo.Helper.DatabaseHelper
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.R
import com.sromku.simple.storage.SimpleStorage
import com.sromku.simple.storage.Storage
import kotlinx.android.synthetic.main.memo_activity.*
import java.io.IOException



class Memo : AppCompatActivity() {

    lateinit var dbHelper: DatabaseHelper
    lateinit var memoTitle: String
    lateinit var memoBody: String
    lateinit var memoDate: String
    var memoLocation: String = ""
    lateinit var titleView: AppCompatEditText
    lateinit var bodyView: AppCompatEditText
    var memoId = -2
    lateinit var storage: Storage
    lateinit var imageNote: ImageView
    val a: Activity = this
    var mediaPlayer: MediaPlayer? = null
    var audioIcons: ArrayList<ImageView> = ArrayList()

    lateinit var shareActionProvider: ShareActionProvider

    lateinit var getImageNotes: AsyncTask<Void, Void, Void>
    lateinit var getAudioNotes: AsyncTask<Void, Void, Void>

    var imagePaths = ArrayList<String>()
    var audioPaths = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()
    }

    fun setUpActivity() {
        setContentView(R.layout.memo_activity)
        setSupportActionBar(memo_toolbar)
        StatusBarColor.changeStatusBarColor(this)

        dbHelper = DatabaseHelper(this)
        storage = SimpleStorage.getInternalStorage(this)

        initializeMemoData()

        setUpViews()
    }

    fun initializeMemoData() {
        if (intent.extras != null) {
            memoTitle = intent.getStringExtra("title")
            memoBody = intent.getStringExtra("body")
            memoId = intent.getIntExtra("id", -2)
            memoDate = intent.getStringExtra("date")
            memoLocation = intent.getStringExtra("location")
        }
    }

    fun setUpViews() {

        titleView = findViewById(R.id.memoTitle) as AppCompatEditText
        bodyView = findViewById(R.id.memoBody) as AppCompatEditText

        dateLabel.text = memoDate

        titleView.setText(memoTitle)
        bodyView.setText(memoBody)

        titleView.isEnabled = false
        bodyView.isEnabled = false

        getImageNotes = GetImageNotes().execute()
        getAudioNotes = GetAudioNotes().execute()
        GetLocations().execute()

    }


    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.memo_toolbar_items, menu)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.editButton -> {

                AsyncTask.execute {

                    val intent = Intent(this, CreateNoteActivity::class.java)
                    intent.putExtra("title", memoTitle)
                    intent.putExtra("body", memoBody)
                    intent.putExtra("id", memoId)
                    intent.putExtra("location", memoLocation);
                    intent.putExtra("imagePaths", imagePaths)
                    intent.putExtra("audioPaths", audioPaths)
                    startActivity(intent)
                }

                return true
            }

            R.id.shareButton -> {

                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, memoTitle + "\n" + memoBody)
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, memoTitle)
                startActivity(Intent.createChooser(sharingIntent, "Share using"))

                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun startPlaying(path: String) {

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener { audioIcons[audioPaths.indexOf(path)].setImageResource(R.drawable.ic_audio_icon) }
        try {
            mediaPlayer?.setDataSource(storage.getFile("Audios", path).absolutePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch(e: IOException) {
            e.printStackTrace()
        }


    }

    fun stopPlaying() {

        mediaPlayer?.release()
        mediaPlayer = null

    }

    fun onPlay(start: Boolean, path: String) {
        if (start) {
            audioIcons[audioPaths.indexOf(path)].setImageResource(R.drawable.ic_audio_icon_playing)
            startPlaying(path)
        } else {
            stopPlaying()
        }
    }

    override fun onStop() {
        super.onStop()

        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null

            for (icon in audioIcons) {
                icon.setImageResource(R.drawable.ic_audio_icon)
            }

        }
    }

    fun goToSpecificLocation(v: View) {

    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

    private inner class GetAudioNotes : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {

            if (dbHelper.getSpecificAudioPaths(memoId)[0] != "No audios found") {
                audioSectionTitle.setText(R.string.audios)
                audioPaths = dbHelper.getSpecificAudioPaths(memoId)

                val audioLinearLayoutParams = LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT)
                audioLinearLayoutParams.setMargins(20, 20, 20, 20)

                val audioIconLayoutParams = LinearLayout.LayoutParams(150, 150)
                audioIconLayoutParams.setMargins(10, 10, 10, 10)
                audioIconLayoutParams.gravity = Gravity.CENTER_HORIZONTAL

                for (path in audioPaths) {
                    val linLayout = LinearLayout(a)
                    linLayout.layoutParams = audioLinearLayoutParams
                    linLayout.setPadding(4, 4, 4, 4)
                    linLayout.orientation = LinearLayout.VERTICAL
                    noteAudiosSection.addView(linLayout)

                    val singleAudioIcon = ImageView(a)
                    singleAudioIcon.layoutParams = audioIconLayoutParams
                    singleAudioIcon.setImageResource(R.drawable.ic_audio_icon)
                    linLayout.addView(singleAudioIcon)

                    audioIcons.add(singleAudioIcon)

                    val audioName = AppCompatTextView(a)
                    audioName.layoutParams = audioLinearLayoutParams
                    audioName.text = path
                    linLayout.addView(audioName)

                    var mStartPlaying = true

                    linLayout.setOnClickListener {
                        onPlay(mStartPlaying, path)
                        mStartPlaying = !mStartPlaying
                    }

                }

            }

            return null
        }
    }

    private inner class GetImageNotes : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {

            getImageNote()

            return null
        }

        fun getImageNote() {

            if (dbHelper.getSpecificImagePaths(memoId)[0] != "No images found") {

                imagePaths = dbHelper.getSpecificImagePaths(memoId)

                val drawableImages = ArrayList<Drawable>()

                for (item in imagePaths) run {
                    drawableImages.add(Drawable.createFromPath(storage.getFile("Images", item).path))
                }

                val imageLayoutParams = LinearLayout.LayoutParams(450, 450)
                imageLayoutParams.setMargins(10, 10, 10, 10)



                a.runOnUiThread {

                    photoSectionTitle.setText(R.string.photos)

                    // create images and put them the image from the phone memory
                    for (dr in drawableImages) {

                        imageNote = ImageView(a)
                        imageNote.layoutParams = imageLayoutParams
                        imageNote.setImageDrawable(dr)
                        noteImagesSection.addView(imageNote)

                        imageNote.setOnClickListener {
                            val i = Intent(a, ImagePagerView::class.java)
                            i.putExtra("images", imagePaths)
                            a.startActivity(i)
                        }

                    }

                }


            }
        }

    }

    private inner class GetLocations : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            if (dbHelper.getSpecificLocation(memoId) != "No location found") {
                a.runOnUiThread {
                    memoLocationCardView.visibility = View.VISIBLE
                    memoLocationTextLabel.text = memoLocation
                    memoLocationCardView.setOnLongClickListener {

                        true
                    }
                }
            }

            return null
        }

    }

}
