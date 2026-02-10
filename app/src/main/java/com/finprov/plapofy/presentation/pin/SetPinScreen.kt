package com.finprov.plapofy.presentation.pin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.finprov.plapofy.presentation.pin.components.PinInput

@Composable
fun SetPinScreen(
    navController: NavController,
    viewModel: PinViewModel = hiltViewModel(),
    onPinSet: () -> Unit // Callback when PIN is successfully set
) {
    var step by remember { mutableStateOf(1) } // 1: Password, 2: Pin, 3: Confirm
    var password by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()
    val isGoogleUser by viewModel.isGoogleUser.collectAsState()
    
    // Skip password step for Google Users
    LaunchedEffect(isGoogleUser) {
        if (isGoogleUser) {
            step = 2
        }
    }
    
    // Listen for ViewModel errors
    LaunchedEffect(viewModelError) {
        if (viewModelError != null) {
            error = viewModelError
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = when (step) {
                1 -> "Konfirmasi Kata Sandi"
                2 -> "Buat PIN Baru"
                else -> "Konfirmasi PIN Anda"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = when (step) {
                1 -> "Demi keamanan, masukkan kata sandi akun Anda"
                2 -> "PIN digunakan untuk otorisasi transaksi"
                else -> "Masukkan ulang PIN Anda"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (step == 1) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Kata Sandi") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (password.isNotEmpty()) {
                        viewModel.verifyPassword(password) {
                            step = 2
                        }
                    } else {
                        error = "Kata sandi wajib diisi"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                     CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), 
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                     )
                } else {
                    Text("Lanjut")
                }
            }
        } else {
            PinInput(
                value = if (step == 3) confirmPin else pin,
                onValueChange = { newValue ->
                    if (step == 3) {
                        confirmPin = newValue
                        error = null
                        if (confirmPin.length == 6) {
                            if (confirmPin == pin) {
                                // Submit
                                val pwd = if (isGoogleUser) "" else password
                                viewModel.setPin(pwd, pin) {
                                    onPinSet()
                                }
                            } else {
                                error = "PIN tidak cocok"
                                confirmPin = "" // Clear confirmation
                            }
                        }
                    } else {
                        pin = newValue
                        if (pin.length == 6) {
                            step = 3
                        }
                    }
                }
            )
        }
        
        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
