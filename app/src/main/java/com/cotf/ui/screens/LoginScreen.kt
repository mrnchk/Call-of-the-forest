package com.cotf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cotf.CotfApp
import com.cotf.ui.components.ForestButton
import com.cotf.ui.theme.DarkSurface
import com.cotf.ui.theme.ForestGreen
import com.cotf.ui.theme.Parchment
import com.cotf.ui.theme.ParchmentDim

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val userSession = remember { (context.applicationContext as CotfApp).userSession }

    var username by remember { mutableStateOf(userSession.getUsername() ?: "") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleLarge,
                color = Parchment
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Поле имени
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    error = ""
                },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.width(280.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Parchment,
                    unfocusedTextColor = Parchment,
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = ParchmentDim,
                    focusedLabelColor = ForestGreen,
                    unfocusedLabelColor = ParchmentDim,
                    cursorColor = ForestGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля (cosmetic — реальной авторизации нет)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = ""
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.width(280.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Parchment,
                    unfocusedTextColor = Parchment,
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = ParchmentDim,
                    focusedLabelColor = ForestGreen,
                    unfocusedLabelColor = ParchmentDim,
                    cursorColor = ForestGreen
                )
            )

            // Сообщение об ошибке
            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ForestButton(
                text = "Login",
                onClick = {
                    if (username.isBlank()) {
                        error = "Enter a username"
                    } else {
                        userSession.saveUsername(username)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.width(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ForestButton(
                text = "Back",
                onClick = { navController.popBackStack() },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}
