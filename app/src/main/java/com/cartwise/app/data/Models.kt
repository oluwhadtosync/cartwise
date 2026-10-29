package com.cartwise.app.data

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String,
    val country: String = "NG",
    val maxBudget: Double? = null
)

@Serializable
data class Deal(
    val id: String,
    val title: String,
    val description: String = "",
    val brand: String = "",
    val category: String = "",
    val price: Double,
    val shippingCost: Double,
    val totalCost: Double,
    val currency: String = "USD",
    val storeName: String,
    val storeUrl: String,
    val imageUrl: String = "",
    val rating: Double = 4.8,
    val reviewsCount: Int = 100,
    val inStock: Boolean = true,
    val reasonWhy: String = "",
    val score: Int = 95
)

@Serializable
data class SearchResponse(
    val query: String,
    val deals: List<Deal>,
    val totalFound: Int = 0
)

@Serializable
data class CreateOrderRequest(
    val itemTitle: String,
    val itemUrl: String,
    val itemPrice: Double,
    val shippingCost: Double,
    val merchantName: String = "Demo Market"
)

@Serializable
data class ShippingAddress(
    val fullName: String = "Ada Lovelace",
    val email: String = "ada@cartwise.shopper",
    val address: String = "14 Marina Street",
    val city: String = "Lagos",
    val state: String = "Lagos",
    val country: String = "NG",
    val postalCode: String = "100001"
)

@Serializable
data class CartwiseOrder(
    val id: String,
    val userId: String = "user_default",
    val itemTitle: String,
    val itemUrl: String = "",
    val itemPrice: Double,
    val shippingCost: Double,
    val serviceFee: Double = 3.00,
    val totalAmount: Double,
    val currency: String = "USD",
    val merchantName: String = "Demo Market",
    val merchantOrderId: String? = null,
    val paypalOrderId: String? = null,
    val paypalAuthId: String? = null,
    val paypalCaptureId: String? = null,
    val paypalApproveUrl: String? = null,
    val status: String,
    val shippingAddress: ShippingAddress? = null,
    val liveViewUrl: String? = null,
    val failureReason: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class TimelineStep(
    val id: String = "",
    val orderId: String = "",
    val stepNumber: Int = 0,
    val stepName: String = "",
    val description: String = "",
    val status: String = "COMPLETED",
    val timestamp: String = ""
)

@Serializable
data class OrderStatusResponse(
    val orderId: String,
    val status: String,
    val liveViewUrl: String? = null,
    val merchantOrderId: String? = null,
    val paypalCaptureId: String? = null,
    val failureReason: String? = null,
    val timeline: List<TimelineStep> = emptyList()
)

@Serializable
data class AgentSseEvent(
    val type: String,
    val orderId: String? = null,
    val stepNumber: Int? = null,
    val stepName: String? = null,
    val description: String? = null,
    val status: String? = null,
    val liveViewUrl: String? = null,
    val merchantOrderId: String? = null,
    val captureId: String? = null,
    val reason: String? = null,
    val voided: Boolean? = null
)
