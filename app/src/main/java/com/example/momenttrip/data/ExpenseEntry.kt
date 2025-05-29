package com.example.momenttrip.data

import com.google.firebase.Timestamp

data class ExpenseEntry(
    val expense_id: String? = null, // Firestore 문서 ID
    val time: Timestamp = Timestamp.now(),
    val title: String = "",
    val detail: String? = null,
    val amount: Double = 0.0,
    val category: String = "",     // 예: 식비, 교통비 등
    val currency: String = "KRW",  // 예: KRW, USD 등
    val paymentType: String = "카드"
)
