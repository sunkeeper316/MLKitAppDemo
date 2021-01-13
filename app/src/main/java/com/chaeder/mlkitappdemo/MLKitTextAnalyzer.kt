package com.chaeder.mlkitappdemo

import android.view.TextureView
import android.widget.ImageView
import androidx.camera.core.CameraX.LensFacing
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage

class MLKitTextAnalyzer : ImageAnalysis.Analyzer {

    lateinit var tv: TextureView
    lateinit var iv: ImageView
    lateinit var lens: LensFacing

    constructor(tv: TextureView, iv: ImageView, lens: LensFacing) {
        this.tv = tv
        this.iv = iv
        this.lens = lens
    }

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        val mediaImage = imageProxy?.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            // Pass image to an ML Kit Vision API
            // ...
        }
    }
}