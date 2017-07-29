package com.katana.memo.memo.Helper

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.katana.memo.memo.R

class Animations {

    companion object {

        fun startBlinkAnimation(v: View, a: AppCompatActivity) {
            val blinkAnimation: Animation = AnimationUtils.loadAnimation(a, R.anim.blink_animation)
            v.startAnimation(blinkAnimation)
        }

        fun stopBlinkAnimation(v: View) {
            v.clearAnimation()
        }

        fun startScaleAnimation(v: View, a: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_size_animation)
            v.startAnimation(anim)
        }

        fun stopScaleAnimation(v: View) {
            v.clearAnimation()
        }

        fun slideUpAndShowAnimation(v: View, a: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slideup_show)
            v.startAnimation(anim)
        }

        fun slideUpAndHideAnimation(v: View, a: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slideup_hide)
            v.startAnimation(anim)
        }

        fun slideDownAndHideAnimation(v: View, a: AppCompatActivity){
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slidedown_hide)
            v.startAnimation(anim)
        }

        fun slideDownAndShowAnimation(v: View, a: AppCompatActivity){
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slidedown_show)
            v.startAnimation(anim)
        }

    }

}