package com.cotf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotf.core.engine.ResourceType
import com.cotf.ui.theme.EarthyBrown
import com.cotf.ui.theme.ForestGreen
import com.cotf.ui.theme.OliveGreen
import com.cotf.ui.theme.Parchment
import com.cotf.ui.theme.WarmAmber

@Composable
fun HpBar(currentHp: Int, maxHp: Int) {
    val fraction = (currentHp.coerceAtLeast(0).toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
    val barColor = when {
        fraction > 0.5f -> ForestGreen
        fraction > 0.25f -> WarmAmber
        else -> Color(0xFFCF6679)
    }

    Column {
        Text("HP", color = Parchment, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1A1A1A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun HungerBar(hunger: Float) {
    val fraction = (hunger / 100f).coerceIn(0f, 1f)
    val barColor = when {
        fraction > 0.4f -> WarmAmber
        else -> Color(0xFFCF6679)
    }

    Column {
        Text("Hunger", color = Parchment, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1A1A1A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun QuickInventoryBar(inventory: Map<ResourceType, Int>, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x80000000))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        InventorySlot("🪵", inventory.getOrDefault(ResourceType.WOOD, 0), EarthyBrown)
        Spacer(modifier = Modifier.width(8.dp))
        InventorySlot("🪨", inventory.getOrDefault(ResourceType.STONE, 0), Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        InventorySlot("🫐", inventory.getOrDefault(ResourceType.BERRY, 0), OliveGreen)
        Spacer(modifier = Modifier.width(8.dp))
        InventorySlot("🥩", inventory.getOrDefault(ResourceType.MEAT, 0), Color(0xFFB71C1C))
    }
}

@Composable
private fun InventorySlot(icon: String, count: Int, bgColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor.copy(alpha = 0.3f))
            .border(1.dp, bgColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 14.sp)
        Text(
            "$count",
            color = Parchment,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
