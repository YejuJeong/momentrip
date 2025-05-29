package com.example.momenttrip.data

import com.google.firebase.Timestamp

data class ReviewEntry(
    val review_id: String? = null,              // Firestore 문서 ID (앱 내부 용도)
    val writer_uid: String = "",                // 리뷰 작성자 UID
    val trip_id: String = "",                   // 리뷰가 속한 여행 ID
    val date: Timestamp = Timestamp.now(),      // 리뷰 날짜
    val title: String = "",                     // 리뷰 제목
    val content: String = "",                   // 리뷰 본문
    val thumbnail_url: String? = null,          // 썸네일 이미지 URL
    val image_urls: List<String> = emptyList(), // 첨부 이미지들
    val created_at: Timestamp = Timestamp.now() // 생성 시간
)
