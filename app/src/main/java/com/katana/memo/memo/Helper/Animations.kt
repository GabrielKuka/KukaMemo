package com.katana.memo.memo.Helper

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.katana.memo.memo.R

class Animations {

    companion object {

        fun animateFab(v: View, a: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slide_up_animation)
            v.startAnimation(anim)
        }

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

        fun slideDownAndHideAnimation(v: View, a: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slidedown_hide)
            v.startAnimation(anim)
        }

        fun slideDownAndShowAnimation(v: View, a: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(a, R.anim.fab_slidedown_show)
            v.startAnimation(anim)
        }


        fun moveToCenterAnimation(rootView: View, view: View) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.animate()
                        .translationX((((rootView.width - view.width) / 4).toFloat()))
                        .translationY((((rootView.height - view.height) / 4).toFloat()))
                        .translationZ(12f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).duration = 600
            } else {
                view.animate()
                        .translationX((((rootView.width - view.width) / 4).toFloat()))
                        .translationY((((rootView.height - view.height) / 4).toFloat()))
                        .setInterpolator(AccelerateDecelerateInterpolator()).duration = 600
            }

        }

        fun moveToDefaultPosAnimation(x: Float, y: Float, view: View) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.animate()
                        .translationX(x)
                        .translationY(y)
                        .translationZ(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).duration = 600
            } else {
                view.animate()
                        .translationX(x)
                        .translationY(y)
                        .setInterpolator(AccelerateDecelerateInterpolator()).duration = 600
            }
        }

        fun onAnimateEdiText(animate: Boolean, rootView: View, view: View, x: Float, y: Float) {
            if (animate) {
                moveToCenterAnimation(rootView, view)
            } else {
                moveToDefaultPosAnimation(x, y, view)
            }
        }

    }

}