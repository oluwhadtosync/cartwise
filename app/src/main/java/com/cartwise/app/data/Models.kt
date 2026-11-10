package com.cartwise.app.data

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariety(
    val id: String = "",
    val name: String,
    val sku: String = "",
    val inStock: Boolean = true,
    val stockCount: Int = 10,
    val priceOffset: Double = 0.0
)

@Serializable
data class WebsiteSafety(
    val score: Int = 96,
    val level: String = "SAFE", // "SAFE" (Green), "CAUTION" (Yellow), "RISKY" (Red)
    val domain: String = "",
    val sslVerified: Boolean = true,
    val summary: String = "Verified merchant with encrypted checkout, high trust rating, and PayPal escrow support."
)

@Serializable
data class SearchRequest(
    val query: String,
    val country: String = "NG",
    val maxBudget: Double? = null,
    val safetyCheck: Boolean = false,
    val language: String? = null,
    val model: String = "gemini-3.8-flash"
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
    val stockCount: Int = 25,
    val reasonWhy: String = "",
    val score: Int = 95,
    val varieties: List<ProductVariety> = emptyList(),
    val selectedVarietyName: String = "",
    val quantity: Int = 1,
    val safety: WebsiteSafety? = null
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
    val merchantName: String = "Demo Market",
    val shippingAddress: ShippingAddress? = null,
    val billingAddress: BillingAddress? = null,
    val selectedVariety: String? = null,
    val quantity: Int = 1
)

@Serializable
data class ShippingAddress(
    val fullName: String = "Ada Lovelace",
    val email: String = "ada@cartwise.shopper",
    val address: String = "14 Marina Street, Lagos Island",
    val city: String = "Lagos",
    val state: String = "Lagos",
    val country: String = "Nigeria",
    val postalCode: String = "100001",
    val phone: String = "+234 801 234 5678"
)

@Serializable
data class BillingAddress(
    val cardholderName: String = "Ada Lovelace",
    val address: String = "14 Marina Street, Lagos Island",
    val city: String = "Lagos",
    val state: String = "Lagos",
    val country: String = "Nigeria",
    val postalCode: String = "100001",
    val sameAsShipping: Boolean = true
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
    val cardUsed: String = "123456*****2234",
    val shippingAddress: ShippingAddress? = null,
    val billingAddress: BillingAddress? = null,
    val selectedVariety: String? = null,
    val quantity: Int = 1,
    val websiteSafety: WebsiteSafety? = null,
    val liveViewUrl: String? = null,
    val failureReason: String? = null,
    val guestEmail: String? = null,
    val guestPhone: String? = null,
    val merchantPasswordRequired: Boolean = false,
    val merchantPassword: String? = null,
    val merchantRecoveryUrl: String? = null,
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
    val guestEmail: String? = null,
    val guestPhone: String? = null,
    val merchantPasswordRequired: Boolean? = null,
    val merchantPassword: String? = null,
    val merchantRecoveryUrl: String? = null,
    val reason: String? = null,
    val voided: Boolean? = null
)
