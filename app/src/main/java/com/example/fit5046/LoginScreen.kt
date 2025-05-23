package com.example.fit5046

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle


@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }

    val auth = Firebase.auth // Reference the Firebase Authentication object

    // Page background
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFECB3))
                    )
                )
        ) {
            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App title
                Text(
                    text = "BookBuddy",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "Welcome to BookBuddy – Let’s start your reading adventure!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6D4C41),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                )
                {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.children),
                            contentDescription = "Kids Reading",
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (isLogin) "Login" else "Register",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Input email address
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Your Email") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFFF8E1),
                                unfocusedContainerColor = Color(0xFFFFFDE7),
                                focusedBorderColor = Color(0xFFFFD54F),
                                unfocusedBorderColor = Color(0xFFFFF176)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Enter password
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                message = ""
                            },
                            label = { Text("Pick a Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                                val desc = if (passwordVisible) "Hide password" else "Show password"
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = icon, contentDescription = desc)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFFF8E1),
                                unfocusedContainerColor = Color(0xFFFFFDE7),
                                focusedBorderColor = Color(0xFFFFD54F),
                                unfocusedBorderColor = Color(0xFFFFF176)
                            )
                        )

                        // If it is registration mode, display the confirmation password field
                        if (!isLogin) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                                    val desc = if (passwordVisible) "Hide password" else "Show password"
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = icon, contentDescription = desc)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFFF8E1),
                                    unfocusedContainerColor = Color(0xFFFFFDE7),
                                    focusedBorderColor = Color(0xFFFFD54F),
                                    unfocusedBorderColor = Color(0xFFFFF176)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }


                        Spacer(modifier = Modifier.height(24.dp))

                        // Login/Register button
                        Button(
                            onClick = {
                                // Verify email format & password length
                                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.length < 6) {
                                    message = "Please enter a valid email and password (min 6 characters)"
                                    return@Button
                                }

                                // Login logic
                                if (isLogin) {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            message = if (task.isSuccessful) {
                                                navController.navigate("main") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                                "✅ Login successful"
                                            } else {
                                                "Login failed: ${task.exception?.message}"
                                            }
                                        }
                                } else {
                                    if (password != confirmPassword) {
                                        message = "❗Passwords do not match"
                                        return@Button
                                    }

                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            message = if (task.isSuccessful) {
                                                "🎉 Registration successful"
                                            } else {
                                                "Registration failed: ${task.exception?.message}"
                                            }
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA726),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Text(
                                text = if (isLogin) "Login" else "Register",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { isLogin = !isLogin }) {
                            Text(
                                text = if (isLogin) "New here? Tap to sign up!" else "Back again? Tap to log in!",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF6D4C41)
                                )
                            )
                        }

                        // Display success/failure information
                        if (message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = message,
                                color = if (message.startsWith("✅") || message.startsWith("🎉")) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}