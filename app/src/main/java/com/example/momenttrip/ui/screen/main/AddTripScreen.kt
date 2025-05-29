package com.example.momenttrip.ui.screen.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.momenttrip.ui.component.CountryBottomSheet
import java.time.LocalDate

@Composable
fun AddTripScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val tripName = remember { mutableStateOf("") }
    val selectedCountries = remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedDates = remember { mutableStateOf<Pair<LocalDate?, LocalDate?>>(null to null) }

    val showCountrySheet = remember { mutableStateOf(false) }
    val showDateSheet = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    if (tripName.value.isBlank() || selectedCountries.value.isEmpty() || selectedDates.value.first == null) {
                        Toast.makeText(context, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                    } else {
                        // 여행 생성 처리
                        Toast.makeText(context, "여행 생성 완료!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("여행 만들기")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("여행 추가", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = tripName.value,
                onValueChange = { tripName.value = it },
                label = { Text("여행 이름") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("여행 조건", style = MaterialTheme.typography.titleMedium)

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("나라 선택", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showCountrySheet.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedCountries.value.isNotEmpty())
                                selectedCountries.value.joinToString(", ")
                            else "나라를 선택하세요"
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("여행 기간", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showDateSheet.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val (start, end) = selectedDates.value
                        Text(
                            text = if (start != null && end != null)
                                "${start} ~ ${end}"
                            else "날짜를 선택하세요"
                        )
                    }
                }
            }
        }

        if (showCountrySheet.value) {
            CountryBottomSheet(
                onDismiss = { showCountrySheet.value = false },
                onCountriesSelected = {
                    selectedCountries.value = it
                    showCountrySheet.value = false
                }
            )
        }

        // DateBottomSheet도 여기에 연동 예정
    }
}

