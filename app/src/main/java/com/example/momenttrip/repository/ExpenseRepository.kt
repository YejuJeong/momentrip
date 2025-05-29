package com.example.momenttrip.repository

import com.example.momenttrip.data.ExpenseEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()

    //지출 항목 추가
    suspend fun addExpense(tripId: String, date: String, entry: ExpenseEntry): Result<Unit> {
        return try {
            db.collection("trips")
                .document(tripId)
                .collection("expenses")
                .document(date)
                .collection("entries")
                .add(entry)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 지출 항목 불러오기
    suspend fun getExpenses(tripId: String, date: String): Result<List<ExpenseEntry>> {
        return try {
            val snapshot = db.collection("trips")
                .document(tripId)
                .collection("expenses")
                .document(date)
                .collection("entries")
                .get()
                .await()

            val entries = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ExpenseEntry::class.java)?.copy(expense_id = doc.id)
            }

            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //지출 항목 수정
    suspend fun updateExpenseFields(
        tripId: String,
        date: String,
        expenseId: String,
        updatedFields: Map<String, Any>
    ): Result<Unit> {
        return try {
            FirebaseFirestore.getInstance()
                .collection("trips")
                .document(tripId)
                .collection("expenses")
                .document(date)
                .collection("entries")
                .document(expenseId)
                .update(updatedFields)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 지출 항목 지우기
    suspend fun deleteExpense(tripId: String, date: String, expenseId: String): Result<Unit> {
        return try {
            db.collection("trips")
                .document(tripId)
                .collection("expenses")
                .document(date)
                .collection("entries")
                .document(expenseId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}