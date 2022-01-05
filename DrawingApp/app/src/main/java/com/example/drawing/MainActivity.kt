package com.example.drawing

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var brushBtn: ImageButton? = null
    private var galleryBtn: ImageButton? = null
    private var undoBtn: ImageButton? = null
    private var redoBtn: ImageButton? = null
    private var saveBtn: ImageButton? = null
    private var mImageBtnCurrentPaint: ImageButton? = null
    private var customProgressDialog: Dialog? = null
    // Pick image from intent
    private val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if(result.resultCode == RESULT_OK && result.data != null) {
            val imageBackground: ImageView = findViewById(R.id.iv_background)
            imageBackground.setImageURI(result.data?.data)
        }
    }
    // Check for permissions
    private val requestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value

            if(isGranted) {
                // Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
                // Use intent to go to gallery and pick an image
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)
            } else {
                if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushBtn = findViewById(R.id.ib_brush)
        galleryBtn = findViewById(R.id.ib_gallery)
        drawingView = findViewById(R.id.drawing_view)
        undoBtn = findViewById(R.id.ib_undo)
        redoBtn = findViewById(R.id.ib_redo)
        saveBtn = findViewById(R.id.ib_save)

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageBtnCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageBtnCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        drawingView?.setSizeForBrush(20f)

        brushBtn!!.setOnClickListener {
            showBrushSizeDialog()
        }

        galleryBtn!!.setOnClickListener {
            requestStoragePermission()
        }

        undoBtn!!.setOnClickListener {
            drawingView!!.undo()
        }

        redoBtn!!.setOnClickListener {
            drawingView!!.redo()
        }

        saveBtn!!.setOnClickListener {
            if(isReadStorageAllowed()) {
                showProgressDialog()
                lifecycleScope.launch{
                    val frameLayout: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    val res = saveBitmapFile(getBitmapFromView(frameLayout))
                }
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val res = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return res == PackageManager.PERMISSION_GRANTED
    }

    // Request permission for external storage, read and write
    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationaleDialog("Drawing App", "Drawing app needs to access your external storage")
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = view.background
        if(background != null) {
            background.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return bitmap
    }

    // Async task
    private suspend fun saveBitmapFile(bitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if(bitmap != null) {
                try {
                    // compress bitmap
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    // create file
                    val f = File(externalCacheDir?.absoluteFile.toString() + File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png")
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath
                    runOnUiThread {
                        if(result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "File saved: $result", Toast.LENGTH_LONG).show()
                            shareFile(result)
                        } else {
                            Toast.makeText(this@MainActivity, "Something went wrong saving the file", Toast.LENGTH_LONG).show()
                        }
                        dismissProgressDialog()
                    }
                } catch(e: Exception) {
                    result = ""
                    Toast.makeText(this@MainActivity, "Exception: Something went wrong saving the file", Toast.LENGTH_LONG).show()
                }
            }
        }

        return result
    }

    // Show dialog to change brushSize
    private fun showBrushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn : ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        val mediumBtn : ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        val largeBtn : ImageButton = brushDialog.findViewById(R.id.ib_large_brush)

        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10f)
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20f)
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if(view !== mImageBtnCurrentPaint) {
            val imageBtn = view as ImageButton
            val colorTag = imageBtn.tag.toString()
            drawingView!!.setColor(colorTag)
            imageBtn.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_selected)
            )
            mImageBtnCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )

            mImageBtnCurrentPaint = view
        }
    }

    // Ask for permissions
    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog?.show()
    }

    private fun dismissProgressDialog() {
        if(customProgressDialog != null) {
            customProgressDialog!!.dismiss()
            customProgressDialog = null
        }
    }

    // share file
    private fun shareFile(dir: String) {
        MediaScannerConnection.scanFile(this, arrayOf(dir), null) {
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }
}