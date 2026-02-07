package com.example.gotouchgrass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {

    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(24.dp)) {

        Text("Settings")

        Text("Dark Mode")
        Switch(
            checked = darkModeEnabled,
            onCheckedChange = { darkModeEnabled = it }
        )

        Text("Notifications")
        Switch(
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it }
        )
    }
}
