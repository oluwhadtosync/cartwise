package com.cartwise.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private var baseUrl: String = "http://10.0.2.2:3000") {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun getBaseUrl(): String = baseUrl

    /**
     * Search products across stores with natural language query
     */
    suspend fun search(query: String, maxBudget: Double? = null, country: String = "NG"): SearchResponse =
        withContext(Dispatchers.IO) {
            val body = json.encodeToString(SearchRequest.serializer(), SearchRequest(query, country, maxBudget))
            val request = Request.Builder()
                .url("$baseUrl/api/search")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Search failed: HTTP ${response.code}")
            }
            val resStr = response.body?.string() ?: throw IOException("Empty search response")
            json.decodeFromString(SearchResponse.serializer(), resStr)
        }

    /**
     * Create Cartwise Order with PayPal AUTHORIZE intent
     */
    suspend fun createOrder(deal: Deal): CartwiseOrder = withContext(Dispatchers.IO) {
        val reqBody = CreateOrderRequest(
            itemTitle = deal.title,
            itemUrl = deal.storeUrl,
            itemPrice = deal.price,
            shippingCost = deal.shippingCost,
            merchantName = deal.storeName
        )
        val body = json.encodeToString(CreateOrderRequest.serializer(), reqBody)
        val request = Request.Builder()
            .url("$baseUrl/api/orders")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Create order failed: HTTP ${response.code}")
        }
        val resStr = response.body?.string() ?: throw IOException("Empty response")
        json.decodeFromString(CartwiseOrder.serializer(), resStr)
    }

    /**
     * Authorize order after buyer returns from PayPal checkout
     */
    suspend fun authorizeOrder(orderId: String): CartwiseOrder = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/orders/$orderId/authorize")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Authorize failed: HTTP ${response.code}")
        }
        val resStr = response.body?.string() ?: throw IOException("Empty response")
        json.decodeFromString(CartwiseOrder.serializer(), resStr)
    }

    /**
     * Start autonomous agent checkout
     */
    suspend fun startCheckout(orderId: String): Unit = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/checkout/$orderId/start")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Start checkout failed: HTTP ${response.code}")
        }
    }

    /**
     * Get order status and timeline logs
     */
    suspend fun getOrderStatus(orderId: String): OrderStatusResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/checkout/$orderId/status")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Get status failed: HTTP ${response.code}")
        }
        val resStr = response.body?.string() ?: throw IOException("Empty response")
        json.decodeFromString(OrderStatusResponse.serializer(), resStr)
    }

    /**
     * Connect to Server-Sent Events (SSE) stream for live agent updates
     */
    fun listenToAgentEvents(orderId: String, onEvent: (AgentSseEvent) -> Unit): EventSource {
        val request = Request.Builder()
            .url("$baseUrl/api/checkout/$orderId/stream")
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(client)
        return factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val sseEvent = json.decodeFromString(AgentSseEvent.serializer(), data)
                    onEvent(sseEvent)
                } catch (e: Exception) {
                    // ignore malformed ping events
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                // connection retry handled by client
            }
        })
    }
}
