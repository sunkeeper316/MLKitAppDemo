package com.chaeder.mlkitappdemo

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PhotographyActivity : AppCompatActivity() {
    val REQUEST_CODE_PERMISSION = 106
    val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private var tv: TextureView? = null
    private var iv: ImageView? = null
    private val TAG = "PhotographyActivity"

    var lens : CameraX.LensFacing  = CameraX.LensFacing.FRONT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photography)
        tv = findViewById(R.id.face_texture_view)
        iv = findViewById(R.id.face_image_view)
        if (allPermissionsGranted()) {
            tv!!.post {
                this.startCamera()
            }
        } else { ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION
            )
        }
    }
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        initCamera()
        val ibSwitch = findViewById<ImageButton>(R.id.btn_switch_face)
        ibSwitch.setOnClickListener { v: View? ->
            if (lens === CameraX.LensFacing.FRONT) lens =
                CameraX.LensFacing.BACK else lens =
                CameraX.LensFacing.FRONT
            try {
                Log.i(
                    TAG,
                    "" + lens
                )
                CameraX.getCameraWithLensFacing(lens)
                initCamera()
            } catch (e: CameraInfoUnavailableException) {
                Log.e(
                    TAG,
                    e.toString()
                )
            }
        }
    }
    private fun initCamera() {
        CameraX.unbindAll()
        val pc: PreviewConfig = PreviewConfig.Builder()
            .setTargetResolution(Size(tv!!.width, tv!!.height))
            .setLensFacing(lens)
            .build()
        val preview = Preview(pc)
        preview.setOnPreviewOutputUpdateListener { output ->
            val vg = tv!!.parent as ViewGroup
            vg.removeView(tv)
            vg.addView(tv, 0)
            tv!!.setSurfaceTexture(output.getSurfaceTexture())
        }
        val iac: ImageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .setTargetResolution(Size(tv!!.width, tv!!.height))
            .setLensFacing(lens)
            .build()
        val imageAnalysis = ImageAnalysis(iac)
        imageAnalysis.setAnalyzer({ obj: Runnable -> obj.run() },
            MLKitTextAnalyzer(tv!!, iv!!, lens)
        )
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                tv!!.post { startCamera() }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}