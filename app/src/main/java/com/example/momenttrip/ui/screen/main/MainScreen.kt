package com.example.momenttrip.ui.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val selectedTab = remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab.value == "friends",
                    onClick = { selectedTab.value = "friends" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "친구") },
                    label = { Text("친구") }
                )
                NavigationBarItem(
                    selected = selectedTab.value == "home",
                    onClick = { selectedTab.value = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
                    label = { Text("홈") }
                )
                NavigationBarItem(
                    selected = selectedTab.value == "settings",
                    onClick = { selectedTab.value = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
                    label = { Text("설정") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (selectedTab.value) {
                "home" -> AddTripScreen(onLogout = onLogout)
                else -> PlaceholderScreen(selectedTab.value)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${name.uppercase()} 탭 (준비 중)",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
