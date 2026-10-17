package com.cheditz.admin.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cheditz.admin.CheditzApp
import com.cheditz.admin.MainActivity
import com.cheditz.admin.R
import com.cheditz.admin.data.model.Order
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object OrderNotifier {

    private const val TAG = "OrderNotifier"
    private val lock = Any()

    fun processOrders(context: Context, orders: List<Order>) {
        synchronized<Unit>(lock) {
            try {
                val prefs = context.getSharedPreferences("cheditz_admin", Context.MODE_PRIVATE)
                val seen = prefs.getStringSet("seen_orders", emptySet())!!.toMutableSet()
                var changed = false

                for (order in orders) {
                    if (order.id.isBlank() || order.id in seen) continue
                    if (order.isPending) {
                        showNewOrderNotification(context, order)
                    }
                    seen.add(order.id)
                    changed = true
                }

                if (changed) {
                    if (seen.size > 500) {
                        val trimmed = seen.toList().takeLast(400).toSet()
                        seen.clear()
                        seen.addAll(trimmed)
                    }
                    prefs.edit().putStringSet("seen_orders", seen).apply()
                }
            } catch (t: Throwable) {
                Log.w(TAG, "processOrders failed: ${t.message}")
            }
        }
    }

    private fun showNewOrderNotification(context: Context, order: Order) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_order_id", order.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, order.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "New Cheditz Order"
        val body = "New order from ${order.name} for ${order.service} (${order.budget})"

        val notification = NotificationCompat.Builder(context, CheditzApp.CHANNEL_ORDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(order.id.hashCode(), notification)
        } catch (_: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted — notification skipped")
        }
    }

    @Suppress("unused")
    private fun orderTimeMillis(o: Order): Long {
        o.createdAt?.toDate()?.let { return it.time }
        val s = o.date
        if (s.length >= 19) {
            try {
                return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                    .parse(s.substring(0, 19))?.time ?: 0L
            } catch (_: Exception) {
            }
        }
        return 0L
    }
}
