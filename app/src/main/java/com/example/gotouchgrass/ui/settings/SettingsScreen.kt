package com.example.gotouchgrass.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        SettingsTopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(8.dp))

        // ACCOUNT Section
        SettingsSection(title = viewModel.accountSectionTitle) {
            SettingsItem(
                icon = Icons.Default.Person,
                title = viewModel.editProfileTitle,
                showArrow = true
            )
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = viewModel.notificationsTitle,
                showSwitch = true,
                switchChecked = viewModel.preferences.notificationsEnabled,
                onSwitchChange = { viewModel.updatePreferences(viewModel.preferences.copy(notificationsEnabled = it)) }
            )
            SettingsItem(
                icon = Icons.Default.VolumeUp,
                title = viewModel.soundEffectsTitle,
                showSwitch = true,
                switchChecked = viewModel.preferences.soundEffectsEnabled,
                onSwitchChange = { viewModel.updatePreferences(viewModel.preferences.copy(soundEffectsEnabled = it)) }
            )
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = viewModel.darkModeTitle,
                showSwitch = true,
                switchChecked = viewModel.preferences.darkModeEnabled,
                onSwitchChange = { viewModel.updatePreferences(viewModel.preferences.copy(darkModeEnabled = it)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRIVACY Section
        SettingsSection(title = viewModel.privacySectionTitle) {
            SettingsItem(
                icon = Icons.Default.LocationOn,
                title = viewModel.locationServicesTitle,
                showSwitch = true,
                switchChecked = viewModel.preferences.locationServicesEnabled,
                onSwitchChange = { viewModel.updatePreferences(viewModel.preferences.copy(locationServicesEnabled = it)) }
            )
            SettingsItem(
                icon = Icons.Default.Shield,
                title = viewModel.privacySettingsTitle,
                showArrow = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SUPPORT Section
        SettingsSection(title = viewModel.supportSectionTitle) {
            SettingsItem(
                icon = Icons.Default.Help,
                title = viewModel.helpCenterTitle,
                showArrow = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log Out Button
        LogoutButton(text = viewModel.logoutButtonText)

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        SettingsFooter(
            version = viewModel.appVersion,
            tagline = viewModel.appTagline
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    showArrow: Boolean = false,
    showSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !showSwitch) { }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with circle background
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        when {
            showArrow -> {
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            showSwitch -> {
                Switch(checked = switchChecked, onCheckedChange = onSwitchChange)
            }
        }
    }
}

@Composable
fun LogoutButton(text: String) {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SettingsFooter(version: String, tagline: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = version,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = tagline,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    GoTouchGrassTheme {
        SettingsScreen(onBackClick = {})
    }
}