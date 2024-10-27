package com.example.myapplication.api

// Podatkovni razred za odgovor o zdravstvenem stanju iz API-ja
data class HealthResponse(
    val health_status: String  // Oznaka, ki jo vrne model, npr. "Zdrav" ali "Bolan"
)
