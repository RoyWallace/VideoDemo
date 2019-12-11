package com.mrhuang.demo.chapter3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mrhuang.demo.BaseActivity
import com.mrhuang.demo.R
import com.mrhuang.demo.chapter3.VideoPlayer.VideoSizeCallBack

class MediaExtractorActivity : BaseActivity(), SurfaceHolder.Callback, View.OnClickListener, VideoSizeCallBack {
    var fileName = "VID_20181014_145854.mp4"
    var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path
    var surfaceView: SurfaceView? = null
    var surfaceHolder: SurfaceHolder? = null
    var playButton: Button? = null
    var videoPlayer: VideoPlayer? = null
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_extractor)
    }

    override fun getViews() {
        surfaceView = findViewById(R.id.surfaceView)
        playButton = findViewById(R.id.playButton)
    }

    override fun viewCreated() {
        videoPlayer = VideoPlayer()
        val filePath = "$path/Camera/$fileName"
        videoPlayer!!.setVideoPath(filePath)
        surfaceView!!.holder.addCallback(this)
        playButton!!.setOnClickListener(this)
        videoPlayer!!.setVideoSizeCallBack(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder = holder
        videoPlayer!!.setSurface(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceHolder = holder
        videoPlayer!!.setSurface(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoPlayer!!.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                videoPlayer!!.play()
                playButton!!.text = "stop"
            } else {
                Toast.makeText(this, "你就给这点权限，我很难帮你办事！", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun startPlay() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        } else {
            videoPlayer!!.play()
            playButton!!.text = "stop"
        }
    }

    override fun onClick(v: View) {
        if (v === playButton) {
            startPlay()
        }
    }

    override fun callback(width: Int, height: Int) {
        runOnUiThread { surfaceHolder!!.setFixedSize(surfaceView!!.width, height * surfaceView!!.width / width) }
    }
}