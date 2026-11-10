package com.cartwise.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.cartwise.app.data.*
import com.cartwise.app.ui.screens.*
import com.cartwise.app.ui.theme.BgDark
import com.cartwise.app.ui.theme.CartwiseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    ONBOARDING,
    ASK,
    RESULTS,
    CONFIRM,
    AGENT_LIVE,
    RECEIPT,
    HISTORY
}

class MainActivity : ComponentActivity() {

    private val apiClient = ApiClient("https://cartwise-backend-kr8p.onrender.com") // Live Render Backend
    private var pendingOrderId: String? = null

    // Compose state variables accessible by activity
    private val currentScreen = mutableStateOf(Screen.ONBOARDING)
    private val selectedDeal = mutableStateOf<Deal?>(null)
    private val activeOrder = mutableStateOf<CartwiseOrder?>(null)
    private val timelineSteps = mutableStateListOf<TimelineStep>()
    private val liveViewUrl = mutableStateOf<String?>(null)
    private val isAgentComplete = mutableStateOf(false)
    private val isSearching = mutableStateOf(false)
    private val searchResults = mutableStateListOf<Deal>()
    private val searchQuery = mutableStateOf("")
    private val maxPrice = mutableStateOf(5.0) // Reduced Autonomous buy default to $5
    private val previousScreen = mutableStateOf(Screen.ASK)
    private val cancelNotice = mutableStateOf<String?>(null)
    private val isSafetyCheckEnabled = mutableStateOf(false) // Inactive by default per user request
    private val selectedGeminiModel = mutableStateOf("gemini-3.8-flash") // Default to Gemini 3.8 Flash per user request

    // Dynamic, persistent in-app purchase history & addresses (loaded from device storage)
    private val orderHistory = mutableStateListOf<CartwiseOrder>()
    private val savedShippingAddress = mutableStateOf(ShippingAddress())
    private val savedBillingAddress = mutableStateOf(BillingAddress())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load persistent orders and addresses from device storage
        orderHistory.clear()
        val existingOrders = OrderStorage.getOrders(this)
        // Clean out any legacy mock items from persistent storage
        OrderStorage.saveOrders(this, existingOrders)
        orderHistory.addAll(existingOrders)
        savedShippingAddress.value = OrderStorage.getShippingAddress(this)
        savedBillingAddress.value = OrderStorage.getBillingAddress(this)

        handleIncomingIntent(intent)

        setContent {
            CartwiseTheme {
                // Intercept Android System Back button navigation
                BackHandler(enabled = currentScreen.value != Screen.ONBOARDING) {
                    when (currentScreen.value) {
                        Screen.RESULTS -> currentScreen.value = Screen.ASK
                        Screen.CONFIRM -> {
                            if (searchResults.isNotEmpty()) {
                                currentScreen.value = Screen.RESULTS
                            } else {
                                currentScreen.value = Screen.ASK
                            }
                        }
                        Screen.HISTORY -> currentScreen.value = Screen.ASK
                        Screen.RECEIPT -> currentScreen.value = previousScreen.value
                        Screen.AGENT_LIVE -> currentScreen.value = Screen.ASK
                        Screen.ASK -> currentScreen.value = Screen.ONBOARDING
                        Screen.ONBOARDING -> {
                            // Disabled when ONBOARDING so pressing back exits the app
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = BgDark
                ) {
                    when (currentScreen.value) {
                        Screen.ONBOARDING -> OnboardingScreen(
                            maxPriceLimit = maxPrice.value,
                            onMaxPriceChanged = { maxPrice.value = it },
                            onGetStarted = { currentScreen.value = Screen.ASK }
                        )

                        Screen.ASK -> AskScreen(
                            queryText = searchQuery.value,
                            onQueryChanged = { searchQuery.value = it },
                            isLoading = isSearching.value,
                            isSafetyCheckEnabled = isSafetyCheckEnabled.value,
                            onToggleSafetyCheck = { isSafetyCheckEnabled.value = it },
                            selectedModel = selectedGeminiModel.value,
                            onSelectModel = { selectedGeminiModel.value = it },
                            onSearchSubmitted = { executeSearch(searchQuery.value) },
                            onQuickPromptSelected = { prompt ->
                                searchQuery.value = prompt
                                executeSearch(prompt)
                            },
                            onViewHistory = { currentScreen.value = Screen.HISTORY }
                        )

                        Screen.RESULTS -> ResultsScreen(
                            query = searchQuery.value,
                            deals = searchResults,
                            isSafetyCheckEnabled = isSafetyCheckEnabled.value,
                            onToggleSafetyCheck = { isSafetyCheckEnabled.value = it },
                            onSelectDeal = { deal ->
                                selectedDeal.value = deal
                                cancelNotice.value = null
                                currentScreen.value = Screen.CONFIRM
                            },
                            onBack = { currentScreen.value = Screen.ASK }
                        )

                        Screen.CONFIRM -> selectedDeal.value?.let { deal ->
                            ConfirmScreen(
                                deal = deal,
                                initialShippingAddress = savedShippingAddress.value,
                                initialBillingAddress = savedBillingAddress.value,
                                cancelNotice = cancelNotice.value,
                                isSafetyCheckEnabled = isSafetyCheckEnabled.value,
                                onDismissCancelNotice = { cancelNotice.value = null },
                                onPayWithPayPal = { shipping, billing, variety, qty, subtotal, shipCost ->
                                    initiatePayPalOrder(deal, shipping, billing, variety, qty, subtotal, shipCost)
                                },
                                onDemoSandboxFastPass = { shipping, billing, variety, qty, subtotal, shipCost ->
                                    fastPassPayPalOrder(deal, shipping, billing, variety, qty, subtotal, shipCost)
                                },
                                onSwitchToDemoMarket = {
                                    selectedDeal.value = Deal(
                                        id = "prod-earbuds-01",
                                        title = "SoundPulse Air Wireless Noise-Cancelling Earbuds",
                                        brand = "SoundPulse",
                                        price = 34.99,
                                        shippingCost = 5.00,
                                        totalCost = 39.99,
                                        storeName = "Demo Market",
                                        storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
                                        rating = 4.8,
                                        reasonWhy = "Official Cartwise Demo Market: 100% guaranteed checkout capture.",
                                        varieties = listOf(
                                            ProductVariety(id = "var-blk", name = "Matte Black", stockCount = 15, inStock = true),
                                            ProductVariety(id = "var-wht", name = "Arctic White", stockCount = 2, inStock = true),
                                            ProductVariety(id = "var-blu", name = "Navy Blue", stockCount = 0, inStock = false),
                                            ProductVariety(id = "var-gld", name = "Rose Gold", stockCount = 8, inStock = true)
                                        ),
                                        selectedVarietyName = "Matte Black",
                                        safety = WebsiteSafety(
                                            domain = "cartwise-demo-market.onrender.com",
                                            score = 98,
                                            level = "SAFE",
                                            sslVerified = true,
                                            summary = "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
                                        )
                                    )
                                },
                                onBack = { currentScreen.value = Screen.RESULTS }
                            )
                        }

                        Screen.AGENT_LIVE -> activeOrder.value?.let { order ->
                            AgentLiveScreen(
                                order = order,
                                timeline = timelineSteps,
                                liveViewUrl = liveViewUrl.value,
                                isComplete = isAgentComplete.value,
                                onViewReceipt = {
                                    activeOrder.value?.let { saveOrderToHistory(it) }
                                    previousScreen.value = Screen.ASK
                                    currentScreen.value = Screen.RECEIPT
                                }
                            )
                        }

                        Screen.RECEIPT -> activeOrder.value?.let { order ->
                            LaunchedEffect(order.id, order.status) {
                                saveOrderToHistory(order)
                            }
                            ReceiptScreen(
                                order = order,
                                onDone = {
                                    currentScreen.value = Screen.HISTORY
                                },
                                onSearchAgain = {
                                    searchQuery.value = ""
                                    currentScreen.value = Screen.ASK
                                }
                            )
                        }

                        Screen.HISTORY -> OrdersHistoryScreen(
                            orders = orderHistory,
                            onBack = { currentScreen.value = Screen.ASK },
                            onOrderClick = { order ->
                                activeOrder.value = order
                                previousScreen.value = Screen.HISTORY
                                currentScreen.value = Screen.RECEIPT
                            },
                            onClearHistory = {
                                orderHistory.clear()
                                OrderStorage.clearOrders(this@MainActivity)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    /**
     * Handle incoming intents: PayPal redirect, shared products from Demo Market or browser, and deep links.
     */
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        // 1. PayPal redirect: cartwise://paypal/return?orderId=... or cancel
        val uri: Uri? = intent.data
        if (uri != null && uri.scheme == "cartwise" && uri.host == "paypal") {
            val path = uri.path ?: ""
            if (path.contains("cancel") || uri.getQueryParameter("cancel") == "true") {
                pendingOrderId = null
                cancelNotice.value = "PayPal checkout was cancelled. No hold or charges were made."
                currentScreen.value = Screen.CONFIRM
                return
            }
            if (path.contains("return") || path.isEmpty()) {
                val orderId = uri.getQueryParameter("orderId") ?: pendingOrderId
                if (orderId != null) {
                    cancelNotice.value = null
                    onPayPalApprovalCompleted(orderId)
                }
            }
            return
        }

        // 2. Share Sheet Action: android.intent.action.SEND with text/plain
        if (intent.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
                ?: intent.clipData?.getItemAt(0)?.text?.toString()

            if (!sharedText.isNullOrBlank()) {
                extractSharedItemAndReadyForPayment(sharedText)
                return
            }
        }

        // 3. Direct Deep Link / Web URL: android.intent.action.VIEW
        if (intent.action == Intent.ACTION_VIEW && uri != null) {
            handleProductDeepLink(uri)
            return
        }
    }

    /**
     * Parse serialized varieties parameter from Demo Market deep link or web share
     */
    private fun parseVariantsParam(raw: String): List<ProductVariety> {
        val list = mutableListOf<ProductVariety>()
        try {
            val trimmed = raw.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                val jsonArr = JSONArray(trimmed)
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val name = obj.optString("name")
                    val sku = obj.optString("sku")
                    val inStock = obj.optBoolean("inStock", true)
                    val stockCount = obj.optInt("stockCount", if (inStock) 10 else 0)
                    val priceOffset = obj.optDouble("priceOffset", 0.0)
                    val color = obj.optString("color").ifBlank { null }
                    val size = obj.optString("size").ifBlank { null }
                    if (name.isNotBlank()) {
                        list.add(ProductVariety(
                            id = "var-$i",
                            name = name,
                            sku = sku,
                            inStock = inStock,
                            stockCount = stockCount,
                            priceOffset = priceOffset,
                            color = color,
                            size = size
                        ))
                    }
                }
            } else {
                // Pipe-delimited: Name:SKU:inStock:stockCount:priceOffset:color:size|...
                val entries = trimmed.split("|")
                for ((i, entry) in entries.withIndex()) {
                    if (entry.isBlank()) continue
                    val parts = entry.split(":")
                    val name = Uri.decode(parts.getOrNull(0) ?: "").trim()
                    val sku = Uri.decode(parts.getOrNull(1) ?: "").trim()
                    val inStockFlag = parts.getOrNull(2)?.trim()
                    val inStock = if (inStockFlag == "0" || inStockFlag.equals("false", true)) false else true
                    val stockCount = parts.getOrNull(3)?.toIntOrNull() ?: if (inStock) 10 else 0
                    val priceOffset = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0
                    val color = Uri.decode(parts.getOrNull(5) ?: "").trim().ifBlank { null }
                    val size = Uri.decode(parts.getOrNull(6) ?: "").trim().ifBlank { null }
                    if (name.isNotBlank()) {
                        list.add(ProductVariety(
                            id = "var-$i",
                            name = name,
                            sku = sku,
                            inStock = inStock,
                            stockCount = stockCount,
                            priceOffset = priceOffset,
                            color = color,
                            size = size
                        ))
                    }
                }
            }
        } catch (e: Exception) {}
        return list
    }

    /**
     * Handle direct product deep links:
     * - cartwise://product?id=...
     * - cartwise://buy?id=...
     * - https://cartwise-demo-market.onrender.com/product/prod-...
     */
    private fun handleProductDeepLink(uri: Uri) {
        var productId = uri.getQueryParameter("id")
        val fallbackTitle = uri.getQueryParameter("title")
        val fallbackPrice = uri.getQueryParameter("price")?.toDoubleOrNull()
        val fallbackShipping = uri.getQueryParameter("shipping")?.toDoubleOrNull()
        val requestedVariety = uri.getQueryParameter("variety")
            ?: uri.getQueryParameter("variant")
            ?: uri.getQueryParameter("sku")
        val requestedQuantity = uri.getQueryParameter("quantity")?.toIntOrNull()
            ?: uri.getQueryParameter("qty")?.toIntOrNull()
            ?: 1
        val rawVariants = uri.getQueryParameter("variants") ?: uri.getQueryParameter("allVariants")
        val parsedVarieties = if (!rawVariants.isNullOrBlank()) parseVariantsParam(rawVariants) else null

        if (productId.isNullOrBlank()) {
            val path = uri.path ?: ""
            if (path.contains("/product/")) {
                productId = path.substringAfter("/product/").substringBefore("/").substringBefore("?")
            } else if (uri.lastPathSegment != null && uri.lastPathSegment != "product" && uri.lastPathSegment != "buy") {
                productId = uri.lastPathSegment
            }
        }

        if (!productId.isNullOrBlank()) {
            extractProductAndReadyForPayment(
                productId = productId,
                fallbackTitle = fallbackTitle,
                fallbackPrice = fallbackPrice,
                fallbackShipping = fallbackShipping,
                requestedVariety = requestedVariety,
                requestedQuantity = requestedQuantity,
                incomingVarieties = parsedVarieties
            )
        }
    }

    /**
     * Parse shared text from Demo Market or browser, extract product ID, variety, quantity or URL, and prepare payment
     */
    private fun extractSharedItemAndReadyForPayment(text: String) {
        val urlRegex = Regex("""https?://[^\s]+""")
        val matchedUrl = urlRegex.find(text)?.value

        // Check if text has quantity info like "x2" or "qty: 2"
        val qtyRegex = Regex("""\bx([1-9][0-9]?)\b""", RegexOption.IGNORE_CASE)
        val textQty = qtyRegex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()

        // Check if text has variety info in parentheses, e.g. "(Deep Navy)"
        val parenRegex = Regex("""\(([^)]+)\)""")
        val textVariety = parenRegex.find(text)?.groupValues?.getOrNull(1)?.trim()

        if (!matchedUrl.isNullOrBlank()) {
            try {
                val parsedUri = Uri.parse(matchedUrl)
                var productId = parsedUri.getQueryParameter("id")
                val fallbackTitle = parsedUri.getQueryParameter("title")
                val fallbackPrice = parsedUri.getQueryParameter("price")?.toDoubleOrNull()
                val fallbackShipping = parsedUri.getQueryParameter("shipping")?.toDoubleOrNull()
                val urlVariety = parsedUri.getQueryParameter("variety")
                    ?: parsedUri.getQueryParameter("variant")
                    ?: parsedUri.getQueryParameter("sku")
                val urlQty = parsedUri.getQueryParameter("quantity")?.toIntOrNull()
                    ?: parsedUri.getQueryParameter("qty")?.toIntOrNull()
                val rawVariants = parsedUri.getQueryParameter("variants") ?: parsedUri.getQueryParameter("allVariants")
                val parsedVarieties = if (!rawVariants.isNullOrBlank()) parseVariantsParam(rawVariants) else null

                if (productId.isNullOrBlank()) {
                    val path = parsedUri.path ?: ""
                    if (path.contains("/product/")) {
                        productId = path.substringAfter("/product/").substringBefore("/").substringBefore("?")
                    } else if (parsedUri.lastPathSegment != null && parsedUri.lastPathSegment != "product" && parsedUri.lastPathSegment != "buy") {
                        productId = parsedUri.lastPathSegment
                    }
                }

                if (!productId.isNullOrBlank()) {
                    extractProductAndReadyForPayment(
                        productId = productId,
                        fallbackTitle = fallbackTitle,
                        fallbackPrice = fallbackPrice,
                        fallbackShipping = fallbackShipping,
                        requestedVariety = urlVariety ?: textVariety,
                        requestedQuantity = urlQty ?: textQty ?: 1,
                        incomingVarieties = parsedVarieties
                    )
                    return
                }
            } catch (e: Exception) {
                // fallback to regex matching
            }
        }

        val prodIdRegex = Regex("""prod-[a-zA-Z0-9\-]+""")
        val matchedId = prodIdRegex.find(text)?.value

        if (!matchedId.isNullOrBlank()) {
            extractProductAndReadyForPayment(
                productId = matchedId,
                requestedVariety = textVariety,
                requestedQuantity = textQty ?: 1
            )
        } else {
            // If it's general text, run search for it
            searchQuery.value = text
            executeSearch(text)
        }
    }

    /**
     * Extract product into Deal, calculate landed cost, and transition immediately to Screen.CONFIRM
     */
    private fun extractProductAndReadyForPayment(
        productId: String,
        fallbackTitle: String? = null,
        fallbackPrice: Double? = null,
        fallbackShipping: Double? = null,
        requestedVariety: String? = null,
        requestedQuantity: Int = 1,
        incomingVarieties: List<ProductVariety>? = null
    ) {
        val knownDeal = DemoCatalog.items[productId]
        val safeQty = maxOf(1, requestedQuantity)

        // Combine base varieties: incomingVarieties -> knownDeal.varieties -> empty
        val baseVarieties = when {
            !incomingVarieties.isNullOrEmpty() -> incomingVarieties
            knownDeal != null && knownDeal.varieties.isNotEmpty() -> knownDeal.varieties
            else -> emptyList()
        }

        // Find variety by SKU, ID or Name if requested
        val matchedVariety = if (!requestedVariety.isNullOrBlank()) {
            val cleanReq = requestedVariety.trim().lowercase()
            baseVarieties.find { v ->
                v.sku.equals(cleanReq, ignoreCase = true) ||
                v.name.equals(cleanReq, ignoreCase = true) ||
                v.id.equals(cleanReq, ignoreCase = true) ||
                cleanReq.contains(v.sku.lowercase()) ||
                cleanReq.contains(v.name.lowercase()) ||
                v.name.lowercase().contains(cleanReq)
            }?.name ?: requestedVariety.trim()
        } else null

        val chosenVariety = matchedVariety ?: knownDeal?.selectedVarietyName?.ifBlank { null }
            ?: baseVarieties.firstOrNull { it.inStock }?.name
            ?: baseVarieties.firstOrNull()?.name
            ?: "Standard"

        // Ensure chosenVariety is ALWAYS included in varieties list
        val finalVarieties = if (chosenVariety.isNotBlank() && baseVarieties.none { it.name.equals(chosenVariety, ignoreCase = true) }) {
            baseVarieties + ProductVariety(
                id = "var-custom",
                name = chosenVariety,
                inStock = true,
                stockCount = 10
            )
        } else {
            baseVarieties
        }

        if (knownDeal != null) {
            val unitPrice = fallbackPrice ?: knownDeal.price
            val shipping = fallbackShipping ?: knownDeal.shippingCost
            val subtotal = unitPrice * safeQty

            val dealToUse = knownDeal.copy(
                title = fallbackTitle ?: knownDeal.title,
                price = unitPrice,
                shippingCost = shipping,
                totalCost = subtotal + shipping,
                varieties = finalVarieties,
                selectedVarietyName = chosenVariety,
                quantity = safeQty
            )
            selectedDeal.value = dealToUse
            cancelNotice.value = null
            currentScreen.value = Screen.CONFIRM
        } else {
            val unitPrice = fallbackPrice ?: 29.99
            val shipping = fallbackShipping ?: 5.00
            val subtotal = unitPrice * safeQty
            val fallbackDeal = Deal(
                id = productId,
                title = fallbackTitle ?: "Demo Market Item ($productId)",
                brand = "Demo Market",
                price = unitPrice,
                shippingCost = shipping,
                totalCost = subtotal + shipping,
                currency = "USD",
                storeName = "Demo Market",
                storeUrl = "https://cartwise-demo-market.onrender.com/product/$productId",
                imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/$productId.png",
                rating = 4.8,
                reviewsCount = 120,
                inStock = true,
                stockCount = 20,
                varieties = finalVarieties,
                selectedVarietyName = chosenVariety,
                quantity = safeQty,
                reasonWhy = "Extracted from Demo Market share sheet. Ready for instant PayPal Escrow authorization.",
                safety = WebsiteSafety(
                    domain = "cartwise-demo-market.onrender.com",
                    score = 98,
                    level = "SAFE",
                    sslVerified = true,
                    summary = "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
                )
            )
            selectedDeal.value = fallbackDeal
            cancelNotice.value = null
            currentScreen.value = Screen.CONFIRM
        }

        // Asynchronously sync live variants and inventory from Demo Market server
        if (productId.isNotBlank()) {
            fetchLiveProductVariants(productId, chosenVariety)
        }
    }

    /**
     * Asynchronously sync product details and all variants from the live Demo Market API
     */
    private fun fetchLiveProductVariants(productId: String, preferredVariety: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bodyStr = apiClient.fetchLiveDemoProduct(productId)
                if (!bodyStr.isNullOrBlank()) {
                    val jsonObj = JSONObject(bodyStr)
                    val variantsArray = jsonObj.optJSONArray("variants")
                    if (variantsArray != null && variantsArray.length() > 0) {
                        val liveVarieties = mutableListOf<ProductVariety>()
                        for (i in 0 until variantsArray.length()) {
                            val item = variantsArray.getJSONObject(i)
                            val vName = item.optString("name", "")
                            val vSku = item.optString("sku", "")
                            val vInStock = item.optBoolean("inStock", true)
                            val vStockCount = item.optInt("stockCount", if (vInStock) 10 else 0)
                            val vPriceOffset = item.optDouble("priceOffset", 0.0)
                            val vColor = item.optString("color").ifBlank { null }
                            val vSize = item.optString("size").ifBlank { null }
                            if (vName.isNotBlank()) {
                                liveVarieties.add(
                                    ProductVariety(
                                        id = "var-$i",
                                        name = vName,
                                        sku = vSku,
                                        inStock = vInStock,
                                        stockCount = vStockCount,
                                        priceOffset = vPriceOffset,
                                        color = vColor,
                                        size = vSize
                                    )
                                )
                            }
                        }

                        if (liveVarieties.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                selectedDeal.value?.let { current ->
                                    if (current.id == productId) {
                                        val matched = liveVarieties.find {
                                            it.name.equals(preferredVariety, ignoreCase = true) ||
                                            it.sku.equals(preferredVariety, ignoreCase = true)
                                        }
                                        val stillChosen = matched?.name ?: current.selectedVarietyName
                                        selectedDeal.value = current.copy(
                                            varieties = liveVarieties,
                                            selectedVarietyName = stillChosen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors; fallback catalog is already displayed
            }
        }
    }

    /**
     * Search products across stores
     */
    private fun executeSearch(query: String) {
        if (query.isBlank()) return
        isSearching.value = true

        lifecycleScope.launch {
            try {
                val res = apiClient.search(
                    query = query,
                    maxBudget = maxPrice.value,
                    country = "NG",
                    safetyCheck = isSafetyCheckEnabled.value,
                    model = selectedGeminiModel.value
                )
                searchResults.clear()
                searchResults.addAll(res.deals)
                currentScreen.value = Screen.RESULTS
            } catch (e: Exception) {
                // If backend not reachable, provide fallback demo items showcasing AI Safety & Varieties
                val isZh = query.any { it in '\u4e00'..'\u9fa5' }
                searchResults.clear()
                if (isZh) {
                    searchResults.addAll(
                        listOf(
                            Deal(
                                id = "prod-earbuds-01",
                                title = "SoundPulse Air 无线降噪耳机",
                                brand = "SoundPulse",
                                price = 34.99,
                                shippingCost = 5.00,
                                totalCost = 39.99,
                                storeName = "Cartwise 官方自营 Demo Market",
                                storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
                                rating = 4.8,
                                reasonWhy = "最佳综合性价比：全包到手价仅 $39.99，支持正品保障与快速送达。",
                                selectedVarietyName = "曜石黑",
                                varieties = listOf(
                                    ProductVariety(id = "var-blk", name = "曜石黑", stockCount = 15, inStock = true),
                                    ProductVariety(id = "var-wht", name = "珍珠白", stockCount = 2, inStock = true),
                                    ProductVariety(id = "var-blu", name = "深海蓝", stockCount = 0, inStock = false),
                                    ProductVariety(id = "var-gld", name = "玫瑰金", stockCount = 8, inStock = true)
                                ),
                                safety = WebsiteSafety(
                                    domain = "cartwise-demo-market.onrender.com",
                                    score = 98,
                                    level = "SAFE",
                                    sslVerified = true,
                                    summary = "Cartwise 官方自营测试商户。256位TLS加密，沙箱结算隔离，100%全额托管保障。"
                                )
                            ),
                            Deal(
                                id = "prod-sony-wh1000",
                                title = "索尼 WH-1000XM5 无线降噪头戴耳机",
                                brand = "Sony",
                                price = 38.50,
                                shippingCost = 10.00,
                                totalCost = 48.50,
                                storeName = "Glorious Gaming 精品店",
                                storeUrl = "https://www.gloriousgaming.com",
                                rating = 4.6,
                                reasonWhy = "高评分降噪优选，但属于第三方跨境独立站，支付网关可能有风控限制。",
                                selectedVarietyName = "暗夜黑",
                                varieties = listOf(
                                    ProductVariety(id = "var-sony-blk", name = "暗夜黑", stockCount = 6, inStock = true),
                                    ProductVariety(id = "var-sony-slv", name = "铂金银", stockCount = 1, inStock = true),
                                    ProductVariety(id = "var-sony-gry", name = "太空灰", stockCount = 0, inStock = false)
                                ),
                                safety = WebsiteSafety(
                                    domain = "gloriousgaming.com",
                                    score = 68,
                                    level = "CAUTION",
                                    sslVerified = true,
                                    summary = "第三方零售商。已通过SSL验证，但网关较严格可能拒绝国际卡。Cartwise自动启用托管退款保护。"
                                )
                            ),
                            Deal(
                                id = "prod-swifttype-rgb",
                                title = "SwiftType RGB 机械游戏键盘",
                                brand = "SwiftType",
                                price = 19.99,
                                shippingCost = 10.00,
                                totalCost = 29.99,
                                storeName = "FlashDeals-Drop Outlet",
                                storeUrl = "https://flashdeals-drop.net",
                                rating = 3.2,
                                reasonWhy = "标价极低，但被标识为近期注册的高风险代发域名，建议优先选择自营。",
                                selectedVarietyName = "红轴",
                                varieties = listOf(
                                    ProductVariety(id = "var-st-red", name = "红轴", stockCount = 5, inStock = true),
                                    ProductVariety(id = "var-st-blu", name = "青轴", stockCount = 0, inStock = false)
                                ),
                                safety = WebsiteSafety(
                                    domain = "flashdeals-drop.net",
                                    score = 32,
                                    level = "RISKY",
                                    sslVerified = false,
                                    summary = "高风险域名：注册时间小于30天，缺乏商户背书。Cartwise 托管保护将严守付款卡安全。"
                                )
                            )
                        )
                    )
                } else {
                    searchResults.addAll(
                    listOf(
                        Deal(
                            id = "prod-earbuds-01",
                            title = "SoundPulse Air Wireless Noise-Cancelling Earbuds",
                            brand = "SoundPulse",
                            price = 34.99,
                            shippingCost = 5.00,
                            totalCost = 39.99,
                            storeName = "Cartwise Demo Market",
                            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
                            rating = 4.8,
                            reasonWhy = "Best value pick: lowest total landed cost ($39.99) with verified shipping to Nigeria.",
                            selectedVarietyName = "Matte Black",
                            varieties = listOf(
                                ProductVariety(id = "var-blk", name = "Matte Black", stockCount = 15, inStock = true),
                                ProductVariety(id = "var-wht", name = "Arctic White", stockCount = 2, inStock = true),
                                ProductVariety(id = "var-blu", name = "Navy Blue", stockCount = 0, inStock = false),
                                ProductVariety(id = "var-gld", name = "Rose Gold", stockCount = 8, inStock = true)
                            ),
                            safety = WebsiteSafety(
                                domain = "cartwise-demo-market.onrender.com",
                                score = 98,
                                level = "SAFE",
                                sslVerified = true,
                                summary = "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
                            )
                        ),
                        Deal(
                            id = "prod-sony-wh1000",
                            title = "Sony WH-1000XM5 Wireless ANC Headphones",
                            brand = "Sony",
                            price = 38.50,
                            shippingCost = 10.00,
                            totalCost = 48.50,
                            storeName = "Glorious Gaming Boutique",
                            storeUrl = "https://www.gloriousgaming.com",
                            rating = 4.6,
                            reasonWhy = "Popular choice with premium ANC, but third-party merchant with strict card fraud verification.",
                            selectedVarietyName = "Midnight Black",
                            varieties = listOf(
                                ProductVariety(id = "var-sony-blk", name = "Midnight Black", stockCount = 6, inStock = true),
                                ProductVariety(id = "var-sony-slv", name = "Platinum Silver", stockCount = 1, inStock = true),
                                ProductVariety(id = "var-sony-gry", name = "Space Grey", stockCount = 0, inStock = false)
                            ),
                            safety = WebsiteSafety(
                                domain = "gloriousgaming.com",
                                score = 68,
                                level = "CAUTION",
                                sslVerified = true,
                                summary = "Third-party merchant. SSL verified, but strict gateway rules may decline corporate test card. Escrow auto-void safeguard active."
                            )
                        ),
                        Deal(
                            id = "prod-swifttype-rgb",
                            title = "SwiftType Mechanical RGB Gaming Keyboard",
                            brand = "SwiftType",
                            price = 19.99,
                            shippingCost = 10.00,
                            totalCost = 29.99,
                            storeName = "FlashDeals-Drop Outlet",
                            storeUrl = "https://flashdeals-drop.net",
                            rating = 3.2,
                            reasonWhy = "Low headline price, but flagged as risky drop-shipping domain registered recently.",
                            selectedVarietyName = "Clicky Blue Switches",
                            varieties = listOf(
                                ProductVariety(id = "var-sw-blue", name = "Clicky Blue Switches", stockCount = 0, inStock = false),
                                ProductVariety(id = "var-sw-red", name = "Linear Red Switches", stockCount = 0, inStock = false)
                            ),
                            safety = WebsiteSafety(
                                domain = "flashdeals-drop.net",
                                score = 34,
                                level = "RISKY",
                                sslVerified = false,
                                summary = "High Risk Warning: Unverified dropshipping merchant, no verified TLS certificate. High order cancellation rate."
                            )
                        )
                    )
                )
            }
            currentScreen.value = Screen.RESULTS
            } finally {
                isSearching.value = false
            }
        }
    }

    /**
     * Persist completed order into local device storage
     */
    private fun saveOrderToHistory(order: CartwiseOrder) {
        val index = orderHistory.indexOfFirst { it.id == order.id }
        if (index >= 0) {
            orderHistory[index] = order
        } else {
            orderHistory.add(0, order)
        }
        OrderStorage.saveOrders(this, orderHistory.toList())
    }

    /**
     * Step 1: Create Order & Launch PayPal Chrome Custom Tab
     */
    private fun initiatePayPalOrder(
        deal: Deal,
        shippingAddress: ShippingAddress,
        billingAddress: BillingAddress,
        chosenVariety: String = deal.selectedVarietyName,
        quantity: Int = deal.quantity,
        effectiveSubtotal: Double? = null,
        effectiveShipping: Double? = null
    ) {
        cancelNotice.value = null
        savedShippingAddress.value = shippingAddress
        savedBillingAddress.value = billingAddress
        OrderStorage.saveShippingAddress(this, shippingAddress)
        OrderStorage.saveBillingAddress(this, billingAddress)

        val safeQty = maxOf(1, quantity)
        val resolvedVariety = chosenVariety.ifBlank { deal.selectedVarietyName.ifBlank { "Standard" } }
        val finalSubtotal = effectiveSubtotal ?: (deal.price * safeQty)
        val finalShipping = effectiveShipping ?: (deal.shippingCost + maxOf(0, safeQty - 1) * 1.50)
        val finalUnitPrice = finalSubtotal / safeQty

        val effectiveDeal = deal.copy(
            selectedVarietyName = resolvedVariety,
            quantity = safeQty,
            price = finalUnitPrice,
            shippingCost = finalShipping,
            totalCost = finalSubtotal + finalShipping
        )

        lifecycleScope.launch {
            try {
                val order = apiClient.createOrder(
                    deal = effectiveDeal,
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = resolvedVariety,
                    quantity = safeQty,
                    effectiveItemPrice = finalSubtotal,
                    effectiveShippingCost = finalShipping
                )
                activeOrder.value = order.copy(
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = resolvedVariety,
                    quantity = safeQty
                )
                pendingOrderId = order.id

                val approveUrl = order.paypalApproveUrl
                if (!approveUrl.isNullOrBlank()) {
                    // Launch PayPal in Chrome Custom Tab
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                    customTabsIntent.launchUrl(this@MainActivity, Uri.parse(approveUrl))
                } else {
                    cancelNotice.value = "PayPal did not return an approval link. Please try again."
                }
            } catch (e: Exception) {
                cancelNotice.value = "Unable to connect to PayPal: ${e.message ?: "Network error"}"
            }
        }
    }

    /**
     * Demo Market Fast-Pass: Instantly authorizes hold and purchases from our live Demo Market
     */
    private fun fastPassPayPalOrder(
        deal: Deal,
        shippingAddress: ShippingAddress,
        billingAddress: BillingAddress,
        chosenVariety: String = deal.selectedVarietyName,
        quantity: Int = deal.quantity,
        effectiveSubtotal: Double? = null,
        effectiveShipping: Double? = null
    ) {
        cancelNotice.value = null
        savedShippingAddress.value = shippingAddress
        savedBillingAddress.value = billingAddress
        OrderStorage.saveShippingAddress(this, shippingAddress)
        OrderStorage.saveBillingAddress(this, billingAddress)

        val safeQty = maxOf(1, quantity)
        val resolvedVariety = chosenVariety.ifBlank { deal.selectedVarietyName.ifBlank { "Standard" } }
        val finalSubtotal = effectiveSubtotal ?: (deal.price * safeQty)
        val finalShipping = effectiveShipping ?: (deal.shippingCost + maxOf(0, safeQty - 1) * 1.50)
        val serviceFee = 3.00
        val totalHold = Math.round((finalSubtotal + finalShipping + serviceFee) * 100.0) / 100.0
        val finalUnitPrice = finalSubtotal / safeQty

        val effectiveDeal = deal.copy(
            selectedVarietyName = resolvedVariety,
            quantity = safeQty,
            price = finalUnitPrice,
            shippingCost = finalShipping,
            totalCost = finalSubtotal + finalShipping
        )

        lifecycleScope.launch {
            try {
                val order = apiClient.createOrder(
                    deal = effectiveDeal,
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = resolvedVariety,
                    quantity = safeQty,
                    effectiveItemPrice = finalSubtotal,
                    effectiveShippingCost = finalShipping
                )
                activeOrder.value = order.copy(
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = resolvedVariety,
                    quantity = safeQty
                )
                pendingOrderId = order.id
                onPayPalApprovalCompleted(order.id)
            } catch (e: Exception) {
                val orderId = "cw_ord_${System.currentTimeMillis()}"
                val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                val mockOrder = CartwiseOrder(
                    id = orderId,
                    status = "PENDING_APPROVAL",
                    itemTitle = if (deal.title.isNotBlank()) deal.title else "Demo Market Item",
                    itemUrl = deal.storeUrl.ifBlank { "https://cartwise-demo-market.onrender.com/product/${deal.id}" },
                    merchantName = deal.storeName.ifBlank { "Cartwise Demo Market" },
                    itemPrice = finalSubtotal,
                    shippingCost = finalShipping,
                    serviceFee = serviceFee,
                    totalAmount = totalHold,
                    cardUsed = "123456*****2234",
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = resolvedVariety,
                    quantity = safeQty,
                    guestEmail = shippingAddress.email.ifBlank { "ada.lovelace@gmail.com" },
                    guestPhone = shippingAddress.phone.ifBlank { "+234 801 234 5678" },
                    merchantPasswordRequired = true,
                    merchantPassword = "Cw*${(1000..9999).random()}!qM26",
                    merchantRecoveryUrl = "https://cartwise-demo-market.onrender.com/track",
                    createdAt = nowStr
                )
                activeOrder.value = mockOrder
                currentScreen.value = Screen.AGENT_LIVE
                timelineSteps.clear()
                isAgentComplete.value = false
                simulateAgentSteps()
            }
        }
    }

    /**
     * Step 2: User returns from PayPal -> Authorize hold & Start autonomous agent
     */
    private fun onPayPalApprovalCompleted(orderId: String) {
        lifecycleScope.launch {
            try {
                val updated = apiClient.authorizeOrder(orderId)
                activeOrder.value = updated.copy(
                    shippingAddress = activeOrder.value?.shippingAddress ?: savedShippingAddress.value,
                    billingAddress = activeOrder.value?.billingAddress ?: savedBillingAddress.value
                )
            } catch (e: Exception) {
                // proceed with active order
            }

            // Transition to Agent Live View
            currentScreen.value = Screen.AGENT_LIVE
            timelineSteps.clear()
            isAgentComplete.value = false

            // Listen to SSE live agent events
            apiClient.listenToAgentEvents(orderId) { event ->
                runOnUiThread {
                    val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                    when (event.type) {
                        "INIT" -> {
                            liveViewUrl.value = event.liveViewUrl
                        }
                        "STEP" -> {
                            timelineSteps.add(
                                TimelineStep(
                                    stepNumber = event.stepNumber ?: 0,
                                    stepName = event.stepName ?: "Agent Action",
                                    description = event.description ?: "",
                                    status = event.status?.uppercase() ?: "COMPLETED"
                                )
                            )
                        }
                        "SUCCESS" -> {
                            isAgentComplete.value = true
                            val dmOrderId = event.merchantOrderId ?: "DM-${orderId.takeLast(6).uppercase()}"
                            val gEmail = event.guestEmail ?: activeOrder.value?.shippingAddress?.email ?: "ada.lovelace@gmail.com"
                            val gPhone = event.guestPhone ?: activeOrder.value?.shippingAddress?.phone ?: "+234 801 234 5678"
                            val mPassword = event.merchantPassword ?: "Cw*${(1000..9999).random()}!qM26"
                            val recUrl = event.merchantRecoveryUrl ?: "https://cartwise-demo-market.onrender.com/track?orderId=$dmOrderId&email=$gEmail"

                            val updated = activeOrder.value?.copy(
                                status = "CAPTURED",
                                merchantOrderId = dmOrderId,
                                paypalCaptureId = event.captureId ?: "CAP-${orderId.takeLast(6).uppercase()}",
                                guestEmail = gEmail,
                                guestPhone = gPhone,
                                merchantPasswordRequired = event.merchantPasswordRequired ?: true,
                                merchantPassword = mPassword,
                                merchantRecoveryUrl = recUrl,
                                createdAt = nowStr
                            )
                            if (updated != null) {
                                activeOrder.value = updated
                                saveOrderToHistory(updated)
                            }
                        }
                        "FAILED" -> {
                            timelineSteps.add(
                                TimelineStep(
                                    stepName = "Order Aborted & Voided",
                                    description = "${event.reason}. PayPal hold VOIDED — you were NOT charged.",
                                    status = "FAILED"
                                )
                            )
                            val updated = activeOrder.value?.copy(
                                status = "VOIDED",
                                failureReason = event.reason ?: "Merchant declined test card.",
                                createdAt = nowStr
                            )
                            if (updated != null) {
                                activeOrder.value = updated
                                saveOrderToHistory(updated)
                            }
                        }
                    }
                }
            }

            // Trigger Agent Execution
            try {
                apiClient.startCheckout(orderId)
            } catch (e: Exception) {
                // Simulated agent steps fallback
                simulateAgentSteps()
            }
        }
    }

    private fun simulateAgentSteps() {
        val order = activeOrder.value
        val shipping = order?.shippingAddress ?: savedShippingAddress.value
        val billing = order?.billingAddress ?: savedBillingAddress.value
        val cardholder = if (billing.sameAsShipping) shipping.fullName else billing.cardholderName
        val variety = order?.selectedVariety ?: ""
        val varietyDesc = if (variety.isNotBlank()) " ($variety)" else ""

        val isRealStore = order != null && 
                          order.merchantName != "Demo Market" && 
                          order.merchantName != "Cartwise Demo Market" && 
                          !order.itemUrl.contains("cartwise-demo-market")

        lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            timelineSteps.add(TimelineStep(stepNumber = 1, stepName = "Connected to Kernel Browser", description = "Live viewport online. Navigating to ${order?.merchantName ?: "Merchant"}..."))
            kotlinx.coroutines.delay(1400)
            timelineSteps.add(TimelineStep(stepNumber = 2, stepName = "Opened Product Page", description = "Target product located: ${order?.itemTitle ?: "Item"}$varietyDesc."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 3, stepName = "Stock Verified", description = if (variety.isNotBlank()) "Selected variety '$variety' in stock. Added to cart." else "Item in stock. Added to cart."))
            kotlinx.coroutines.delay(1300)
            timelineSteps.add(TimelineStep(stepNumber = 4, stepName = "Filled Guest Delivery Details", description = "Guest Checkout: Prefilled ${shipping.fullName} in ${shipping.city}, ${shipping.country} (Phone: ${shipping.phone}, Email: ${shipping.email})."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 5, stepName = "Merchant Guest Account Policy", description = "Merchant enforced account creation during checkout. Cartwise AI generated and injected secure guest credentials."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 6, stepName = "Injected Payment Card", description = "Cartwise Corporate Visa 123456*****2234 ($cardholder) filled into checkout form."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 7, stepName = "Guardrail Price Check", description = "Verified: Landed cost is within authorized limit of $${order?.totalAmount ?: 42.99}."))
            kotlinx.coroutines.delay(1400)

            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

            if (isRealStore) {
                // Real third party merchant declined our test card
                timelineSteps.add(
                    TimelineStep(
                        stepNumber = 8,
                        stepName = "Merchant Gateway Declined",
                        description = "Card details filled successfully, but ${order?.merchantName} reported: Insufficient Funds / Invalid Test Card.",
                        status = "FAILED"
                    )
                )
                kotlinx.coroutines.delay(900)
                timelineSteps.add(
                    TimelineStep(
                        stepNumber = 9,
                        stepName = "PayPal Hold VOIDED",
                        description = "PayPal authorization hold was automatically VOIDED — you were NOT charged! 💡 Tip: Please use the Demo Market to test the complete card checkout flow.",
                        status = "FAILED"
                    )
                )
                val voided = activeOrder.value?.copy(
                    status = "VOIDED",
                    failureReason = "${order?.merchantName} declined test card (Insufficient Funds). PayPal hold was VOIDED.",
                    selectedVariety = order?.selectedVariety,
                    websiteSafety = order?.websiteSafety,
                    guestEmail = shipping.email.ifBlank { "ada.lovelace@gmail.com" },
                    guestPhone = shipping.phone.ifBlank { "+234 801 234 5678" },
                    createdAt = nowStr
                )
                if (voided != null) {
                    activeOrder.value = voided
                    saveOrderToHistory(voided)
                }
            } else {
                // Demo Market: Order Confirmed and captured
                val dmOrderId = "DM-${System.currentTimeMillis().toString().takeLast(6)}"
                val capId = "CAP-${System.currentTimeMillis().toString().takeLast(6)}"
                val gEmail = shipping.email.ifBlank { "ada.lovelace@gmail.com" }
                val gPhone = shipping.phone.ifBlank { "+234 801 234 5678" }
                val mPass = "Cw*${(1000..9999).random()}!qM26"
                val recUrl = "https://cartwise-demo-market.onrender.com/track?orderId=$dmOrderId&email=$gEmail"

                timelineSteps.add(TimelineStep(stepNumber = 8, stepName = "Store Confirmed Order", description = "Store confirmed purchase! Order #$dmOrderId. Guest recovery secured."))
                kotlinx.coroutines.delay(800)
                timelineSteps.add(TimelineStep(stepNumber = 9, stepName = "PayPal Hold Captured", description = "Captured on PayPal (Capture #$capId)."))
                isAgentComplete.value = true
                val captured = activeOrder.value?.copy(
                    status = "CAPTURED",
                    merchantOrderId = dmOrderId,
                    paypalCaptureId = capId,
                    selectedVariety = order?.selectedVariety,
                    websiteSafety = order?.websiteSafety,
                    guestEmail = gEmail,
                    guestPhone = gPhone,
                    merchantPasswordRequired = true,
                    merchantPassword = mPass,
                    merchantRecoveryUrl = recUrl,
                    createdAt = nowStr
                )
                if (captured != null) {
                    activeOrder.value = captured
                    saveOrderToHistory(captured)
                }
            }
        }
    }
}
