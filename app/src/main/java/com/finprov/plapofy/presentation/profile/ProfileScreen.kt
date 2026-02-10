package com.finprov.plapofy.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.presentation.theme.*

import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onKycClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onPinClick: (Boolean) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel(),
    pinViewModel: com.finprov.plapofy.presentation.pin.PinViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val hasPin by pinViewModel.hasPin.collectAsState()

    // Handle initial login check
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == false) {
            onNavigateToLogin()
        }
    }
    
    // Check PIN status
    LaunchedEffect(Unit) {
        pinViewModel.checkPinStatus()
    }

    // Handle save success
    LaunchedEffect(profileState.saveSuccess) {
        if (profileState.saveSuccess) {
            delay(2000)
            viewModel.clearSaveSuccess()
        }
    }

    if (isUserLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Error)
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
                profileState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                profileState.error != null && profileState.user == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
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
                            text = profileState.error!!,
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {
                    ProfileForm(
                        formState = formState,
                        isSaving = profileState.isSaving,
                        errorMessage = profileState.error,
                        saveSuccess = profileState.saveSuccess,
                        hasPin = hasPin,
                        onFieldChange = { field, value -> viewModel.updateFormField(field, value) },
                        onSaveClick = { viewModel.saveProfile() },
                        onKycClick = onKycClick,
                        onHistoryClick = onHistoryClick,
                        onChangePasswordClick = onChangePasswordClick,
                        onPinClick = { onPinClick(hasPin == true) },
                        ktpUrl = profileState.user?.getKtpUrl(),
                        selfieUrl = profileState.user?.getSelfieUrl()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileForm(
    formState: ProfileFormState,
    isSaving: Boolean,
    errorMessage: String?,
    saveSuccess: Boolean,
    hasPin: Boolean?,
    onFieldChange: (ProfileField, String) -> Unit,
    onSaveClick: () -> Unit,
    onKycClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onPinClick: () -> Unit,
    ktpUrl: String? = null,
    selfieUrl: String? = null
) {
    // State for Full Screen Image Preview
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    
    // Show Preview Dialog if needed
    if (previewImageUrl != null) {
        com.finprov.plapofy.presentation.kyc.FullScreenImageDialog(
            imageSource = previewImageUrl,
            onDismiss = { previewImageUrl = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Primary.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture (Placeholder for now, can be updated later)
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = formState.name.ifBlank { "Nama Belum Diisi" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formState.email.ifBlank { "Email belum diisi" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // KYC Images Section (Only visible if Verified or Submitted)
        if ((formState.kycStatus == "VERIFIED" || formState.kycStatus == "SUBMITTED") && (ktpUrl != null || selfieUrl != null)) {
             Spacer(modifier = Modifier.height(24.dp))
             SectionHeader(title = "Dokumen KYC", icon = Icons.Default.Description)
             Spacer(modifier = Modifier.height(12.dp))
             
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                 if (ktpUrl != null) {
                     KycImageCard(
                         title = "Foto KTP",
                         imageUrl = ktpUrl,
                         modifier = Modifier.weight(1f),
                         onClick = { previewImageUrl = ktpUrl }
                     )
                 }
                 if (selfieUrl != null) {
                     KycImageCard(
                         title = "Foto Selfie",
                         imageUrl = selfieUrl,
                         modifier = Modifier.weight(1f),
                         onClick = { previewImageUrl = selfieUrl }
                     )
                 }
             }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Button
        Button(
            onClick = onHistoryClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(Icons.Default.History, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Riwayat Pinjaman", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Information Section
        SectionHeader(title = "Informasi Pribadi", icon = Icons.Default.Person)
        
        Spacer(modifier = Modifier.height(12.dp))

        // KYC Button
        // KYC Button
        val kycStatus = formState.kycStatus
        val isVerified = kycStatus == "VERIFIED"
        val isSubmitted = kycStatus == "SUBMITTED"

        Button(
            onClick = onKycClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isVerified -> Success.copy(alpha = 0.1f)
                    isSubmitted -> Warning.copy(alpha = 0.1f)
                    else -> Primary.copy(alpha = 0.1f)
                }
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                when {
                    isVerified -> Icons.Default.Verified
                    isSubmitted -> Icons.Default.HourglassEmpty
                    else -> Icons.Default.VerifiedUser
                },
                null,
                tint = when {
                    isVerified -> Success
                    isSubmitted -> Warning
                    else -> Primary
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                when {
                    isVerified -> "Akun Terverifikasi"
                    isSubmitted -> "Menunggu Verifikasi"
                    else -> "Verifikasi Akun (KYC)"
                },
                color = when {
                    isVerified -> Success
                    isSubmitted -> Warning
                    else -> Primary
                },
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        ProfileTextField(
            value = formState.name,
            onValueChange = { onFieldChange(ProfileField.NAME, it) },
            label = "Nama Lengkap",
            icon = Icons.Default.Person
        )

        ProfileTextField(
            value = formState.email,
            onValueChange = { onFieldChange(ProfileField.EMAIL, it) },
            label = "Email",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        ProfileTextField(
            value = formState.phoneNumber,
            onValueChange = { onFieldChange(ProfileField.PHONE, it) },
            label = "Nomor Telepon",
            icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Identity Section
        SectionHeader(title = "Identitas", icon = Icons.Default.Badge)
        
        Spacer(modifier = Modifier.height(12.dp))

        ProfileTextField(
            value = formState.nik,
            onValueChange = { onFieldChange(ProfileField.NIK, it) },
            label = "NIK (16 digit)",
            icon = Icons.Default.CreditCard,
            keyboardType = KeyboardType.Number
        )

        ProfileTextField(
            value = formState.npwp,
            onValueChange = { onFieldChange(ProfileField.NPWP, it) },
            label = "NPWP (opsional)",
            icon = Icons.Default.Assignment,
            keyboardType = KeyboardType.Number
        )

        ProfileTextField(
            value = formState.address,
            onValueChange = { onFieldChange(ProfileField.ADDRESS, it) },
            label = "Alamat Lengkap",
            icon = Icons.Default.Home,
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Employment Section
        SectionHeader(title = "Pekerjaan & Penghasilan", icon = Icons.Default.Work)
        
        Spacer(modifier = Modifier.height(12.dp))

        ProfileTextField(
            value = formState.occupation,
            onValueChange = { onFieldChange(ProfileField.OCCUPATION, it) },
            label = "Pekerjaan",
            icon = Icons.Default.Work
        )

        ProfileTextField(
            value = formState.monthlyIncome,
            onValueChange = { onFieldChange(ProfileField.INCOME, it) },
            label = "Penghasilan Bulanan (Rp)",
            icon = Icons.Default.AttachMoney,
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bank Account Section
        SectionHeader(title = "Rekening Pencairan", icon = Icons.Default.AccountBalance)
        
        Spacer(modifier = Modifier.height(12.dp))

        ProfileTextField(
            value = formState.bankName,
            onValueChange = { onFieldChange(ProfileField.BANK_NAME, it) },
            label = "Nama Bank",
            icon = Icons.Default.AccountBalanceWallet
        )

        ProfileTextField(
            value = formState.bankAccountNumber,
            onValueChange = { onFieldChange(ProfileField.BANK_ACCOUNT, it) },
            label = "Nomor Rekening",
            icon = Icons.Default.CreditCard,
            keyboardType = KeyboardType.Number
        )

        // Error Message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
            ) {
                Text(
                    text = errorMessage,
                    color = Error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Success Message
        if (saveSuccess) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Success)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profil berhasil disimpan!", color = Success)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Profil", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Change Password Button (Only for non-Google users)
        if (formState.isGoogleUser != true) {
             OutlinedButton(
                onClick = onChangePasswordClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Default.Lock, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ganti Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // PIN Button
        OutlinedButton(
            onClick = onPinClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
        ) {
            Icon(Icons.Default.Security, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasPin == true) "Ubah PIN" else "Buat PIN Transaksi",
                fontSize = 16.sp, 
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun KycImageCard(
    title: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.5f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_report_image) // Native Android error icon
                    .placeholder(android.R.drawable.ic_menu_gallery) // Native Android placeholder
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            // Overlay Title
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Primary) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = TextHint
        )
    )
}
