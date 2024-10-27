package com.example.myapplication.api

// Podatkovni razred, ki predstavlja odgovor API-ja za prepoznavo vrste rastline
data class SpeciesResponse(

    // Ime vrste rastline, ki ga vrne API po analizi slike
    val species_name: String,  // Ime vrste rastline

    // Stopnja zaupanja pri prepoznavi vrste, izražena kot odstotek (float vrednost med 0 in 1)
    val confidence: Float      // Odstotek zaupanja v prepoznavo vrste
)
