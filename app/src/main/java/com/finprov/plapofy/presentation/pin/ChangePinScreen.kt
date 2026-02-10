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
fun ChangePinScreen(
    navController: NavController,
    viewModel: PinViewModel = hiltViewModel(),
    onPinChanged: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Old, 2: New, 3: Confirm
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()
    
    // Listen for ViewModel errors
    LaunchedEffect(viewModelError) {
        if (viewModelError != null) {
            error = viewModelError
             if (step == 1) {
                 oldPin = "" // Clear if verification failed
             }
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
            text = when(step) {
                1 -> "Masukkan PIN Lama"
                2 -> "Buat PIN Baru"
                else -> "Konfirmasi PIN Baru"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PinInput(
            value = when(step) {
                1 -> oldPin
                2 -> newPin
                else -> confirmPin
            },
            onValueChange = { newValue ->
                error = null
                when (step) {
                    1 -> {
                        oldPin = newValue
                        if (oldPin.length == 6) {
                            // Verify OLD PIN local first? No, need backend verify check?
                            // For Change Password we usually Verify Old Password first.
                            // But here we can just collect it and send it at the end?
                            // Or verify efficiently? The API has `changePin(old, new)`.
                            // So we just collect it. But good UX verifies old PIN first?
                            // Let's assume user knows their PIN. We'll proceed to step 2 directly 
                            // but implementation wise we don't verify until the end call `changePin`.
                            // Unless we want `verifyPin` call first.
                            // Let's call verifyPin first for better UX.
                            viewModel.verifyPin(oldPin) {
                                step = 2
                            }
                        }
                    }
                    2 -> {
                        newPin = newValue
                        if (newPin.length == 6) {
                            step = 3
                        }
                    }
                    3 -> {
                        confirmPin = newValue
                        if (confirmPin.length == 6) {
                            if (confirmPin == newPin) {
                                // Submit Change
                                viewModel.changePin(oldPin, newPin) {
                                    onPinChanged()
                                }
                            } else {
                                error = "PIN Baru tidak cocok"
                                confirmPin = ""
                            }
                        }
                    }
                }
            }
        )
        
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
