package com.example.myapplication.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Definicija vmesnika ApiService za Retrofit klice API
interface ApiService {
    // Metoda za nalaganje slike za analizo zdravja rastline
    @Multipart
    @POST("health")
    fun uploadHealthImage(
        @Part file: MultipartBody.Part  // Del zahteve, ki vsebuje datoteko slike
    ): Call<HealthResponse>            // Vrne klic, ki bo podal HealthResponse

    // Metoda za nalaganje slike za identifikacijo vrste rastline
    @Multipart
    @POST("species")
    fun uploadSpeciesImage(
        @Part file: MultipartBody.Part  // Del zahteve, ki vsebuje datoteko slike
    ): Call<SpeciesResponse>           // Vrne klic, ki bo podal SpeciesResponse

    // Metoda za nalaganje slike za klasifikacijo bolezni rastline
    @Multipart
    @POST("sickness")
    fun uploadSicknessImage(
        @Part file: MultipartBody.Part  // Del zahteve, ki vsebuje datoteko slike
    ): Call<SicknessResponse>          // Vrne klic, ki bo podal SicknessResponse
}
