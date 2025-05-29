package com.example.momenttrip.repository

import com.example.momenttrip.data.ReviewEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ReviewRepository {
    private val db = FirebaseFirestore.getInstance()

    // 리뷰 추가
    suspend fun addReview(entry: ReviewEntry): Result<String> {
        return try {
            val docRef = db.collection("reviews").add(entry).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 여행 ID로 리뷰 목록 가져오기
    suspend fun getReviewsByTrip(tripId: String): Result<List<ReviewEntry>> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("trip_id", tripId)
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { it.toObject(ReviewEntry::class.java)?.copy(review_id = it.id) }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 리뷰 삭제
    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            db.collection("reviews").document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 리뷰 필드 일부 수정
    suspend fun updateReviewFields(reviewId: String, updatedFields: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("reviews").document(reviewId).update(updatedFields).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
