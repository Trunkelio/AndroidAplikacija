package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.ApiService
import com.example.myapplication.api.SicknessResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.DecimalFormat  // Za formatiranje odstotkov
import android.util.Log

class ActivityOutput : AppCompatActivity() {

    // Uporabniški vmesni elementi
    private lateinit var capturedImage: ImageView           // Prikazana slika rastline
    private lateinit var plantNameBox: TextView             // Prikaz imena rastline
    private lateinit var plantAccuracyBox: TextView         // Prikaz zaupanja pri določanju vrste
    private lateinit var plantStanjeBox: TextView           // Prikaz zdravstvenega stanja rastline
    private lateinit var diseaseCheckSwitch: Switch         // Stikalo za preverjanje bolezni
    private lateinit var diseaseResultBox: TextView         // Prikaz rezultata bolezni
    private lateinit var retakeImageButton: ImageButton     // Gumb za ponovno zajemanje slike

    // Spremenljivke za API storitev in podatke
    private lateinit var apiService: ApiService             // API storitev za klice API
    private var imageFilePath: String? = null               // Pot do slikovne datoteke
    private var speciesName: String? = null                 // Shranjeno ime vrste rastline
    private var speciesConfidence: Float = 0f               // Shranjena stopnja zaupanja pri določanju vrste

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_output)            // Nastavi izgled aktivnosti

        // Inicializacija elementov uporabniškega vmesnika
        capturedImage = findViewById(R.id.captured_image)
        plantNameBox = findViewById(R.id.plant_name_box)
        plantAccuracyBox = findViewById(R.id.plant_accuracy_box)
        plantStanjeBox = findViewById(R.id.plant_stanje_box)
        diseaseCheckSwitch = findViewById(R.id.disease_check_switch)
        diseaseResultBox = findViewById(R.id.disease_result_box)
        retakeImageButton = findViewById(R.id.retake_image_button)

        // Inicializacija Retrofit API storitve
        val retrofit = Retrofit.Builder()
            .baseUrl("http://129.152.26.137/") // Zamenjajte z dejanskim URL-jem vašega API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java) // Ustvari instanco API storitve

        // Inicializiraj TextView za prikaz bolezni na privzeto besedilo
        diseaseResultBox.text = "Ni podatkov o bolezni"

        // Pridobi podatke iz LoadingActivity
        imageFilePath = intent.getStringExtra("image_file_path")
        val healthStatus = intent.getStringExtra("health_status")
        speciesName = intent.getStringExtra("species_name")
        speciesConfidence = intent.getFloatExtra("species_confidence", 0f)

        // Logiraj ime vrste, prejeto iz Intent-a
        Log.d("ActivityOutput", "Ime vrste iz Intent-a: '$speciesName'")

        // Prikaži sliko iz predpomnilnika (cache)
        if (imageFilePath != null) {
            val imageFile = File(imageFilePath!!)
            if (imageFile.exists()) {
                capturedImage.setImageURI(Uri.fromFile(imageFile))
            } else {
                Toast.makeText(this, "Slika ni najdena", Toast.LENGTH_SHORT).show()
            }
        }

        // Prikaži rezultate klasifikacije
        plantStanjeBox.text = healthStatus ?: "Ni podatkov o zdravju"
        plantNameBox.text = speciesName ?: "Ni podatkov o vrsti"

        // Formatiraj vrednost zaupanja pri določanju vrste
        val decimalFormat = DecimalFormat("#.##")  // Nastavi format na dve decimalni mesti
        val formattedConfidence = decimalFormat.format(speciesConfidence)
        plantAccuracyBox.text = "$formattedConfidence%"

        // Obravnavaj klasifikacijo bolezni, ko je stikalo pritisnjeno
        diseaseCheckSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Pokliči API za bolezen, ko je stikalo vklopljeno
                performSicknessClassification()
            } else {
                // Ponastavi TextView, ko je stikalo izklopljeno
                diseaseResultBox.text = "Ni podatkov o bolezni"
            }
        }

        // Gumb za ponovno zajemanje slike bo ponastavil procese in se vrnil na CameraActivity
        retakeImageButton.setOnClickListener {
            retakeImage()
        }
    }

    // Metoda za izvedbo API klica za klasifikacijo bolezni
    private fun performSicknessClassification() {
        if (imageFilePath != null) {
            val imageFile = File(imageFilePath!!)
            if (imageFile.exists()) {
                val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
                // Pošlji sliko na API za klasifikacijo bolezni
                apiService.uploadSicknessImage(body).enqueue(object : Callback<SicknessResponse> {
                    override fun onResponse(call: Call<SicknessResponse>, response: Response<SicknessResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                // Posodobi ime vrste in stopnjo zaupanja, če je potrebno
                                val newSpeciesName = it.species_name
                                val newSpeciesConfidence = it.species_confidence?.toFloat() ?: 0f
                                // Posodobi ime vrste in stopnjo zaupanja, če sta različna
                                if (newSpeciesName != null && newSpeciesName != speciesName) {
                                    speciesName = newSpeciesName
                                    speciesConfidence = newSpeciesConfidence
                                    // Prikaži posodobljeno ime vrste in stopnjo zaupanja
                                    plantNameBox.text = speciesName ?: "Ni podatkov o vrsti"
                                    val decimalFormat = DecimalFormat("#.##")
                                    val formattedConfidence = decimalFormat.format(speciesConfidence)
                                    plantAccuracyBox.text = "$formattedConfidence%"
                                }
                                // Prikaži bolezen
                                var sicknessName = it.disease
                                val sicknessConfidence = it.disease_confidence?.toFloat() ?: 0f
                                val formattedSicknessConfidence = DecimalFormat("#.##").format(sicknessConfidence)
                                sicknessName = sicknessName.replace("_", " ")  // Zamenjaj podčrtaje z presledki

                                diseaseResultBox.text = "$sicknessName [$formattedSicknessConfidence%]"
                            }
                        } else {
                            // Če odgovor ni uspešen, prikaži sporočilo o napaki
                            Toast.makeText(this@ActivityOutput, "Neuspešno pridobivanje veljavnega odgovora", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<SicknessResponse>, t: Throwable) {
                        // Če pride do napake v omrežju, prikaži sporočilo o napaki
                        Toast.makeText(this@ActivityOutput, "Napaka v omrežju: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // Če slikovna datoteka ne obstaja, prikaži sporočilo o napaki
                Toast.makeText(this, "Slikovna datoteka ni najdena", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Če pot do slikovne datoteke ni na voljo, prikaži sporočilo o napaki
            Toast.makeText(this, "Pot do slikovne datoteke ni na voljo", Toast.LENGTH_SHORT).show()
        }
    }

    // Funkcija za ponovno zajemanje slike
    private fun retakeImage() {
        // Izbriši slikovno datoteko iz predpomnilnika
        val cacheFile = File(cacheDir, "confirmed_image.jpg")
        if (cacheFile.exists()) {
            cacheFile.delete()
        }

        // Pomakni se nazaj na CameraActivity za ponovno zajemanje slike
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)

        // Zaključi trenutno aktivnost, da jo odstraniš iz sklada
        finish()
    }
}
