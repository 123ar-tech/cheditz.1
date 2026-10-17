package com.cheditz.admin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cheditz.admin.service.OrderPollWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

class CheditzApp : Application() {

    companion object {
        const val CHANNEL_ORDERS = "cheditz_orders"
        const val CHANNEL_GENERAL = "cheditz_general"
        lateinit var instance: CheditzApp
            private set
    }

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        subscribeToTopics()
        scheduleOrderPolling()
    }

    private fun scheduleOrderPolling() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<OrderPollWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cheditz_order_poll", ExistingPeriodicWorkPolicy.KEEP, periodic
        )

        val immediate = OneTimeWorkRequestBuilder<OrderPollWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "cheditz_order_poll_once", ExistingWorkPolicy.KEEP, immediate
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val orderChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "New Orders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new customer orders"
                enableVibration(true)
                setShowBadge(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            manager.createNotificationChannel(orderChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    private fun subscribeToTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("cheditz_orders")
            .addOnCompleteListener { /* silent */ }
    }
}
