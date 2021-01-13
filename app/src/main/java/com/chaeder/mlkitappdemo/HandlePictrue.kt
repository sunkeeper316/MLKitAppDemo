package com.chaeder.mlkitappdemo

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.util.*

const val REQ_TAKE_PICTURE = 101
 const val REQ_PICK_PICTURE = 102
 const val REQ_CROP_PICTURE = 103
const val REQ_PICKMORE_PICTURE = 105
 const val PER_EXTERNAL_STORAGE = 201

fun Fragment.askExternalStoragePermission(){
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val result = ContextCompat.checkSelfPermission(requireContext(), permissions[0])
    if (result == PackageManager.PERMISSION_DENIED) {
        requestPermissions(permissions,PER_EXTERNAL_STORAGE)
    }
}

fun Fragment.takePictrue() : Uri?{
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    var contentUri : Uri? = null
    val dir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    if (dir != null && !dir.exists()) {
        if (!dir.mkdirs()) {
            Log.e("pickimage", getString(R.string.textDirNotCreated))
            return null
        }
    }
    var file = File(dir, "picture.jpg")
    file.let {
        if (true){
            contentUri = FileProvider.getUriForFile(requireActivity(), requireActivity().packageName + ".provider", it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, REQ_TAKE_PICTURE)
            } else {
                Toast.makeText(requireActivity(), R.string.textNoCameraAppFound, Toast.LENGTH_SHORT).show()
            }
        }
    }
    return contentUri
}

fun Fragment.pickPictrue(){
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    if (intent.resolveActivity(activity!!.packageManager) != null) {
        startActivityForResult(intent, REQ_PICK_PICTURE)
    } else {
        Toast.makeText(activity, R.string.textNoImagePickerAppFound, Toast.LENGTH_SHORT).show()
    }
}
fun Fragment.pickMorePictrue(){
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    if (intent.resolveActivity(requireActivity().packageManager) != null) {
        startActivityForResult(intent, REQ_PICKMORE_PICTURE)
    } else {
        Toast.makeText(
            activity, R.string.textNoImagePickerAppFound, Toast.LENGTH_SHORT
        ).show()
    }
}
fun Fragment.crop(sourceImageUri: Uri){
    sourceImageUri.path?.let { Log.e("uripath", it) }
    var file =
        activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    file = File(file, "picture_cropped.jpg")

    val uri = Uri.fromFile(file) // 開啟截圖功能
    val intent = Intent("com.android.camera.action.CROP") // 授權讓截圖程式可以讀取資料
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 設定圖片來源與類型
    intent.setDataAndType(sourceImageUri, "image/*") // 設定要截圖
    intent.putExtra("crop", "true") // 設定截圖框大小，0代表user任意調整大小
    intent.putExtra("aspectX", 0)
    intent.putExtra("aspectY", 0) // 設定圖片輸出寬高，0代表維持原尺寸
    intent.putExtra("outputX", 0)
    intent.putExtra("outputY", 0) // 是否保持原圖比例
    intent.putExtra("scale", true) // 設定截圖後圖片位置
    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri) // 設定是否要回傳值
    intent.putExtra("return-data", true)
    if (intent.resolveActivity(activity!!.packageManager) != null) { // 開啟截圖activity
        startActivityForResult(intent, REQ_CROP_PICTURE)
    } else {
        Toast.makeText(
            activity, R.string.textNoImageCropAppFound,
            Toast.LENGTH_SHORT
        ).show()
    }
}


fun Fragment.reqCropPictrue(intent:Intent) : Bitmap?{
    var uri: Uri?
    var bitmap: Bitmap? = null
    intent.getData().let {
        uri = it
    }
    if (uri != null) {
        try {
            bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                BitmapFactory.decodeStream(
                    activity!!.contentResolver.openInputStream(uri!!)
                )
            } else {
                val source =
                    ImageDecoder.createSource(activity!!.contentResolver, uri!!)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: IOException) {
            Log.e("IOException", e.toString())
        }
    }
    if (bitmap != null) {
        bitmap?.let{
            val width: Float = it.width.toFloat()
            val height: Float = it.height.toFloat()
            if (width > 1024){
                val scaleWidth: Float = 1024 / width
                val matrix = Matrix()
                matrix.postScale(scaleWidth, scaleWidth)
                bitmap = Bitmap.createBitmap(it, 0, 0, width.toInt(), height.toInt(), matrix,
                    true);
                Log.e("bitmap" , "width height = ${bitmap?.width}  ${bitmap?.height}")
            }

        }
        return bitmap
    } else {
        return null
    }
}

@Throws(IOException::class)
fun saveImage(activity: Activity,bitmap: Bitmap, name: String) {
    val fos: OutputStream?
    var path = ""
    fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver: ContentResolver = activity.getContentResolver()
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        val imageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        path = imageUri?.path.toString()
        resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)

    }
    else {
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .toString()
        val image = File(imagesDir, "$name.jpg")
        path = image?.path.toString()
        FileOutputStream(image)
    }
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    Objects.requireNonNull(fos)?.close()
    MediaScannerConnection.scanFile(activity, arrayOf(path), arrayOf("image/jpeg"),object  : MediaScannerConnection.MediaScannerConnectionClient{
        override fun onScanCompleted(path: String?, uri: Uri?) {
            Log.d("test", "onMediaScannerConnected ${path} ${uri}");
            activity.runOnUiThread{
                Toast.makeText(activity,"照片儲存成功",Toast.LENGTH_SHORT).show()
            }

        }
        override fun onMediaScannerConnected() {
            Log.d("test", "onScanCompleted");
        }
    })
}

fun loadFromUri(activity: Activity , photoUri: Uri): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        // check version of Android on device
        bitmap = if (Build.VERSION.SDK_INT > 27) {
            // on newer versions of Android, use the new decodeBitmap method
            val source: ImageDecoder.Source =
                ImageDecoder.createSource(activity.getContentResolver(), photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            // support older versions of Android by using getBitmap
            MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photoUri)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    if (bitmap != null) {
        bitmap?.let{
            val width: Float = it.width.toFloat()
            val height: Float = it.height.toFloat()
            if (width > 1024){
                val scaleWidth: Float = 1024 / width
                val matrix = Matrix()
                matrix.postScale(scaleWidth, scaleWidth)
                bitmap = Bitmap.createBitmap(it, 0, 0, width.toInt(), height.toInt(), matrix,
                    true);
                Log.e("bitmap" , "width height = ${bitmap?.width}  ${bitmap?.height}")
            }

        }
        return bitmap
    } else {
        return null
    }
    return bitmap
}