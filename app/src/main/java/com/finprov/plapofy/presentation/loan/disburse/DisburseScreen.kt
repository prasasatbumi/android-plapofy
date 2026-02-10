package com.finprov.plapofy.presentation.loan.disburse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.presentation.theme.Primary
import com.finprov.plapofy.presentation.theme.White
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisburseScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: DisburseViewModel = hiltViewModel(),
    pinViewModel: com.finprov.plapofy.presentation.pin.PinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }
    var selectedTenor by remember { mutableStateOf(3) } // Default tenor 3
    
    val tenors = listOf(1, 3, 6, 12)
    
    // PIN Logic
    val hasPin by pinViewModel.hasPin.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var showNoPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        pinViewModel.checkPinStatus()
    }

    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) {
            showSuccessDialog = true
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onSuccess()
            },
            title = { 
                Text(
                    if (state.isPendingSync) "Disimpan!" else "Berhasil!"
                ) 
            },
            text = { 
                Text(
                    if (state.isPendingSync) 
                        "Pengajuan pencairan disimpan. Akan dikirim otomatis saat terhubung ke internet."
                    else 
                        "Pencairan dana berhasil diproses!"
                )
            },
            confirmButton = {
                Button(onClick = { 
                    showSuccessDialog = false
                    onSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // PIN Dialogs
    if (showPinDialog) {
        com.finprov.plapofy.presentation.pin.VerifyPinDialog(
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt > 0) viewModel.submitDisbursement(amt, selectedTenor)
            }
        )
    }

    if (showNoPinDialog) {
        AlertDialog(
            onDismissRequest = { showNoPinDialog = false },
            title = { Text("PIN Belum Diatur") },
            text = { Text("Anda wajib mengatur PIN Transaksi sebelum mencairkan dana. Silakan atur PIN di menu Profil.") },
            confirmButton = {
                Button(onClick = {
                    showNoPinDialog = false
                    // Ideally navigate to profile, but here we just dismiss to allow user to go back
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cairkan Dana", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
             if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.creditLine != null) {
                 val creditLine = state.creditLine!!
                 Column(modifier = Modifier.padding(16.dp)) {
                     Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Primary),
                        shape = RoundedCornerShape(16.dp)
                     ) {
                         Column(modifier = Modifier.padding(16.dp)) {
                             Text("Saldo Tersedia", color = White.copy(alpha = 0.8f))
                             Text(formatCurrency(creditLine.availableBalance), style = MaterialTheme.typography.headlineMedium, color = White, fontWeight = FontWeight.Bold)
                         }
                     }
                     
                     // Pending Disbursements Section
                     if (state.pendingDisbursements.isNotEmpty()) {
                         Spacer(modifier = Modifier.height(16.dp))
                         Card(
                             modifier = Modifier.fillMaxWidth(),
                             colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                             shape = RoundedCornerShape(12.dp)
                         ) {
                             Column(modifier = Modifier.padding(12.dp)) {
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Icon(
                                         Icons.Default.Sync,
                                         contentDescription = null,
                                         tint = Color(0xFFFF9800),
                                         modifier = Modifier.size(20.dp)
                                     )
                                     Spacer(modifier = Modifier.width(8.dp))
                                     Text(
                                         "Menunggu Dikirim (${state.pendingDisbursements.size})",
                                         fontWeight = FontWeight.SemiBold,
                                         color = Color(0xFFE65100)
                                     )
                                 }
                                 state.pendingDisbursements.forEach { pending ->
                                     Spacer(modifier = Modifier.height(8.dp))
                                     Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.SpaceBetween
                                     ) {
                                         Text(
                                             formatCurrency(pending.amount),
                                             style = MaterialTheme.typography.bodyMedium
                                         )
                                         Text(
                                             "${pending.tenor} bulan",
                                             style = MaterialTheme.typography.bodySmall,
                                             color = Color.Gray
                                         )
                                     }
                                 }
                                 Spacer(modifier = Modifier.height(4.dp))
                                 Text(
                                     "Akan dikirim otomatis saat online",
                                     style = MaterialTheme.typography.bodySmall,
                                     color = Color.Gray
                                 )
                             }
                         }
                     }
                     
                     Spacer(modifier = Modifier.height(24.dp))
                     
                     OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() } },
                        label = { Text("Nominal Pencairan") },
                        prefix = { Text("Rp ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                     )
                     
                     Spacer(modifier = Modifier.height(16.dp))
                     
                     Text("Tenor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                     Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         tenors.forEach { t ->
                             FilterChip(
                                 selected = selectedTenor == t,
                                 onClick = { selectedTenor = t },
                                 label = { Text("$t Bulan") }
                             )
                         }
                     }
                     
                     Spacer(modifier = Modifier.height(24.dp))
                     
                     if (state.error != null) {
                         Text(state.error!!, color = MaterialTheme.colorScheme.error)
                         Spacer(modifier = Modifier.height(16.dp))
                     }
                     
                     Button(
                         onClick = {
                             val amt = amount.toDoubleOrNull() ?: 0.0
                             if (amt > 0) {
                                 // Check PIN
                                 if (hasPin == true) {
                                     showPinDialog = true
                                 } else {
                                     showNoPinDialog = true
                                 }
                             }
                         },
                         modifier = Modifier.fillMaxWidth().height(50.dp),
                         enabled = !state.isSubmitting && amount.isNotEmpty()
                     ) {
                         if (state.isSubmitting) {
                             CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                         } else {
                             Text("Cairkan Sekarang")
                         }
                     }
                 }
            } else if (state.error != null) {
                Text(state.error!!, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}
