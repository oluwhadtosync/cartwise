package com.cartwise.app.ui.screens

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cartwise.app.R
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
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                // Brand Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
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
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cartwise",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Text(
                    text = "AI Shopping Agent • Powered by PayPal",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Tell Cartwise what you want. It finds the cheapest real deal across the web and buys for you — even from stores that don't accept PayPal.\n\nYou pay safely with PayPal.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Safety Guardrail Setting: Autonomous Buy Limit
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, PrimaryIndigo)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                    text = "Autonomous Buy Limit",
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
                            text = "Agent autonomously buys verified deals up to $${maxPriceLimit.toInt()}. Higher totals always require your explicit confirmation.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Slider(
                            value = maxPriceLimit.toFloat(),
                            onValueChange = { onMaxPriceChanged(it.toDouble()) },
                            valueRange = 5f..150f,
                            steps = 28,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = PrimaryIndigo
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text(
                        text = "Start Shopping with Cartwise",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Official Hackathon Partner Logos
                PoweredByPartnersBar()
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
    var pendingPrompt by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val suggestedPrompts = listOf(
        "Find me wireless noise-cancelling earbuds under $40 that ship to Lagos",
        "Smart fitness tracker with heart rate monitor below $30",
        "Fast 20000mAh 65W power bank that ships to Nigeria",
        "Mechanical gaming keyboard with RGB under $50"
    )

    // Accidental Tap Confirmation Dialog
    if (pendingPrompt != null) {
        AlertDialog(
            onDismissRequest = { pendingPrompt = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Suggested Search",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Do you want Cartwise AI to find and rank the best landed deals for:",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = BgDark,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "\"${pendingPrompt}\"",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentCyan,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 19.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val promptToSearch = pendingPrompt ?: ""
                        pendingPrompt = null
                        onQuickPromptSelected(promptToSearch)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Search Deals", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPrompt = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 20.dp),
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

        Spacer(modifier = Modifier.height(18.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Suggested Searches",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Text(
                text = "Tap to verify",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        suggestedPrompts.forEach { prompt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { pendingPrompt = prompt }, // Shows confirmation modal to prevent accidental tap
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

        Spacer(modifier = Modifier.height(20.dp))

        // Official Hackathon Partner Logos
        PoweredByPartnersBar(modifier = Modifier.padding(bottom = 16.dp))
    }
}

// -------------------------------------------------------------
// PARTNERS LOGO BAR (PAYPAL, RENDER, CHANNEL3, KERNEL)
// -------------------------------------------------------------

@Composable
fun PoweredByPartnersBar(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(24.dp, 1.dp).background(CardBorder))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "POWERED BY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(24.dp, 1.dp).background(CardBorder))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Moving animated ticker: scrolls smoothly across screen infinitely to save space
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    velocity = 35.dp
                )
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) {
                // PayPal Real Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_paypal),
                    contentDescription = "PayPal",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(20.dp)
                        .width(76.dp)
                )

                // Render Real Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_render),
                    contentDescription = "Render",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(18.dp)
                        .width(76.dp)
                )

                // Channel3 Real Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_channel3),
                    contentDescription = "Channel3",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(18.dp)
                        .width(76.dp)
                )

                // Kernel Real Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_kernel),
                    contentDescription = "Kernel",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(16.dp)
                        .width(70.dp)
                )
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
    cancelNotice: String? = null,
    onDismissCancelNotice: () -> Unit = {},
    onPayWithPayPal: () -> Unit,
    onBack: () -> Unit
) {
    val serviceFee = 3.00
    val totalHold = deal.price + deal.shippingCost + serviceFee
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Order Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Scrollable Breakdown details
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Cancellation Notice Banner (shown if user cancelled on PayPal)
            if (!cancelNotice.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                    border = BorderStroke(1.dp, Color(0xFFB45309))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = cancelNotice,
                            fontSize = 12.5.sp,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissCancelNotice, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        }
                    }
                }
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
                    HorizontalDivider(color = CardBorder)
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
                        Text("$${"%.2f".format(serviceFee)}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Escrow Hold", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("$${"%.2f".format(totalHold)}", color = Color(0xFF38BDF8), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Purchasing Card Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E3A8A))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Autonomous Purchasing Card", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Cartwise Corporate Visa • 123456*****2234", fontSize = 12.sp, color = TextSecondary)
                        Text("Single-use hold funded via your PayPal authorization", fontSize = 11.sp, color = AccentCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Action Area: Official Branded "Pay with PayPal" Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onPayWithPayPal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp), // Official PayPal pill shape
                colors = ButtonDefaults.buttonColors(containerColor = PayPalGold),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_paypal_btn),
                        contentDescription = "PayPal",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pay with",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003087)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "PayPal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0079C1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🔒 Authorized hold in escrow • Voided if not fulfilled",
                fontSize = 11.sp,
                color = TextMuted
            )
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
    val latestStep = timeline.lastOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Top Header
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
                    .background(if (isComplete) SuccessBg else if (order.status == "VOIDED") DangerBg else Color(0xFF78350F))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isComplete) "COMPLETED" else if (order.status == "VOIDED") "VOIDED" else "LIVE STREAM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isComplete) SuccessGreen else if (order.status == "VOIDED") DangerRed else Color(0xFFFCD34D)
                )
            }
        }

        // Top Half: Simulated/Real Browser Window Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(230.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser Chrome / Address Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Window dots
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // URL Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1F2937))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔒 https://${order.merchantName.lowercase().replace(" ", "")}.com/checkout",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isComplete) SuccessGreen else AccentCyan)
                    )
                }

                // Browser Viewport
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF030712))
                ) {
                    // If liveViewUrl is valid HTTP, embed WebView
                    if (!liveViewUrl.isNullOrBlank() && liveViewUrl.startsWith("http")) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.databaseEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    webViewClient = object : WebViewClient() {
                                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                            handler?.proceed()
                                        }
                                    }
                                    loadUrl(liveViewUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Live DOM Overlay HUD (guarantees the user always sees rich animated action, never black void)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xCC050811), Color(0xEE090D1A))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(order.merchantName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("CARD GATEWAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                            }

                            // Active agent step highlight in viewport
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isComplete) Icons.Default.CheckCircle else if (order.status == "VOIDED") Icons.Default.Cancel else Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (isComplete) SuccessGreen else if (order.status == "VOIDED") DangerRed else AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = latestStep?.stepName ?: "Autonomous Purchasing in Progress",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = latestStep?.description ?: "Executing DOM runner via Kernel CDP...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Card in use in viewport
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Card: ${order.cardUsed}", fontSize = 11.sp, color = TextSecondary)
                                Text("Escrow Hold: $${order.totalAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                        }
                    }
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
            } else if (order.status == "VOIDED") {
                Button(
                    onClick = onViewReceipt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("View Voided Receipt", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    val isVoided = order.status == "VOIDED"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top navigation bar with Back icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDone) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isVoided) "Voided Order Details" else "Order Receipt",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(CardBorder, if (isVoided) DangerRed else SuccessGreen)
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isVoided) DangerBg else SuccessBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVoided) Icons.Default.Close else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isVoided) DangerRed else SuccessGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isVoided) "Payment Voided" else "Purchase Confirmed!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isVoided) "Escrow Released • You Were NOT Charged" else "Bought autonomously with Cartwise",
                        fontSize = 12.5.sp,
                        color = if (isVoided) DangerRed else AccentCyan,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dedicated Autonomous Card Used Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A8A))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Autonomous Purchasing Card",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Cartwise Corporate Visa • ${order.cardUsed}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "Injected into ${order.merchantName} checkout",
                                    fontSize = 10.5.sp,
                                    color = AccentCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item Title
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            order.itemTitle,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Store", color = TextSecondary, fontSize = 13.sp)
                        Text(order.merchantName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Merchant Order ID", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            if (isVoided) "Not Placed (Decline / Stockout)" else (order.merchantOrderId ?: "DM-${order.id.takeLast(6).uppercase()}"),
                            color = if (isVoided) DangerRed else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isVoided) "PayPal Void ID" else "PayPal Capture ID", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            order.paypalCaptureId ?: (if (isVoided) "VOID-${order.id.takeLast(6).uppercase()}" else "CAP-${order.id.takeLast(6).uppercase()}"),
                            color = Color(0xFF60A5FA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isVoided) "Escrow Released" else "Total Captured", color = TextSecondary, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$${order.totalAmount}",
                                color = if (isVoided) TextMuted else SuccessGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (isVoided) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("$0.00 CHARGED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                        }
                    }

                    // Explanatory Card
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isVoided) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1E2E))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Tip: Real third-party stores decline test card ${order.cardUsed} with Insufficient Funds / Invalid Card. Cartwise automatically voided the PayPal authorization so no money left your account. Please use 'Demo Market' to test the complete purchase & capture flow!",
                                fontSize = 11.5.sp,
                                color = Color(0xFFF1F5F9),
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✅ Order confirmed by ${order.merchantName}! PayPal escrow hold was captured and payment completed. Your item is being processed for dispatch to Nigeria.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF93C5FD),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text(if (isVoided) "Back to Orders" else "Shop Another Item", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    PoweredByPartnersBar()
                }
            }
        }
    }
}
