package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import android.util.Log  // Za logiranje
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.ApiService
import com.example.myapplication.api.HealthResponse
import com.example.myapplication.api.SpeciesResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.Locale  // Uvoz Locale

class LoadingActivity : AppCompatActivity() {

    // Spremenljivke za API storitev in podatke
    private lateinit var apiService: ApiService          // API storitev za komunikacijo s strežnikom
    private var healthStatus: String? = null             // Zdravstveno stanje rastline
    private var speciesName: String? = null              // Ime vrste rastline
    private var speciesConfidence: Float? = null         // Stopnja zaupanja pri določanju vrste
    private var imageFilePath: String? = null            // Pot do slikovne datoteke

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)        // Nastavi izgled aktivnosti

        // Inicializacija Retrofit za klice API
        val retrofit = Retrofit.Builder()
            .baseUrl("http://129.152.26.137/")           // Zamenjaj z dejanskim URL-jem API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)  // Ustvari instanco API storitve

        // Pridobi pot do slikovne datoteke iz ConfirmActivity
        imageFilePath = intent.getStringExtra("image_file_path")
        if (imageFilePath != null) {
            val originalImageFile = File(imageFilePath!!)
            // Spremeni velikost slike na 256x256 preden jo pošlješ na API
            val resizedImageFile = resizeImage(originalImageFile)
            fetchHealthData(resizedImageFile)            // Pridobi podatke o zdravju rastline
        } else {
            // Če slike ni, prikaži sporočilo in zaključi aktivnost
            Toast.makeText(this, "Slika ni najdena", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Spremeni velikost slike na 256x256 preden jo naložiš
    private fun resizeImage(imageFile: File): File {
        // Dekodiraj slikovno datoteko v bitmap
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

        // Preveri, ali je slika že velikosti 256x256
        if (bitmap.width == 256 && bitmap.height == 256) {
            // Če je slika že 256x256, vrni originalno datoteko
            return imageFile
        }

        // Če slika ni 256x256, spremeni njeno velikost
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
        val resizedFile = File(cacheDir, "resized_image.jpg")
        val outputStream = FileOutputStream(resizedFile)

        // Stisni spremenjeno sliko in jo shrani v novo datoteko
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Vrni datoteko spremenjene slike
        return resizedFile
    }

    // Pridobi podatke o zdravju rastline iz API
    private fun fetchHealthData(imageFile: File) {
        val mimeType = "image/jpeg"                                      // Tip vsebine za sliko
        val requestFile = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
        // Pošlji sliko na API za analizo zdravja
        apiService.uploadHealthImage(body).enqueue(object : Callback<HealthResponse> {
            // Obravnava odgovora na uspešno zahtevo
            override fun onResponse(call: Call<HealthResponse>, response: Response<HealthResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        healthStatus = it.health_status                  // Shrani zdravstveno stanje
                        // Nadaljuj s pridobivanjem podatkov o vrsti rastline
                        fetchSpeciesData(imageFile)
                    } ?: run {
                        handleError("Neuspešna obdelava podatkov o zdravju")
                    }
                } else {
                    handleError("Neuspešno pridobivanje podatkov o zdravju: ${response.errorBody()?.string()}")
                }
            }
            // Obravnava napake pri zahtevi
            override fun onFailure(call: Call<HealthResponse>, t: Throwable) {
                handleError("Napaka v omrežju: ${t.message}", t)
            }
        })
    }

    // Pridobi podatke o vrsti rastline iz API
    private fun fetchSpeciesData(imageFile: File) {
        val mimeType = "image/jpeg"                                      // Tip vsebine za sliko
        val requestFile = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        // Pošlji sliko na API za določitev vrste rastline
        apiService.uploadSpeciesImage(body).enqueue(object : Callback<SpeciesResponse> {
            // Obravnava odgovora na uspešno zahtevo
            override fun onResponse(call: Call<SpeciesResponse>, response: Response<SpeciesResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        // Izvleči ime rastline
                        speciesName = it.species_name.split("/").first().trim()

                        // Logiraj ime vrste
                        Log.d("LoadingActivity", "Ime vrste: '$speciesName'")

                        speciesConfidence = it.confidence.toFloat()      // Shrani stopnjo zaupanja
                        // Podatki so pripravljeni; nadaljuj na naslednjo aktivnost
                        proceedToOutput()
                    } ?: run {
                        handleError("Neuspešna obdelava podatkov o vrsti")
                    }
                } else {
                    handleError("Neuspešno pridobivanje podatkov o vrsti: ${response.errorBody()?.string()}")
                }
            }

            // Obravnava napake pri zahtevi
            override fun onFailure(call: Call<SpeciesResponse>, t: Throwable) {
                handleError("Napaka v omrežju: ${t.message}", t)
            }
        })
    }

    // Nadaljuj na ActivityOutput, ko so podatki pripravljeni
    private fun proceedToOutput() {
        val intent = Intent(this@LoadingActivity, ActivityOutput::class.java)
        intent.putExtra("image_file_path", imageFilePath)             // Posreduj pot do originalne slike
        intent.putExtra("health_status", healthStatus)                // Posreduj zdravstveno stanje
        intent.putExtra("species_name", speciesName)                  // Posreduj ime vrste
        intent.putExtra("species_confidence", speciesConfidence!!)    // Posreduj stopnjo zaupanja
        startActivity(intent)                                         // Zaženi ActivityOutput
        finish()                                                      // Zaključi trenutno aktivnost
    }

    // Enotna obravnava napak
    private fun handleError(message: String, throwable: Throwable? = null) {
        throwable?.printStackTrace()                                  // Izpiši sled napake, če obstaja
        runOnUiThread {
            Toast.makeText(this@LoadingActivity, message, Toast.LENGTH_LONG).show()  // Prikaži sporočilo o napaki
            finish()                                                                  // Zaključi trenutno aktivnost
        }
    }
}
