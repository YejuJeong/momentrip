package com.example.momenttrip.ui_screen.uicomponent

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.momenttrip.data.ExpenseEntry
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailDialog(
    tripId: String,
    date: String,
    entry: ExpenseEntry,
    currencyOptions: List<String>,
    exchangeRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onUpdate: (Map<String, Any>) -> Unit
) {
    val context = LocalContext.current

    var amount by remember { mutableStateOf(entry.amount.toString()) }
    var title by remember { mutableStateOf(entry.title) }
    var detail by remember { mutableStateOf(entry.detail ?: "") }
    var paymentType by remember { mutableStateOf(entry.paymentType) }
    var selectedCurrency by remember {
        mutableStateOf(
            entry.currency ?: currencyOptions.firstOrNull() ?: "USD"
        )
    }
    var time by remember {
        mutableStateOf(
            entry.time?.toDate()?.toInstant()
                ?.atZone(ZoneId.systemDefault())?.toLocalTime() ?: LocalTime.now()
        )
    }

    var expanded by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            time = LocalTime.of(hour, minute)
        },
        time.hour,
        time.minute,
        true
    )

    val rate = exchangeRates[selectedCurrency]
    val convertedAmount = amount.toDoubleOrNull()?.let { amt ->
        if (rate != null && rate > 0) amt * rate else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        confirmButton = {
            TextButton(onClick = {
                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    Toast.makeText(context, "올바른 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                if (title.isBlank()) {
                    Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                val fixedDate = LocalDate.parse(date)
                val dateTime = LocalDateTime.of(fixedDate, time)
                val timestamp =
                    Timestamp(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))

                val updatedData = mapOf(
                    "amount" to amountValue,
                    "title" to title,
                    "detail" to detail,
                    "paymentType" to paymentType,
                    "currency" to selectedCurrency,
                    "time" to timestamp
                )
                onUpdate(updatedData)
                onDismiss()
            }) {
                Text("수정")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        title = { Text("지출 수정") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 통화 선택
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCurrency,
                        onValueChange = {},
                        label = { Text("통화 선택") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        currencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCurrency = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("금액") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                if (convertedAmount != null) {
                    Text(
                        "환산 금액: ≈ ${"%,.0f".format(convertedAmount)} KRW",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("상세 내용") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("시간 선택: ${"%02d:%02d".format(time.hour, time.minute)}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("결제 수단", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { paymentType = "카드" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentType == "카드") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("카드")
                    }
                    Button(
                        onClick = { paymentType = "현금" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentType == "현금") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("현금")
                    }
                }
            }
        }
    )
}
