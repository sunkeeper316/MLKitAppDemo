package com.chaeder.mlkitappdemo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition


class MainFragment : Fragment() {

    lateinit var btPickPictrue : Button
    lateinit var btTakePictrue : Button
    lateinit var btPhotography : Button


    lateinit var tvShow : TextView
    lateinit var ivShow : ImageView

    private var bitmap : Bitmap? = null
    private var contentUri: Uri? = null

    val recognizer = TextRecognition.getClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findbyid(view)
        btPhotography.setOnClickListener {
            val intent = Intent(requireContext(), PhotographyActivity::class.java)
            this.startActivity(intent)
        }
        btPickPictrue.setOnClickListener {
            pickPictrue()
        }
        btTakePictrue.setOnClickListener {
            contentUri = takePictrue()
        }
    }

    fun findbyid(view: View){

        btPhotography = view.findViewById(R.id.btPhotography)
        btPickPictrue = view.findViewById(R.id.btPickPictrue)
        btTakePictrue = view.findViewById(R.id.btTakePictrue)
        tvShow = view.findViewById(R.id.tvShow)
        ivShow = view.findViewById(R.id.ivShow)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            val uri = intent?.data
            if (true) {
//                Log.e("pickimage", requestCode.toString())
                when (requestCode) {

                    REQ_TAKE_PICTURE ->
                        if (contentUri != null) {
                            crop(contentUri!!)
                        }
                    REQ_PICK_PICTURE ->
                        intent?.getData()?.let { crop(it) }
                    REQ_CROP_PICTURE -> {
                        intent.let {
                            if (it != null) {
                                bitmap = reqCropPictrue(it)
                                if (bitmap != null) {
                                    ivShow.setImageBitmap(bitmap)
//                                    classifyDrawing()
                                    val image = InputImage.fromBitmap(bitmap, 0)

                                    try {
                                        val result = recognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                Log.e("visionText", visionText.text)

                                                val str: String = visionText.text
                                                tvShow.setText("${str}")
                                                // Task completed successfully
                                                // ...
                                            }
                                            .addOnFailureListener { e ->
                                                // Task failed with an exception
                                                // ...
                                                Log.e("visionText", e.localizedMessage)
                                            }

                                    } catch (error: Exception) {
                                        Log.e("visionText", error.localizedMessage)
                                    }

                                }
                            }
                        }

                    }// Android 9開始可以使用ImageDecoder
                    REQ_PICKMORE_PICTURE -> {
                        intent?.let {
                            if (it.clipData != null) {
                                val mClipData = it.clipData
                                for (i in 0..mClipData!!.itemCount - 1) {
                                    val item = mClipData.getItemAt(i)
                                    val uri: Uri = item.uri
                                    val bitmap: Bitmap? = loadFromUri(requireActivity(), uri)
                                    var path = uri.path.toString()
                                    val pathList = path.split("/")
                                    val name = pathList.last() + ".jpg"
//                                    val showitem = ShowItem()
//                                    showitem.img = bitmap
//                                    showItemList.add(showitem)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}