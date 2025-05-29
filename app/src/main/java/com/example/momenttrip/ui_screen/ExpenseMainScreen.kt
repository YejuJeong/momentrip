package com.example.momenttrip.ui_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momenttrip.data.CountryData
import java.time.LocalDate

@Composable
fun ExpenseMainScreen(
    tripId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    tripCountries: List<String>,
    allCountries: List<CountryData>
) {
    var selectedDate by remember { mutableStateOf(startDate) }

    val dateRange = remember(startDate, endDate) {
        generateDateRange(startDate, endDate)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "가계부", fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dateRange) { date ->
                val isSelected = date == selectedDate
                Button(
                    onClick = { selectedDate = date },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = date.toString().substring(5), // MM-DD
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        HorizontalDivider()

        ExpenseScreen(
            tripId = tripId,
            date = selectedDate.toString(),
            startDate = startDate,
            tripCountries = tripCountries,
            allCountries = allCountries
        )
    }
}

fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = start
    while (!current.isAfter(end)) {
        dates.add(current)
        current = current.plusDays(1)
    }
    return dates
}