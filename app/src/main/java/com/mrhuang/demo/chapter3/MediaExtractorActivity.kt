package com.mrhuang.demo.chapter3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mrhuang.demo.R
import kotlinx.android.synthetic.main.activity_media_extractor.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.min

class MediaExtractorActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var fileName = "jingqidama.mp4"
    private var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path
    private var surfaceHolder: SurfaceHolder? = null
    private lateinit var videoPlayer: VideoPlayer
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_extractor)

        videoPlayer = VideoPlayer()
        val filePath = "$path/Camera/$fileName"
        videoPlayer.setVideoPath(filePath)
        surfaceView.holder.addCallback(this)
        playButton.setOnClickListener {
            startPlay()
        }
        videoPlayer.videoSizeCallBack = { width, height ->
            val scale: Float = min(surfaceView.width / width.toFloat(), surfaceView.height / height.toFloat())
            GlobalScope.launch(Dispatchers.Main) {
                surfaceView.layoutParams.width = (width * scale).toInt()
                surfaceView.layoutParams.height = (height * scale).toInt()
                surfaceView.requestLayout()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder = holder
        videoPlayer.setSurface(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceHolder = holder
        videoPlayer.setSurface(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoPlayer.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                videoPlayer.play()
                playButton.text = "stop"
            } else {
                Toast.makeText(this, "你就给这点权限，我很难帮你办事！", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startPlay() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        } else {
            videoPlayer.play()
            playButton.text = "stop"
        }
    }
}