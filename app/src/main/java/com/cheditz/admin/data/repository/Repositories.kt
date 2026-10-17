package com.cheditz.admin.data.repository

import com.cheditz.admin.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val ordersRef = db.collection("cheditz_orders")

    fun observeOrders(): Flow<List<Order>> = callbackFlow {
        val reg = ordersRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val orders = snap?.documents?.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { reg.remove() }
    }

    fun observePendingOrders(): Flow<List<Order>> = callbackFlow {
        val reg = ordersRef
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val orders = snap?.documents?.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { reg.remove() }
    }

    suspend fun getOrder(id: String): Order? =
        ordersRef.document(id).get().await().toObject(Order::class.java)?.copy(id = id)

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
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snap?.toObject(SiteConfig::class.java))
        }
        awaitClose { reg.remove() }
    }

    suspend fun getConfig(): SiteConfig? =
        configRef.get().await().toObject(SiteConfig::class.java)

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
        val reg = ref.orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(Testimonial::class.java)?.copy(id = it.id) } ?: emptyList()
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
