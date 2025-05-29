package com.example.momenttrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.theme.MomentTripTheme
import com.example.momenttrip.ui_screen.ExpenseMainScreen
import com.example.momenttrip.ui_screen.ExpenseScreen
import com.example.momenttrip.viewmodel.CountryViewModel
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            MomentTripTheme {
//               AppEntryPoint()
                val countryViewModel: CountryViewModel = viewModel()
                val allCountries by countryViewModel.countries.collectAsState()

                LaunchedEffect(Unit) {
                    countryViewModel.fetchCountries()
                }

                ExpenseMainScreen(
                    tripId = "trip_id",
                    startDate = LocalDate.parse("2025-05-01"),
                    endDate = LocalDate.parse("2025-05-05"),
                    tripCountries = listOf("Switzerland", "France"),
                    allCountries = allCountries
                )

            }
        }
    }
}

