package com.example.momenttrip.data

import com.google.firebase.Timestamp

data class FriendRequest(
    val request_id: String? = null,
    val from_uid: String = "",       // 요청 보낸 사용자 UID
    val to_uid: String = "",         // 요청 받은 사용자 UID
    val created_at: Timestamp = Timestamp.now()  // 요청 시간
)
