package com.cotf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cotf.core.engine.GameStats
import com.cotf.core.engine.ScoreCalculator
import com.cotf.ui.theme.ForestGreen
import com.cotf.ui.theme.Parchment
import com.cotf.ui.theme.ParchmentDim
import com.cotf.viewmodel.SubmitState

@Composable
fun GameOverOverlay(
    stats: GameStats,
    submitState: SubmitState,
    onRetrySubmit: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val score = ScoreCalculator.calculate(stats)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "YOU DIED",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFCF6679)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Final score",
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentDim
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(20.dp))

            StatRow("Survived", "${stats.survivedSeconds.toInt()}s")
            StatRow("Days survived", "${stats.daysSurvived}")
            StatRow("Mobs killed", "${stats.mobsKilled}")
            StatRow("Resources gathered", "${stats.resourcesGathered}")
            Spacer(modifier = Modifier.height(20.dp))

            SubmitStatusRow(submitState)
            Spacer(modifier = Modifier.height(20.dp))

            if (submitState is SubmitState.Error) {
                ForestButton(text = "Retry submit", onClick = onRetrySubmit)
                Spacer(modifier = Modifier.height(12.dp))
            }
            ForestButton(text = "Return to Menu", onClick = onExitToMenu)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.width(240.dp).padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ParchmentDim, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = Parchment, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SubmitStatusRow(state: SubmitState) {
    when (state) {
        SubmitState.Idle, SubmitState.Submitting -> Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = ForestGreen,
                modifier = Modifier.width(18.dp).height(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submitting…", color = ParchmentDim, style = MaterialTheme.typography.bodyMedium)
        }
        is SubmitState.Success -> Text(
            "Submitted ✓",
            color = ForestGreen,
            style = MaterialTheme.typography.bodyMedium
        )
        SubmitState.Skipped -> Text(
            "Login to submit your runs",
            color = ParchmentDim,
            style = MaterialTheme.typography.bodyMedium
        )
        is SubmitState.Error -> Text(
            "Submit failed: ${state.message}",
            color = Color(0xFFCF6679),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
