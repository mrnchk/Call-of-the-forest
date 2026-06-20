package com.cotf.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cotf.CotfApp
import com.cotf.navigation.Routes
import com.cotf.network.dto.GameResultDto
import com.cotf.network.dto.LeaderboardEntryDto
import com.cotf.network.dto.MyLeaderboardDto
import com.cotf.ui.components.ForestButton
import com.cotf.ui.theme.DarkSurface
import com.cotf.ui.theme.ForestGreen
import com.cotf.ui.theme.Parchment
import com.cotf.ui.theme.ParchmentDim
import com.cotf.viewmodel.LeaderboardViewModel
import com.cotf.viewmodel.MeState
import com.cotf.viewmodel.TopState

@Composable
fun LeaderboardScreen(navController: NavController) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as CotfApp }
    val viewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModel.Factory(app.leaderboardApi, app.userSession)
    )

    val topState by viewModel.topState.collectAsState()
    val meState by viewModel.meState.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.titleLarge,
                color = Parchment,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TabRow(selectedTabIndex = selectedTab, containerColor = DarkSurface) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; viewModel.loadTop() },
                    text = { Text("Top", color = if (selectedTab == 0) ForestGreen else ParchmentDim) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; viewModel.loadMe() },
                    text = { Text("Me", color = if (selectedTab == 1) ForestGreen else ParchmentDim) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> TopTab(topState)
                    else -> MeTab(meState, onLogin = { navController.navigate(Routes.LOGIN) })
                }
            }

            ForestButton(
                text = "Back",
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun TopTab(state: TopState) {
    when (state) {
        TopState.Loading -> CenteredLoading()
        is TopState.Error -> CenteredMessage("Couldn't load top: ${state.message}")
        is TopState.Success -> if (state.entries.isEmpty()) {
            CenteredMessage("No results yet. Be the first!")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.entries, key = { "${it.rank}-${it.username}-${it.createdAt}" }) { entry ->
                    LeaderboardRow(entry)
                }
            }
        }
    }
}

@Composable
private fun MeTab(state: MeState, onLogin: () -> Unit) {
    when (state) {
        MeState.Loading -> CenteredLoading()
        MeState.Anonymous -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Login to submit your runs and track your rank.",
                color = ParchmentDim,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            ForestButton(text = "Login", onClick = onLogin, modifier = Modifier.width(200.dp))
        }
        is MeState.Error -> CenteredMessage("Couldn't load: ${state.message}")
        is MeState.Success -> MeContent(state.payload)
    }
}

@Composable
private fun MeContent(payload: MyLeaderboardDto) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            BestCard(best = payload.best, rank = payload.rank)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Recent runs",
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentDim,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        if (payload.recent.isEmpty()) {
            item {
                Text(
                    "Nothing submitted yet.",
                    color = ParchmentDim,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(payload.recent, key = { it.id }) { run -> RecentRunRow(run) }
        }
    }
}

@Composable
private fun BestCard(best: GameResultDto?, rank: Int?) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1F2A1F))
            .padding(12.dp)
    ) {
        if (best == null) {
            Text("No personal best yet.", color = ParchmentDim, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Personal best", color = ParchmentDim, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    if (rank != null) {
                        Text("Rank #$rank", color = ForestGreen, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${best.score} pts",
                    color = ForestGreen,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatSummary(
                    survivedSeconds = best.survivedSeconds,
                    mobsKilled = best.mobsKilled,
                    resourcesGathered = best.resourcesGathered,
                    daysSurvived = best.daysSurvived
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntryDto) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1F2A1F))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#${entry.rank}",
            color = ForestGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.username, color = Parchment, style = MaterialTheme.typography.bodyMedium)
            StatSummary(
                survivedSeconds = entry.survivedSeconds,
                mobsKilled = entry.mobsKilled,
                resourcesGathered = entry.resourcesGathered,
                daysSurvived = entry.daysSurvived
            )
        }
        Text(
            "${entry.score}",
            color = ForestGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecentRunRow(run: GameResultDto) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF182218))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${run.score} pts", color = Parchment, style = MaterialTheme.typography.bodyMedium)
            StatSummary(
                survivedSeconds = run.survivedSeconds,
                mobsKilled = run.mobsKilled,
                resourcesGathered = run.resourcesGathered,
                daysSurvived = run.daysSurvived
            )
        }
    }
}

@Composable
private fun StatSummary(
    survivedSeconds: Int,
    mobsKilled: Int,
    resourcesGathered: Int,
    daysSurvived: Int
) {
    Text(
        text = "⏱ ${survivedSeconds}s · 🗡 $mobsKilled · 🪵 $resourcesGathered · ☀ $daysSurvived",
        color = ParchmentDim,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun CenteredLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ForestGreen)
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = ParchmentDim, style = MaterialTheme.typography.bodyMedium)
    }
}
