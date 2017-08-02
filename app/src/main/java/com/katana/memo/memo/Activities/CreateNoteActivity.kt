package com.katana.memo.memo.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.desai.vatsal.mydynamictoast.MyDynamicToast
import com.katana.memo.memo.Activities.CreateNoteActivity.Constants.POSITION_CORRECTION
import com.katana.memo.memo.Helper.*
import com.katana.memo.memo.R
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import com.sromku.simple.storage.SimpleStorage
import com.sromku.simple.storage.Storage
import com.yalantis.ucrop.util.BitmapLoadUtils.calculateInSampleSize
import kotlinx.android.synthetic.main.create_note_activity.*
import xyz.dev_juyoung.cropicker.CroPicker
import xyz.dev_juyoung.cropicker.models.Media
import java.io.File
import java.io.IOException
import java.util.*


class CreateNoteActivity : android.support.v7.app.AppCompatActivity() {

    //private val TAG: String = "CreateNoteActivity"

    lateinit var dbHelper: DatabaseHelper
    lateinit var fabHelper: FabHelper
    lateinit var storage: Storage

    var recTitle: String? = null
    var recBody: String? = null
    var recId = -2

    var contentBG: Drawable? = null
    var needBlur = true
    var titleNeedBlur = true
    var bodyNeedBlur = true

    var noteImages: ArrayList<Bitmap?> = ArrayList()
    var imagePaths: ArrayList<String> = ArrayList()
    var noteImage: Bitmap? = null
    var imagePath: String = ""
    var originalExtImgPath = ""

    var audioPaths: ArrayList<String> = ArrayList()
    var audioPath = ""

    var mediaPlayer: MediaPlayer? = null

    lateinit var imageNote: ImageView

    var isBlockedScrollView: Boolean = false

    val a: AppCompatActivity = this

    var memoLocation: String = ""

    var memoTitleDefaultXPos = 0f
    var memoTitleDefaultYPos = 0f

    var animateToCenter = true

    object Constants {
        const val ANIMATION_DURATION = 300
        const val NUM_OF_SIDES = 5
        const val POSITION_CORRECTION = 11
    }

    fun enableBackground() {

        if (memoTitle.isFocused) {
            memoTitle.clearFocus()
        } else if (memoBody.isFocused) {
            memoBody.clearFocus()
        }

        memoTitle.isEnabled = true
        memoBody.isEnabled = true

        isBlockedScrollView = false
    }

    fun disableBackground(view: View?) {

        if (view == null) {
            memoTitle.isEnabled = true
            memoBody.isEnabled = true

        } else if (view.id == R.id.memoTitle) {

            memoBody.isEnabled = false
            memoTitle.isEnabled = true

            memoTitle.requestFocus()

        } else if (view.id == R.id.memoBody) {

            memoBody.isEnabled = true
            memoTitle.isEnabled = false

            memoBody.requestFocus()

        }

        isBlockedScrollView = true
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()
    }

    fun setUpActivity() {
        setContentView(com.katana.memo.memo.R.layout.create_note_activity)

        setSupportActionBar(createNoteToolbar)
        com.katana.memo.memo.Helper.StatusBarColor.Companion.changeStatusBarColor(this)
        dbHelper = DatabaseHelper(this)
        fabHelper = FabHelper(this)

        storage = SimpleStorage.getInternalStorage(this)

        getDataFromIntent()

        for (path in imagePaths) {
            Log.d("Image_item:", path)
        }

        for (path in audioPaths) {
            Log.d("Audio_item", path)
        }

        setUpViews()

        addPhotosToScrollView()

        addAudiosToScrollView()

        addLocationToScrollView(memoLocation)

    }

    fun getDataFromIntent() {


        if (intent.extras != null) {
            recTitle = intent.getStringExtra("title")
            recBody = intent.getStringExtra("body")
            recId = intent.getIntExtra("id", -2)
            memoLocation = intent.getStringExtra("location")
        }

        if (intent.getStringArrayListExtra("imagePaths") != null) {
            imagePaths = intent.getStringArrayListExtra("imagePaths")
        }

        if (intent.getStringArrayListExtra("audioPaths") != null) {
            audioPaths = intent.getStringArrayListExtra("audioPaths")
        }

    }

    fun setUpViews() {


        memoTitleDefaultXPos = memoTitle.x
        memoTitleDefaultYPos = memoTitle.y

        if (recTitle?.length != 0) {
            memoTitle.setText(recTitle)

            memoTitle.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    Animations.onAnimateEdiText(animateToCenter, textFieldsLayout, memoTitle, memoTitleDefaultXPos, memoTitleDefaultYPos)
                    animateToCenter = !animateToCenter
                    blurExceptMemoTitle()
                    disableBackground(memoTitle)

                }
            }

            memoTitle.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    Animations.onAnimateEdiText(animateToCenter, textFieldsLayout, memoTitle, memoTitleDefaultXPos, memoTitleDefaultYPos)
                    animateToCenter = !animateToCenter
                    blurExceptMemoTitle()
                    disableBackground(memoTitle)
                }

                true
            }

            memoBody.setText(recBody)
        }

        // Me keto rreshta bej qe editText-i te jete Scrollable
        memoBody.setOnTouchListener { v, event ->

            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }

            false
        }

        add_data_fab.setOnClickListener(fabHelper)
        fabHelper.calculatePentagonVertices(fabHelper.radius, POSITION_CORRECTION)
        Animations.animateFab(add_data_fab, this)

        // Ketu e bej nestedScrollView scrollable ose jo ne baze te vleres se variables isBlockedScrollView
        nestedScrollView.setOnTouchListener({ _, _ -> isBlockedScrollView })

    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(com.katana.memo.memo.R.menu.create_note_app_bar_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem?): Boolean {
        when (item?.itemId) {
            com.katana.memo.memo.R.id.doneButton -> {

                // validate the fields and save the memo
                saveMemo()
                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun goToSpecificLocation(v: View) {

    }

    override fun onBackPressed() {

        if (fabHelper.isFabOpened()) {
            fabHelper.closeFab()
        } else if (!animateToCenter) {
            blurExceptMemoTitle()
            Animations.onAnimateEdiText(animateToCenter, textFieldsLayout, memoTitle, memoTitleDefaultXPos, memoTitleDefaultYPos)
            animateToCenter = !animateToCenter
            enableBackground()
        } else {
            deleteSpecificPhoto(imagePath)
            deleteSpecificAudio(audioPath)
            super.onBackPressed()
        }

    }

    fun deleteSpecificAudio(path: String) {
        if (path != "") {
            try {

                AsyncTask.execute {
                    storage.deleteFile("Audios", path)
                    audioPaths.remove(path)
                }

                MyDynamicToast.informationMessage(this, path + " deleted.")

            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSpecificPhoto(path: String) {
        if (path != "") {
            try {

                AsyncTask.execute {
                    storage.deleteFile("Images", path)
                    imagePaths.remove(path)
                }

                MyDynamicToast.informationMessage(this, path + " deleted.")
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.DRAW_IMAGE -> {

                if (data != null) {

                    val tempImagePath = ArrayList<String>()
                    val tempNoteImages = ArrayList<Bitmap?>()

                    noteImage = decodeSampledBitmapFromByteArray(data.getByteArrayExtra("byteArray"), 400, 400)
                    imagePath = "noteImage" + generateRandomNumber()

                    tempNoteImages.add(noteImage)
                    tempImagePath.add(imagePath)

                    addPhotosToMemo(tempNoteImages, tempImagePath)
                    addPhotoToScrollView(noteImage, imagePath)
                }

            }

            AppConstants.ADD_AUDIO -> {
                if (data != null) {

                    audioPath = data.getStringExtra("audioPath")

                    audioPaths.add(audioPath)

                    addAudioToScrollView(audioPath)

                }
            }

            AppConstants.ADD_LOCATION -> {
                if (data != null) {
                    MyDynamicToast.informationMessage(this, data.getStringExtra("streetAddress"))
                    memoLocation = data.getStringExtra("streetAddress")
                    addLocationToScrollView(memoLocation)
                }
            }

            AppConstants.CAPTURE_PHOTO -> {
                if (data != null) {

                    val tempImagePath = ArrayList<String>()
                    val tempNoteImages = ArrayList<Bitmap?>()

                    originalExtImgPath = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH)

                    noteImage = decodeSampledBitmapFromFile(originalExtImgPath, 400, 400)

                    val path = "KukaMemo" + originalExtImgPath.substring(originalExtImgPath.lastIndexOf("/") + 1)

                    tempImagePath.add(path)
                    tempNoteImages.add(noteImage)

                    addPhotosToMemo(tempNoteImages, tempImagePath)
                    addPhotoToScrollView(noteImage, path)


                }
            }

            CroPicker.REQUEST_ALBUM -> {
                if (data != null) {
                    val results: ArrayList<Media> = data.getParcelableArrayListExtra(CroPicker.EXTRA_RESULT_IMAGES)

                    val notePickedImages = ArrayList<Bitmap?>()
                    val pickedImagesPaths = ArrayList<String>()

                    for (item: Media in results) {
                        notePickedImages.add(decodeSampledBitmapFromFile(item.imagePath, 300, 300))
                        pickedImagesPaths.add((item.imagePath.substring(item.imagePath.lastIndexOf("/") + 1)) + "memoAddedImage")
                    }

                    addPhotosToMemo(notePickedImages, pickedImagesPaths)
                    addPhotosToScrollView(notePickedImages, pickedImagesPaths)

                }
            }

        }
    }

    fun addLocationToScrollView(location: String) {

        if (location != "") {
            if (location != "No location found") {
                locationCardView.visibility = View.VISIBLE
                locationTextLabel.text = location
                locationCardView.setOnLongClickListener {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setMessage("Do you want to delete this location?")
                            .setCancelable(false)
                            .setPositiveButton("Yes I do",
                                    { _, _ ->
                                        locationCardView.visibility = View.GONE
                                        memoLocation = ""
                                    })
                    alertDialogBuilder.setNegativeButton("Nope",
                            { dialog, _ ->
                                run {
                                    dialog.cancel()
                                }
                            })
                    val alert = alertDialogBuilder.create()
                    alert.show()

                    true
                }
            }
        }
    }

    fun addPhotosToMemo(images: ArrayList<Bitmap?>, paths: ArrayList<String>) {

        AsyncTask.execute {
            for (index in images.indices) {
                storage.createFile("Images", paths[index], images[index])
            }


            for (path in paths) {
                imagePaths.add(path)
            }

            for (img in images) {
                noteImages.add(img)
            }



            if (originalExtImgPath != "" && originalExtImgPath != "Deleted") {

                val extImg = File(originalExtImgPath)
                if (extImg.exists()) {
                    extImg.delete()
                    originalExtImgPath = "Deleted"
                }
            }

        }

    }

    fun addAudioToScrollView(path: String) {
        audioSectionTitleAtCreateNote.setText(R.string.audios)

        val audioLinearLayoutParams = LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT)
        audioLinearLayoutParams.setMargins(20, 20, 20, 20)

        val linLayout = LinearLayout(a)
        linLayout.setPadding(4, 4, 4, 4)
        linLayout.layoutParams = audioLinearLayoutParams
        linLayout.orientation = LinearLayout.VERTICAL
        noteAudiosSectionAtCreateNote.addView(linLayout)

        val delAudioButtonParams = LinearLayout.LayoutParams(100, 100)
        delAudioButtonParams.setMargins(3, 3, 3, 100)
        delAudioButtonParams.gravity = Gravity.CENTER_HORIZONTAL

        val delAudioButton = Button(a)
        delAudioButton.layoutParams = delAudioButtonParams
        delAudioButton.setBackgroundDrawable(a.resources.getDrawable(R.drawable.ic_delete_image))

        val audioName = AppCompatTextView(this)
        audioName.layoutParams = audioLinearLayoutParams
        audioName.text = path

        val audioIconLayoutParams = LinearLayout.LayoutParams(150, 150)
        audioIconLayoutParams.setMargins(10, 10, 10, 10)
        audioIconLayoutParams.gravity = Gravity.CENTER_HORIZONTAL

        val audioIcon = ImageView(this)
        audioIcon.layoutParams = audioIconLayoutParams
        audioIcon.setImageResource(R.drawable.ic_audio_icon)


        linLayout.addView(delAudioButton)
        linLayout.addView(audioIcon)
        linLayout.addView(audioName)

        delAudioButton.setOnClickListener {
            linLayout.removeView(audioName)
            linLayout.removeView(delAudioButton)
            noteAudiosSectionAtCreateNote.removeView(linLayout)
            deleteSpecificAudio(path)
        }

        var mStartPlaying = true

        linLayout.setOnClickListener {
            onPlay(mStartPlaying, path)
            mStartPlaying = !mStartPlaying
        }

    }

    fun addAudiosToScrollView() {
        if (audioPaths.size > 0) {
            val audioLinearLayoutParams = LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT)
            audioLinearLayoutParams.setMargins(20, 20, 20, 20)

            val delAudioButtonParams = LinearLayout.LayoutParams(100, 100)
            delAudioButtonParams.setMargins(3, 3, 3, 100)
            delAudioButtonParams.gravity = Gravity.CENTER_HORIZONTAL

            val audioIconLayoutParams = LinearLayout.LayoutParams(150, 150)
            audioIconLayoutParams.setMargins(10, 10, 10, 10)
            audioIconLayoutParams.gravity = Gravity.CENTER_HORIZONTAL

            for (path in audioPaths) run {

                val linearLayout = LinearLayout(this)
                linearLayout.setPadding(4, 4, 4, 4)
                linearLayout.layoutParams = audioLinearLayoutParams
                linearLayout.orientation = LinearLayout.VERTICAL
                noteAudiosSectionAtCreateNote.addView(linearLayout)

                val delAudioButton = Button(this)
                delAudioButton.layoutParams = delAudioButtonParams
                delAudioButton.setBackgroundDrawable(a.resources.getDrawable(R.drawable.ic_delete_image))
                linearLayout.addView(delAudioButton)

                val audioIcon = ImageView(this)
                audioIcon.layoutParams = audioIconLayoutParams
                audioIcon.setImageResource(R.drawable.ic_audio_icon)
                linearLayout.addView(audioIcon)

                val audioName = AppCompatTextView(this)
                audioName.layoutParams = audioLinearLayoutParams
                audioName.text = path
                linearLayout.addView(audioName)

                delAudioButton.setOnClickListener {
                    linearLayout.removeAllViews()
                    noteAudiosSectionAtCreateNote.removeView(linearLayout)
                    deleteSpecificAudio(path)
                }

                var mStartPlaying = true

                linearLayout.setOnClickListener {
                    onPlay(mStartPlaying, path)
                    mStartPlaying = !mStartPlaying
                }

            }
        }
    }

    fun addPhotoToScrollView(img: Bitmap?, imgPath: String) {

        photoSectionTitleAtCreateNote.setText(R.string.photos)

        val imageLinearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        imageLinearLayoutParams.setMargins(20, 20, 20, 20)

        val linLayout = LinearLayout(a)
        linLayout.setPadding(4, 4, 4, 4)
        linLayout.orientation = LinearLayout.VERTICAL
        noteImagesSectionAtCreateNote.addView(linLayout)

        val delImgButtonParams = LinearLayout.LayoutParams(100, 100)
        delImgButtonParams.setMargins(3, 3, 3, 100)
        delImgButtonParams.gravity = Gravity.CENTER_HORIZONTAL

        val delImgButton = Button(a)
        delImgButton.layoutParams = delImgButtonParams
        delImgButton.setBackgroundDrawable(a.resources.getDrawable(R.drawable.ic_delete_image))
        linLayout.addView(delImgButton)

        val imageLayoutParams = LinearLayout.LayoutParams(450, 450)
        imageLayoutParams.setMargins(10, 10, 10, 10)



        imageNote = ImageView(this)
        imageNote.layoutParams = imageLayoutParams
        imageNote.setImageBitmap(img)
        linLayout.addView(imageNote)


        delImgButton.setOnClickListener {
            linLayout.removeView(imageNote)
            linLayout.removeView(delImgButton)
            noteImagesSectionAtCreateNote.removeView(linLayout)
            deleteSpecificPhoto(imgPath)
        }

    }


    fun addPhotosToScrollView() {
        if (imagePaths.size > 0) {

            val imageLinearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            imageLinearLayoutParams.setMargins(20, 20, 20, 20)

            val delImgButtonParams = LinearLayout.LayoutParams(100, 100)
            delImgButtonParams.setMargins(3, 3, 3, 100)
            delImgButtonParams.gravity = Gravity.CENTER_HORIZONTAL

            val imageLayoutParams = LinearLayout.LayoutParams(450, 450)
            imageLayoutParams.setMargins(10, 10, 10, 10)


            for (item in imagePaths) run {
                val drawableImage = Drawable.createFromPath(storage.getFile("Images", item).path)

                val linearLayout = LinearLayout(this)
                linearLayout.setPadding(4, 4, 4, 4)
                linearLayout.orientation = LinearLayout.VERTICAL
                noteImagesSectionAtCreateNote.addView(linearLayout)

                val delImgButton = Button(a)
                delImgButton.layoutParams = delImgButtonParams
                delImgButton.setBackgroundDrawable(a.resources.getDrawable(R.drawable.ic_delete_image))
                linearLayout.addView(delImgButton)


                imageNote = ImageView(this)
                imageNote.layoutParams = imageLayoutParams
                imageNote.setImageDrawable(drawableImage)
                linearLayout.addView(imageNote)

                delImgButton.setOnClickListener {

                    linearLayout.removeView(imageNote)
                    linearLayout.removeView(delImgButton)
                    noteImagesSectionAtCreateNote.removeView(linearLayout)
                    deleteSpecificPhoto(item)

                }

            }

        }
    }

    fun addPhotosToScrollView(imgArray: ArrayList<Bitmap?>, imgPaths: ArrayList<String>) {

        photoSectionTitleAtCreateNote.setText(R.string.photos)

        val imageLinearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        imageLinearLayoutParams.setMargins(20, 20, 20, 20)

        val delImgButtonParams = LinearLayout.LayoutParams(100, 100)
        delImgButtonParams.setMargins(3, 3, 3, 100)
        delImgButtonParams.gravity = Gravity.CENTER_HORIZONTAL

        val imageLayoutParams = LinearLayout.LayoutParams(450, 450)
        imageLayoutParams.setMargins(10, 10, 10, 10)


        for (img in imgArray) {

            val linLayout = LinearLayout(a)
            linLayout.setPadding(4, 4, 4, 4)
            linLayout.orientation = LinearLayout.VERTICAL
            noteImagesSectionAtCreateNote.addView(linLayout)

            val delImgButton = Button(a)
            delImgButton.layoutParams = delImgButtonParams
            delImgButton.setBackgroundDrawable(a.resources.getDrawable(R.drawable.ic_delete_image))
            linLayout.addView(delImgButton)

            imageNote = ImageView(this)
            imageNote.layoutParams = imageLayoutParams
            imageNote.setImageBitmap(img)
            linLayout.addView(imageNote)

            delImgButton.setOnClickListener {
                linLayout.removeView(imageNote)
                linLayout.removeView(delImgButton)
                noteImagesSectionAtCreateNote.removeView(linLayout)
                deleteSpecificPhoto(imgPaths[imgArray.indexOf(img)])
            }

        }


    }

    fun decodeSampledBitmapFromFile(path: String,
                                    reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    fun decodeSampledBitmapFromByteArray(byte: ByteArray, width: Int, height: Int): Bitmap? {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeByteArray(byte, 0, byte.size, options)

        options.inSampleSize = calculateInSampleSize(options, width, height)

        options.inJustDecodeBounds = false

        return BitmapFactory.decodeByteArray(byte, 0, byte.size, options)

    }


    fun generateRandomNumber(): Int {

        val rand: Random = Random()
        val number = rand.nextInt(382901380) + 1

        return number
    }

    fun launchCamera() {
        SandriosCamera(this, AppConstants.CAPTURE_PHOTO)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO) // default is CameraConfiguration.MEDIA_ACTION_BOTH
                .enableImageCropping(true) // Default is false.
                .launchCamera()
    }

    fun saveMemo() {
        SaveMemo().execute()
    }

    fun applyBlur() {
        val view = a.findViewById(android.R.id.content).rootView

        if (view.width > 0) {
            contentBG ?: let { contentBG = view.background }
            if (needBlur) {

                val layout = BitmapDrawable(resources, BlurBuilder.blur(view))
                window.setBackgroundDrawable(layout)
                memoTitle.alpha = 0.1f
                memoBody.alpha = 0.1f
                photosLinearLayoutSection.alpha = 0.1f
                audiosLinearLayoutSection.alpha = 0.1f

                needBlur = false
            } else {

                window.setBackgroundDrawable(contentBG)
                memoTitle.alpha = 1.0f
                memoBody.alpha = 1.0f
                photosLinearLayoutSection.alpha = 1.0f
                audiosLinearLayoutSection.alpha = 1.0f


                needBlur = true
            }


        } else {
            view.viewTreeObserver.addOnGlobalLayoutListener({

                val image = BlurBuilder.blur(view)
                window.setBackgroundDrawable(BitmapDrawable(a.resources, image))

            })
        }
    }

    fun blurExceptMemoTitle() {
        val view = a.findViewById(android.R.id.content).rootView

        if (view.width > 0) {

            contentBG ?: let { contentBG = view.background }

            if (titleNeedBlur) {

                titleNeedBlur = false

                val layout = BitmapDrawable(resources, BlurBuilder.blur(view))
                window.setBackgroundDrawable(layout)
                memoBody.alpha = 0.1f
                photosLinearLayoutSection.alpha = 0.1f
                audiosLinearLayoutSection.alpha = 0.1f
                add_data_fab.alpha = 0.1f
                add_data_fab.isEnabled = false

            } else {

                window.setBackgroundDrawable(contentBG)
                memoBody.alpha = 1.0f
                photosLinearLayoutSection.alpha = 1.0f
                audiosLinearLayoutSection.alpha = 1.0f
                add_data_fab.alpha = 1.0f
                add_data_fab.isEnabled = true

                titleNeedBlur = true
            }

        } else {
            view.viewTreeObserver.addOnGlobalLayoutListener({

                val image = BlurBuilder.blur(view)
                window.setBackgroundDrawable(BitmapDrawable(a.resources, image))

            })
        }

    }

    fun blurExceptMemoBody() {
        val view = a.findViewById(android.R.id.content).rootView

        if (view.width > 0) {

            contentBG ?: let { contentBG = view.background }

            if (bodyNeedBlur) {

                titleNeedBlur = false
                blurExceptMemoTitle()

                val layout = BitmapDrawable(resources, BlurBuilder.blur(view))
                window.setBackgroundDrawable(layout)
                memoTitle.alpha = 0.1f
                photosLinearLayoutSection.alpha = 0.1f
                audiosLinearLayoutSection.alpha = 0.1f

            } else {

                window.setBackgroundDrawable(contentBG)
                memoTitle.alpha = 1.0f
                photosLinearLayoutSection.alpha = 1.0f
                audiosLinearLayoutSection.alpha = 1.0f

            }

        } else {
            view.viewTreeObserver.addOnGlobalLayoutListener({

                val image = BlurBuilder.blur(view)
                window.setBackgroundDrawable(BitmapDrawable(a.resources, image))

            })
        }
    }

    fun startPlaying(path: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
        }
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
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private inner class SaveMemo : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {

            // Validate fields
            if (memoTitle.text.toString().trim().isNullOrEmpty() || memoBody.text.toString().trim().isNullOrEmpty()) {

                a.runOnUiThread {
                    MyDynamicToast.warningMessage(a, "One of the fields is Empty")
                    val intent = Intent(a, Homepage::class.java)
                    intent.putExtra("suggestion", title)
                }

            } else {

                val title = memoTitle.text.toString()
                val body = memoBody.text.toString()

                // save memo to the database
                if (recId != -2) {
                    dbHelper.changeMemo(title, body, recId, imagePaths, audioPaths, memoLocation)
                    finish()
                    startActivity(Intent(a, Homepage::class.java))
                } else {
                    dbHelper.addMemo(title, body, dbHelper.theAmountOfMemos + 1, imagePaths, audioPaths, memoLocation)
                    // Return to the home page
                    finish()
                    startActivity(Intent(a, Homepage::class.java))
                }


            }

            return null
        }
    }


}

