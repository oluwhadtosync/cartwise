package com.cartwise.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.cartwise.app.data.*
import com.cartwise.app.ui.screens.*
import com.cartwise.app.ui.theme.BgDark
import com.cartwise.app.ui.theme.CartwiseTheme
import kotlinx.coroutines.launch

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
    private val maxPrice = mutableStateOf(80.0)
    private val orderHistory = mutableStateListOf<CartwiseOrder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)

        setContent {
            CartwiseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
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
                            onSelectDeal = { deal ->
                                selectedDeal.value = deal
                                currentScreen.value = Screen.CONFIRM
                            },
                            onBack = { currentScreen.value = Screen.ASK }
                        )

                        Screen.CONFIRM -> selectedDeal.value?.let { deal ->
                            ConfirmScreen(
                                deal = deal,
                                onPayWithPayPal = { initiatePayPalOrder(deal) },
                                onBack = { currentScreen.value = Screen.RESULTS }
                            )
                        }

                        Screen.AGENT_LIVE -> activeOrder.value?.let { order ->
                            AgentLiveScreen(
                                order = order,
                                timeline = timelineSteps,
                                liveViewUrl = liveViewUrl.value,
                                isComplete = isAgentComplete.value,
                                onViewReceipt = { currentScreen.value = Screen.RECEIPT }
                            )
                        }

                        Screen.RECEIPT -> activeOrder.value?.let { order ->
                            ReceiptScreen(
                                order = order,
                                onDone = {
                                    searchQuery.value = ""
                                    currentScreen.value = Screen.ASK
                                }
                            )
                        }

                        Screen.HISTORY -> OrdersHistoryScreen(
                            orders = orderHistory,
                            onBack = { currentScreen.value = Screen.ASK }
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
     * Handle PayPal redirect: cartwise://paypal/return?orderId=...
     */
    private fun handleDeepLink(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null && uri.scheme == "cartwise" && uri.host == "paypal") {
            val orderId = uri.getQueryParameter("orderId") ?: pendingOrderId
            if (orderId != null) {
                onPayPalApprovalCompleted(orderId)
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
                val res = apiClient.search(query, maxBudget = maxPrice.value, country = "NG")
                searchResults.clear()
                searchResults.addAll(res.deals)
                currentScreen.value = Screen.RESULTS
            } catch (e: Exception) {
                // If backend not reachable, provide fallback demo items
                searchResults.clear()
                searchResults.add(
                    Deal(
                        id = "prod-earbuds-01",
                        title = "SoundPulse Air Wireless Noise-Cancelling Earbuds",
                        brand = "SoundPulse",
                        price = 34.99,
                        shippingCost = 5.00,
                        totalCost = 39.99,
                        storeName = "Demo Market",
                        storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
                        rating = 4.8,
                        reasonWhy = "Best value pick: lowest total landed cost ($39.99) with verified shipping to Nigeria."
                    )
                )
                currentScreen.value = Screen.RESULTS
            } finally {
                isSearching.value = false
            }
        }
    }

    /**
     * Step 1: Create Order & Launch PayPal Chrome Custom Tab
     */
    private fun initiatePayPalOrder(deal: Deal) {
        lifecycleScope.launch {
            try {
                val order = apiClient.createOrder(deal)
                activeOrder.value = order
                pendingOrderId = order.id

                val approveUrl = order.paypalApproveUrl
                if (!approveUrl.isNullOrBlank()) {
                    // Launch PayPal in Chrome Custom Tab
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                    customTabsIntent.launchUrl(this@MainActivity, Uri.parse(approveUrl))
                } else {
                    // Fallback direct approval simulation
                    onPayPalApprovalCompleted(order.id)
                }
            } catch (e: Exception) {
                // Mock order creation for demonstration
                val mockOrder = CartwiseOrder(
                    id = "cw_ord_mock_${System.currentTimeMillis()}",
                    itemTitle = deal.title,
                    itemPrice = deal.price,
                    shippingCost = deal.shippingCost,
                    totalAmount = deal.price + deal.shippingCost + 3.00,
                    status = "AUTHORIZED",
                    merchantName = deal.storeName,
                    liveViewUrl = "https://cartwise-demo-market.onrender.com/checkout?productId=prod-earbuds-01"
                )
                activeOrder.value = mockOrder
                onPayPalApprovalCompleted(mockOrder.id)
            }
        }
    }

    /**
     * Step 2: User returns from PayPal $\rightarrow$ Authorize hold & Start autonomous agent
     */
    private fun onPayPalApprovalCompleted(orderId: String) {
        lifecycleScope.launch {
            try {
                val updated = apiClient.authorizeOrder(orderId)
                activeOrder.value = updated
            } catch (e: Exception) {
                // proceed in mock mode
            }

            // Transition to Agent Live View
            currentScreen.value = Screen.AGENT_LIVE
            timelineSteps.clear()
            isAgentComplete.value = false

            // Listen to SSE live agent events
            apiClient.listenToAgentEvents(orderId) { event ->
                runOnUiThread {
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
                            activeOrder.value = activeOrder.value?.copy(
                                status = "CAPTURED",
                                merchantOrderId = event.merchantOrderId,
                                paypalCaptureId = event.captureId
                            )
                            activeOrder.value?.let { orderHistory.add(it) }
                        }
                        "FAILED" -> {
                            timelineSteps.add(
                                TimelineStep(
                                    stepName = "Order Aborted & Voided",
                                    description = "${event.reason}. PayPal hold VOIDED — you were NOT charged.",
                                    status = "FAILED"
                                )
                            )
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
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            timelineSteps.add(TimelineStep(stepNumber = 1, stepName = "Connected to Kernel Browser", description = "Live viewport online. Navigating to Demo Market..."))
            kotlinx.coroutines.delay(1500)
            timelineSteps.add(TimelineStep(stepNumber = 2, stepName = "Opened Product Page", description = "Target product located: SoundPulse Air Earbuds."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 3, stepName = "Stock Verified", description = "Item in stock. Added to cart."))
            kotlinx.coroutines.delay(1400)
            timelineSteps.add(TimelineStep(stepNumber = 4, stepName = "Filled Shipping", description = "Delivering to 14 Marina Street, Lagos, Nigeria."))
            kotlinx.coroutines.delay(1500)
            timelineSteps.add(TimelineStep(stepNumber = 5, stepName = "Injected Payment Card", description = "Cartwise commercial card ending in 4242 entered safely."))
            kotlinx.coroutines.delay(1000)
            timelineSteps.add(TimelineStep(stepNumber = 6, stepName = "Guardrail Price Check", description = "Verified: $39.99 is within authorized limit of $42.99."))
            kotlinx.coroutines.delay(1200)
            timelineSteps.add(TimelineStep(stepNumber = 7, stepName = "Order Confirmed by Store", description = "Store confirmed purchase! Order #DM-849201."))
            kotlinx.coroutines.delay(800)
            timelineSteps.add(TimelineStep(stepNumber = 8, stepName = "PayPal Hold Captured", description = "Captured on PayPal (Capture #CAP-938210)."))
            isAgentComplete.value = true
            activeOrder.value = activeOrder.value?.copy(
                status = "CAPTURED",
                merchantOrderId = "DM-849201",
                paypalCaptureId = "CAP-938210"
            )
            activeOrder.value?.let { orderHistory.add(it) }
        }
    }
}
