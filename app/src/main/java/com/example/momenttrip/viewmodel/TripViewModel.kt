package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.repository.TripRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class TripViewModel: ViewModel() {
    
    // 여행 생성
    fun createTrip(
        ownerUid: String,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate,
        countries: List<String>,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripRepository.createTrip(ownerUid, title, startDate, endDate, countries)
            if (result.isSuccess) {
                callback(true, result.getOrNull()) // tripId 반환
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    //여행 삭제
    fun deleteTrip(tripId: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = TripRepository.deleteTrip(tripId)
            if (result.isSuccess) {
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

}