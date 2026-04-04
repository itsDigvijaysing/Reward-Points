package com.rewardpoints.app.ui.screen.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.rewardpoints.app.domain.model.Reward
import com.rewardpoints.app.ui.components.glass.*
import com.rewardpoints.app.ui.navigation.Routes
import com.rewardpoints.app.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun RewardsScreen(
    navController: NavController,
    viewModel: RewardsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rewards Shop",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                GlassIconButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Reward",
                            tint = AccentPrimary
                        )
                    },
                    onClick = { viewModel.showCreateDialog() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Balance Header — tap to view history
            GlassCardWithHighlight(
                modifier = Modifier.fillMaxWidth(),
                elevated = true,
                onClick = { navController.navigate(Routes.HISTORY) }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Points",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✨ ${uiState.currentBalance}",
                        color = PointsGold,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = Inter
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            } else if (uiState.rewards.isEmpty()) {
                EmptyRewardsState(onCreateClick = { viewModel.showCreateDialog() })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(uiState.rewards, key = { it.id }) { reward ->
                        RewardCard(
                            reward = reward,
                            canAfford = uiState.currentBalance >= reward.pointsCost,
                            onRedeem = { viewModel.redeemReward(reward) },
                            onDelete = { viewModel.deleteReward(reward) }
                        )
                    }
                }
            }
        }

        // Create Dialog
        if (uiState.showCreateDialog) {
            CreateRewardDialog(
                onDismiss = { viewModel.hideCreateDialog() },
                onCreate = { name, desc, cost, emoji, category ->
                    viewModel.createReward(name, desc, cost, emoji, category)
                }
            )
        }

        // Success snackbar
        uiState.redeemSuccess?.let { rewardName ->
            LaunchedEffect(rewardName) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearRedeemSuccess()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = AccentSuccess.copy(alpha = 0.9f)
            ) {
                Text("🎉 Redeemed: $rewardName", color = BackgroundBase)
            }
        }
    }
}

@Composable
private fun EmptyRewardsState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎁", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Rewards Yet",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create rewards to motivate yourself!\nEarn points by completing tasks.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = Inter,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                GlassButton(
                    text = "+ Create Reward",
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RewardCard(
    reward: Reward,
    canAfford: Boolean,
    onRedeem: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PointsGold.copy(alpha = 0.2f),
                                PointsGold.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = reward.emoji ?: "🎁", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Inter
                )
                reward.description?.let {
                    Text(
                        text = it,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${reward.pointsCost} pts",
                    color = if (canAfford) PointsGold else AccentError,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                GlassButtonSmall(
                    text = "Redeem",
                    onClick = onRedeem,
                    enabled = canAfford,
                    primary = canAfford
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateRewardDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String?, cost: Int, emoji: String, category: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("10") }
    var selectedEmoji by remember { mutableStateOf("🎁") }

    val emojis = listOf("🎁", "🍕", "🎮", "☕", "🎬", "🛍️", "🍦", "📱", "🎧", "✈️", "💆", "🍫")
    val costs = listOf("5", "10", "20", "50", "100")

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
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Create Reward",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name
                    GlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Reward Name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    GlassTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Emoji selector - 2 rows of 6
                    Text(
                        text = "Icon:",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0 until 6) {
                                    val idx = row * 6 + col
                                    if (idx < emojis.size) {
                                        val emoji = emojis[idx]
                                        GlassIconButton(
                                            icon = { Text(emoji, fontSize = 20.sp) },
                                            onClick = { selectedEmoji = emoji },
                                            size = 40.dp,
                                            selected = selectedEmoji == emoji
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cost selector - 2 rows
                    Text(
                        text = "Cost (points):",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("5", "10", "20").forEach { c ->
                                GlassButtonSmall(
                                    text = c,
                                    onClick = { cost = c },
                                    primary = cost == c,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("50", "100").forEach { c ->
                                GlassButtonSmall(
                                    text = c,
                                    onClick = { cost = c },
                                    primary = cost == c,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Empty space to balance
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
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
                            text = "Create",
                            onClick = {
                                if (name.isNotBlank()) {
                                    onCreate(
                                        name,
                                        description.takeIf { it.isNotBlank() },
                                        cost.toIntOrNull() ?: 10,
                                        selectedEmoji,
                                        null
                                    )
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
