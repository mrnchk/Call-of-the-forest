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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cotf.CotfApp
import com.cotf.network.dto.LoginRequest
import com.cotf.network.dto.RegisterRequest
import com.cotf.ui.components.ForestButton
import com.cotf.ui.theme.DarkSurface
import com.cotf.ui.theme.ForestGreen
import com.cotf.ui.theme.Parchment
import com.cotf.ui.theme.ParchmentDim
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as CotfApp }
    val userSession = remember { app.userSession }
    val authApi = remember { app.authApi }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                enabled = !isLoading,
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

            // Поле пароля
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
                enabled = !isLoading,
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

            if (isLoading) {
                CircularProgressIndicator(color = ForestGreen)
            } else {
                // Кнопка Login
                ForestButton(
                    text = "Login",
                    onClick = {
                        if (username.isBlank()) {
                            error = "Enter a username"
                            return@ForestButton
                        }
                        if (password.isBlank()) {
                            error = "Enter a password"
                            return@ForestButton
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val response = authApi.login(LoginRequest(username, password))

                                if (response.isSuccessful) {
                                    val body = response.body()
                                    if (body != null) {
                                        userSession.saveAuth(
                                            body.accessToken,
                                            body.refreshToken,
                                            body.username
                                        )
                                        navController.popBackStack()
                                    } else {
                                        error = "Empty response from server"
                                    }
                                } else {
                                    error = when (response.code()) {
                                        400 -> "Invalid username or password"
                                        else -> "Server error (${response.code()})"
                                    }
                                }
                            } catch (e: Exception) {
                                error = "Connection error: ${e.message ?: "unknown"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка Register
                ForestButton(
                    text = "Register",
                    onClick = {
                        if (username.isBlank()) {
                            error = "Enter a username"
                            return@ForestButton
                        }
                        if (password.isBlank()) {
                            error = "Enter a password"
                            return@ForestButton
                        }
                        if (password.length < 6) {
                            error = "Password must be at least 6 characters"
                            return@ForestButton
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val response = authApi.register(RegisterRequest(username, password))

                                if (response.isSuccessful) {
                                    val body = response.body()
                                    if (body != null) {
                                        userSession.saveAuth(
                                            body.accessToken,
                                            body.refreshToken,
                                            body.username
                                        )
                                        navController.popBackStack()
                                    } else {
                                        error = "Empty response from server"
                                    }
                                } else {
                                    error = when (response.code()) {
                                        400 -> "Invalid username or password"
                                        409 -> "Username already taken"
                                        else -> "Server error (${response.code()})"
                                    }
                                }
                            } catch (e: Exception) {
                                error = "Connection error: ${e.message ?: "unknown"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ForestButton(
                    text = "Back",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}
