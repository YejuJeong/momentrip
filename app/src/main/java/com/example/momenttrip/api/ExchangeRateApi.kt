package com.example.momenttrip.api

import com.example.momenttrip.data.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    @GET("v6/{apiKey}/latest/{base}")
    suspend fun getLatestRates(
        @Path("apiKey") apiKey: String,
        @Path("base") baseCurrency: String
    ): ExchangeRateResponse
}