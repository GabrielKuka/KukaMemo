package com.katana.memo.memo.Activities

import android.appwidget.AppWidgetManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.katana.memo.memo.Adapters.FavoritesAdapter
import com.katana.memo.memo.Helper.DatabaseHelper
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.Models.MemoModel
import com.katana.memo.memo.R
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener
import kotlinx.android.synthetic.main.add_widget_activity.*

class AddWidget : AppCompatActivity(), RecyclerTouchListener.OnRowClickListener {

    lateinit var onTouchListener: RecyclerTouchListener

    lateinit var dbHelper: DatabaseHelper
    lateinit var mAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()
    }

    override fun onResume() {
        super.onResume()
        chooseWidgetRecyclerView.addOnItemTouchListener(onTouchListener)
        mAdapter.reloadRecyclerView(getData())
    }

    override fun onPause() {
        super.onPause()
        chooseWidgetRecyclerView.removeOnItemTouchListener(onTouchListener)
    }

    fun setUpActivity() {
        setContentView(R.layout.add_widget_activity)
        setSupportActionBar(chooseWidgetToolbar)
        chooseWidgetToolbar.setBackgroundResource(R.drawable.gradient_list)
        StatusBarColor.changeStatusBarColor(this)

        dbHelper = DatabaseHelper(this)


        setUpViews()
    }

    fun setUpViews() {
        mAdapter = FavoritesAdapter(this, getData())
        chooseWidgetRecyclerView.adapter = mAdapter
        chooseWidgetRecyclerView.layoutManager = LinearLayoutManager(this)

        onTouchListener = RecyclerTouchListener(this, chooseWidgetRecyclerView)
        onTouchListener.setClickable(true)
        onTouchListener.setClickable(this)
    }

    fun getData(): ArrayList<MemoModel> {
        val list = ArrayList<MemoModel>(dbHelper.theAmountOfMemos)

        for (i: Int in dbHelper.theAmountOfMemos downTo 1) {
            list.add(MemoModel(dbHelper.getSpecificMemoTitle(i), dbHelper.getSpecificMemoBody(i), i))
            Log.d("items", dbHelper.getSpecificMemoTitle(i))
        }


        return list
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onRowClicked(position: Int) {
        AsyncTask.execute {

            val itemId = dbHelper.theAmountOfMemos - position
            val remoteViews: RemoteViews = RemoteViews(applicationContext.packageName, R.layout.widget_memo)

            remoteViews.setTextViewText(R.id.memoTitleWidget, dbHelper.getSpecificMemoTitle(itemId))
            remoteViews.setTextViewText(R.id.memoBodyWidget, dbHelper.getSpecificMemoBody(itemId))
            remoteViews.setViewVisibility(R.id.addMemoWidgetButton, View.GONE)
            AppWidgetManager.getInstance(applicationContext).updateAppWidget(intent.getIntExtra("widgetId", 0), remoteViews)

            dbHelper.setKeyWidget(itemId, intent.getIntExtra("widgetId", 0))

            finishAffinity()
        }

    }

    override fun onIndependentViewClicked(independentViewID: Int, position: Int) {
    }


}
