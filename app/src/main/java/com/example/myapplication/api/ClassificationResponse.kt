package com.example.myapplication.api

// Podatkovni razred za shranjevanje odgovora klasifikacije iz API-ja
data class ClassificationResponse(
    // Ime vrste rastline, kot ga vrne API
    val species_name: String?,  // Lahko je null, če API ne vrne imena vrste

    // Stopnja zaupanja pri določanju vrste
    val confidence: Float?      // Lahko je null, če API ne vrne stopnje zaupanja za vse končne točke
)
