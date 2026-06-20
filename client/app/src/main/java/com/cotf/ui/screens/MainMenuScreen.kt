package com.cotf.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cotf.CotfApp
import com.cotf.navigation.Routes
import com.cotf.ui.components.ForestButton
import com.cotf.ui.theme.DarkSurface
import com.cotf.ui.theme.ForestGreen
import com.cotf.ui.theme.Parchment
import com.cotf.ui.theme.ParchmentDim

@Composable
fun MainMenuScreen(navController: NavController) {
    val context = LocalContext.current
    val userSession = remember { (context.applicationContext as CotfApp).userSession }
    val username by remember { mutableStateOf(userSession.getUsername()) }
    val isLoggedIn = remember { userSession.isLoggedIn() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        MenuBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок
            Text(
                text = "Call of\nthe Forest",
                style = MaterialTheme.typography.titleLarge,
                color = Parchment,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Survive. Explore. Conquer.",
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentDim
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoggedIn && username != null) {
                // Залогинен — меню с Play
                Text(
                    text = username!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestGreen
                )

                Spacer(modifier = Modifier.height(24.dp))

                ForestButton(
                    text = "Play",
                    onClick = { navController.navigate(Routes.GAME) },
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ForestButton(
                    text = "Leaderboard",
                    onClick = { navController.navigate(Routes.LEADERBOARD) },
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ForestButton(
                    text = "Logout",
                    onClick = {
                        userSession.logout()
                        navController.navigate(Routes.MENU) {
                            popUpTo(Routes.MENU) { inclusive = true }
                        }
                    },
                    modifier = Modifier.width(200.dp)
                )
            } else {
                // Не залогинен — Login + Leaderboard (как витрина)
                ForestButton(
                    text = "Login",
                    onClick = { navController.navigate(Routes.LOGIN) },
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ForestButton(
                    text = "Leaderboard",
                    onClick = { navController.navigate(Routes.LEADERBOARD) },
                    modifier = Modifier.width(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ForestButton(
                text = "Exit",
                onClick = {
                    (context as? androidx.activity.ComponentActivity)?.finish()
                },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
private fun MenuBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawTreeSilhouette(Offset(size.width * 0.1f, size.height * 0.85f), 80f)
        drawTreeSilhouette(Offset(size.width * 0.25f, size.height * 0.9f), 60f)
        drawTreeSilhouette(Offset(size.width * 0.75f, size.height * 0.85f), 70f)
        drawTreeSilhouette(Offset(size.width * 0.9f, size.height * 0.88f), 55f)
    }
}

private fun DrawScope.drawTreeSilhouette(center: Offset, height: Float) {
    drawRect(
        color = Color(0xFF1A1A0E),
        topLeft = Offset(center.x - 4f, center.y - height * 0.3f),
        size = androidx.compose.ui.geometry.Size(8f, height * 0.3f)
    )
    for (i in 0..1) {
        val yOff = center.y - height * (0.3f + i * 0.3f)
        val halfW = height * (0.25f - i * 0.05f)
        drawTriangle(
            color = Color(0xFF0D1F0D),
            peak = Offset(center.x, yOff - height * 0.25f),
            bottomLeft = Offset(center.x - halfW, yOff),
            bottomRight = Offset(center.x + halfW, yOff)
        )
    }
}

private fun DrawScope.drawTriangle(
    color: Color,
    peak: Offset,
    bottomLeft: Offset,
    bottomRight: Offset
) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(peak.x, peak.y)
        lineTo(bottomLeft.x, bottomLeft.y)
        lineTo(bottomRight.x, bottomRight.y)
        close()
    }
    drawPath(path, color)
}
