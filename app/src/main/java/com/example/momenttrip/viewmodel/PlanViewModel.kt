package com.example.momenttrip.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.Plan
import com.example.momenttrip.repository.PlanRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class PlanViewModel: ViewModel() {
    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans

    private val _selectedPlan = MutableStateFlow<Plan?>(null)
    val selectedPlan: StateFlow<Plan?> = _selectedPlan

    // 일정 선택
    fun selectPlan(plan: Plan) {
        _selectedPlan.value = plan
    }

    // 일정 선택 해제
    fun clearSelectedPlan() {
        _selectedPlan.value = null
    }

    // 날짜별 일정 불러오기
    fun loadPlansByDate(tripId: String, date: LocalDate, onError: ((String) -> Unit)? = null) {
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        viewModelScope.launch {
            val result = PlanRepository.getPlansByDate(tripId, dateStr)
            if (result.isSuccess) {
                _plans.value = result.getOrNull()?.sortedBy { it.start_time.toDate() } ?: emptyList()
            } else {
                Log.e("PlanViewModel", "일정 불러오기 실패: ${result.exceptionOrNull()?.message}")
                onError?.invoke(result.exceptionOrNull()?.message ?: "알 수 없는 오류")
            }
        }
    }

    // 일정 추가
    fun addPlanFromInput(
        tripId: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        title: String,
        memo: String?,
        location: Map<String, Any>?,
        callback: (Boolean, String?) -> Unit
    ) {
        if (startTime >= endTime) {
            return callback(false, "시작 시간은 종료 시간보다 빨라야 합니다.")
        }

        viewModelScope.launch {
            try {
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)

                val startTimestamp = Timestamp(
                    Date.from(LocalDateTime.of(date, startTime).atZone(ZoneId.systemDefault()).toInstant())
                )

                val endTimestamp = Timestamp(
                    Date.from(LocalDateTime.of(date, endTime).atZone(ZoneId.systemDefault()).toInstant())
                )

                val plan = Plan(
                    start_time = startTimestamp,
                    end_time = endTimestamp,
                    title = title,
                    memo = memo,
                    location = location
                )

                val result = PlanRepository.addPlan(tripId, dateStr, plan)
                if (result.isSuccess) {
                    loadPlansByDate(tripId, date)
                    callback(true, null)
                } else {
                    callback(false, result.exceptionOrNull()?.message)
                }

            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    // 일정 수정
    fun updateSelectedPlan(
        tripId: String,
        date: LocalDate,
        newTitle: String,
        newStartTime: LocalTime,
        newEndTime: LocalTime,
        newMemo: String?,
        newLocation: Map<String, Any>?,
        callback: (Boolean, String?) -> Unit
    ) {
        val current = _selectedPlan.value ?: return callback(false, "선택된 일정이 없습니다.")
        val planId = current.plan_id ?: return callback(false, "일정 ID 없음")
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)

        val startTimestamp = Timestamp(
            Date.from(LocalDateTime.of(date, newStartTime).atZone(ZoneId.systemDefault()).toInstant())
        )
        val endTimestamp = Timestamp(
            Date.from(LocalDateTime.of(date, newEndTime).atZone(ZoneId.systemDefault()).toInstant())
        )

        val updated = current.copy(
            title = newTitle,
            start_time = startTimestamp,
            end_time = endTimestamp,
            memo = newMemo,
            location = newLocation
        )

        viewModelScope.launch {
            val result = PlanRepository.updatePlan(tripId, dateStr, planId, updated)
            if (result.isSuccess) {
                loadPlansByDate(tripId, date)
                clearSelectedPlan()
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    // 일정 삭제
    fun deleteSelectedPlan(
        tripId: String,
        date: LocalDate,
        callback: (Boolean, String?) -> Unit
    ) {
        val plan = _selectedPlan.value ?: return callback(false, "선택된 일정이 없습니다.")
        val planId = plan.plan_id ?: return callback(false, "일정 ID 없음")
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)

        viewModelScope.launch {
            val result = PlanRepository.deletePlan(tripId, dateStr, planId)
            if (result.isSuccess) {
                loadPlansByDate(tripId, date)
                _selectedPlan.value = null
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }
}
