package com.example.momenttrip.ui_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.data.CountryData
import com.example.momenttrip.data.ExpenseEntry
import com.example.momenttrip.ui_screen.uicomponent.ExpenseAddBottomSheet
import com.example.momenttrip.ui_screen.uicomponent.ExpenseDetailDialog
import com.example.momenttrip.ui_screen.uicomponent.ExpenseViewDialog
import com.example.momenttrip.viewmodel.ExpenseViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun ExpenseScreen(
    tripId: String,
    date: String,
    startDate: LocalDate,
    tripCountries: List<String>,
    allCountries: List<CountryData>
) {
    val viewModel: ExpenseViewModel = viewModel()
    val expenses by viewModel.expenses.collectAsState()
    val exchangeRate by viewModel.exchangeRate.collectAsState()
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntry?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val currencyOptions = remember(tripCountries, allCountries) {
        tripCountries.mapNotNull { tripCountry ->
            allCountries.find { it.name == tripCountry }?.currencyCode
        }.distinct()
    }

    LaunchedEffect(currencyOptions) {
        if (currencyOptions.isNotEmpty()) {
            currencyOptions.forEach { code ->
                viewModel.loadExchangeRate(context, code, "KRW")
            }
        }
    }

    LaunchedEffect(tripId, date) {
        viewModel.loadExpenses(tripId, date)
    }

    val currentDate = LocalDate.parse(date)
    val dayIndex = ChronoUnit.DAYS.between(startDate, currentDate).toInt() + 1

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$date (DAY $dayIndex)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "지출 합계")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(expenses, key = { it.expense_id ?: it.hashCode() }) { entry ->
                    var offsetX by remember { mutableStateOf(0f) }
                    val maxSwipe = 250f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.Red),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            IconButton(onClick = {
                                entry.expense_id?.let { id ->
                                    viewModel.deleteExpense(tripId, date, id) { success, msg ->
                                        if (!success) Log.e("삭제 실패", msg ?: "")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = Color.White
                                )
                            }
                        }

                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), 0) }
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (offsetX < -maxSwipe * 0.7f) {
                                                offsetX = 0f
                                                entry.expense_id?.let { id ->
                                                    viewModel.deleteExpense(tripId, date, id) { success, msg ->
                                                        if (!success) Log.e("삭제 실패", msg ?: "")
                                                    }
                                                }
                                            } else {
                                                offsetX = 0f
                                            }
                                        },
                                        onHorizontalDrag = { _, delta ->
                                            val newOffset = (offsetX + delta).coerceIn(-maxSwipe, 0f)
                                            offsetX = newOffset
                                        }
                                    )
                                }
                                .fillMaxWidth()
                        ) {
                            val rate = exchangeRate[entry.currency]
                            val converted = if (rate != null && rate > 0) entry.amount * rate else null

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clickable {
                                        selectedExpense = entry
                                        showEditDialog = false
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    val firebaseTimestamp = entry.time
                                    val instant = firebaseTimestamp.toDate().toInstant()
                                    val localDateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                                    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                                    val formattedTimeText = localDateTime.format(timeFormatter)

                                    Text(formattedTimeText)
                                    Text("${entry.title} [${entry.category}]")
//                                    Text("결제수단: ${entry.paymentType}")
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${entry.amount} ${entry.currency} [${entry.paymentType}]")
                                    if (converted != null) {
                                        Text("≈ ${"%,.0f".format(converted)} KRW", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text("환율 정보 없음", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "추가")
        }

        if (showSheet) {
            ExpenseAddBottomSheet(
                date = date,
                currencyOptions = currencyOptions,
                exchangeRates = exchangeRate,
                onDismiss = { showSheet = false },
                onSubmit = { entry ->
                    viewModel.addExpense(tripId, date, entry) { success, message ->
                        if (!success) println("지출 추가 실패: $message")
                    }
                }
            )
        }

        selectedExpense?.let { expense ->
            if (showEditDialog) {
                ExpenseDetailDialog(
                    tripId = tripId,
                    date = date,
                    entry = expense,
                    currencyOptions = currencyOptions,
                    exchangeRates = exchangeRate,
                    onDismiss = {
                        showEditDialog = false
                        selectedExpense = null
                    },
                    onUpdate = { updatedFields ->
                        val id = expense.expense_id ?: return@ExpenseDetailDialog
                        viewModel.updateExpenseFields(
                            tripId = tripId,
                            date = date,
                            expenseId = id,
                            updatedFields = updatedFields
                        ) { success, message ->
                            if (!success) Log.e("수정 실패", message ?: "")
                        }
                    }
                )
            } else {
                ExpenseViewDialog(
                    entry = expense,
                    onDismiss = { selectedExpense = null },
                    onEditClick = { showEditDialog = true },
                    onDeleteClick = {
                        val id = expense.expense_id ?: return@ExpenseViewDialog
                        viewModel.deleteExpense(tripId, date, id) { success, error ->
                            if (success) {
                                selectedExpense = null
                            } else {
                                Log.e("삭제 실패", error ?: "알 수 없는 오류")
                            }
                        }
                    }
                )
            }
        }
    }
}