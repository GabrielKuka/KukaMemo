package com.katana.memo.memo.Helper

import android.app.Application


class AppController : Application() {


    override fun onCreate() {
        super.onCreate()
        mInstance = this
    }

    companion object {

        lateinit var mInstance : AppController

        fun getInstance(): AppController {
            synchronized(this) {
                return mInstance
            }
        }
    }


}