package com.example.momenttrip.repository

import com.example.momenttrip.data.Plan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object PlanRepository {
    private val db = FirebaseFirestore.getInstance()

    // schedules/{date} 문서가 없으면 생성
    private suspend fun ensureScheduleExists(tripId: String, date: String) {
        val docRef = db.collection("trips").document(tripId)
            .collection("schedules").document(date)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            docRef.set(mapOf("created_at" to com.google.firebase.Timestamp.now())).await()
        }
    }

    // 일정 추가
    suspend fun addPlan(tripId: String, date: String, plan: Plan): Result<Unit> {
        return try {
            ensureScheduleExists(tripId, date)
            db.collection("trips")
                .document(tripId)
                .collection("schedules")
                .document(date)
                .collection("plans")
                .add(plan)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 일정 불러오기
    suspend fun getPlansByDate(tripId: String, date: String): Result<List<Plan>> {
        return try {
            val snapshot = db.collection("trips")
                .document(tripId)
                .collection("schedules")
                .document(date)
                .collection("plans")
                .get()
                .await()

            val plans = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Plan::class.java)?.copy(plan_id = doc.id)
            }

            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 일정 수정
    suspend fun updatePlan(tripId: String, date: String, planId: String, plan: Plan): Result<Unit> {
        return try {
            db.collection("trips")
                .document(tripId)
                .collection("schedules")
                .document(date)
                .collection("plans")
                .document(planId)
                .set(plan.copy(plan_id = null), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 일정 삭제
    suspend fun deletePlan(tripId: String, date: String, planId: String): Result<Unit> {
        return try {
            db.collection("trips")
                .document(tripId)
                .collection("schedules")
                .document(date)
                .collection("plans")
                .document(planId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}