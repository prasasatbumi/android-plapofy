package com.finprov.plapofy.presentation.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.presentation.pin.components.PinInput

@Composable
fun VerifyPinDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()
    
    // Reset state on showing
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }
    
    LaunchedEffect(viewModelError) {
        if (viewModelError != null) {
            error = viewModelError
            pin = "" // Clear PIN on error
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width?
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Masukkan PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Verifikasi diperlukan untuk melanjutkan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
                
                PinInput(
                    value = pin,
                    onValueChange = { newValue ->
                        error = null
                        if (newValue.length <= 6) {
                            pin = newValue
                            if (pin.length == 6) {
                                viewModel.verifyPin(pin) {
                                    onSuccess()
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Batal")
                }
            }
        }
    }
}
