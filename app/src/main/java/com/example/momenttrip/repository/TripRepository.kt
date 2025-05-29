package com.example.momenttrip.repository

import com.example.momenttrip.data.Trip
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TripRepository {
    private val db = FirebaseFirestore.getInstance()

    //여행 생성
    suspend fun createTrip(
        ownerUid: String,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate,
        countries: List<String>
    ): Result<String> {
        return try {
            val trip = Trip(
                owner_uid = ownerUid,
                title = title,
                start_date = Timestamp(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond, 0),
                end_date = Timestamp(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond, 0),
                countries = countries,
                participants = listOf(ownerUid),
                created_at = Timestamp.now()
            )
            val docRef = db.collection("trips").add(trip).await()
            createSchedules(docRef.id, startDate, endDate)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //날짜 별로 문서 생성
    private suspend fun createSchedules(tripId: String, start: LocalDate, end: LocalDate) {
        val formatter = DateTimeFormatter.ISO_DATE
        val baseRef = db.collection("trips").document(tripId)
        val schedulesRef = baseRef.collection("schedules")
        val expensesRef = baseRef.collection("expenses")
        val reviewsRef = baseRef.collection("reviews")
        val batch = db.batch()

        val dates = generateSequence(start) { if (it < end) it.plusDays(1) else null }.toList() + end

        for (date in dates) {
            val dateStr = date.format(formatter)
            val now = Timestamp.now()

            // schedules 문서 생성
            batch.set(schedulesRef.document(dateStr), mapOf("created_at" to now))
            // expenses 문서 생성
            batch.set(expensesRef.document(dateStr), mapOf("created_at" to now))
            // reviews 문서 생성
            batch.set(reviewsRef.document(dateStr), mapOf("created_at" to now))
        }

        batch.commit().await()
    }


    suspend fun deleteTrip(tripId: String): Result<Unit> {
        val db = FirebaseFirestore.getInstance()
        val tripRef = db.collection("trips").document(tripId)

        return try {
            // 1. schedules/{date}/plans 삭제
            val schedulesSnap = tripRef.collection("schedules").get().await()
            for (scheduleDoc in schedulesSnap.documents) {
                val plansSnap = scheduleDoc.reference.collection("plans").get().await()
                for (planDoc in plansSnap.documents) {
                    planDoc.reference.delete().await()
                }
                scheduleDoc.reference.delete().await()
            }

            // 2. expenses/{date}/entries 삭제
            val expensesSnap = tripRef.collection("expenses").get().await()
            for (dateDoc in expensesSnap.documents) {
                val entriesSnap = dateDoc.reference.collection("entries").get().await()
                for (entry in entriesSnap.documents) {
                    entry.reference.delete().await()
                }
                dateDoc.reference.delete().await()
            }

            // 3. reviews 삭제 (리뷰가 별도 컬렉션이면 이건 생략 가능)
            val reviewsSnap = tripRef.collection("reviews").get().await()
            for (doc in reviewsSnap.documents) {
                doc.reference.delete().await()
            }

            // 4. trips 문서 삭제
            tripRef.delete().await()

            // 5. 내 current_trip_id가 해당 trip이면 null로 갱신
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .update("current_trip_id", FieldValue.delete()) // 또는 null로 set
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}