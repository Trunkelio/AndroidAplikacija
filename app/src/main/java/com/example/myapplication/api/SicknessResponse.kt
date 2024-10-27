package com.example.myapplication.api

// Podatkovni razred za odgovor o bolezni rastline iz API-ja
data class SicknessResponse(
    val species_name: String?,          // Ime vrste rastline, lahko je null, če ni na voljo
    val species_confidence: String?,    // Stopnja zaupanja pri določanju vrste, kot niz (lahko je null)
    val disease: String,                // Ime bolezni, ki jo je model zaznal
    val disease_confidence: String?     // Stopnja zaupanja pri klasifikaciji bolezni, kot niz (lahko je null)
)
