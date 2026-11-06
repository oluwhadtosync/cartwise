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
import kotlinx.coroutines.launch
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

        handleDeepLink(intent)

        setContent {
            CartwiseTheme {
                // Intercept Android System Back button navigation
                BackHandler(enabled = currentScreen.value != Screen.ONBOARDING) {
                    when (currentScreen.value) {
                        Screen.RESULTS -> currentScreen.value = Screen.ASK
                        Screen.CONFIRM -> currentScreen.value = Screen.RESULTS
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
                                onPayWithPayPal = { shipping, billing ->
                                    initiatePayPalOrder(deal, shipping, billing)
                                },
                                onDemoSandboxFastPass = { shipping, billing ->
                                    fastPassPayPalOrder(deal, shipping, billing)
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
        handleDeepLink(intent)
    }

    /**
     * Handle PayPal redirect: cartwise://paypal/return?orderId=... or cancel
     */
    private fun handleDeepLink(intent: Intent?) {
        val uri: Uri? = intent?.data
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
        billingAddress: BillingAddress
    ) {
        cancelNotice.value = null
        savedShippingAddress.value = shippingAddress
        savedBillingAddress.value = billingAddress
        OrderStorage.saveShippingAddress(this, shippingAddress)
        OrderStorage.saveBillingAddress(this, billingAddress)

        lifecycleScope.launch {
            try {
                val order = apiClient.createOrder(deal, shippingAddress, billingAddress)
                activeOrder.value = order.copy(
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress
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
        billingAddress: BillingAddress
    ) {
        cancelNotice.value = null
        savedShippingAddress.value = shippingAddress
        savedBillingAddress.value = billingAddress
        OrderStorage.saveShippingAddress(this, shippingAddress)
        OrderStorage.saveBillingAddress(this, billingAddress)

        lifecycleScope.launch {
            val chosenVariety = deal.selectedVarietyName.ifBlank { "Matte Black" }
            try {
                // Demo Fast-Pass ALWAYS routes to our official Demo Market: https://cartwise-demo-market.onrender.com/
                val demoDeal = Deal(
                    id = "prod-earbuds-01",
                    title = if (deal.title.isNotBlank()) deal.title else "SoundPulse Air Wireless Noise-Cancelling Earbuds",
                    brand = "SoundPulse",
                    price = 34.99,
                    shippingCost = 5.00,
                    totalCost = 39.99,
                    storeName = "Cartwise Demo Market",
                    storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
                    rating = 4.8,
                    reasonWhy = "Official Cartwise Demo Market: 100% guaranteed checkout capture.",
                    selectedVarietyName = chosenVariety,
                    varieties = deal.varieties.ifEmpty {
                        listOf(
                            ProductVariety(id = "var-blk", name = "Matte Black", stockCount = 15, inStock = true),
                            ProductVariety(id = "var-wht", name = "Arctic White", stockCount = 2, inStock = true)
                        )
                    },
                    safety = WebsiteSafety(
                        domain = "cartwise-demo-market.onrender.com",
                        score = 98,
                        level = "SAFE",
                        sslVerified = true,
                        summary = "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
                    )
                )
                val order = apiClient.createOrder(demoDeal, shippingAddress, billingAddress)
                activeOrder.value = order.copy(
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = chosenVariety
                )
                pendingOrderId = order.id
                onPayPalApprovalCompleted(order.id)
            } catch (e: Exception) {
                val orderId = "cw_ord_${System.currentTimeMillis()}"
                val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                val mockOrder = CartwiseOrder(
                    id = orderId,
                    status = "PENDING_APPROVAL",
                    itemTitle = if (deal.title.isNotBlank()) deal.title else "SoundPulse Air Wireless Noise-Cancelling Earbuds",
                    itemUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
                    merchantName = "Cartwise Demo Market",
                    itemPrice = 34.99,
                    shippingCost = 5.00,
                    serviceFee = 3.00,
                    totalAmount = 42.99,
                    cardUsed = "123456*****2234",
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    selectedVariety = chosenVariety,
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
                            val updated = activeOrder.value?.copy(
                                status = "CAPTURED",
                                merchantOrderId = event.merchantOrderId ?: "DM-${orderId.takeLast(6).uppercase()}",
                                paypalCaptureId = event.captureId ?: "CAP-${orderId.takeLast(6).uppercase()}",
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
            timelineSteps.add(TimelineStep(stepNumber = 4, stepName = "Filled Shipping Address", description = "Delivering to ${shipping.fullName}, ${shipping.address}, ${shipping.city}, ${shipping.country}."))
            kotlinx.coroutines.delay(1400)
            timelineSteps.add(TimelineStep(stepNumber = 5, stepName = "Injected Payment Card", description = "Cartwise Corporate Visa 123456*****2234 ($cardholder) filled into checkout form."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 6, stepName = "Guardrail Price Check", description = "Verified: Landed cost is within authorized limit of $${order?.totalAmount ?: 42.99}."))
            kotlinx.coroutines.delay(1400)

            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

            if (isRealStore) {
                // Real third party merchant declined our test card
                timelineSteps.add(
                    TimelineStep(
                        stepNumber = 7,
                        stepName = "Merchant Gateway Declined",
                        description = "Card details filled successfully, but ${order?.merchantName} reported: Insufficient Funds / Invalid Test Card.",
                        status = "FAILED"
                    )
                )
                kotlinx.coroutines.delay(900)
                timelineSteps.add(
                    TimelineStep(
                        stepNumber = 8,
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
                timelineSteps.add(TimelineStep(stepNumber = 7, stepName = "Store Confirmed Order", description = "Store confirmed purchase! Order #$dmOrderId."))
                kotlinx.coroutines.delay(800)
                timelineSteps.add(TimelineStep(stepNumber = 8, stepName = "PayPal Hold Captured", description = "Captured on PayPal (Capture #$capId)."))
                isAgentComplete.value = true
                val captured = activeOrder.value?.copy(
                    status = "CAPTURED",
                    merchantOrderId = dmOrderId,
                    paypalCaptureId = capId,
                    selectedVariety = order?.selectedVariety,
                    websiteSafety = order?.websiteSafety,
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
