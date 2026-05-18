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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.AccentRose
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

private val ageRanges = listOf("Under 18", "18–25", "26–35", "36–50", "50+")
private val sexes = listOf("Female", "Male", "Non-binary", "Prefer not to say")

@Composable
fun ProfilePage(
    vm: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "What should we call you?",
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A few details help Sleep Tracker tailor your sleep insights.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::onNameChange,
                placeholder = { Text("First name", color = TextMuted) },
                isError = state.nameError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = MidnightIndigo,
                    focusedContainerColor = MidnightIndigo,
                    unfocusedContainerColor = MidnightIndigo,
                    cursorColor = AccentIndigo
                )
            )
            state.nameError?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, color = AccentRose, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Age range",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ageRanges) { label ->
                    FilterChip(
                        selected = state.ageRange == label,
                        onClick = { vm.onAgeRangeChange(label) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MidnightIndigo,
                            labelColor = TextMuted,
                            selectedContainerColor = AccentIndigo,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }
            state.ageError?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, color = AccentRose, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Biological sex (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sexes.take(2).forEach { label ->
                    FilterChip(
                        selected = state.biologicalSex == label,
                        onClick = { vm.onBiologicalSexChange(label) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MidnightIndigo,
                            labelColor = TextMuted,
                            selectedContainerColor = AccentIndigo,
                            selectedLabelColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sexes.drop(2).forEach { label ->
                    FilterChip(
                        selected = state.biologicalSex == label,
                        onClick = { vm.onBiologicalSexChange(label) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MidnightIndigo,
                            labelColor = TextMuted,
                            selectedContainerColor = AccentIndigo,
                            selectedLabelColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = { if (vm.validateProfileForAdvance()) onNext() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo,
                    contentColor = TextPrimary
                )
            ) { Text("Next", style = MaterialTheme.typography.titleMedium) }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Back", color = TextMuted)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

