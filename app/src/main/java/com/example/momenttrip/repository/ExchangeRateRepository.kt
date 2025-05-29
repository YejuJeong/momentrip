package com.example.momenttrip.repository

import android.content.Context
import android.util.Log
import com.example.momenttrip.api.ExchangeRateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object ExchangeRateRepository {
    private const val API_KEY = "d016425be0c93272858b9f4e" // TODO: 안전하게 관리 필요

//    // base 통화 → target 통화의 환율 가져오기
//    suspend fun getRate(base: String, target: String): Result<Double> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = ExchangeRateService.api.getLatestRates(API_KEY, base)
//                val rate = response.conversion_rates[target]
//                    ?: return@withContext Result.failure(Exception("통화 코드 없음: $target"))
//                Result.success(rate)
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
//        }
//    }

    // 캐시 포함: 하루에 1번만 API 호출
    suspend fun getRateWithCache(context: Context, base: String, target: String): Result<Double> {
        return withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("exchange_rate_cache", Context.MODE_PRIVATE)
            val today = LocalDate.now().toEpochDay()
            val lastUpdated = prefs.getLong("last_updated_day", -1L)
            val cacheKey = "rate_${base}_$target"

            // 캐시된 환율이 오늘 날짜면 그대로 사용
            if (lastUpdated == today) {
                val cached = prefs.getFloat(cacheKey, -1f)
                if (cached >= 0) return@withContext Result.success(cached.toDouble())
            }

            // 최신 환율 API 호출
            try {
                val response = ExchangeRateService.api.getLatestRates(API_KEY, base)
                val rate = response.conversion_rates[target]
                    ?: return@withContext Result.failure(Exception("환율 없음: $target"))

                // 캐시에 저장
                prefs.edit()
                    .putFloat(cacheKey, rate.toFloat())
                    .putLong("last_updated_day", today)
                    .apply()

                Result.success(rate)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}