package com.example.momenttrip.data

import com.google.firebase.Timestamp

data class TripInviteEntry(
    val invite_id: String? = null,
    val trip_id: String = "",
    val from_uid: String = "",
    val to_uid: String = "",
    val created_at: Timestamp = Timestamp.now()
)
