package com.example.momenttrip.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExchangeRateService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://v6.exchangerate-api.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ExchangeRateApi = retrofit.create(ExchangeRateApi::class.java)
}