package com.example.momenttrip.data.model

data class CountryResponse(
    val name: Name,
    val currencies: Map<String, CurrencyInfo>? = null
) {
    data class Name(
        val common: String = ""
    )
    data class CurrencyInfo( // 통화 정보 내부 구조
        val name: String? = null,
        val symbol: String? = null
    )
}