package com.cheditz.admin.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Order(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val service: String = "",
    val budget: String = "",
    val deadline: String = "",
    val description: String = "",
    val date: String = "",
    val status: String = "pending",
    val receiptNumber: String = "",
    val createdAt: Timestamp? = null
) {
    val isPending: Boolean get() = status.equals("pending", ignoreCase = true)
    val isCompleted: Boolean get() = status.equals("completed", ignoreCase = true)
    val isProcessing: Boolean get() = status.equals("processing", ignoreCase = true)
    val isCancelled: Boolean get() = status.equals("cancelled", ignoreCase = true)

    fun withStatus(newStatus: String): Map<String, Any> = mapOf("status" to newStatus)
}

data class Testimonial(
    @DocumentId val id: String = "",
    val name: String = "",
    val role: String = "",
    val rating: Int = 5,
    val text: String = "",
    val date: String = ""
)

data class WorkItem(
    val id: String = "",
    val title: String = "",
    val sub: String = "",
    val cat: String = "",
    val meta: String = "",
    val type: String = "image",
    val url: String = "",
    val visible: Boolean = true
)

data class Service(
    val name: String = "",
    val desc: String = "",
    val feats: List<String> = emptyList(),
    val price: String = ""
)

data class Contact(
    val email: String = "",
    val insta: String = "",
    val instaUrl: String = "",
    val whatsapp: String = "",
    val whatsappLabel: String = "",
    val whatsappText: String = "",
    val hours: String = "",
    val reply: String = ""
)

data class ContentBlock(
    val heroBadge: String = "",
    val heroHead: String = "",
    val heroSub: String = "",
    val worksTitle: String = "",
    val svcTitle: String = "",
    val orderTitle: String = "",
    val orderDesc: String = "",
    val contactHead: String = "",
    val contactDesc: String = ""
)

data class SiteConfig(
    val accent: String = "#bc6bff",
    val works: List<WorkItem> = emptyList(),
    val content: ContentBlock = ContentBlock(),
    val services: List<Service> = emptyList(),
    val contact: Contact = Contact(),
    val testimonials: List<Testimonial> = emptyList()
)
