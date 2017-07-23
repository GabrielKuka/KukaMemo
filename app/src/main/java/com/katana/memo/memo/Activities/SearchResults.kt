package com.katana.memo.memo.Activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import com.katana.memo.memo.Fragments.FavoriteMemoResults
import com.katana.memo.memo.Fragments.MemoResults
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.R
import kotlinx.android.synthetic.main.search_results_activity.*

class SearchResults : AppCompatActivity() {

    lateinit private var sectionPageAdapter: SectionPageAdapter
    private var memosList: ArrayList<String> = ArrayList()
    private var favoriteMemosList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_results_activity)
        setUpActivity()
    }

    fun setUpActivity() {
        setSupportActionBar(resultsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        resultsToolbar.setNavigationOnClickListener { finish() }

        StatusBarColor.changeStatusBarColor(this)

        getDataFromParentActivity()

        sectionPageAdapter = SectionPageAdapter(supportFragmentManager)

        viewPager.adapter = sectionPageAdapter

        tabs.setupWithViewPager(viewPager)



    }

    fun getDataFromParentActivity() {
        val bundle: Bundle = this.intent.extras

        try {

            memosList = bundle.getStringArrayList("memos")
            favoriteMemosList = bundle.getStringArrayList("favoriteMemos")

        } catch(e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private inner class SectionPageAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment? {
            val b: Bundle = Bundle()

            when (position) {
                0 -> {
                    val memoResults: MemoResults = MemoResults()

                    b.putStringArrayList("memos", memosList)
                    memoResults.arguments = b

                    return memoResults
                }

                1 -> {
                    val favoriteMemoResults = FavoriteMemoResults()

                    b.putStringArrayList("favoriteMemos", favoriteMemosList)
                    favoriteMemoResults.arguments = b

                    return favoriteMemoResults
                }

                else -> {
                    return null
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> {
                    return "Memos"
                }

                1 -> {
                    return "Favorite memos"
                }
                else -> {
                    return ""
                }
            }
        }

    }

}
