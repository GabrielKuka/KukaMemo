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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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


    val storage: Storage = SimpleStorage.getInternalStorage(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_audio_activity)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

    }

    override fun onStart() {
        super.onStart()

        var mStartPlaying: Boolean = true
        var mStartRecording: Boolean = true

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

        status_text.setText(R.string.doneRecording)
        stopBlinkAnimation(status_text)

        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

        delete_button.isEnabled = true
        save_button.isEnabled = true
    }

    private fun startRecording() {

        status_text.setText(R.string.recording)
        startBlinkAnimation(status_text)

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
            Log.d("Record_error", e.message)
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

                play_button.setText(R.string.playButton)
                stopBlinkAnimation(play_button)

            }
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            status_text.setText(R.string.playingAudio)
            startBlinkAnimation(status_text)

        } catch(e: IOException) {
            Log.d("Play_error", e.message)
        }
    }

    private fun stopPlaying() {

        status_text.setText(R.string.donePlaying)
        stopBlinkAnimation(status_text)

        mediaPlayer?.release()
        mediaPlayer = null

        play_button.setText(R.string.playButton)

    }

    private fun deleteAudio() {

        status_text.setText(R.string.deletedAudio)

        delete_button.isEnabled = false
        save_button.isEnabled = false
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

    fun generateRandomNumber(): Int {
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

    fun startBlinkAnimation(v: View) {
        val blinkAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.blink_animation)
        v.startAnimation(blinkAnimation)
    }

    fun stopBlinkAnimation(v: View) {
        v.clearAnimation()
    }

}
