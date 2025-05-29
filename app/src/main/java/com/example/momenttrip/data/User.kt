package com.example.momenttrip.data

data class User(
    val uid: String = "",
    val email: String = "",
//    val password: String = "",
    val login_type: LoginType,
    val name: String = "",
    val nickname: String = "",
    val phone_number: String = "",
    val profile_url: String? = null,
    val created_at: com.google.firebase.Timestamp? = null,
    val search_key: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val friend_request_ids: List<String> = emptyList(),
    val trip_invite_ids: List<String> = emptyList()
)
