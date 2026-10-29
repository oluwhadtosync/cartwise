package com.cartwise.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cartwise.app.data.CartwiseOrder
import com.cartwise.app.data.Deal
import com.cartwise.app.data.TimelineStep
import com.cartwise.app.ui.theme.*

// -------------------------------------------------------------
// 1. ONBOARDING SCREEN
// -------------------------------------------------------------

@Composable
fun OnboardingScreen(
    maxPriceLimit: Double,
    onMaxPriceChanged: (Double) -> Unit,
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // Brand Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PrimaryIndigo, AccentCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Cartwise",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Cartwise",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Text(
                    text = "AI Shopping Agent &bull; Powered by PayPal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tell Cartwise what you want. It finds the cheapest real deal across the web and buys for you — even from stores that don't accept PayPal.\n\nYou pay safely with PayPal.",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Safety Guardrail Setting: Max Price
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, PrimaryIndigo)))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = PrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Approval Limit",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "$${maxPriceLimit.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Agent will never place an order exceeding this amount without asking your permission first.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = maxPriceLimit.toFloat(),
                            onValueChange = { onMaxPriceChanged(it.toDouble()) },
                            valueRange = 20f..300f,
                            steps = 28,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = PrimaryIndigo
                            )
                        )
                    }
                }
            }

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text(
                    text = "Start Shopping with Cartwise",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    CartwiseTheme {
        OnboardingScreen(maxPriceLimit = 100.0, onMaxPriceChanged = {}, onGetStarted = {})
    }
}

// -------------------------------------------------------------
// 2. ASK SCREEN (NATURAL LANGUAGE SEARCH)
// -------------------------------------------------------------

@Composable
fun AskScreen(
    queryText: String,
    onQueryChanged: (String) -> Unit,
    isLoading: Boolean,
    onSearchSubmitted: () -> Unit,
    onQuickPromptSelected: (String) -> Unit,
    onViewHistory: () -> Unit
) {
    val suggestedPrompts = listOf(
        "Find me wireless noise-cancelling earbuds under $40 that ship to Lagos",
        "Smart fitness tracker with heart rate monitor below $30",
        "Fast 20000mAh 65W power bank that ships to Nigeria",
        "Mechanical gaming keyboard with RGB under $50"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Cartwise", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Lagos, Nigeria 🇳🇬", fontSize = 11.sp, color = TextSecondary)
                }
            }
            IconButton(onClick = onViewHistory) {
                Icon(Icons.Default.History, contentDescription = "Order History", tint = TextSecondary)
            }
        }

        Text(
            text = "What would you like\nCartwise to buy for you?",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Search Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, PrimaryIndigo)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = onQueryChanged,
                    placeholder = {
                        Text("e.g. Find noise-cancelling earbuds under $40 that ship to Lagos...", color = TextMuted, fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmitted() })
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSearchSubmitted,
                    enabled = queryText.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini is searching deals...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Find Best Landed Deal", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Suggested Searches",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        suggestedPrompts.forEach { prompt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onQuickPromptSelected(prompt) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(prompt, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun AskScreenPreview() {
    CartwiseTheme {
        AskScreen(
            queryText = "Find earbuds under $40",
            onQueryChanged = {},
            isLoading = false,
            onSearchSubmitted = {},
            onQuickPromptSelected = {},
            onViewHistory = {}
        )
    }
}

// -------------------------------------------------------------
// 3. RESULTS SCREEN (TOP 3 DEALS)
// -------------------------------------------------------------

@Composable
fun ResultsScreen(
    query: String,
    deals: List<Deal>,
    onSelectDeal: (Deal) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("AI Deal Recommendations", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Landed cost including shipping to Lagos", fontSize = 11.sp, color = AccentCyan)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(deals) { deal ->
                DealCard(deal = deal, onSelect = { onSelectDeal(deal) })
            }
        }
    }
}

@Composable
fun DealCard(deal: Deal, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (deal.score >= 95) listOf(CardBorder, PrimaryIndigo) else listOf(CardBorder, CardBorder)
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (deal.score >= 95) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.linearGradient(listOf(PrimaryIndigo, AccentCyan)))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("★ TOP PICK: BEST VALUE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CardBorder)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(deal.storeName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${deal.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = deal.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI "Why This One" Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deal.reasonWhy,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing Row & Buy Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$${deal.totalCost}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$${deal.price} item + $${deal.shippingCost} shipping",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Select Deal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. CONFIRM SCREEN (PAY WITH PAYPAL ESCROW)
// -------------------------------------------------------------

@Composable
fun ConfirmScreen(
    deal: Deal,
    onPayWithPayPal: () -> Unit,
    onBack: () -> Unit
) {
    val serviceFee = 3.00
    val totalHold = deal.price + deal.shippingCost + serviceFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Order Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            // Product Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(deal.storeName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(deal.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item Price", color = TextSecondary, fontSize = 14.sp)
                        Text("$${deal.price}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shipping to Lagos, Nigeria", color = TextSecondary, fontSize = 14.sp)
                        Text("$${deal.shippingCost}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cartwise Autonomous Service Fee", color = TextSecondary, fontSize = 14.sp)
                        Text("$${serviceFee}0", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Escrow Hold", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("$${"%.2f".format(totalHold)}", color = Color(0xFF38BDF8), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PayPal Escrow Guarantee Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("PayPal Escrow Protection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Funds are authorized and held in escrow. They are ONLY captured after the store confirms the order. If checkout fails or the item sells out, your hold is instantly voided and you are never charged.",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Official "Pay with PayPal" Branded Button
        Button(
            onClick = onPayWithPayPal,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp), // Official PayPal pill shape
            colors = ButtonDefaults.buttonColors(containerColor = PayPalGold)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pay with",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayPalBlue
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PayPal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PayPalBlue
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 5. AGENT LIVE SCREEN (WEBVIEW + SSE TIMELINE)
// -------------------------------------------------------------

@Composable
fun AgentLiveScreen(
    order: CartwiseOrder,
    timeline: List<TimelineStep>,
    liveViewUrl: String?,
    isComplete: Boolean,
    onViewReceipt: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Autonomous Agent Live", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(order.merchantName, fontSize = 12.sp, color = AccentCyan)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isComplete) SuccessBg else Color(0xFF78350F))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isComplete) "COMPLETED" else "LIVE STREAM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isComplete) SuccessGreen else Color(0xFFFCD34D)
                )
            }
        }

        // Top Half: Kernel Cloud Browser Live View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color.Black)
        ) {
            if (!liveViewUrl.isNullOrBlank()) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webViewClient = WebViewClient()
                            loadUrl(liveViewUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            }
        }

        // Bottom Half: Step-by-Step SSE Timeline
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Execution Audit Timeline",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(timeline) { step ->
                    TimelineItem(step = step)
                }
            }

            if (isComplete) {
                Button(
                    onClick = onViewReceipt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("View Order Receipt", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TimelineItem(step: TimelineStep) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (step.status == "FAILED") DangerBg else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (step.status == "FAILED") Icons.Default.Close else Icons.Default.Check,
                contentDescription = null,
                tint = if (step.status == "FAILED") DangerRed else SuccessGreen,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(step.stepName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(step.description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}

// -------------------------------------------------------------
// 6. RECEIPT SCREEN
// -------------------------------------------------------------

@Composable
fun ReceiptScreen(
    order: CartwiseOrder,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, SuccessGreen)))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SuccessBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Purchase Confirmed!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Bought autonomously with Cartwise", fontSize = 12.sp, color = AccentCyan)

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = CardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Merchant Order ID", color = TextSecondary, fontSize = 13.sp)
                    Text(order.merchantOrderId ?: "DM-849201", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PayPal Capture ID", color = TextSecondary, fontSize = 13.sp)
                    Text(order.paypalCaptureId ?: "CAP-928103", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Store", color = TextSecondary, fontSize = 13.sp)
                    Text(order.merchantName, color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Captured", color = TextSecondary, fontSize = 13.sp)
                    Text("$${order.totalAmount}", color = SuccessGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Shop Another Item", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
