package com.rewardpoints.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.rewardpoints.app.ui.components.glass.GlassButton
import com.rewardpoints.app.ui.components.glass.GlassCard
import com.rewardpoints.app.ui.components.glass.GlassTextField
import com.rewardpoints.app.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var usernameInput by remember(uiState.username) { mutableStateOf(uiState.username) }
    var showTodoistDialog by remember { mutableStateOf(false) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Section
        SettingsSection(title = "Profile") {
            GlassTextField(
                value = usernameInput,
                onValueChange = {
                    usernameInput = it
                    viewModel.updateUsername(it)
                },
                label = "Username",
                placeholder = "Enter your name"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Integrations Section
        SettingsSection(title = "Integrations") {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showTodoistDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Todoist",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Inter
                        )
                        Text(
                            text = if (uiState.todoistConnected) "✅ Connected - Tap to change" else "Tap to connect",
                            color = if (uiState.todoistConnected) AccentSuccess else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }
                    Icon(
                        imageVector = if (uiState.todoistConnected) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (uiState.todoistConnected) AccentSuccess else TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showGeminiDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Agent (Gemini)",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Inter
                        )
                        Text(
                            text = if (uiState.geminiConnected) "✅ Connected - Tap to change" else "Tap to connect (free API)",
                            color = if (uiState.geminiConnected) AccentSuccess else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }
                    Icon(
                        imageVector = if (uiState.geminiConnected) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (uiState.geminiConnected) AccentSuccess else TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Section
        SettingsSection(title = "Preferences") {
            // Hexagon Style Selector
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Hexagon Style",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Choose the visual style for your stats hexagon",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HexagonStyleOption(
                            title = "Simple",
                            description = "Clean overlay",
                            selected = uiState.hexagonStyle == "simple",
                            onClick = { viewModel.updateHexagonStyle("simple") },
                            modifier = Modifier.weight(1f)
                        )
                        HexagonStyleOption(
                            title = "Glow",
                            description = "RPG aura",
                            selected = uiState.hexagonStyle == "glow",
                            onClick = { viewModel.updateHexagonStyle("glow") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Quote source. OFFLINE (bundled pack, zero network) is the default to
            // keep the offline-first stance; online sources are an explicit opt-in here.
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Daily Quote",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Where your quote of the day comes from",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HexagonStyleOption(
                            title = "Offline",
                            description = "Bundled pack",
                            selected = uiState.quoteSource == "OFFLINE",
                            onClick = { viewModel.updateQuoteSource("OFFLINE") },
                            modifier = Modifier.weight(1f)
                        )
                        HexagonStyleOption(
                            title = "Anime",
                            description = "Animechan",
                            selected = uiState.quoteSource == "ANIME",
                            onClick = { viewModel.updateQuoteSource("ANIME") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HexagonStyleOption(
                            title = "Motivation",
                            description = "ZenQuotes",
                            selected = uiState.quoteSource == "MOTIVATION",
                            onClick = { viewModel.updateQuoteSource("MOTIVATION") },
                            modifier = Modifier.weight(1f)
                        )
                        HexagonStyleOption(
                            title = "Mixed",
                            description = "Alternate daily",
                            selected = uiState.quoteSource == "MIXED",
                            onClick = { viewModel.updateQuoteSource("MIXED") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggle(
                title = "Decay Animations",
                description = "Show visual effects when stats decay",
                checked = uiState.showDecayAnimations,
                onCheckedChange = { viewModel.updateShowDecayAnimations(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggle(
                title = "Haptic Feedback",
                description = "Vibrate on actions",
                checked = uiState.hapticFeedback,
                onCheckedChange = { viewModel.updateHapticFeedback(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SettingsSection(title = "About") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Stat Up",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "v${com.rewardpoints.app.BuildConfig.VERSION_NAME}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Danger Zone
        SettingsSection(title = "Danger Zone") {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showResetDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗑️",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Full Reset",
                            color = AccentError,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Inter
                        )
                        Text(
                            text = "Delete all data and start fresh",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Todoist Connection Dialog
    if (showTodoistDialog) {
        TodoistConnectionDialog(
            viewModel = viewModel,
            isConnected = uiState.todoistConnected,
            onDismiss = { showTodoistDialog = false },
            onDisconnect = {
                viewModel.setTodoistToken(null)
                showTodoistDialog = false
            },
            onConnected = {
                showTodoistDialog = false
            }
        )
    }

    // Gemini Connection Dialog
    if (showGeminiDialog) {
        GeminiConnectionDialog(
            isConnected = uiState.geminiConnected,
            onDismiss = { showGeminiDialog = false },
            onSave = { key ->
                viewModel.setGeminiApiKey(key)
                showGeminiDialog = false
            },
            onDisconnect = {
                viewModel.setGeminiApiKey(null)
                showGeminiDialog = false
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                viewModel.fullReset()
                showResetDialog = false
            }
        )
    }
}

@Composable
private fun TodoistConnectionDialog(
    viewModel: SettingsViewModel,
    isConnected: Boolean,
    onDismiss: () -> Unit,
    onDisconnect: () -> Unit,
    onConnected: () -> Unit
) {
    var tokenInput by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📋", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Todoist Integration",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                            Text(
                                text = if (isConnected) "Connected" else "Not connected",
                                color = if (isConnected) AccentSuccess else TextSecondary,
                                fontSize = 14.sp,
                                fontFamily = Inter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isConnected) {
                        Text(
                            text = "Your Todoist account is connected. Completed tasks will automatically earn you points!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = Inter
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                text = "Cancel",
                                onClick = onDismiss,
                                primary = false,
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "Disconnect",
                                onClick = onDisconnect,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "Connect your Todoist account to earn points for completing tasks.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = Inter
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Get your API token from: Settings → Integrations → Developer in Todoist",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Token input with visibility toggle
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { 
                                tokenInput = it
                                errorMessage = null  // Clear error on input change
                            },
                            label = { Text("API Token", color = TextSecondary) },
                            placeholder = { Text("Paste your Todoist API token", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = errorMessage != null,
                            enabled = !isValidating,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = AccentPrimary,
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = GlassBorder,
                                errorBorderColor = AccentError
                            ),
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.getText()?.let { 
                                                tokenInput = it.text
                                                errorMessage = null
                                            }
                                        },
                                        enabled = !isValidating
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = TextSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { showToken = !showToken }
                                    ) {
                                        Icon(
                                            imageVector = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showToken) "Hide" else "Show",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true
                        )

                        // Error message
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = AccentError,
                                fontSize = 12.sp,
                                fontFamily = Inter
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                text = "Cancel",
                                onClick = onDismiss,
                                primary = false,
                                modifier = Modifier.weight(1f),
                                enabled = !isValidating
                            )
                            GlassButton(
                                text = if (isValidating) "Validating" else "Connect",
                                onClick = {
                                    if (tokenInput.isBlank()) {
                                        errorMessage = "Please enter your API token"
                                        return@GlassButton
                                    }
                                    
                                    isValidating = true
                                    errorMessage = null
                                    
                                    scope.launch {
                                        val result = viewModel.validateAndConnectTodoist(tokenInput)
                                        isValidating = false
                                        
                                        result.fold(
                                            onSuccess = {
                                                onConnected()
                                            },
                                            onFailure = { error ->
                                                errorMessage = error.message ?: "Connection failed"
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isValidating
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiConnectionDialog(
    isConnected: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDisconnect: () -> Unit
) {
    var keyInput by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🤖", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Agent",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter
                            )
                            Text(
                                text = if (isConnected) "Connected to Gemini" else "Not connected",
                                color = if (isConnected) AccentSuccess else TextSecondary,
                                fontSize = 14.sp,
                                fontFamily = Inter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isConnected) {
                        Text(
                            text = "Your Gemini API key is stored encrypted on this device. Disconnect to remove it.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = Inter
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                text = "Cancel",
                                onClick = onDismiss,
                                primary = false,
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "Disconnect",
                                onClick = onDisconnect,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "Powered by Google Gemini. Free tier: 10 requests/min, 500 requests/day.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = Inter
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Get a free key at aistudio.google.com/apikey, then paste it below.",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("API Key", color = TextSecondary) },
                            placeholder = { Text("Paste your Gemini API key", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = AccentPrimary,
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = {
                                        clipboardManager.getText()?.let { keyInput = it.text }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = TextSecondary
                                        )
                                    }
                                    IconButton(onClick = { showKey = !showKey }) {
                                        Icon(
                                            imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showKey) "Hide" else "Show",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                text = "Cancel",
                                onClick = onDismiss,
                                primary = false,
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "Save",
                                onClick = { if (keyInput.isNotBlank()) onSave(keyInput) },
                                enabled = keyInput.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HexagonStyleOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (selected) AccentPrimary else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = Inter
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Inter,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Inter
                )
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = Inter
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPrimary,
                    checkedTrackColor = AccentPrimary.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = TextTertiary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ Full Reset",
                        color = AccentError,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "This will permanently delete:",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "• All your stats (STR, INT, WIS, DEX, CHA, VIT)",
                        "• Your rank and streak progress",
                        "• All transactions history",
                        "• All rewards and missions",
                        "• Todoist connection"
                    ).forEach { item ->
                        Text(
                            text = item,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "This action cannot be undone!",
                        color = AccentError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            primary = false,
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Reset All",
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
