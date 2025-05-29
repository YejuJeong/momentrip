package com.example.momenttrip.data

data class ExchangeRateResponse(
    val result: String,
    val base_code: String = "KRW",
    val conversion_rates: Map<String, Double>
)
