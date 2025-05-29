package com.example.momenttrip.api

import com.example.momenttrip.data.model.CountryResponse
import retrofit2.http.GET

interface CountryApiService {
    @GET("v3.1/all")
    suspend fun getAllCountries(): List<CountryResponse>
}