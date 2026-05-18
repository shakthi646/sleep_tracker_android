package com.ksp.sleeptracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.data.model.UserProfile
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileHeader(
    profile: UserProfile?,
    nightsTracked: Int,
    avgScore: Int
) {
    val name = profile?.name?.takeIf { it.isNotBlank() } ?: "Sleeper"
    val initial = name.firstOrNull()?.uppercase() ?: "S"
    val memberSince = profile?.memberSince?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initial,
                    color = NightNavy,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineMedium
                )
                memberSince?.let {
                    Text(
                        "Member since $it",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(value = nightsTracked.toString(), label = "Nights tracked", modifier = Modifier.weight(1f))
            StatTile(value = avgScore.toString(), label = "Avg score", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(value, color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}
