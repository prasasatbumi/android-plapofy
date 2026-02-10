package com.finprov.plapofy.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.BuildConfig
import com.finprov.plapofy.R
import com.finprov.plapofy.presentation.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // FCM State
    var fcmToken by remember { mutableStateOf<String?>(null) }
    
    // Google Sign-In setup
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.signInWithGoogle(idToken, fcmToken)
                }
            } catch (e: ApiException) {
                android.util.Log.e("LoginScreen", "Google Sign-In failed: ${e.statusCode}")
            }
        }
    }
    
    LaunchedEffect(Unit) {
        // Force delete old token and get a fresh one to avoid UNREGISTERED errors
        com.google.firebase.messaging.FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    android.util.Log.d("LoginScreen", "Old FCM token deleted, requesting fresh token")
                }
                // Get fresh token regardless of delete result
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            fcmToken = task.result
                            android.util.Log.d("LoginScreen", "Fresh FCM Token: ${fcmToken?.take(20)}...")
                        } else {
                            android.util.Log.e("LoginScreen", "Failed to get FCM token", task.exception)
                        }
                    }
            }
    }

    // Handle success
    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) {
            viewModel.resetLoginState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryDark, Primary, PrimaryLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo/Title
            Text(
                text = "PLAPOFY",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )
            
            Text(
                text = "Pinjaman Mudah & Cepat",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Masuk ke Akun",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = TextHint
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility 
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = TextHint
                        )
                    )


                    // Error Message
                    if (loginState.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = loginState.error!!,
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // Forgot Password
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                text = "Lupa Password?",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    Button(
                        onClick = { viewModel.login(username, password, fcmToken) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        enabled = !loginState.isLoading
                    ) {
                        if (loginState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Masuk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider with text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = TextHint)
                        Text(
                            text = "  atau  ",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Divider(modifier = Modifier.weight(1f), color = TextHint)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        enabled = !loginState.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Google "G" colored text as icon substitute
                            Text(
                                text = "G",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4) // Google Blue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Masuk dengan Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register Link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Belum punya akun? ",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = onRegisterClick) {
                            Text(
                                text = "Daftar",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Home
            TextButton(onClick = onBackClick) {
                Text(
                    text = "← Kembali ke Beranda",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

