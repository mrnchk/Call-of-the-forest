package com.cotf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cotf.ui.theme.Parchment

/**
 * Полупрозрачный overlay паузы поверх игры.
 */
@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onExitToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PAUSED",
                style = MaterialTheme.typography.titleLarge,
                color = Parchment
            )

            Spacer(modifier = Modifier.height(32.dp))

            ForestButton(
                text = "Resume",
                onClick = onResume
            )

            Spacer(modifier = Modifier.height(16.dp))

            ForestButton(
                text = "Exit to Menu",
                onClick = onExitToMenu
            )
        }
    }
}
