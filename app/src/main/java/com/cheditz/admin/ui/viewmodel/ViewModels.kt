package com.cheditz.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheditz.admin.data.model.*
import com.cheditz.admin.data.repository.ConfigRepository
import com.cheditz.admin.data.repository.OrderRepository
import com.cheditz.admin.data.repository.TestimonialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repo = OrderRepository()

    val orders: StateFlow<List<Order>> = repo.observeOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingOrders: StateFlow<List<Order>> = orders
        .map { list -> list.filter { it.isPending } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateStatus(orderId: String, status: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try { repo.updateStatus(orderId, status) } catch (_: Exception) {}
            onDone()
        }
    }

    fun deleteOrder(orderId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try { repo.deleteOrder(orderId) } catch (_: Exception) {}
            onDone()
        }
    }
}

class DashboardViewModel : ViewModel() {
    private val orderRepo = OrderRepository()

    val orders: StateFlow<List<Order>> = orderRepo.observeOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = orders
        .map { list -> list.count { it.isPending } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> = orders
        .map { list -> list.count { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = orders.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

class ConfigViewModel : ViewModel() {
    private val repo = ConfigRepository()

    val config: StateFlow<SiteConfig?> = repo.observeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateAccent(accent: String) = viewModelScope.launch { try { repo.updateAccent(accent) } catch (_: Exception) {} }
    fun updateContent(content: ContentBlock) = viewModelScope.launch { try { repo.updateContent(content) } catch (_: Exception) {} }
    fun updateServices(services: List<Service>) = viewModelScope.launch { try { repo.updateServices(services) } catch (_: Exception) {} }
    fun updateContact(contact: Contact) = viewModelScope.launch { try { repo.updateContact(contact) } catch (_: Exception) {} }
    fun removeWork(workId: String) = viewModelScope.launch { try { repo.removeWork(workId) } catch (_: Exception) {} }
    fun toggleWorkVisibility(workId: String, visible: Boolean) = viewModelScope.launch { try { repo.toggleWorkVisibility(workId, visible) } catch (_: Exception) {} }
}

class TestimonialViewModel : ViewModel() {
    private val repo = TestimonialRepository()

    val testimonials: StateFlow<List<Testimonial>> = repo.observeTestimonials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(id: String) = viewModelScope.launch { try { repo.deleteTestimonial(id) } catch (_: Exception) {} }
}
