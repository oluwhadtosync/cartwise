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

class ApiClient(private var baseUrl: String = "https://cartwise-backend-kr8p.onrender.com") {

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
    suspend fun search(
        query: String,
        maxBudget: Double? = null,
        country: String = "NG",
        safetyCheck: Boolean = false,
        language: String? = null,
        model: String = "gemini-3.8-flash"
    ): SearchResponse =
        withContext(Dispatchers.IO) {
            val detectedLang = language ?: if (query.any { it in '\u4e00'..'\u9fa5' }) "zh" else "en"
            val body = json.encodeToString(SearchRequest.serializer(), SearchRequest(query, country, maxBudget, safetyCheck, detectedLang, model))
            val request = Request.Builder()
                .url("$baseUrl/api/search")
                .header("Accept-Language", if (detectedLang == "zh") "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7" else "en-US,en;q=0.9")
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
     * Extract full product details, varieties, and real pricing from any web link
     * using Kernel Cloud Browser and Gemini AI on the backend
     */
    suspend fun extractProductViaCloudBrowser(
        url: String,
        shippingAddress: ShippingAddress? = null
    ): Deal? = withContext(Dispatchers.IO) {
        val destCountry = shippingAddress?.country?.ifBlank { "NG" } ?: "NG"
        val destCity = shippingAddress?.city?.ifBlank { "Lagos" } ?: "Lagos"
        val reqBody = ExtractRequest(
            url = url.trim(),
            destinationCountry = destCountry,
            destinationCity = destCity,
            shippingAddress = shippingAddress
        )
        val bodyStr = json.encodeToString(ExtractRequest.serializer(), reqBody)

        val candidateUrls = listOf(
            "$baseUrl/api/extract",
            "http://127.0.0.1:3000/api/extract",
            "http://10.0.2.2:3000/api/extract"
        )

        for (endpoint in candidateUrls) {
            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .post(bodyStr.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val resStr = response.body?.string()
                    if (!resStr.isNullOrBlank()) {
                        val parsed = json.decodeFromString(ExtractResponse.serializer(), resStr)
                        if (parsed.deal != null) {
                            return@withContext parsed.deal
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue to next endpoint
            }
        }
        null
    }

    /**
     * Create Cartwise Order with PayPal AUTHORIZE intent
     */
    suspend fun createOrder(
        deal: Deal,
        shippingAddress: ShippingAddress? = null,
        billingAddress: BillingAddress? = null,
        selectedVariety: String? = null,
        quantity: Int = deal.quantity,
        effectiveItemPrice: Double? = null,
        effectiveShippingCost: Double? = null
    ): CartwiseOrder = withContext(Dispatchers.IO) {
        val qty = maxOf(1, quantity)
        val variety = selectedVariety?.ifBlank { null } ?: deal.selectedVarietyName.ifBlank { null }
        val titleWithVariety = if (!variety.isNullOrBlank()) {
            if (qty > 1) "${deal.title} (${variety}) x$qty" else "${deal.title} (${variety})"
        } else {
            if (qty > 1) "${deal.title} x$qty" else deal.title
        }
        val subtotal = effectiveItemPrice ?: (deal.price * qty)
        val shipping = effectiveShippingCost ?: deal.shippingCost

        val reqBody = CreateOrderRequest(
            itemTitle = titleWithVariety,
            itemUrl = deal.storeUrl,
            itemPrice = subtotal,
            shippingCost = shipping,
            merchantName = deal.storeName,
            shippingAddress = shippingAddress,
            billingAddress = billingAddress,
            selectedVariety = variety,
            quantity = qty
        )
        val body = json.encodeToString(CreateOrderRequest.serializer(), reqBody)
        val request = Request.Builder()
            .url("$baseUrl/api/orders")
            .header("User-Agent", "Cartwise-Android/1.0")
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

    /**
     * Fetch live product variants and stock from Cartwise Demo Market
     */
    suspend fun fetchLiveDemoProduct(productId: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://cartwise-demo-market.onrender.com/api/products/$productId")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch live metadata (og:title, og:image, og:price, title tag) from any external store URL (AliExpress, Amazon, etc.)
     */
    suspend fun fetchWebPageMeta(url: String): ExtractedWebMeta? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val stream = response.body?.byteStream() ?: return@withContext null
            val buffer = ByteArray(256 * 1024)
            val bytesRead = stream.read(buffer, 0, buffer.size)
            if (bytesRead <= 0) return@withContext null
            val html = String(buffer, 0, bytesRead, Charsets.UTF_8)

            fun extractMeta(prop: String): String? {
                val escaped = Regex.escape(prop)
                val pattern1 = Regex("""<meta\s+[^>]*?(?:property|name)=["'](?:og:|twitter:|product:|)$escaped["'][^>]*?content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                val m1 = pattern1.find(html)?.groupValues?.getOrNull(1)
                if (!m1.isNullOrBlank()) return decodeHtmlEntities(m1).trim()

                val pattern2 = Regex("""<meta\s+[^>]*?content=["']([^"']+)["'][^>]*?(?:property|name)=["'](?:og:|twitter:|product:|)$escaped["']""", RegexOption.IGNORE_CASE)
                val m2 = pattern2.find(html)?.groupValues?.getOrNull(1)
                if (!m2.isNullOrBlank()) return decodeHtmlEntities(m2).trim()

                return null
            }

            val metaTitle = extractMeta("title")
                ?: Regex("""<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)?.let { decodeHtmlEntities(it).trim() }
                ?: Regex(""""(?:name|subject|productTitle|itemTitle)":\s*"([^"]{4,120})"""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)?.let { decodeHtmlEntities(it).trim() }

            val metaImage = extractMeta("image")
                ?: Regex(""""(?:image|mainPic|imageUrl|picUrl)":\s*"([^"]+?\.(?:jpg|jpeg|png|webp)[^"]*)"""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)?.let { decodeHtmlEntities(it).trim() }

            val priceStr = extractMeta("price:amount")
                ?: extractMeta("price")
                ?: Regex(""""(?:price|actSkuMultiCurrencyDisplayPrice|minPrice|lowPrice)":\s*"?[A-Z$₦]*\s*([0-9]+\.?[0-9]*)"?""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)
                ?: Regex("""["']price["']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)

            val metaCurrency = extractMeta("price:currency") ?: "USD"
            val metaSiteName = extractMeta("site_name")
            val metaDesc = extractMeta("description")

            val parsedPrice = priceStr?.toDoubleOrNull()

            ExtractedWebMeta(
                title = metaTitle?.takeIf { it.isNotBlank() && it.length > 3 && !it.contains("AliExpress Shopping") },
                imageUrl = metaImage?.takeIf { it.isNotBlank() },
                price = parsedPrice,
                currency = metaCurrency,
                siteName = metaSiteName?.takeIf { it.isNotBlank() },
                description = metaDesc?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#x2F;", "/")
            .replace("&nbsp;", " ")
    }
}

@kotlinx.serialization.Serializable
data class ExtractedWebMeta(
    val title: String? = null,
    val imageUrl: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val siteName: String? = null,
    val description: String? = null
)
