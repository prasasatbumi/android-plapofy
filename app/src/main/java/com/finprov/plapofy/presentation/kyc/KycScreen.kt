package com.finprov.plapofy.presentation.kyc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.presentation.theme.*

import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: KycViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // State for image source selection dialog
    var showKtpSourceDialog by remember { mutableStateOf(false) }
    var showSelfieSourceDialog by remember { mutableStateOf(false) }
    
    // Camera URI states
    var ktpCameraUri by remember { mutableStateOf<Uri?>(null) }
    var selfieCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    // Camera permission state
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) }
    
    // Preview Image State
    var previewImageSource by remember { mutableStateOf<Any?>(null) } // Can be File or String (URL)

    // Gallery launchers for picking images
    val ktpGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it, "ktp_image.jpg")
            if (file != null) viewModel.onKtpSelected(file)
        }
    }

    val selfieGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it, "selfie_image.jpg")
            if (file != null) viewModel.onSelfieSelected(file)
        }
    }
    
    // Camera launchers
    val ktpCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && ktpCameraUri != null) {
            val file = uriToFile(context, ktpCameraUri!!, "ktp_camera.jpg")
            if (file != null) viewModel.onKtpSelected(file)
        }
    }
    
    val selfieCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && selfieCameraUri != null) {
            val file = uriToFile(context, selfieCameraUri!!, "selfie_camera.jpg")
            if (file != null) viewModel.onSelfieSelected(file)
        }
    }
    
    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            when (pendingCameraAction) {
                "ktp" -> {
                    ktpCameraUri = createImageUri(context, "ktp_capture.jpg")
                    ktpCameraUri?.let { ktpCameraLauncher.launch(it) }
                }
                "selfie" -> {
                    selfieCameraUri = createImageUri(context, "selfie_capture.jpg")
                    selfieCameraUri?.let { selfieCameraLauncher.launch(it) }
                }
            }
        } else {
            showPermissionDeniedDialog = true
        }
        pendingCameraAction = null
    }
    
    // Function to launch camera with permission check
    fun launchCameraWithPermission(type: String) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                when (type) {
                    "ktp" -> {
                        ktpCameraUri = createImageUri(context, "ktp_capture.jpg")
                        ktpCameraUri?.let { ktpCameraLauncher.launch(it) }
                    }
                    "selfie" -> {
                        selfieCameraUri = createImageUri(context, "selfie_capture.jpg")
                        selfieCameraUri?.let { selfieCameraLauncher.launch(it) }
                    }
                }
            }
            else -> {
                pendingCameraAction = type
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Handle success navigation
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            delay(1500)
            onSuccess()
        }
    }
    
    // Image Source Selection Dialog
    if (showKtpSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showKtpSourceDialog = false },
            onCameraClick = {
                showKtpSourceDialog = false
                launchCameraWithPermission("ktp")
            },
            onGalleryClick = {
                showKtpSourceDialog = false
                ktpGalleryLauncher.launch("image/*")
            }
        )
    }
    
    if (showSelfieSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showSelfieSourceDialog = false },
            onCameraClick = {
                showSelfieSourceDialog = false
                launchCameraWithPermission("selfie")
            },
            onGalleryClick = {
                showSelfieSourceDialog = false
                selfieGalleryLauncher.launch("image/*")
            }
        )
    }
    
    // Permission Denied Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Izin Kamera Ditolak") },
            text = { Text("Aplikasi membutuhkan izin kamera untuk mengambil foto. Silakan aktifkan izin di pengaturan.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Full Screen Preview Dialog
    if (previewImageSource != null) {
        FullScreenImageDialog(
            imageSource = previewImageSource,
            onDismiss = { previewImageSource = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifikasi Akun (KYC)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppPrimary,
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
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppPrimary
                )
            } else if (state.isSuccess) {
                SuccessView(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Upload Dokumen Pendukung",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lengkapi data berikut untuk verifikasi akun Anda.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // KTP Upload Section
                    UploadCard(
                        title = "Foto KTP",
                        file = state.ktpFile,
                        imageUrl = state.ktpUrl,
                        onClick = { showKtpSourceDialog = true },
                        onPreview = { source -> previewImageSource = source }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selfie Upload Section
                    UploadCard(
                        title = "Foto Selfie",
                        file = state.selfieFile,
                        imageUrl = state.selfieUrl,
                        onClick = { showSelfieSourceDialog = true },
                        onPreview = { source -> previewImageSource = source }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // AppError Message
                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = AppError, // Ensure AppError is imported from theme
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { viewModel.submitKyc() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        enabled = (state.ktpFile != null && state.selfieFile != null) || (state.kycStatus == "VERIFIED" && false) // Disable if verified? Or keep enabled for re-upload? Assuming re-upload allowed unless verified.
                        // Actually, let's keep it simple: Enable if new files selected. If old files exist, user might want to update.
                        // But if user just wants to see, button shouldn't do anything or say "Update".
                        // Let's just stick to original logic: enabled if files selected. If user selects files, they can submit.
                    ) {
                        Text(if (state.kycStatus == "VERIFIED") "Data Terverifikasi" else "Kirim Data Verifikasi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Pilih Sumber Gambar", 
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            ) 
        },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCameraClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = AppPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Kamera", fontWeight = FontWeight.Medium)
                        Text(
                            "Ambil foto langsung",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGalleryClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = AppPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Galeri", fontWeight = FontWeight.Medium)
                        Text(
                            "Pilih dari galeri foto",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun UploadCard(
    title: String,
    file: File?,
    imageUrl: String? = null,
    onClick: () -> Unit,
    onPreview: (Any) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = if (file != null || imageUrl != null) Color.Transparent else Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = file != null || imageUrl != null) {
                        if (file != null) onPreview(file)
                        else if (imageUrl != null) onPreview(imageUrl)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (file != null) {
                    coil.compose.AsyncImage(
                        model = file,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onError = { error ->
                            android.util.Log.e("KYC_IMAGE", "File Load Error: ${error.result.throwable.message}")
                        }
                    )
                } else if (imageUrl != null) {
                    val fullUrl = sanitizeUrl(imageUrl)
                    android.util.Log.d("KYC_IMAGE", "Loading KYC image from URL: $fullUrl")
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(fullUrl)
                            .crossfade(true)
                            .error(android.R.drawable.ic_menu_report_image)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        onError = { error ->
                            android.util.Log.e("KYC_IMAGE", "URL Load Error for $fullUrl: ${error.result.throwable.message}")
                            android.util.Log.e("KYC_IMAGE", "Error cause: ${error.result.throwable.cause}")
                        },
                        onSuccess = {
                            android.util.Log.d("KYC_IMAGE", "Successfully loaded: $fullUrl")
                        }
                    )
                } else {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = AppPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = when {
                        file != null -> "Klik foto untuk perbesar" // Changed from file name
                        imageUrl != null -> "Klik foto untuk perbesar"
                        else -> "Tap untuk upload"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (file != null || imageUrl != null) AppSuccess else TextSecondary
                )
            }
            
            if (file != null || imageUrl != null) {
                Text(
                    text = "Ubah",
                    color = AppPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SuccessView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AppSuccess,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Akun Terverifikasi!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppSuccess
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selamat! Akun Anda telah terverifikasi.\nAnda sekarang dapat mengajukan pinjaman.",
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
    }
}

// Helper to create URI for camera output
fun createImageUri(context: Context, fileName: String): Uri? {
    return try {
        val file = File(context.cacheDir, fileName)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Helper to convert URI to File
fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Helper to sanitize URL (handle Windows backslashes and missing leading slash)
// Note: Logic moved to User.kt, keeping here as standalone helper if needed for non-User context
fun sanitizeUrl(path: String): String {
    if (path.startsWith("http")) return path
    
    val cleanPath = path.replace("\\", "/")
    val fileName = if (cleanPath.startsWith("/")) cleanPath.substring(1) else cleanPath
    
    // BASE_URL already ends with /api/
    // We need to construct: http://host:port/api/files/filename
    return com.finprov.plapofy.BuildConfig.BASE_URL + "files/" + fileName
}

@Composable
fun FullScreenImageDialog(
    imageSource: Any?,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false // This makes it truly full screen
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() }, // Close when clicking background
            contentAlignment = Alignment.Center
        ) {
            val model = if (imageSource is String) {
                sanitizeUrl(imageSource)
            } else {
                imageSource
            }

            coil.compose.AsyncImage(
                model = model,
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Add some padding so controls are visible
                contentScale = androidx.compose.ui.layout.ContentScale.Fit // Fit entire image on screen
            )
            
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp) // Adjusted padding for status bar/corners
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
