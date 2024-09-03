package com.example.cha00

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Camera : AppCompatActivity() {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resoucers)

        val buttonCamera: Button = findViewById(R.id.button_camera)
        val buttonGallery: Button = findViewById(R.id.button_gallery)
        val buttonPredict: Button = findViewById(R.id.predict)
        val image:ImageView = findViewById(R.id.image)

        buttonPredict.setOnClickListener {
            val image: ImageView = findViewById(R.id.image)
            val bitmap = (image.drawable as BitmapDrawable).bitmap
            val intent = Intent(this, Reconhecimento::class.java)
            intent.putExtra("imagem", bitmap)
            startActivity(intent)
        }

        // Configurando o launcher da câmera
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                image.setImageBitmap(imageBitmap)
                buttonPredict.visibility = View.VISIBLE
            }
        }




        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImage: Uri? = result.data?.data
                val bitmap = selectedImage?.let { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                }

                // Redimensiona o bitmap para 260x260 pixels
                val resizedBitmap = bitmap?.let { resizeBitmap(it, 260, 260) }

                // Define o bitmap redimensionado no ImageView
                image.setImageBitmap(resizedBitmap)
                buttonPredict.visibility = View.VISIBLE
            }
        }

        buttonCamera.setOnClickListener {
            // Verificar permissão da câmera
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            } else {
                openCamera()
            }
        }

        buttonGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
            102 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
        }
    }
}