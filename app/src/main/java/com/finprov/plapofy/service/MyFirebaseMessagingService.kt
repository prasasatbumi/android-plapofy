package com.finprov.plapofy.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.finprov.plapofy.MainActivity
import com.finprov.plapofy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        // sendRegistrationToServer(token) 
        // We handle this in LoginScreen by retrieving the current token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Extract loan ID from data payload
        val loanId = remoteMessage.data["loanId"]
        val loanApplicationId = remoteMessage.data["loanApplicationId"]
        
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        Log.d(TAG, "Loan ID: $loanId, LoanApplicationId: $loanApplicationId")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "Plapofy", 
                messageBody = it.body ?: "",
                loanId = loanId ?: loanApplicationId
            )
        }
    }

    private fun sendNotification(title: String, messageBody: String, loanId: String? = null) {
        val intent = if (loanId != null) {
            // Deep link to loan detail
            Intent(Intent.ACTION_VIEW, Uri.parse("plapofy://loan/$loanId")).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        } else {
            // Default: open my-loans screen
            Intent(Intent.ACTION_VIEW, Uri.parse("plapofy://loans")).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            loanId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Plapofy Loan Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}

