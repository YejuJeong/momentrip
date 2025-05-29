package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.ReviewEntry
import com.example.momenttrip.repository.ReviewRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class ReviewViewModel : ViewModel() {
    // 리뷰 목록 상태 저장
    private val _reviews = MutableStateFlow<List<ReviewEntry>>(emptyList())
    val reviews: StateFlow<List<ReviewEntry>> = _reviews

    // 특정 여행의 리뷰 불러오기
    fun loadReviews(tripId: String) {
        viewModelScope.launch {
            val result = ReviewRepository.getReviewsByTrip(tripId)
            if (result.isSuccess) {
                _reviews.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    // 리뷰 추가
    fun addReview(
        writerUid: String,
        tripId: String,
        date: LocalDate,
        title: String,
        content: String,
        thumbnailUrl: String?,          // 선택사항
        imageUrls: List<String>,       // 이미지 여러 장
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val timestamp = Timestamp(
                    Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                )

                val review = ReviewEntry(
                    writer_uid = writerUid,
                    trip_id = tripId,
                    date = timestamp,
                    title = title,
                    content = content,
                    thumbnail_url = thumbnailUrl,
                    image_urls = imageUrls,
                    created_at = Timestamp.now()
                )

                val result = ReviewRepository.addReview(review)
                if (result.isSuccess) {
                    loadReviews(tripId)
                    callback(true, null)
                } else {
                    callback(false, result.exceptionOrNull()?.message)
                }

            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }


    // 리뷰 삭제
    fun deleteReview(reviewId: String, tripId: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = ReviewRepository.deleteReview(reviewId)
            if (result.isSuccess) {
                loadReviews(tripId)
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message)
            }
        }
    }

    // 리뷰 수정
    fun updateReview(
        reviewId: String,
        tripId: String,
        newTitle: String,
        newContent: String,
        newThumbnailUrl: String?,
        newImageUrls: List<String>,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedFields = mutableMapOf<String, Any>()

                if (newTitle.isNotBlank()) updatedFields["title"] = newTitle
                if (newContent.isNotBlank()) updatedFields["content"] = newContent
                updatedFields["thumbnail_url"] = newThumbnailUrl ?: ""
                updatedFields["image_urls"] = newImageUrls

                val result = ReviewRepository.updateReviewFields(reviewId, updatedFields)
                if (result.isSuccess) {
                    loadReviews(tripId) // 리스트 갱신
                    callback(true, null)
                } else {
                    callback(false, result.exceptionOrNull()?.message)
                }

            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }
}
