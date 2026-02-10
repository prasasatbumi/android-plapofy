package com.finprov.plapofy.presentation.loan.apply

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.presentation.theme.Primary
import com.finprov.plapofy.presentation.theme.TextHint
import com.finprov.plapofy.presentation.theme.White
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyCreditScreen(
    plafondId: Long,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onGoToProfile: () -> Unit,
    viewModel: ApplyCreditViewModel = hiltViewModel(),
    pinViewModel: com.finprov.plapofy.presentation.pin.PinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    // Form State
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf<Long?>(null) }
    
    // UI State
    var currentStep by remember { mutableStateOf(1) }
    var isBranchDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }
    
    // Location & Submit Logic
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val submitWithLoc = { loc: android.location.Location? ->
            submit(viewModel, amount, purpose, selectedBranchId, loc?.latitude, loc?.longitude)
        }
        
        if (isGranted) {
             try {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { loc -> submitWithLoc(loc) }
                        .addOnFailureListener { submitWithLoc(null) }
                        .addOnCanceledListener { submitWithLoc(null) }
                    return@rememberLauncherForActivityResult
                }
             } catch (e: Exception) {}
        }
        submitWithLoc(null)
    }

    LaunchedEffect(plafondId) {
        viewModel.loadData(plafondId)
    }
    
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            showSuccessDialog = true
        }
    }

    // Handle Submission Errors
    LaunchedEffect(state.error) {
        if (state.error != null && state.plafond != null) {
            com.finprov.plapofy.presentation.common.SnackbarManager.showMessage(state.error!!)
            viewModel.clearError()
        }
    }

    // PIN Logic
    val hasPin by pinViewModel.hasPin.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var showNoPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        pinViewModel.checkPinStatus()
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
                        "Pengajuan limit kredit disimpan. Akan dikirim otomatis saat terhubung ke internet."
                    else 
                        "Pengajuan limit kredit berhasil dikirim! Mohon tunggu proses verifikasi."
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

    if (showPinDialog) {
        com.finprov.plapofy.presentation.pin.VerifyPinDialog(
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )
    }

    if (showNoPinDialog) {
        AlertDialog(
            onDismissRequest = { showNoPinDialog = false },
            title = { Text("PIN Belum Diatur") },
            text = { Text("Anda wajib mengatur PIN Transaksi sebelum mengajukan kredit. Silakan atur PIN di menu Profil.") },
            confirmButton = {
                Button(onClick = {
                    showNoPinDialog = false
                    onGoToProfile()
                }) {
                    Text("Ke Profil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoPinDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajukan Limit Kredit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBackClick()
                    }) {
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
            } else if (state.error != null && state.plafond == null) {
                 Text(state.error!!, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            } else if (state.plafond != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val plafond = state.plafond!!
                    
                    // Step Indicator
                    ApplyCreditProgressTracker(currentStep = currentStep)
                    
                    Divider(modifier = Modifier.padding(bottom = 16.dp))
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (currentStep) {
                            1 -> {
                                // Step 1: Data Pengajuan
                                Text("Data Pengajuan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Product Info
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.1f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Produk: ${plafond.name}", fontWeight = FontWeight.SemiBold)
                                        Text("Max Limit: ${formatCurrency(plafond.maxAmount)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                                    label = { Text("Nominal Pengajuan (Rp)") },
                                    leadingIcon = { Icon(Icons.Default.Money, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                ExposedDropdownMenuBox(
                                    expanded = isBranchDropdownExpanded,
                                    onExpandedChange = { isBranchDropdownExpanded = !isBranchDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val selectedBranchName = state.branches.find { it.id == selectedBranchId }?.name ?: "Pilih Cabang"
                                    OutlinedTextField(
                                        value = selectedBranchName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Cabang") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBranchDropdownExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isBranchDropdownExpanded,
                                        onDismissRequest = { isBranchDropdownExpanded = false }
                                    ) {
                                        state.branches.forEach { branch ->
                                            DropdownMenuItem(
                                                text = { Text(branch.name) },
                                                onClick = { selectedBranchId = branch.id; isBranchDropdownExpanded = false }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = purpose,
                                    onValueChange = { purpose = it },
                                    label = { Text("Tujuan (Opsional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                            }
                            2 -> {
                                // Step 2: Dokumen Checklist
                                Text("Kelengkapan Dokumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pastikan dokumen berikut sudah lengkap di profil Anda.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                val missing = state.profileCheck?.missingFields ?: emptyList()
                                val ktpMissing = missing.any { it.contains("ktp", true) }
                                val selfieMissing = missing.any { it.contains("selfie", true) }
                                val profileMissing = missing.any { !it.contains("ktp", true) && !it.contains("selfie", true) }
                                
                                DocumentCheckItem("Foto KTP", !ktpMissing)
                                DocumentCheckItem("Selfie dengan KTP", !selfieMissing)
                                DocumentCheckItem("Data Diri Lengkap", !profileMissing)
                                
                                if (ktpMissing || selfieMissing || profileMissing) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Mohon lengkapi data/dokumen di menu Profil sebelum melanjutkan.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = onGoToProfile,
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Lengkapi Profil")
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> {
                                // Step 3: Review
                                Text("Review Pengajuan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = White),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        ReviewRow("Produk", plafond.name)
                                        ReviewRow("Nominal", formatCurrency(amount.toDoubleOrNull() ?: 0.0))
                                        val branchName = state.branches.find { it.id == selectedBranchId }?.name ?: "-"
                                        ReviewRow("Cabang", branchName)
                                        ReviewRow("Tujuan", purpose.ifEmpty { "-" })
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Dengan melanjutkan, saya menyatakan data yang diisi adalah benar.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                    
                    // Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Text("Kembali")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++ 
                                } else {
                                     // Verify PIN before submitting
                                    if (hasPin == true) {
                                        showPinDialog = true
                                    } else {
                                        showNoPinDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = if (currentStep > 1) 8.dp else 0.dp),
                            enabled = when (currentStep) {
                                1 -> amount.isNotEmpty() && selectedBranchId != null
                                2 -> state.profileCheck?.isComplete == true
                                3 -> !state.isSubmitting
                                else -> false
                            }
                        ) {
                            if (currentStep == 3 && state.isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = White)
                            } else {
                                Text(if (currentStep < 3) "Lanjut" else "Kirim Pengajuan")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentCheckItem(title: String, isComplete: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isComplete) Color(0xFF43A047) else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun submit(viewModel: ApplyCreditViewModel, amountStr: String, purpose: String, branchId: Long?, lat: Double?, long: Double?) {
    val amount = amountStr.toDoubleOrNull() ?: 0.0
    if (branchId != null && amount > 0) {
        viewModel.submitApplication(amount, purpose, branchId, lat, long)
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}
