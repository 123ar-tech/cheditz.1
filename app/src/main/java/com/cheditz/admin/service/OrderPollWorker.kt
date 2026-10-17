package com.cheditz.admin.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cheditz.admin.CheditzApp
import com.cheditz.admin.MainActivity
import com.cheditz.admin.R
import com.cheditz.admin.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class OrderPollWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("cheditz_admin", Context.MODE_PRIVATE)
            val firstRun = !prefs.contains("seen_orders")
            val seen = prefs.getStringSet("seen_orders", emptySet())!!.toMutableSet()

            val snap = FirebaseFirestore.getInstance()
                .collection("cheditz_orders")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            for (doc in snap.documents) {
                val id = doc.id
                if (id in seen) continue
                val order = try {
                    doc.toObject(Order::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Skipping order $id: ${e.message}")
                    null
                }
                if (!firstRun && order != null && order.isPending) {
                    showNewOrderNotification(order)
                }
                seen.add(id)
            }

            if (seen.size > 500) {
                val trimmed = seen.toList().takeLast(400).toSet()
                seen.clear()
                seen.addAll(trimmed)
            }
            prefs.edit().putStringSet("seen_orders", seen).apply()
            Result.success()
        } catch (e: Throwable) {
            Log.w(TAG, "Order poll failed: ${e.message}")
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }

    private fun showNewOrderNotification(order: Order) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_order_id", order.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, order.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "New Cheditz Order"
        val body = "New order from ${order.name} for ${order.service} (${order.budget})"

        val notification = NotificationCompat.Builder(applicationContext, CheditzApp.CHANNEL_ORDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(Random.nextInt(), notification)
        } catch (_: SecurityException) {
        }
    }

    companion object {
        private const val TAG = "OrderPollWorker"
    }
}
