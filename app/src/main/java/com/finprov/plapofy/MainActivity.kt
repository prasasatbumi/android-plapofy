package com.finprov.plapofy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.finprov.plapofy.presentation.common.SnackbarManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.finprov.plapofy.presentation.navigation.PlapofyNavGraph
import com.finprov.plapofy.presentation.theme.PlapofyTheme
import com.finprov.plapofy.domain.session.SessionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var authRepository: com.finprov.plapofy.domain.repository.AuthRepository
    
    private val deepLinkPath = mutableStateOf<String?>(null)
    private val sessionExpiredPath = mutableStateOf<String?>(null)
    private val showRootWarning = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Log.d("MainActivity", "Notification permission granted")
        } else {
            // TODO: Inform user that that your app will not show notifications.
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Security: Block screen capture and screen recording
        // window.setFlags(
        //     WindowManager.LayoutParams.FLAG_SECURE,
        //     WindowManager.LayoutParams.FLAG_SECURE
        // )
        
        // Security: Root detection
        val rootBeer = RootBeer(this)
        if (rootBeer.isRooted) {
            Log.w("MainActivity", "Device is rooted!")
            showRootWarning.value = true
        }
        
        // Android 13+ Notification Permission
        askNotificationPermission()
        
        // Create channel (required for Android 8+)
        createNotificationChannel()
        
        // Handle deep link from notification
        handleDeepLink(intent)
        
        // Observe session expiration events
        lifecycleScope.launch {
            sessionManager.sessionExpiredEvent.collect { event ->
                runOnUiThread {
                    sessionExpiredPath.value = "expired" // Trigger dialog
                }
            }
        }
        
        // Force refresh FCM Token (delete old + get fresh)
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Log.d("MainActivity", "Old FCM token deleted")
            }
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d("MainActivity", "Fresh FCM Token: ${token.take(20)}...")
                
                // Sync with backend if logged in
                lifecycleScope.launch {
                    try {
                        if (authRepository.isLoggedIn()) {
                            val result = authRepository.updateFcmToken(token)
                            if (result.isSuccess) {
                                Log.d("MainActivity", "FCM Token synced with backend successfully")
                            } else {
                                Log.e("MainActivity", "FCM Token sync failed: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to sync FCM token", e)
                    }
                }
            })
        }

        setContent {
            PlapofyTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    SnackbarManager.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Root warning dialog
                    if (showRootWarning.value) {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text("Peringatan Keamanan") },
                            text = { Text("Perangkat Anda terdeteksi dalam keadaan rooted. Untuk keamanan, beberapa fitur mungkin tidak berfungsi dengan baik.") },
                            confirmButton = {
                                TextButton(
                                    onClick = { showRootWarning.value = false }
                                ) {
                                    Text("Saya Mengerti")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { finish() }
                                ) {
                                    Text("Keluar")
                                }
                            }
                        )
                    }
                    
                    if (sessionExpiredPath.value != null) {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text("Session Expired") },
                            text = { Text("Session habis, silahkan login ulang") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        sessionExpiredPath.value = null
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                ) {
                                    Text("Login Ulang")
                                }
                            }
                        )
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            PlapofyNavGraph(
                                navController = navController,
                                deepLinkPath = deepLinkPath.value,
                                onDeepLinkHandled = { deepLinkPath.value = null }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null) {
            val host = data.host
            val path = data.pathSegments
            Log.d("MainActivity", "Deep link: host=$host, path=$path")
            
            when (host) {
                "loan" -> {
                    val loanId = path.getOrNull(0)
                    if (loanId != null) {
                        deepLinkPath.value = "loan-detail/$loanId"
                    } else {
                        deepLinkPath.value = "my-loans"
                    }
                }
                "loans" -> {
                    deepLinkPath.value = "my-loans"
                }
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining to the user
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "Plapofy Notifications"
            val channelDescription = "Notifications for Plapofy updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

