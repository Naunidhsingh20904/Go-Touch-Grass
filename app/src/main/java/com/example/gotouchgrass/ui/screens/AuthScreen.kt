package com.example.gotouchgrass.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gotouchgrass.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onSignIn: (email: String, password: String) -> Unit = { _, _ -> },
    onSignUp: (username: String, displayName: String, email: String, password: String) -> Unit = { _, _, _, _ -> },
    onForgotPassword: () -> Unit = {}
) {
    val isSignIn by viewModel.isSignInTab.collectAsState()
    val username by viewModel.username.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            HeaderSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = GoTouchGrassDimens.SpacingLg)
            )

            // Form Card
            FormCard(
                isSignIn = isSignIn,
                onTabChange = viewModel::setSignInTab,
                username = username,
                onUsernameChange = viewModel::setUsername,
                displayName = displayName,
                onDisplayNameChange = viewModel::setDisplayName,
                email = email,
                onEmailChange = viewModel::setEmail,
                password = password,
                onPasswordChange = viewModel::setPassword,
                onPrimaryAction = {
                    if (isSignIn) {
                        when (val result = viewModel.signIn()) {
                            is AuthResult.Success -> onSignIn(email, password)
                            is AuthResult.Error -> { /* error shown via viewModel.errorMessage */ }
                        }
                    } else {
                        when (val result = viewModel.signUp()) {
                            is AuthResult.Success -> onSignUp(username, displayName, email, password)
                            is AuthResult.Error -> { /* error shown via viewModel.errorMessage */ }
                        }
                    }
                },
                onForgotPassword = onForgotPassword,
                errorMessage = errorMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HeaderSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        AppLogo()

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))

        // Main Title
        Text(
            text = "Go Touch Grass",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

        // Tagline
        Text(
            text = "Discover your world. Capture landmarks.\nEarn rewards for exploring.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))

        // Feature Chips
        FeatureChips()
    }
}

@Composable
private fun AppLogo() {
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Main logo container - dark green rounded square
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(GoTouchGrassDimens.RadiusLarge))
                .background(ForestGreen),
            contentAlignment = Alignment.Center
        ) {
            // Leaf icon (change this later!!!!)
            Text(
                text = "\uD83C\uDF3F",  // Leaf emoji as placeholder
                fontSize = 36.sp
            )
        }

        // Yellow badge with location pin
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(GoldenYellow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = TextPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun FeatureChips() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = GoTouchGrassDimens.SpacingMd)
    ) {
        val features = listOf("Capture Zones", "Earn XP", "Compete", "Explore")
        features.forEach { feature ->
            FeatureChip(text = feature)
        }
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = GoTouchGrassDimens.SpacingMd,
                vertical = GoTouchGrassDimens.SpacingSm
            )
        )
    }
}

@Composable
private fun FormCard(
    isSignIn: Boolean,
    onTabChange: (Boolean) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onForgotPassword: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = GoTouchGrassDimens.RadiusXLarge,
            topEnd = GoTouchGrassDimens.RadiusXLarge
        ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = GoTouchGrassDimens.ElevationHigh
    ) {
        Column(
            modifier = Modifier
                .padding(GoTouchGrassDimens.SpacingLg)
                .animateContentSize()
        ) {
            // Tab Switcher
            TabSwitcher(
                isSignIn = isSignIn,
                onTabChange = onTabChange
            )

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))

            // Form Fields
            if (!isSignIn) {
                AuthTextField(
                    label = "Display Name",
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    placeholder = "How friends will see you",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

                AuthTextField(
                    label = "Username",
                    value = username,
                    onValueChange = onUsernameChange,
                    placeholder = "Choose a unique username",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))
            }

            AuthTextField(
                label = "Email",
                value = email,
                onValueChange = onEmailChange,
                placeholder = "you@example.com",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

            AuthTextField(
                label = "Password",
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Enter password",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))

            // Primary Button
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GoTouchGrassDimens.ButtonHeight),
                shape = RoundedCornerShape(GoTouchGrassDimens.RadiusMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen
                )
            ) {
                Text(
                    text = if (isSignIn) "Start Exploring" else "Create Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingSm))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Footer Link (Sign In only)
            if (isSignIn) {
                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Forgot your password? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Reset it",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ForestGreen,
                        modifier = Modifier.clickable { onForgotPassword() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))
        }
    }
}

@Composable
private fun TabSwitcher(
    isSignIn: Boolean,
    onTabChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingXs)
        ) {
            TabButton(
                text = "Sign In",
                isSelected = isSignIn,
                onClick = { onTabChange(true) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Sign Up",
                isSelected = !isSignIn,
                onClick = { onTabChange(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(GoTouchGrassDimens.RadiusFull))
            .clickable { onClick() },
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
        color = if (isSelected) ForestGreen else Color.Transparent
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) WarmWhite else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = GoTouchGrassDimens.SpacingLg,
                vertical = GoTouchGrassDimens.SpacingMd
            )
        )
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(GoTouchGrassDimens.RadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = ForestGreen,
                cursorColor = ForestGreen
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    GoTouchGrassTheme {
        AuthScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenSignUpPreview() {
    GoTouchGrassTheme {
        AuthScreen()
    }
}
