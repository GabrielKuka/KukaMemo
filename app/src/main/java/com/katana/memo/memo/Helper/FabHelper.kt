package com.katana.memo.memo.Helper

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.katana.memo.memo.Activities.CreateNoteActivity
import com.katana.memo.memo.Activities.DrawImage
import com.katana.memo.memo.Activities.Location
import com.katana.memo.memo.Activities.RecordAudio
import com.katana.memo.memo.R
import net.frederico.showtipsview.ShowTipsBuilder
import xyz.dev_juyoung.cropicker.CroPicker


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class FabHelper(var a: AppCompatActivity) : View.OnClickListener {
    override fun onClick(v: View?) {


        val capturePhoto = ShowTipsBuilder(a)
                .setTarget(buttons[3])
                .setTitle("Take a photo with your camera")
                .build()


        val addLocation = ShowTipsBuilder(a)
                .setTarget(buttons[4])
                .setTitle("Add a location to your note")
                .setCallback { capturePhoto.show(a) }
                .build()

        val addAudio = ShowTipsBuilder(a)
                .setTarget(buttons[1])
                .setTitle("Add an audio as a note")
                .setCallback { addLocation.show(a) }
                .build()

        val addImage = ShowTipsBuilder(a)
                .setTarget(buttons[2])
                .setTitle("Add an image from your phone")
                .setCallback { addAudio.show(a) }
                .build()


        val getPrefs = PreferenceManager.getDefaultSharedPreferences(a)
        val addDataTip = getPrefs.getBoolean("addDataTip", true)

        if (addDataTip) {

            val addDrawing = ShowTipsBuilder(a)
                    .setTarget(buttons[0])
                    .setTitle("Draw something and keep it as a note")
                    .setCallback {
                        addImage.show(a)
                    }
                    .build()
                    .show(a)

        }

        val e: SharedPreferences.Editor = getPrefs.edit()

        e.putBoolean("addDataTip", false)
        e.apply()

        val isFabClicked = false

        when (v?.id) {
            R.id.add_data_fab -> {

                if (whichAnimation === 0) {

                    // çaktivizo fushat dhe scrolling në background

                    if (a is CreateNoteActivity) {
                        (a as CreateNoteActivity).disableBackground()
                        (a as CreateNoteActivity).applyBlur()
                    }
                    /**
                     * Getting the center point of floating action button
                     * to set start point of buttons
                     */
                    startPositionX = v.x + 50
                    startPositionY = v.y + 50

                    for (button in buttons) {
                        button?.x = startPositionX
                        button?.y = startPositionY
                        button?.visibility = View.VISIBLE
                    }
                    for (i in buttons.indices) {
                        playEnterAnimation(buttons[i] as Button, i)
                    }
                    whichAnimation = 1
                } else {
                    for (i in buttons.indices) {
                        playExitAnimation(buttons[i] as Button, i)
                    }

                    if (a is CreateNoteActivity) {
                        (a as CreateNoteActivity).enableBackground()
                        (a as CreateNoteActivity).applyBlur()
                    }

                    whichAnimation = 0

                }
            }
        }

        if (!isFabClicked) {
            when (v?.tag) {
                0 -> {
                    a.startActivityForResult(Intent(a, DrawImage::class.java), AppConstants.DRAW_IMAGE)
                }
                1 -> {
                    val intent: Intent = Intent(a, RecordAudio::class.java)
                    a.startActivityForResult(intent, AppConstants.ADD_AUDIO)
                }
                2 -> {
                    val options = CroPicker.Options()
                    options.setLimitedCount(5)
                    options.setMessageViewType(CroPicker.MESSAGE_VIEW_TYPE_SNACKBAR)

                    CroPicker
                            .init(a)
                            .withOptions(options) //Optional
                            .start()
                }
                3 -> (a as CreateNoteActivity).launchCamera()
                4 -> {
                    a.startActivityForResult(Intent(a, Location::class.java), AppConstants.ADD_LOCATION)
                }
            }
        }

    }

    var enterDelay: IntArray = intArrayOf(80, 120, 160, 40, 0)
    var exitDelay: IntArray = intArrayOf(80, 40, 0, 120, 160)

    var height: Int = 0
    var width: Int = 0
    var radius: Int = 0
    var startPositionX: Float = 0F
    var startPositionY: Float = 0F
    var whichAnimation = 0
    val pentagonVertices = arrayOfNulls<Point>(CreateNoteActivity.Constants.NUM_OF_SIDES)
    val buttons = arrayOfNulls<Button>(pentagonVertices.size)

    init {
        height = a.resources.getDimension(R.dimen.button_height).toInt()
        width = a.resources.getDimension(R.dimen.button_width).toInt()
        radius = a.resources.getDimension(R.dimen.radius).toInt()
    }

    fun calculatePentagonVertices(radius: Int, rotation: Int) {


        /**
         * Calculating the center of pentagon
         */
        val display = a.windowManager.defaultDisplay
        val centerX = display.width / 2
        val centerY = display.height / 2

        /**
         * Calculating the coordinates of vertices of pentagon
         */
        for (i in 0..CreateNoteActivity.Constants.NUM_OF_SIDES - 1) {
            pentagonVertices[i] = Point((radius * Math.cos(rotation + i.toDouble() * 2.0 * Math.PI / CreateNoteActivity.Constants.NUM_OF_SIDES)).toInt() + centerX,
                    (radius * Math.sin(rotation + i.toDouble() * 2.0 * Math.PI / CreateNoteActivity.Constants.NUM_OF_SIDES)).toInt() + centerY - 100)
        }


        for (i in buttons.indices) {
            //Adding button at (0,0) coordinates and setting their visibility to zero
            buttons[i] = Button(a)
            buttons[i]?.layoutParams = ConstraintLayout.LayoutParams(5, 5)
            buttons[i]?.x = 0F
            buttons[i]?.y = 0F
            buttons[i]?.tag = i
            buttons[i]?.setOnClickListener(this)
            buttons[i]?.visibility = View.INVISIBLE

            when ((i + 1)) {
                1 -> buttons[i]?.setBackgroundResource(R.drawable.ic_draw_background)
                2 -> buttons[i]?.setBackgroundResource(R.drawable.ic_add_audio_icon)
                3 -> buttons[i]?.setBackgroundResource(R.drawable.ic_add_image_icon)
                4 -> buttons[i]?.setBackgroundResource(R.drawable.ic_take_photo_icon)
                5 -> buttons[i]?.setBackgroundResource(R.drawable.ic_add_location_icon)
            }

            /**
             * Adding those buttons in acitvities layout
             */
            (a.findViewById(R.id.activity_parent_layout) as ConstraintLayout).addView(buttons[i])
        }
    }

    fun playEnterAnimation(button: Button, position: Int) {

        /**
         * Animator that animates buttons x and y position simultaneously with size
         */
        val buttonAnimator = AnimatorSet()

        /**
         * ValueAnimator to update x position of a button
         */
        val buttonAnimatorX = ValueAnimator.ofFloat(startPositionX + button.layoutParams.width / 2,
                pentagonVertices[position]?.x?.toFloat() as Float)
        buttonAnimatorX.addUpdateListener { animation ->
            button.x = animation.animatedValue as Float - button.layoutParams.width / 2
            button.requestLayout()
        }
        buttonAnimatorX.duration = CreateNoteActivity.Constants.ANIMATION_DURATION.toLong()

        /**
         * ValueAnimator to update y position of a button
         */
        val buttonAnimatorY = ValueAnimator.ofFloat(startPositionY + 5,
                pentagonVertices[position]?.y?.toFloat() as Float)
        buttonAnimatorY.addUpdateListener { animation ->
            button.y = animation.animatedValue as Float
            button.requestLayout()
        }
        buttonAnimatorY.duration = CreateNoteActivity.Constants.ANIMATION_DURATION.toLong()

        /**
         * This will increase the size of button
         */
        val buttonSizeAnimator = ValueAnimator.ofInt(5, width)
        buttonSizeAnimator.addUpdateListener { animation ->
            button.layoutParams.width = animation.animatedValue as Int
            button.layoutParams.height = animation.animatedValue as Int
            button.requestLayout()
        }
        buttonSizeAnimator.duration = CreateNoteActivity.Constants.ANIMATION_DURATION.toLong()

        /**
         * Add both x and y position update animation in
         * animator set
         */
        buttonAnimator.play(buttonAnimatorX).with(buttonAnimatorY).with(buttonSizeAnimator)
        buttonAnimator.startDelay = enterDelay[position].toLong()
        buttonAnimator.start()
    }

    fun playExitAnimation(button: Button, position: Int) {


        /**
         * Animator that animates buttons x and y position simultaneously with size
         */
        val buttonAnimator = AnimatorSet()

        /**
         * ValueAnimator to update x position of a button
         */
        val buttonAnimatorX = ValueAnimator.ofFloat((pentagonVertices[position]?.x?.minus(button.layoutParams.width / 2))?.toFloat() as Float,
                startPositionX)
        buttonAnimatorX.addUpdateListener { animation ->
            button.x = animation.animatedValue as Float
            button.requestLayout()
        }
        buttonAnimatorX.duration = CreateNoteActivity.Constants.ANIMATION_DURATION.toLong()

        /**
         * ValueAnimator to update y position of a button
         */
        val buttonAnimatorY = ValueAnimator.ofFloat(pentagonVertices[position]?.y?.toFloat() as Float,
                startPositionY + 5)
        buttonAnimatorY.addUpdateListener { animation ->
            button.y = animation.animatedValue as Float
            button.requestLayout()
        }
        buttonAnimatorY.duration = CreateNoteActivity.Constants.ANIMATION_DURATION.toLong()

        /**
         * This will decrease the size of button
         */
        val buttonSizeAnimator = ValueAnimator.ofInt(width, 5)
        buttonSizeAnimator.addUpdateListener { animation ->
            button.layoutParams.width = animation.animatedValue as Int
            button.layoutParams.height = animation.animatedValue as Int
            button.requestLayout()
        }
        buttonSizeAnimator.duration = CreateNoteActivity.Constants.ANIMATION_DURATION.toLong()

        /**
         * Add both x and y position update animation in
         * animator set
         */
        buttonAnimator.play(buttonAnimatorX).with(buttonAnimatorY).with(buttonSizeAnimator)
        buttonAnimator.startDelay = exitDelay[position].toLong()
        buttonAnimator.start()
    }

    fun closeFab() {

        for (i in buttons.indices) {
            playExitAnimation(buttons[i] as Button, i)
        }

        if (a is CreateNoteActivity) {
            (a as CreateNoteActivity).enableBackground()
            (a as CreateNoteActivity).applyBlur()
        }
        whichAnimation = 0

    }

    fun isFabOpened(): Boolean {
        return whichAnimation != 0
    }


}

