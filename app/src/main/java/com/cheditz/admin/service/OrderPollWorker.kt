package com.cheditz.admin.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cheditz.admin.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderPollWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val snap = FirebaseFirestore.getInstance()
                .collection("cheditz_orders")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val orders = snap.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.w(TAG, "Skipping order ${doc.id}: ${e.message}")
                    null
                }
            }

            OrderNotifier.processOrders(applicationContext, orders)
            Result.success()
        } catch (e: Throwable) {
            Log.w(TAG, "Order poll failed: ${e.message}")
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }

    companion object {
        private const val TAG = "OrderPollWorker"
    }
}
