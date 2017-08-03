package com.katana.memo.memo.Activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.katana.memo.memo.R
import java.io.ByteArrayOutputStream

class DrawImage : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View?) {
        if (v == clear) {
            image.clear()
        } else if (v == save) {
            image.save()
        }
    }

    lateinit var image: Image
    lateinit var mContent: LinearLayout
    lateinit var clear: Button
    lateinit var save: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.draw_image_activity)

        save = findViewById(R.id.save) as Button
        save.isEnabled = false
        clear = findViewById(R.id.clear) as Button
        mContent = findViewById(R.id.image_draw) as LinearLayout

        image = Image(this, null)
        mContent.addView(image)

        save.setOnClickListener(this)
        clear.setOnClickListener(this)
    }


    override fun onBackPressed() {

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Save the drawing?")
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setNegativeButton("Nope", DialogInterface.OnClickListener { dialog, id ->
            run {
                dialog.cancel()
                super.onBackPressed()
            }
        })
        alertDialogBuilder.setPositiveButton("Save", DialogInterface.OnClickListener { dialog, id ->
            run {
                SaveDrawing().execute()
            }
        })
        val dialog = alertDialogBuilder.create()
        dialog.show()

    }


    inner class Image(context: Context, attrs: AttributeSet?) : View(context, attrs) {


        val STROKE_WIDTH = 10f
        val HALF_STROKE_WIDTH = STROKE_WIDTH / 2


        var paint: Paint = Paint()
        var path: Path = Path()

        var lastTouchX: Float = 0f
        var lastTouchY: Float = 0f

        val dirtyRect: RectF = RectF()


        fun clear() {
            path.reset()
            invalidate()
            save.isEnabled = false
        }

        fun save() {

            SaveDrawing().execute()
        }


        override fun onDraw(canvas: Canvas) {
            canvas.drawPath(path, paint)
        }


        override fun onTouchEvent(event: MotionEvent): Boolean {

            val eventX: Float = event.x
            val eventY: Float = event.y


            save.isEnabled = true

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(eventX, eventY)
                    lastTouchX = eventX
                    lastTouchY = eventY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    resetDirtyRect(eventX, eventY)
                    val historySize: Int = event.historySize
                    for (i: Int in 0..historySize - 1) {
                        val historicalX: Float = event.getHistoricalX(i)
                        val historicalY: Float = event.getHistoricalY(i)
                        path.lineTo(historicalX, historicalY)
                    }
                    path.lineTo(eventX, eventY)
                }

                MotionEvent.ACTION_UP -> {

                    resetDirtyRect(eventX, eventY)
                    val historySize: Int = event.historySize
                    for (i: Int in 0..historySize - 1) {
                        val historicalX: Float = event.getHistoricalX(i)
                        val historicalY: Float = event.getHistoricalY(i)
                        path.lineTo(historicalX, historicalY)
                    }
                    path.lineTo(eventX, eventY)

                }
            }

            invalidate(((dirtyRect.left - HALF_STROKE_WIDTH).toInt()),
                    (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt())

            lastTouchX = eventX
            lastTouchY = eventY

            return true
        }

        private fun resetDirtyRect(eventX: Float, eventY: Float) {
            dirtyRect.left = Math.min(lastTouchX, eventX)
            dirtyRect.right = Math.max(lastTouchX, eventX)
            dirtyRect.top = Math.min(lastTouchY, eventY)
            dirtyRect.bottom = Math.max(lastTouchY, eventY)
        }

        init {
            paint.isAntiAlias = true
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeWidth = STROKE_WIDTH
        }
    }

    private inner class SaveDrawing : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {

            save()

            return null
        }

        fun save() {

            val returnedBitmap: Bitmap = Bitmap.createBitmap(mContent.width,
                    mContent.height, Bitmap.Config.ARGB_8888)
            val canvas: Canvas = Canvas(returnedBitmap)
            val bgDrawable = mContent.background
            if (bgDrawable != null)
                bgDrawable.draw(canvas)
            else
                canvas.drawColor(Color.WHITE)
            mContent.draw(canvas)

            val bs: ByteArrayOutputStream = ByteArrayOutputStream()
            returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs)
            val intent: Intent = Intent()
            intent.putExtra("byteArray", bs.toByteArray())
            setResult(1, intent)
            finish()
        }

    }

}