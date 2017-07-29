package com.katana.memo.memo.Activities

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.katana.memo.memo.Helper.Animations
import com.katana.memo.memo.Helper.StatusBarColor
import com.katana.memo.memo.R
import com.sromku.simple.storage.SimpleStorage
import com.sromku.simple.storage.Storage
import kotlinx.android.synthetic.main.record_audio_activity.*
import java.io.File
import java.io.IOException
import java.util.*

class RecordAudio : AppCompatActivity() {

    companion object {
        val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    var mediaPlayer: MediaPlayer? = null
    var mediaRecorder: MediaRecorder? = null

    var permissionToRecordAccepted: Boolean = false
    var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    var filePath: String = ""
    var fileName: String = ""

    var mStartPlaying: Boolean = true
    var mStartRecording: Boolean = true

    val a: AppCompatActivity = this


    val storage: Storage = SimpleStorage.getInternalStorage(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_audio_activity)
        StatusBarColor.changeStatusBarColor(this)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        setUpViews()
    }

    private fun setUpViews() {
        delete_button.visibility = View.INVISIBLE
        save_button.visibility = View.INVISIBLE
        play_button.visibility = View.INVISIBLE
        record_button.visibility = View.VISIBLE

        Animations.slideUpAndShowAnimation(record_button, this)

    }

    override fun onStart() {
        super.onStart()



        play_button.setOnClickListener {
            onPlay(mStartPlaying)
            if (mStartPlaying) {
                play_button.text = "Stop"
            } else {
                play_button.text = "Play"
            }

            mStartPlaying = !mStartPlaying

        }

        record_button.setOnClickListener {
            onRecord(mStartRecording)
            if (mStartRecording) {
                record_button.text = "Stop"
            } else {
                record_button.text = "Start"
            }

            mStartRecording = !mStartRecording
        }

        delete_button.setOnClickListener {
            deleteAudio()
        }

        save_button.setOnClickListener {

            val intent = Intent(this, CreateNoteActivity::class.java)
            intent.putExtra("audioPath", fileName)
            setResult(1, intent)
            finish()
        }

    }

    override fun onStop() {
        super.onStop()
        if (mediaRecorder != null) {
            mediaRecorder?.release()
            mediaRecorder = null
        }

        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun stopRecording() {

        Animations.stopScaleAnimation(record_button)
        record_button.isEnabled = false

        status_text.setText(R.string.doneRecording)
        Animations.stopBlinkAnimation(status_text)

        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

        Thread().run {
            try {
                Thread.sleep(400)
            } catch(e: InterruptedException) {
                e.printStackTrace()
            } finally {

                a.runOnUiThread {

                    delete_button.visibility = View.VISIBLE
                    save_button.visibility = View.VISIBLE
                    play_button.visibility = View.VISIBLE

                    delete_button.isEnabled = true
                    save_button.isEnabled = true

                    Animations.slideUpAndHideAnimation(record_button, a)
                    Animations.slideUpAndShowAnimation(play_button, a)
                    Animations.slideUpAndShowAnimation(delete_button, a)
                    Animations.slideUpAndShowAnimation(save_button, a)
                }
            }

        }
    }

    private fun startRecording() {

        Animations.startScaleAnimation(record_button, this)

        status_text.setText(R.string.recording)
        Animations.startBlinkAnimation(status_text, this)

        val cw: ContextWrapper = ContextWrapper(this)

        val dir: File = cw.getDir("Audios", Context.MODE_PRIVATE)

        fileName = "MemoAudioRecord" + generateRandomNumber()

        filePath = dir.absolutePath + "/" + fileName

        Log.d("Record_path", dir.absolutePath + fileName)

        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setOutputFile(filePath)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mediaRecorder?.prepare()
        } catch(e: IOException) {
            e.printStackTrace()
        }

        mediaRecorder?.start()

    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(filePath)
            mediaPlayer?.setOnCompletionListener {

                stopPlaying()

                mStartPlaying = !mStartPlaying

            }
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            status_text.setText(R.string.playingAudio)
            Animations.startBlinkAnimation(status_text, this)
            Animations.startScaleAnimation(play_button, this)

        } catch(e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopPlaying() {

        status_text.setText(R.string.donePlaying)
        play_button.setText(R.string.playButton)

        Animations.stopBlinkAnimation(status_text)
        Animations.stopScaleAnimation(play_button)

        mediaPlayer?.release()
        mediaPlayer = null


    }

    private fun deleteAudio() {


        if (mediaPlayer != null && mediaPlayer?.isPlaying!!) {
            stopPlaying()
        }

        storage.deleteFile("Audios", fileName)

        status_text.setText(R.string.deletedAudio)

        delete_button.isEnabled = false
        save_button.isEnabled = false

        Animations.slideDownAndHideAnimation(delete_button, a)
        Animations.slideDownAndHideAnimation(play_button, a)
        Animations.slideDownAndHideAnimation(save_button, a)
        Animations.slideDownAndShowAnimation(record_button, a)

        delete_button.visibility = View.INVISIBLE
        play_button.visibility = View.INVISIBLE
        save_button.visibility = View.INVISIBLE
        record_button.visibility = View.VISIBLE
        record_button.isEnabled = true


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }

        if (!permissionToRecordAccepted) {
            finish()
        }

    }

    private fun generateRandomNumber(): Int {
        val rand: Random = Random()
        val number = rand.nextInt(382901380) + 1

        return number
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (storage.isFileExist("Audios", fileName)) {
            storage.deleteFile("Audios", fileName)
        }
    }

}
