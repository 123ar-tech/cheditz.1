package com.cheditz.admin.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cheditz.admin.CheditzApp
import com.cheditz.admin.MainActivity
import com.cheditz.admin.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class CheditzMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Optionally upload token to a Firestore doc for Cloud Functions targeting
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val orderId = data["orderId"] ?: ""
        val title = data["title"] ?: message.notification?.title ?: "New Cheditz Order"
        val body = data["body"]
            ?: message.notification?.body
            ?: "New order from ${data["name"] ?: "customer"} for ${data["service"] ?: "a service"}"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (orderId.isNotBlank()) putExtra("open_order_id", orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CheditzApp.CHANNEL_ORDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted on Android 13+
        }
    }
}
