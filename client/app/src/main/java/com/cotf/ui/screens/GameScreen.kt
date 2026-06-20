package com.cotf.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Spoke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cotf.CotfApp
import com.cotf.core.engine.ResourceType
import com.cotf.render.GameRenderer
import com.cotf.ui.VirtualJoystick
import com.cotf.ui.components.GameOverOverlay
import com.cotf.ui.components.HpBar
import com.cotf.ui.components.HungerBar
import com.cotf.ui.components.PauseOverlay
import com.cotf.ui.components.QuickInventoryBar
import com.cotf.ui.theme.DarkSurface
import com.cotf.ui.theme.Parchment
import com.cotf.viewmodel.GameViewModel

@Composable
fun GameScreen(
    onExitToMenu: () -> Unit
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as CotfApp }
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(app.leaderboardApi, app.userSession)
    )

    val state by viewModel.engine.state.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.startGame() }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) viewModel.submitResultIfNeeded(state.stats)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.pauseGame() }
    }

    BackHandler(enabled = true) {
        if (isPaused) { isPaused = false; viewModel.resumeGame() }
        else { isPaused = true; viewModel.pauseGame() }
    }

    Box(modifier = Modifier) {
        GameRenderer(engine = viewModel.engine)

        if (!isPaused && !state.isGameOver) {
            // HUD — верхний левый угол
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                val context = LocalContext.current
                val userSession = remember { (context.applicationContext as CotfApp).userSession }
                val username = userSession.getUsername()
                if (username != null) {
                    Text(username, color = Parchment.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                HpBar(currentHp = state.player.hp, maxHp = state.player.maxHp)
                Spacer(modifier = Modifier.height(6.dp))
                HungerBar(hunger = state.player.hunger)
            }

            // Инвентарь — нижний центр
            QuickInventoryBar(
                inventory = state.player.inventory,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )

            // Джойстик — нижний левый
            VirtualJoystick(
                engine = viewModel.engine,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(32.dp)
            )

            // Кнопки действий — нижний правый
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Съесть ягоду (только если есть ягоды)
                val berryCount = state.player.inventory.getOrDefault(ResourceType.BERRY, 0)
                if (berryCount > 0) {
                    ActionButton(
                        onClick = { viewModel.requestConsumeBerry() },
                        icon = { Icon(Icons.Default.LocalFlorist, "Eat Berry", tint = Parchment, modifier = Modifier.size(24.dp)) },
                        label = "🫐$berryCount"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Собрать ресурс
                ActionButton(
                    onClick = { viewModel.requestHarvest() },
                    icon = { Icon(Icons.Default.Spoke, "Harvest", tint = Parchment, modifier = Modifier.size(24.dp)) },
                    label = "Gather"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Атака
                ActionButton(
                    onClick = { viewModel.requestAttack() },
                    icon = { Icon(Icons.Default.ContentCut, "Attack", tint = Parchment, modifier = Modifier.size(24.dp)) },
                    label = "Attack"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Пауза
                IconButton(
                    onClick = { isPaused = true; viewModel.pauseGame() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.6f))
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Pause, "Pause", tint = Parchment, modifier = Modifier.size(28.dp))
                }
            }
        }

        if (isPaused) {
            PauseOverlay(
                onResume = { isPaused = false; viewModel.resumeGame() },
                onExitToMenu = { viewModel.exitGame(); isPaused = false; onExitToMenu() }
            )
        }

        if (state.isGameOver) {
            GameOverOverlay(
                stats = state.stats,
                submitState = submitState,
                onRetrySubmit = { viewModel.retrySubmit(state.stats) },
                onExitToMenu = { viewModel.exitGame(); onExitToMenu() }
            )
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(DarkSurface.copy(alpha = 0.6f))
            .size(52.dp)
    ) {
        icon()
    }
}
