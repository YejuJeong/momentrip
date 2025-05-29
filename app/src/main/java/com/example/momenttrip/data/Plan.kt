package com.example.momenttrip.data

import com.google.firebase.Timestamp

data class Plan(
    val plan_id: String? = null, // Firestore 문서 ID
    val start_time: Timestamp = Timestamp.now(),
    val end_time: Timestamp = Timestamp.now(),
    val title: String = "",
    val memo: String? = null,
    val location: Map<String, Any>? = null // { title: String, lat: Double, lng: Double }
)
