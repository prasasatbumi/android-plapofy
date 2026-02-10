package com.finprov.plapofy.presentation.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.presentation.theme.*
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoanScreen(
    plafondId: Long,
    initialAmount: String? = null,
    initialTenor: String? = null,
    onBackClick: () -> Unit,
    onCompleteProfile: () -> Unit,
    onVerifyKyc: () -> Unit,
    onLoginRequired: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val state by viewModel.applyLoanState.collectAsState()
    
    var amount by remember { mutableStateOf(initialAmount ?: "") }
    var selectedTenor by remember { mutableStateOf(initialTenor?.toIntOrNull()) }
    var selectedBranchId by remember { mutableStateOf<Long?>(null) }
    var purpose by remember { mutableStateOf("") }
    
    var isBranchDropdownExpanded by remember { mutableStateOf(false) }
    var isTenorDropdownExpanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val submitWithLocation = { location: android.location.Location? ->
            if (amount.isNotEmpty() && selectedTenor != null && selectedBranchId != null) {
                val amountValue = amount.filter { c -> c.isDigit() }.toDoubleOrNull() ?: 0.0
                val tenorValue = selectedTenor ?: 0
                val branchIdValue = selectedBranchId ?: 0L
                viewModel.submitLoan(amountValue, tenorValue, purpose.ifBlank { null }, branchIdValue, location?.latitude, location?.longitude)
            }
        }

        if (isGranted) {
            try {
                // Check permission again explicitly just to be safe for lint/runtime
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location -> submitWithLocation(location) }
                        .addOnFailureListener { submitWithLocation(null) }
                        .addOnCanceledListener { submitWithLocation(null) }
                } else {
                    submitWithLocation(null)
                }
            } catch (e: SecurityException) {
                submitWithLocation(null)
            }
        } else {
            // Permission denied, submit without location
            submitWithLocation(null)
        }
    }

    LaunchedEffect(plafondId) {
        viewModel.loadPlafondForApplication(plafondId)
    }

    // Handle success
    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajukan Pinjaman", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                state.error != null && state.plafond == null -> {
                    ErrorView(
                        message = state.error!!,
                        onRetry = { viewModel.loadPlafondForApplication(plafondId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.requiresLogin -> {
                    LoginRequiredView(
                        onLogin = onLoginRequired,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null && state.error!!.contains("not verified", ignoreCase = true) -> {
                    KycRequiredView(
                        onVerifyKyc = onVerifyKyc,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.profileCheck?.isComplete == false -> {
                    ProfileIncompleteView(
                        missingFields = state.profileCheck!!.missingFields,
                        onCompleteProfile = onCompleteProfile,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.plafond != null -> {
                    val plafond = state.plafond!!
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Product Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = plafond.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Percent, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${plafond.interestRate}% per tahun",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachMoney, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${formatCurrency(plafond.minAmount)} - ${formatCurrency(plafond.maxAmount)}",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Loan Amount
                        Text(
                            text = "Jumlah Pinjaman",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it.filter { c -> c.isDigit() } },
                            label = { Text("Nominal") },
                            prefix = { Text("Rp ") },
                            leadingIcon = { Icon(Icons.Default.Money, null, tint = Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = TextHint
                            )
                        )
                        
                        // Suggestion Chips
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val chips = listOf(
                                "Min" to plafond.minAmount,
                                "50%" to ((plafond.minAmount + plafond.maxAmount) / 2),
                                "Max" to plafond.maxAmount
                            )
                            items(chips) { (label, value) ->
                                SuggestionChip(
                                    onClick = { 
                                        amount = value.toLong().toString() 
                                    },
                                    label = { Text(label) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }

                        if (amount.isNotEmpty()) {
                             Text(
                                text = formatCurrency(amount.toDoubleOrNull() ?: 0.0),
                                color = Primary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tenor
                        Text(
                            text = "Tenor (Bulan)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = isTenorDropdownExpanded,
                            onExpandedChange = { isTenorDropdownExpanded = !isTenorDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (selectedTenor != null) "$selectedTenor Bulan" else "Pilih Tenor",
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTenorDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = TextHint
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = isTenorDropdownExpanded,
                                onDismissRequest = { isTenorDropdownExpanded = false }
                            ) {
                                val sortedInterests = plafond.interests.sortedBy { it.tenor }
                                sortedInterests.forEach { interest ->
                                    DropdownMenuItem(
                                        text = { Text("${interest.tenor} Bulan (Bunga: ${interest.interestRate}%)") },
                                        onClick = {
                                            selectedTenor = interest.tenor
                                            isTenorDropdownExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Timer, null, tint = TextSecondary) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Branch
                        Text(
                            text = "Cabang Pengajuan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
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
                                leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = Primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBranchDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = TextHint
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = isBranchDropdownExpanded,
                                onDismissRequest = { isBranchDropdownExpanded = false }
                            ) {
                                state.branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch.name) },
                                        onClick = {
                                            selectedBranchId = branch.id
                                            isBranchDropdownExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Place, null, tint = TextSecondary) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Purpose
                        Text(
                            text = "Tujuan Pinjaman (Opsional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = purpose,
                            onValueChange = { purpose = it },
                            label = { Text("Jelaskan tujuan pinjaman") },
                            leadingIcon = { Icon(Icons.Default.Description, null, tint = Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = TextHint
                            )
                        )

                        // Error Message
                        if (state.error != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = state.error!!,
                                    color = Error,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                            enabled = !state.isSubmitting && amount.isNotEmpty() && selectedTenor != null && selectedBranchId != null
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Send, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Ajukan Pinjaman",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Coba Lagi")
        }
    }
}

@Composable
private fun KycRequiredView(
    onVerifyKyc: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.VerifiedUser,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Akun Belum Terverifikasi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Untuk mengajukan pinjaman, Anda perlu melakukan verifikasi identitas (KYC) terlebih dahulu.",
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onVerifyKyc,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Verifikasi Sekarang")
        }
    }
}

@Composable
private fun ProfileIncompleteView(
    missingFields: List<String>,
    onCompleteProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Warning
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lengkapi Profil Anda",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Untuk mengajukan pinjaman, lengkapi data berikut:",
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        missingFields.forEach { field ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                    tint = Warning
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(field, color = TextPrimary)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCompleteProfile,
            colors = ButtonDefaults.buttonColors(containerColor = Warning)
        ) {
            Icon(Icons.Default.Person, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lengkapi Profil")
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}

@Composable
private fun LoginRequiredView(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Login Diperlukan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Untuk mengajukan pinjaman, Anda perlu login terlebih dahulu.",
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogin,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Default.Login, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login Sekarang")
        }
    }
}
