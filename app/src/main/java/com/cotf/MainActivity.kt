package com.cotf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotf.core.engine.GameEngine
import com.cotf.render.GameRenderer
import com.cotf.ui.VirtualJoystick

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val engine = remember { GameEngine() }

            Box(modifier = Modifier.fillMaxSize()) {
                // Рендер мира (на весь экран)
                GameRenderer(engine = engine)

                // Джойстик (внизу слева)
                VirtualJoystick(
                    engine = engine,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp)
                )
            }
        }
    }
}
