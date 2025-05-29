package com.example.momenttrip.data

import com.google.firebase.Timestamp

data class Trip(
    val trip_id: String? = null,
    val owner_uid: String = "",
    val title: String = "",
    val start_date: Timestamp = Timestamp.now(),
    val end_date: Timestamp = Timestamp.now(),
    val countries: List<String> = emptyList(),
    val participants: List<String> = emptyList(), // 여행 참여자들
    val created_at: Timestamp = Timestamp.now()
)
