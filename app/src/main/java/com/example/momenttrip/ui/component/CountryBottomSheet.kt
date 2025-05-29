package com.example.momenttrip.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.CountryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryBottomSheet(
    onDismiss: () -> Unit,
    onCountriesSelected: (List<String>) -> Unit
) {
    val viewModel: CountryViewModel = viewModel()
    val countries by viewModel.countries.collectAsState()
    val searchText = remember { mutableStateOf("") }
    val selectedCountries = remember { mutableStateOf(setOf<String>()) }
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCountries()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                label = { Text("나라 검색") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                ) {
                    items(countries.filter {
                        it.name.contains(searchText.value, ignoreCase = true)
                    }) { country ->
                        val isSelected = selectedCountries.value.contains(country.name)

                        ListItem(
                            headlineContent = { Text(country.name) },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountries.value =
                                        if (isSelected)
                                            selectedCountries.value - country.name
                                        else
                                            selectedCountries.value + country.name
                                }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        onCountriesSelected(selectedCountries.value.toList())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("선택 완료 (${selectedCountries.value.size}개)")
                }
            }
        }
    }
}
