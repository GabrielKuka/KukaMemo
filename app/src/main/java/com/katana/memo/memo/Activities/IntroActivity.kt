package com.katana.memo.memo.Activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.R
import java.util.*

class IntroActivity : AppIntro2(), PermissionListener {
    override fun onPermissionGranted() {

    }

    override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TedPermission(this).setPermissionListener(this)
                .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissions(android.Manifest.permission.CAMERA).check()

        addSlide(AppIntro2Fragment.newInstance("Welcome to your notes", "Add any note you like", R.drawable.first_intro_img, Color.parseColor("#3F51B5")))
        addSlide(AppIntro2Fragment.newInstance("Audio your notes", "Add any audio recording as a note", R.drawable.second_intro_img, Color.parseColor("#3693D0")))
        addSlide(AppIntro2Fragment.newInstance("Photograph your notes", "Take pictures of something to keep as a note", R.drawable.third_intro_img, Color.parseColor("#ff1744")))
        addSlide(AppIntro2Fragment.newInstance("Get encouraged by drawing", "If you're feeling inspired take the time to make a paint-note", R.drawable.fourth_intro_img, Color.parseColor("#36CF8E")))


        StatusBarColor.changeStatusBarColor(this)


        setVibrate(true)
        setVibrateIntensity(5)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startActivity(Intent(this, Homepage::class.java))
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startActivity(Intent(this, Homepage::class.java))
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }

}
