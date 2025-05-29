package com.example.momenttrip.ui_screen.uicomponent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.momenttrip.data.ExpenseEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseViewDialog(
    entry: ExpenseEntry,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val receiptColor = Color(0xFFFAFAFA)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(min = 400.dp)
        ) {
            // 배경 Surface
            Surface(
                modifier = Modifier.matchParentSize(),
                shape = RectangleShape,
                color = receiptColor,
                tonalElevation = 3.dp
            ) {
                // 콘텐츠 배치
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // 상단 아이콘 Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "수정")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Text("시간: ", fontWeight = FontWeight.Bold)
                        Text(entry.time.toDate().formatToReceiptTime())
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("금액: ", fontWeight = FontWeight.Bold)
                        Text("${entry.amount} ${entry.currency}")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("[${entry.paymentType}]")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("상세:", fontWeight = FontWeight.Bold)
                    Text(entry.detail?.ifBlank { "-" } ?: "-")
                }
            }

            // 하단 닫기 버튼
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("닫기")
            }
        }
    }
}

fun Date.formatToReceiptTime(): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return sdf.format(this)
}
