package com.ksp.sleeptracker.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.components.MoonCrescent
import com.ksp.sleeptracker.ui.components.StarfieldBackground
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        StarfieldBackground(modifier = Modifier.fillMaxSize(), starCount = 60)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            MoonCrescent(size = 112.dp)
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "Your sleep, understood.",
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sleep Tracker learns your rhythm and helps you rest, wake, and feel like yourself again.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(40.dp))
            BulletRow(Icons.Outlined.NightsStay, "Track sleep stages automatically")
            Spacer(modifier = Modifier.height(16.dp))
            BulletRow(Icons.Outlined.NotificationsActive, "Wake up at the perfect moment")
            Spacer(modifier = Modifier.height(16.dp))
            BulletRow(Icons.Outlined.BarChart, "See patterns that shape your energy")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo,
                    contentColor = TextPrimary
                )
            ) { Text("Get started", style = MaterialTheme.typography.titleMedium) }

            TextButton(onClick = {}) {
                Text(
                    text = "I already have an account",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun BulletRow(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MidnightIndigo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentIndigo)
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = label, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
    }
}
