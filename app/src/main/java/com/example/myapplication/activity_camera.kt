package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    // Uporabniški vmesni elementi za prikaz kamere in gumbe
    private lateinit var previewView: PreviewView  // Pogled za predogled kamere
    private lateinit var captureButton: ImageButton  // Gumb za zajem slike
    private lateinit var galleryButton: ImageButton  // Gumb za odprtje galerije

    // Spremenljivka za zajem slike z uporabo CameraX
    private var imageCapture: ImageCapture? = null

    // Seznam zahtevanih dovoljenj
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA  // Dovoljenje za uporabo kamere
    ).apply {
        // Če je verzija Androida ≤ P, dodaj dovoljenje za pisanje v zunanji pomnilnik
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        // Dodaj dovoljenje za branje zunanjega pomnilnika glede na verzijo Androida
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }.toTypedArray()

    private val REQUEST_CODE_PERMISSIONS = 10  // Koda za zahtevo dovoljenj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_activity)  // Nastavi izgled aktivnosti

        // Inicializacija elementov uporabniškega vmesnika
        previewView = findViewById(R.id.preview_view)
        captureButton = findViewById(R.id.capture_button)
        galleryButton = findViewById(R.id.gallery_button)

        // Nastavi poslušalce klikov na gumbe
        captureButton.setOnClickListener {
            if (allPermissionsGranted()) {
                takePhoto()  // Če so dovoljenja podeljena, zajemi sliko
            } else {
                // Če dovoljenja niso podeljena, jih zahteva
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }

        galleryButton.setOnClickListener {
            if (allPermissionsGranted()) {
                selectImageFromGallery()  // Če so dovoljenja podeljena, odpri galerijo
            } else {
                // Če dovoljenja niso podeljena, jih zahteva
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }

        // Preveri in zahteva potrebna dovoljenja
        if (allPermissionsGranted()) {
            startCamera()  // Zaženi kamero, če so dovoljenja podeljena
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    // Funkcija za preverjanje, ali so vsa zahtevana dovoljenja podeljena
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Funkcija, ki obravnava rezultat zahteve za dovoljenja
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()  // Zaženi kamero, če so dovoljenja podeljena
            } else {
                // Če dovoljenja niso podeljena, prikaži sporočilo in zaključi aktivnost
                Toast.makeText(
                    this,
                    "Uporabnik ni podelil dovoljenj.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    // Funkcija za inicializacijo kamere in nastavitev primerov uporabe
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Pridobi CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Nastavi predogled kamere
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            // Nastavi zajem slike
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)  // Nastavi rotacijo glede na zaslon
                .build()
            // Izberi zadnjo (glavno) kamero kot privzeto
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Odveži vse primere uporabe pred ponovnim vezanjem
                cameraProvider.unbindAll()
                // Poveži primere uporabe (predogled in zajem slike) s kamero
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Vezava primerov uporabe ni uspela", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Funkcija za zajem slike
    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            // Če imageCapture ni inicializiran, prikaži sporočilo in se vrni
            Toast.makeText(this, "Kamera ni inicializirana.", Toast.LENGTH_SHORT).show()
            return
        }
        // Ustvari ime datoteke z uporabo trenutnega časa
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(getOutputDirectory(), "$name.jpg")  // Ustvari datoteko za shranjevanje slike
        // Ustvari nastavitve za izhodno datoteko
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // Zajemi sliko in obravnavaj rezultat
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                // Funkcija, ki se pokliče ob napaki pri shranjevanju slike
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Zajem slike ni uspel: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Zajem slike ni uspel", Toast.LENGTH_SHORT).show()
                }
                // Funkcija, ki se pokliče ob uspešnem shranjevanju slike
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)  // Pridobi URI shranjene slike
                    Log.d(TAG, "Zajem slike je uspel: $savedUri")
                    // Ustvari namen za prehod na ConfirmActivity in posreduj URI slike
                    val intent = Intent(this@CameraActivity, ConfirmActivity::class.java)
                    intent.putExtra("image_uri", savedUri.toString())
                    startActivity(intent)  // Zaženi ConfirmActivity
                }
            }
        )
    }

    // Funkcija za pridobitev direktorija, kamor se bodo shranjevale slike
    private fun getOutputDirectory(): File {
        // Pridobi zunanji medijski direktorij in ustvari podmapo z imenom aplikacije
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.Analiza_rastlin)).apply { mkdirs() }
        }
        // Če medijski direktorij ni na voljo, uporabi notranji pomnilnik aplikacije
        return mediaDir ?: filesDir
    }

    // Funkcija za izbiro slike iz galerije
    private fun selectImageFromGallery() {
        // Ustvari namen za izbiro slike
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)  // Zaženi namen z uporabo ActivityResultLauncher
    }

    // Aktivnost za obravnavo rezultata izbire slike iz galerije
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Preveri, ali je rezultat uspešen
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri = result.data?.data  // Pridobi URI izbrane slike
            if (selectedImageUri != null) {
                // Ustvari namen za prehod na ConfirmActivity in posreduj URI slike
                val intent = Intent(this, ConfirmActivity::class.java)
                intent.putExtra("image_uri", selectedImageUri.toString())
                startActivity(intent)  // Zaženi ConfirmActivity
            }
        }
    }

    companion object {
        private const val TAG = "CameraActivity"  // Oznaka za logiranje
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"  // Format za ime datoteke slike
    }
}
