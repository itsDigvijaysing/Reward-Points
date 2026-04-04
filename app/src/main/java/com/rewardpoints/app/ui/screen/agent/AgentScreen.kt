package com.rewardpoints.app.ui.screen.agent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rewardpoints.app.ui.components.glass.GlassCard
import com.rewardpoints.app.ui.theme.*

@Composable
fun AgentScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            elevated = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤖",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "AI Agent",
                    color = AccentPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Coming in Next Version",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontFamily = Inter
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Your personal AI assistant powered by Gemini will be here soon. It will help you stay motivated, track your progress, and provide personalized insights.",
                    color = TextTertiary,
                    fontSize = 14.sp,
                    fontFamily = Inter,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Stay tuned, Champion! 🏆",
                    color = PointsGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Inter
                )
            }
        }
    }
}
