package com.cheditz.admin.data.repository

import android.util.Log
import com.cheditz.admin.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val TAG = "CheditzRepo"

private inline fun <reified T> DocumentSnapshot.safeParse(): T? = try {
    toObject(T::class.java)
} catch (e: Exception) {
    Log.w(TAG, "Skipping document $id (type mismatch): ${e.message}")
    null
}

private fun isoTime(s: String?): Long {
    if (s == null || s.length < 19) return 0L
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(s.substring(0, 19))?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}

private fun orderTime(o: Order): Long =
    o.createdAt?.toDate()?.time ?: isoTime(o.date)

class OrderRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val ordersRef = db.collection("cheditz_orders")

    fun observeOrders(): Flow<List<Order>> = callbackFlow {
        val reg = ordersRef.addSnapshotListener { snap, error ->
            if (error != null) {
                Log.w(TAG, "Orders listener error (kept last state): ${error.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            val orders = docs
                .mapNotNull { d -> d.safeParse<Order>()?.copy(id = d.id) }
                .sortedByDescending { orderTime(it) }
            trySend(orders)
        }
        awaitClose { reg.remove() }
    }

    suspend fun getOrder(id: String): Order? =
        ordersRef.document(id).get().await().safeParse<Order>()?.copy(id = id)

    suspend fun updateStatus(id: String, status: String) {
        ordersRef.document(id).update("status", status).await()
    }

    suspend fun deleteOrder(id: String) {
        ordersRef.document(id).delete().await()
    }
}

class ConfigRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val configRef = db.collection("cheditz").document("config")

    fun observeConfig(): Flow<SiteConfig?> = callbackFlow {
        val reg = configRef.addSnapshotListener { snap, error ->
            if (error != null) {
                Log.w(TAG, "Config listener error (kept last state): ${error.message}")
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) return@addSnapshotListener
            trySend(snap.safeParse())
        }
        awaitClose { reg.remove() }
    }

    suspend fun getConfig(): SiteConfig? =
        configRef.get().await().safeParse()

    suspend fun updateAccent(accent: String) {
        configRef.update("accent", accent).await()
    }

    suspend fun updateContent(content: ContentBlock) {
        configRef.update("content", content).await()
    }

    suspend fun updateServices(services: List<Service>) {
        configRef.update("services", services).await()
    }

    suspend fun updateContact(contact: Contact) {
        configRef.update("contact", contact).await()
    }

    suspend fun updateWorks(works: List<WorkItem>) {
        configRef.update("works", works).await()
    }

    suspend fun addWork(work: WorkItem) {
        val current = getConfig()?.works ?: emptyList()
        configRef.update("works", current + work).await()
    }

    suspend fun removeWork(workId: String) {
        val current = getConfig()?.works ?: emptyList()
        configRef.update("works", current.filter { it.id != workId }).await()
    }

    suspend fun toggleWorkVisibility(workId: String, visible: Boolean) {
        val current = getConfig()?.works ?: emptyList()
        configRef.update("works", current.map { if (it.id == workId) it.copy(visible = visible) else it }).await()
    }
}

class TestimonialRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val ref = db.collection("cheditz_testimonials")

    fun observeTestimonials(): Flow<List<Testimonial>> = callbackFlow {
        val reg = ref.addSnapshotListener { snap, error ->
            if (error != null) {
                Log.w(TAG, "Testimonials listener error (kept last state): ${error.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            val list = docs
                .mapNotNull { d -> d.safeParse<Testimonial>()?.copy(id = d.id) }
                .sortedByDescending { isoTime(it.date) }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun addTestimonial(t: Testimonial) {
        ref.add(t).await()
    }

    suspend fun deleteTestimonial(id: String) {
        ref.document(id).delete().await()
    }
}
