package com.cotf.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handyman
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cotf.CotfApp
import com.cotf.R
import com.cotf.core.engine.ResourceType
import com.cotf.render.AnimClip
import com.cotf.render.CharSpriteCache
import com.cotf.render.CharSpritesConfig
import com.cotf.render.GameRenderer
import com.cotf.render.rememberCharSpriteCache
import com.cotf.ui.VirtualJoystick
import com.cotf.ui.components.GameOverOverlay
import com.cotf.ui.components.HpBar
import com.cotf.ui.components.HungerBar
import com.cotf.ui.components.PauseOverlay
import com.cotf.ui.components.QuickInventoryBar
import com.cotf.ui.theme.DarkSurface
import com.cotf.ui.theme.Parchment
import com.cotf.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private fun spriteConfigs(): Map<String, CharSpritesConfig> = mapOf(
     "player" to CharSpritesConfig(
         drawableResId = R.drawable.player,
         frameWidth    = 48,
         frameHeight   = 48,
         clips = mapOf(
              // ── idle ──────────────────
              "idle_down" to AnimClip(row = 0, frameCount = 1, fps = 1f),
              "idle_up"   to AnimClip(row = 1, frameCount = 1, fps = 1f),
              "idle_side" to AnimClip(row = 2, frameCount = 1, fps = 1f),
              // ── walk ─────────────────────────────────────
              "walk_down" to AnimClip(row = 3, frameCount = 6, fps = 8f),
              "walk_up"   to AnimClip(row = 4, frameCount = 6, fps = 8f),
              "walk_side" to AnimClip(row = 5, frameCount = 6, fps = 8f),
              // ── attack ───────────────────────────────────
              "attack_down" to AnimClip(row = 6, frameCount = 4, fps = 10f),
              "attack_up"   to AnimClip(row = 7, frameCount = 4, fps = 10f),
              "attack_side" to AnimClip(row = 8, frameCount = 4, fps = 10f),
              // ── death ────────────────────────────────────
              "death"       to AnimClip(row = 9, frameCount = 6, fps = 8f),
          )
     ),

     "wolf" to CharSpritesConfig(
         drawableResId = R.drawable.wolf,
         frameWidth    = 32,
         frameHeight   = 32,
         clips = mapOf(
             "idle"   to AnimClip(row = 5, frameCount = 7, fps = 4f),
             "patrol" to AnimClip(row = 0, frameCount = 6, fps = 6f),
             "run"    to AnimClip(row = 1, frameCount = 6, fps = 10f),
             "attack" to AnimClip(row = 3, frameCount = 5, fps = 12f),
             "death"  to AnimClip(row = 7, frameCount = 4, fps = 8f),
         )
     ),

     "bear" to CharSpritesConfig(
         drawableResId = R.drawable.bear,
         frameWidth    = 32,
         frameHeight   = 32,
         clips = mapOf(
             "idle"   to AnimClip(row = 5, frameCount = 10, fps = 3f),
             "patrol" to AnimClip(row = 0, frameCount = 4, fps = 5f),
             "run"    to AnimClip(row = 1, frameCount = 6, fps = 8f),
             "attack" to AnimClip(row = 3, frameCount = 6, fps = 10f),
             "death"  to AnimClip(row = 6, frameCount = 4, fps = 8f),
         )
     ),

     "fox" to CharSpritesConfig(
         drawableResId = R.drawable.fox,
         frameWidth    = 32,
         frameHeight   = 32,
         clips = mapOf(
             "idle"   to AnimClip(row = 0, frameCount = 3, fps = 4f),
             "patrol" to AnimClip(row = 1, frameCount = 4, fps = 6f),
             "run"    to AnimClip(row = 1, frameCount = 4, fps = 12f),
             "attack" to AnimClip(row = 2, frameCount = 4, fps = 12f),
             "death"  to AnimClip(row = 3, frameCount = 4, fps = 8f),
         )
     ),

     "deer" to CharSpritesConfig(
         drawableResId = R.drawable.deer,
         frameWidth    = 32,
         frameHeight   = 32,
         clips = mapOf(
             "idle"   to AnimClip(row = 0, frameCount = 4, fps = 4f),
             "patrol" to AnimClip(row = 1, frameCount = 4, fps = 5f),
             "run"    to AnimClip(row = 1, frameCount = 4, fps = 10f),
             "death"  to AnimClip(row = 2, frameCount = 4, fps = 8f),
         )
     ),

     "rabbit" to CharSpritesConfig(
         drawableResId = R.drawable.rabbit,
         frameWidth    = 32,
         frameHeight   = 32,
         clips = mapOf(
             "idle"   to AnimClip(row = 0, frameCount = 2, fps = 3f),
             "patrol" to AnimClip(row = 1, frameCount = 4, fps = 8f),
             "run"    to AnimClip(row = 1, frameCount = 4, fps = 14f),
             "death"  to AnimClip(row = 3, frameCount = 4, fps = 8f),
         )
     ),

     "bird" to CharSpritesConfig(
         drawableResId = R.drawable.bird,
         frameWidth    = 16,
         frameHeight   = 16,
         clips = mapOf(
             "idle"   to AnimClip(row = 0, frameCount = 4, fps = 6f),
             "patrol" to AnimClip(row = 1, frameCount = 4, fps = 10f),
             "run"    to AnimClip(row = 1, frameCount = 4, fps = 10f),
             "death"  to AnimClip(row = 2, frameCount = 4, fps = 8f),
         )
     ),

     // Berries — single image 980×980
     "berry" to CharSpritesConfig(
         drawableResId = R.drawable.berry,
         frameWidth    = 980,
         frameHeight   = 980,
         clips = mapOf(
             "idle" to AnimClip(row = 0, frameCount = 1, fps = 1f),
         )
     ),

     // bush.png — 360×360
     "bush" to CharSpritesConfig(
         drawableResId = R.drawable.bush,
         frameWidth    = 360,
         frameHeight   = 360,
         clips = mapOf(
             "idle" to AnimClip(row = 0, frameCount = 1, fps = 1f),
         )
     ),

    // bush.png — 360×360, single image → replaces rock
    "rock" to CharSpritesConfig(
        drawableResId = R.drawable.rock,
        frameWidth    = 360,
        frameHeight   = 360,
        clips = mapOf(
            "idle" to AnimClip(row = 0, frameCount = 1, fps = 1f),
        )
    ),

     // tree1.png — 1200×1200
     "tree1" to CharSpritesConfig(
         drawableResId = R.drawable.tree1,
         frameWidth    = 1200,
         frameHeight   = 1200,
         clips = mapOf(
             "idle" to AnimClip(row = 0, frameCount = 1, fps = 1f),
         )
     ),

     // tree2.png — 880×1232
     "tree2" to CharSpritesConfig(
         drawableResId = R.drawable.tree2,
         frameWidth    = 880,
         frameHeight   = 1232,
         clips = mapOf(
             "idle" to AnimClip(row = 0, frameCount = 1, fps = 1f),
         )
     ),
)

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

    // Animation time counter (seconds)
    var gameTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L.milliseconds)       // ~60 fps
            gameTime += 0.016f
        }
    }

    // Load sprites once at startup (fallback circles if not loaded)
    val sprites: CharSpriteCache = rememberCharSpriteCache(spriteConfigs())

    LaunchedEffect(Unit) { viewModel.startGame() }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) viewModel.submitResultIfNeeded(state.stats)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.pauseGame() }
    }

    BackHandler(enabled = true) {
        if (isPaused) { isPaused = false; viewModel.resumeGame() }
        else          { isPaused = true;  viewModel.pauseGame()  }
    }

    Box(modifier = Modifier) {
        GameRenderer(
            engine   = viewModel.engine,
            sprites  = sprites,
            gameTime = gameTime
        )

        if (!isPaused && !state.isGameOver) {
            // HUD — top-left corner
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

            // Day/night clock — top-right corner
            DayNightClock(
                timeOfDay = state.timeOfDay,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
            )

            // Inventory bar — bottom center
            QuickInventoryBar(
                inventory = state.player.inventory,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )

            // Joystick — bottom-left (slightly raised)
            VirtualJoystick(
                engine = viewModel.engine,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 40.dp)
            )

            // Action buttons — bottom-right
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val berryCount = state.player.inventory.getOrDefault(ResourceType.BERRY, 0)
                if (berryCount > 0) {
                    ActionButton(
                        onClick = { viewModel.requestConsumeBerry() },
                        icon = { Icon(painterResource(R.drawable.ic_eat), "Eat", tint = Parchment, modifier = Modifier.size(24.dp)) },
                        label = "🫐$berryCount"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ActionButton(
                    onClick = { viewModel.requestHarvest() },
                    icon = { Icon(painterResource(R.drawable.ic_take), "Harvest", tint = Parchment, modifier = Modifier.size(24.dp)) },
                    label = "Gather"
                )
                Spacer(modifier = Modifier.height(8.dp))

                ActionButton(
                    onClick = { viewModel.requestAttack() },
                    icon = { Icon(painterResource(R.drawable.ic_sword), "Attack", tint = Parchment, modifier = Modifier.size(24.dp)) },
                    label = "Attack"
                )
                Spacer(modifier = Modifier.height(8.dp))

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

// ── Day/night clock widget ─────────────────────────────────────────────────

/**
 * A small HUD clock that shows a half-arc sky with a moving sun or moon.
 *
 * [timeOfDay] 0 = midnight, 0.5 = noon (same as GameState.timeOfDay).
 *
 * Arc coordinate system:
 *   - Left end of arc  = east horizon (sunrise, timeOfDay ≈ 0.23)
 *   - Arc apex (top)   = noon          (timeOfDay = 0.50)
 *   - Right end of arc = west horizon  (sunset,  timeOfDay ≈ 0.77)
 *   - Below the arc    = night (sun hidden; moon shown on mirrored arc)
 */
@Composable
private fun DayNightClock(
    timeOfDay: Float,
    modifier: Modifier = Modifier
) {
    val isNight = timeOfDay < 0.20f || timeOfDay > 0.80f
    val isDawn  = timeOfDay in 0.20f..0.28f
    val isDusk  = timeOfDay in 0.72f..0.80f

    val label = when {
        isNight -> "🌙 Night"
        isDawn  -> "🌅 Dawn"
        isDusk  -> "🌇 Dusk"
        else    -> "☀️ Day"
    }

    // Fraction along the daytime arc [0=sunrise…1=sunset]
    val dayFrac = ((timeOfDay - 0.23f) / 0.54f).coerceIn(0f, 1f)
    // Night arc runs from sunset→midnight→sunrise (wrapped)
    val nightFrac: Float = when {
        timeOfDay > 0.77f -> (timeOfDay - 0.77f) / 0.46f
        else              -> (timeOfDay + 0.23f) / 0.46f   // 0.00–0.23
    }.coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arc canvas — 80×40 dp
        Canvas(modifier = Modifier.size(width = 80.dp, height = 40.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h            // center of the full circle at bottom of canvas
            val r  = w / 2f - 4f

            // Track arc (semicircle)
            drawArc(
                color     = Color.White.copy(alpha = 0.15f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter  = false,
                topLeft    = Offset(cx - r, cy - r),
                size       = Size(r * 2, r * 2),
                style      = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // Sun position: arc from 180° (left/sunrise) to 0° (right/sunset)
            // Compose arc: 0° = right, angles go clockwise
            // Our semantic: dayFrac=0 → left (180°), dayFrac=1 → right (0°/360°)
            val sunAngleRad = (PI - dayFrac * PI).toFloat()   // radians, 0° = right
            val sunX = cx + r * cos(sunAngleRad)
            val sunY = cy - r * sin(sunAngleRad)              // canvas Y is inverted

            // Moon position: same arc but offset by half (opposite side)
            val moonAngleRad = (PI - nightFrac * PI).toFloat()
            val moonX = cx + r * cos(moonAngleRad)
            val moonY = cy - r * sin(moonAngleRad)

            if (!isNight) {
                // Sun glow
                drawCircle(Color(1f, 0.9f, 0.3f, 0.25f), radius = 9f, center = Offset(sunX, sunY))
                drawCircle(Color(1f, 0.95f, 0.5f, 1f),   radius = 5f, center = Offset(sunX, sunY))
            } else {
                // Moon
                drawCircle(Color(0.85f, 0.90f, 1f, 0.20f), radius = 7f,  center = Offset(moonX, moonY))
                drawCircle(Color(0.90f, 0.93f, 1f, 1f),    radius = 4.5f, center = Offset(moonX, moonY))
                // Crescent shadow
                drawCircle(DarkSurface.copy(alpha = 0.9f), radius = 3.2f,
                    center = Offset(moonX - 1.8f, moonY))
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text  = label,
            color = Parchment.copy(alpha = 0.85f),
            fontSize = 10.sp
        )
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
