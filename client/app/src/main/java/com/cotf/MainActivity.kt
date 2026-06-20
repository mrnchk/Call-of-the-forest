package com.cotf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cotf.navigation.Routes
import com.cotf.ui.screens.GameScreen
import com.cotf.ui.screens.LeaderboardScreen
import com.cotf.ui.screens.LoginScreen
import com.cotf.ui.screens.MainMenuScreen
import com.cotf.ui.theme.CallOfTheForestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallOfTheForestTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Routes.MENU
                    ) {
                        composable(Routes.MENU) {
                            MainMenuScreen(navController = navController)
                        }
                        composable(Routes.LOGIN) {
                            LoginScreen(navController = navController)
                        }
                        composable(Routes.GAME) {
                            GameScreen(
                                onExitToMenu = {
                                    navController.navigate(Routes.MENU) {
                                        popUpTo(Routes.MENU) { inclusive = false }
                                    }
                                }
                            )
                        }
                        composable(Routes.LEADERBOARD) {
                            LeaderboardScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
