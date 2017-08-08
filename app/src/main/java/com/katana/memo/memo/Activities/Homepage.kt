package com.katana.memo.memo.Activities

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.TextView
import br.com.mauker.materialsearchview.MaterialSearchView
import com.desai.vatsal.mydynamictoast.MyDynamicToast
import com.katana.memo.memo.Adapters.HomepageAdapter
import com.katana.memo.memo.Helper.DatabaseHelper
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.Models.MemoModel
import com.katana.memo.memo.R
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener
import com.sromku.simple.storage.SimpleStorage
import com.sromku.simple.storage.Storage
import com.sromku.simple.storage.helpers.OrderType
import kotlinx.android.synthetic.main.homepage_acitivty.*
import java.io.File

class Homepage : AppCompatActivity(), RecyclerTouchListener.OnRowLongClickListener, RecyclerTouchListener.OnSwipeOptionsClickListener, RecyclerTouchListener.OnRowClickListener, AdapterView.OnItemClickListener, MaterialSearchView.OnQueryTextListener {

    val TAG: String = "HomePage"
    lateinit var mAdapter: HomepageAdapter
    lateinit var onTouchListener: RecyclerTouchListener
    lateinit var dbHelper: DatabaseHelper
    lateinit var storage: Storage
    lateinit var suggestedTitle: String
    lateinit var hoverMemoDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        Thread().run {
            val getPrefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
            val isFirstStart = getPrefs.getBoolean("firstStart", true)

            if (isFirstStart) {
                val intent: Intent = Intent(applicationContext, IntroActivity::class.java)
                AsyncTask.execute {
                    startActivity(intent)
                }
            }


            val e: SharedPreferences.Editor = getPrefs.edit()

            e.putBoolean("firstStart", false)
            e.apply()
        }
        super.onCreate(savedInstanceState)
        setUpActivity()


    }

    override fun onStart() {
        super.onStart()

        if (!canAccessAllPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1)
        }

    }

    override fun onResume() {
        super.onResume()
        recyclerView.addOnItemTouchListener(onTouchListener)
        mAdapter.reloadRecyclerView(getData())
    }

    override fun onPause() {
        super.onPause()
        recyclerView.removeOnItemTouchListener(onTouchListener)
        dbHelper.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    fun setUpActivity() {
        setContentView(R.layout.homepage_acitivty)
        setSupportActionBar(appToolbar)
        appToolbar.setBackgroundResource(R.drawable.gradient_list)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        StatusBarColor.changeStatusBarColor(this)

        val animationDrawable: AnimationDrawable = homepage_root_layout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        storage = SimpleStorage.getInternalStorage(this)

        addNewSuggestion()

        checkFolders()

        val imageFiles: List<File> = storage.getFiles("Images", OrderType.DATE)
        val audioFiles: List<File> = storage.getFiles("Audios", OrderType.DATE)

        Log.d("File_audio_size", " " + audioFiles.size)
        Log.d("File_image_size:", " " + imageFiles.size)

        dbHelper = DatabaseHelper(this)

        setUpViews()
    }

    fun setUpViews() {

        hoverMemoDialog = Dialog(this)

        mAdapter = HomepageAdapter(this, getData())
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()


        onTouchListener = RecyclerTouchListener(this, recyclerView)

        onTouchListener
                .setIndependentViews(R.id.favoriteButton)
                .setViewsToFade(R.id.favoriteButton)
                .setClickable(true)
                .setClickable(this)
                .setSwipeable(true)
                .setSwipeOptionViews(R.id.edit, R.id.delete)
                .setSwipeable(R.id.rowFG, R.id.rowBG, this)
                .setLongClickable(true)
                .setLongClickable(true, this)


        addMemoFab.setOnClickListener { bottom_sheet.expandFab() }
        addMemoFab.attachToRecyclerView(recyclerView)
        animateFab(addMemoFab)

        bottom_sheet.setFab(addMemoFab)
        bottom_sheet.setFabAnimationEndListener { startActivityForResult(Intent(this, CreateNoteActivity::class.java), 1) }

        initializeSearchView()

    }

    fun animateFab(v: View) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.fab_slide_right_animation)
        v.startAnimation(anim)
    }

    fun initializeSearchView() {
        search_view.setOnItemClickListener(this)
        search_view.setOnQueryTextListener(this)
        search_view.setShouldKeepHistory(false)
        search_view.setVoiceIcon(0)
        search_view.adjustTintAlpha(0.9f)
        refreshSuggestions(dbHelper.memoTitles)

    }

    fun refreshSuggestions(list: ArrayList<String>) {

        AsyncTask.execute {
            if (list.size > 0) {
                search_view.clearSuggestions()
                search_view.addSuggestions(list)
            }
        }

    }

    fun addNewSuggestion() {
        AsyncTask.execute {
            if (intent.getStringExtra("suggestion") != null) {
                suggestedTitle = intent.getStringExtra("suggestion")
            }
        }

    }

    fun checkFolders() {

        AsyncTask.execute {
            if (!storage.isDirectoryExists("Images")) {
                storage.createDirectory("Images")
            }

            if (!storage.isDirectoryExists("Audios")) {
                storage.createDirectory("Audios")
            }

        }

    }

    fun getData(): ArrayList<MemoModel> {
        val list = ArrayList<MemoModel>(dbHelper.theAmountOfMemos)

//        (1..dbHelper.theAmountOfMemos)
//                .filter { dbHelper.getSpecificMemoTitle(it) != "Title not found!" }
//                .mapTo(list) { MemoModel(dbHelper.getSpecificMemoTitle(it), dbHelper.getSpecificMemoBody(it), it) }
//

        for (i: Int in dbHelper.theAmountOfMemos downTo 1) {
            list.add(MemoModel(dbHelper.getSpecificMemoTitle(i), dbHelper.getSpecificMemoBody(i), i))
            Log.d("items", dbHelper.getSpecificMemoTitle(i))
        }


        return list
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_app_bar_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.deleteNotes -> {
                deleteAllMemos()
                return true

            }
            R.id.appIntro -> {
                startActivity(Intent(this, IntroActivity::class.java))
                return true

            }
            R.id.favorites -> {
                startActivity(Intent(this, Favorites::class.java))
                return true
            }
            R.id.action_search -> {
                search_view.openSearch()
                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {

        if (hoverMemoDialog.isShowing) {
            hoverMemoDialog.dismiss()
        } else if (search_view.isOpen) {
            search_view.closeSearch()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.finishAffinity()
            super.onBackPressed()
        }
    }

    fun deleteSpecificPhoto(paths: ArrayList<String>) {
        if (paths[0] != "No Images Found") {
            try {

                for (path in paths) {
                    storage.deleteFile("Images", path)
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
        } else {
            MyDynamicToast.errorMessage(this, "There is no image")
        }
    }

    fun deleteSpecificAudio(paths: ArrayList<String>) {
        if (paths[0] != "No audio found") {
            try {
                for (path in paths) {
                    storage.deleteFile("Audios", path)
                }
            } catch(e: Exception) {
                MyDynamicToast.errorMessage(this, e.message)
            }
        }
    }

    fun deletePhotos() {
        val files: List<File> = storage.getFiles("Images", OrderType.DATE)

        for (index in files.indices) {
            files[index].delete()
        }

    }

    fun deleteAudios() {
        val files: List<File> = storage.getFiles("Audios", OrderType.DATE)

        for (index in files.indices) {
            files[index].delete()
        }

    }

    private fun deleteAllMemos() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Do you want to delete all the memos?")
                .setCancelable(false)
                .setPositiveButton("Yes I do",
                        DialogInterface.OnClickListener { dialog, id ->
                            dbHelper.deleteMemos()
                            mAdapter.reloadRecyclerView(getData())
                            search_view.clearSuggestions()
                            deletePhotos()
                            deleteAudios()
                        })
        alertDialogBuilder.setNegativeButton("Nope",
                DialogInterface.OnClickListener { dialog, id ->
                    run {
                        dialog.cancel()
                    }
                })
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun hasPermission(perm: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm)
        } else {
            return false
        }
    }

    fun canAccessAllPermissions(): Boolean {
        return (hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                &&
                hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                &&
                hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                &&
                hasPermission(android.Manifest.permission.RECORD_AUDIO)
                &&
                hasPermission(android.Manifest.permission.CAMERA)
                &&
                hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                &&
                hasPermission(android.Manifest.permission.INTERNET)
                )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            bottom_sheet.contractFab()
        }
    }

    // SearchView event listeners

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        AsyncTask.execute {
            val titleResult: String = search_view.getSuggestionAtPosition(position)
            val idFromResult: Int = dbHelper.getMemoIdFromTitle(titleResult)

            val intent = Intent(this, Memo::class.java)
            intent.putExtra("id", idFromResult)
            intent.putExtra("title", titleResult)
            intent.putExtra("body", dbHelper.getSpecificMemoBody(idFromResult))
            intent.putExtra("date", dbHelper.getSpecificMemoDate(idFromResult))
            intent.putExtra("location", dbHelper.getSpecificLocation(idFromResult))
            startActivity(intent)
        }


    }

    override fun onQueryTextSubmit(query: String): Boolean {

        search_view.closeSearch()

        AsyncTask.execute {
            var emptyMemoArray: Boolean = true
            var emptyFavoriteMemoArray: Boolean = true

            val memoNumbers = dbHelper.memoTitles.size
            val favoriteMemoNumbers = dbHelper.favoriteMemoTitles.size

            val resultMemosArray: ArrayList<String> = ArrayList()
            val resultFavoriteMemosArray: ArrayList<String> = ArrayList()

            val intent: Intent = Intent(this, SearchResults::class.java)

            for (i: Int in 0..memoNumbers - 1) {
                if (dbHelper.memoTitles[i].contains(query)) {
                    emptyMemoArray = false
                    resultMemosArray.add(dbHelper.memoTitles[i])
                }
            }

            for (i: Int in 0..favoriteMemoNumbers - 1) {
                if (dbHelper.favoriteMemoTitles[i].contains(query)) {
                    emptyFavoriteMemoArray = false
                    resultFavoriteMemosArray.add(dbHelper.favoriteMemoTitles[i])
                }
            }

            if (!emptyMemoArray || !emptyFavoriteMemoArray) {
                val b: Bundle = Bundle()

                b.putStringArrayList("memos", resultMemosArray)
                b.putStringArrayList("favoriteMemos", resultFavoriteMemosArray)

                intent.putExtras(b)
                startActivity(intent)
            } else if (emptyMemoArray && emptyFavoriteMemoArray) {
                runOnUiThread { MyDynamicToast.informationMessage(this, "No memos found") }

            }
        }



        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {


        return false
    }


    // Recyclerview eventlisteners

    override fun onRowClicked(position: Int) {
        val itemId = dbHelper.theAmountOfMemos - position
        AsyncTask.execute {
            val intent = Intent(this, Memo::class.java)
            intent.putExtra("id", (itemId))
            intent.putExtra("title", dbHelper.getSpecificMemoTitle(itemId))
            intent.putExtra("body", dbHelper.getSpecificMemoBody(itemId))
            intent.putExtra("date", dbHelper.getSpecificMemoDate(itemId))
            intent.putExtra("location", dbHelper.getSpecificLocation(itemId))

            startActivity(intent)
        }

    }

    override fun onRowLongClicked(position: Int) {

        val itemId = dbHelper.theAmountOfMemos - position

        MyDynamicToast.informationMessage(this, "Triggered")
        hoverMemoDialog = Dialog(this)
        hoverMemoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        hoverMemoDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        hoverMemoDialog.setContentView(R.layout.hover_memo)

        val hoverTitle: TextView = hoverMemoDialog.findViewById(R.id.hoverMemoTitle) as TextView
        val hoverBody: TextView = hoverMemoDialog.findViewById(R.id.hoverMemoBody) as TextView

        hoverTitle.text = dbHelper.getSpecificMemoTitle(itemId)
        hoverBody.text = dbHelper.getSpecificMemoBody(itemId)
        hoverMemoDialog.show()
    }

    override fun onIndependentViewClicked(independentViewID: Int, position: Int) {
        val itemId = dbHelper.theAmountOfMemos - position
        dbHelper.favoriteMemo(itemId)

        mAdapter.reloadRecyclerView(getData())

    }

    override fun onSwipeOptionClicked(viewID: Int, position: Int) {

        val itemId = dbHelper.theAmountOfMemos - position

        when (viewID) {

            R.id.edit -> {

                AsyncTask.execute {
                    intent = Intent(this, CreateNoteActivity::class.java)
                    intent.putExtra("id", itemId)
                    intent.putExtra("title", dbHelper.getSpecificMemoTitle(itemId))
                    intent.putExtra("body", dbHelper.getSpecificMemoBody(itemId))
                    intent.putExtra("location", dbHelper.getSpecificLocation(itemId))

                    if (dbHelper.getSpecificAudioPaths(itemId)[0] != "No audios found") {
                        intent.putExtra("audioPaths", dbHelper.getSpecificAudioPaths(itemId))
                    }

                    if (dbHelper.getSpecificImagePaths(itemId)[0] != "No images found") {
                        intent.putExtra("imagePaths", dbHelper.getSpecificImagePaths(itemId))
                    }

                    startActivity(intent)
                }


            }

            R.id.delete -> {


                search_view.removeSuggestion(dbHelper.getSpecificMemoTitle(dbHelper.theAmountOfMemos - position))

                deleteSpecificPhoto(dbHelper.getSpecificImagePaths(itemId))
                deleteSpecificAudio(dbHelper.getSpecificAudioPaths(itemId))

                dbHelper.deleteSpecificMemo(itemId)
                mAdapter.reloadRecyclerView(getData())
            }
        }
    }


}
