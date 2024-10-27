package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ConfirmActivity : AppCompatActivity() {

    // Uporabniški vmesnik
    private lateinit var imageView: ImageView       // Prikaz slike za potrditev
    private lateinit var confirmButton: ImageButton // Gumb za potrditev slike
    private lateinit var retakeButton: ImageButton  // Gumb za ponovno zajemanje slike

    // Spremenljivke za upravljanje slike
    private var imageUriString: String? = null      // Niz, ki vsebuje URI slike
    private var imageUri: Uri? = null               // URI objekta slike

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)   // Nastavi izgled aktivnosti

        // Inicializacija elementov uporabniškega vmesnika
        imageView = findViewById(R.id.image_view)
        confirmButton = findViewById(R.id.confirm_button)
        retakeButton = findViewById(R.id.retake_button)

        // Pridobi URI slike, ki je bila poslana iz CameraActivity
        imageUriString = intent.getStringExtra("image_uri")
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString)    // Pretvori niz v URI
            imageView.setImageURI(imageUri)         // Nastavi sliko v ImageView
        } else {
            // Če URI ni na voljo, prikaži sporočilo in zaključi aktivnost
            Toast.makeText(this, "Slika ni najdena", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Nastavi poslušalce klikov na gumbe
        confirmButton.setOnClickListener {
            imageUri?.let {
                val savedFilePath = saveImageToCache(it)   // Shrani sliko v predpomnilnik
                if (savedFilePath != null) {
                    // Če je shranjevanje uspešno, pojdi na LoadingActivity in posreduj pot do slike
                    val intent = Intent(this@ConfirmActivity, LoadingActivity::class.java)
                    intent.putExtra("image_file_path", savedFilePath)  // Posreduj pot datoteke v LoadingActivity
                    startActivity(intent)
                    finish()
                } else {
                    // Če shranjevanje ni uspelo, prikaži sporočilo
                    Toast.makeText(this, "Neuspešno shranjevanje slike", Toast.LENGTH_SHORT).show()
                }
            }
        }

        retakeButton.setOnClickListener {
            // Vrni se v CameraActivity za ponovno zajemanje slike
            finish()    // Zaključi to aktivnost, da se vrneš na prejšnjo
        }
    }

    // Funkcija za shranjevanje slike v imenik predpomnilnika in vrnitev poti do datoteke
    private fun saveImageToCache(imageUri: Uri): String? {
        try {
            // Odpri InputStream iz URI slike
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                // Ustvari datoteko v imeniku predpomnilnika z imenom "confirmed_image.jpg"
                val file = File(cacheDir, "confirmed_image.jpg")
                // Ustvari FileOutputStream za pisanje v datoteko
                val fos = FileOutputStream(file)
                // Kopiraj podatke iz InputStream v FileOutputStream
                inputStream.copyTo(fos)
                // Počisti in zapri tokove
                fos.flush()
                fos.close()
                inputStream.close()
                // Vrni absolutno pot shranjene datoteke
                return file.absolutePath
            } else {
                // Če InputStream ni mogoče odpreti, vrzi izjemo
                throw Exception("Neuspešno odpiranje vhodnega toka iz URI")
            }
        } catch (e: Exception) {
            // Če pride do izjeme, jo izpiši in vrni null
            e.printStackTrace()
            return null
        }
    }
}
