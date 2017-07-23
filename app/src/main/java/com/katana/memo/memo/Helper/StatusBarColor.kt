package com.katana.memo.memo.Helper

import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.katana.memo.memo.R


class StatusBarColor {
    companion object {
        fun changeStatusBarColor(a: AppCompatActivity){
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                val window = a.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = a.resources.getColor(R.color.colorAccent)
            }
        }
    }
}