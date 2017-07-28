package com.katana.memo.memo.Activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.katana.memo.memo.Adapters.FavoritesAdapter
import com.katana.memo.memo.Helper.DatabaseHelper
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.R
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener
import kotlinx.android.synthetic.main.favorites_activity.*

class Favorites : AppCompatActivity(), RecyclerTouchListener.OnRowClickListener {

    lateinit var onTouchListener: RecyclerTouchListener

    lateinit var dbHelper: DatabaseHelper
    lateinit var mAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()
    }

    fun setUpActivity() {
        setContentView(R.layout.favorites_activity)
        setSupportActionBar(favoriteNotesToolbar)
        favoriteNotesToolbar.setBackgroundResource(R.drawable.gradient_list)
        StatusBarColor.changeStatusBarColor(this)

        dbHelper = DatabaseHelper(this)

        setUpViews()
    }

    override fun onRowClicked(position: Int) {
        AsyncTask.execute {
            val intent = Intent(this, Memo::class.java)

            val id: Int = mAdapter.getMemoIdOfView(position)

            intent.putExtra("id",id)
            intent.putExtra("title", dbHelper.getSpecificMemoTitle(id))
            intent.putExtra("body", dbHelper.getSpecificMemoBody(id))
            intent.putExtra("date", dbHelper.getSpecificMemoDate(id))
            intent.putExtra("location", dbHelper.getSpecificLocation(id))
            startActivity(intent)
        }

    }

    override fun onIndependentViewClicked(independentViewID: Int, position: Int) {

    }


    fun setUpViews() {
        mAdapter = FavoritesAdapter(this, dbHelper.favoriteMemos)
        favoritesRecyclerView.adapter = mAdapter
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)

        onTouchListener = RecyclerTouchListener(this, favoritesRecyclerView)
        onTouchListener.setClickable(true)
        onTouchListener.setClickable(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.favorite_notes_toolbar_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.clearFav -> {

                // Remove from favorites and Reload recyclerview
                dbHelper.clearFavoriteMemos()
                mAdapter.reloadRecyclerView(dbHelper.favoriteMemos)

                return true

            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        favoritesRecyclerView.addOnItemTouchListener(onTouchListener)
        mAdapter.reloadRecyclerView(dbHelper.favoriteMemos)
    }

    override fun onPause() {
        super.onPause()
        favoritesRecyclerView.removeOnItemTouchListener(onTouchListener)
    }


}
