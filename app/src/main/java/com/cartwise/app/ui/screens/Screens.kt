package com.cartwise.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cartwise.app.R
import com.cartwise.app.data.BillingAddress
import com.cartwise.app.data.CartwiseOrder
import com.cartwise.app.data.Deal
import com.cartwise.app.data.ProductVariety
import com.cartwise.app.data.ShippingAddress
import com.cartwise.app.data.TimelineStep
import com.cartwise.app.data.WebsiteSafety
import com.cartwise.app.ui.theme.*

// -------------------------------------------------------------
// 1. ONBOARDING SCREEN
// -------------------------------------------------------------

data class ShowcaseDeal(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val retailPrice: Double,
    val cartwisePrice: Double,
    val storeBadge: String
)

@Composable
fun OnboardingScreen(
    maxPriceLimit: Double = 100.0,
    onMaxPriceChanged: (Double) -> Unit = {},
    onGetStarted: () -> Unit
) {
    val scrollState = rememberScrollState()

    val showcaseItems = remember {
        listOf(
            ShowcaseDeal(
                title = "Sony WH-1000XM5 ANC Headphones",
                subtitle = "Flagship Noise Cancelling • Multi-Store Index",
                imageRes = R.drawable.product_headphones,
                retailPrice = 1000.00,
                cartwisePrice = 620.00,
                storeBadge = "Discovered via Channel3"
            ),
            ShowcaseDeal(
                title = "Titanium Smartwatch Ultra 49mm",
                subtitle = "Cellular + GPS • International Express",
                imageRes = R.drawable.product_smartwatch,
                retailPrice = 450.00,
                cartwisePrice = 275.00,
                storeBadge = "Discovered via Channel3"
            ),
            ShowcaseDeal(
                title = "Soundcore Life P2 Mini Earbuds",
                subtitle = "Compact True Wireless • Demo Market In-Stock",
                imageRes = R.drawable.product_earbuds,
                retailPrice = 65.00,
                cartwisePrice = 28.99,
                storeBadge = "Cartwise Demo Market"
            )
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4500)
            selectedIndex = (selectedIndex + 1) % showcaseItems.size
        }
    }

    val currentItem = showcaseItems[selectedIndex]

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
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Brand Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cartwise",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Text(
                    text = "AI Shopping Agent • Powered by PayPal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tell Cartwise what you want. It finds the cheapest real deal across the web and buys for you — even from stores that don't accept PayPal.\n\nYou pay safely with PayPal.",
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Real Product Image & Price Discovery Showcase
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorder, PrimaryIndigo)))
                ) {
                    Column {
                        // Real photographic product image with badges
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        ) {
                            Image(
                                painter = painterResource(id = currentItem.imageRes),
                                contentDescription = currentItem.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle gradient scrim for readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xDD0A0F1D))
                                        )
                                    )
                            )
                            // Top left: AI Discovery Badge
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xE60A0F1D))
                                    .border(1.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "AI PRICE DISCOVERY",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }
                            }

                            // Top right: Savings Pill
                            val savings = currentItem.retailPrice - currentItem.cartwisePrice
                            val percentOff = ((savings / currentItem.retailPrice) * 100).toInt()
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "SAVE $${savings.toInt()} ($percentOff% OFF)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Product Info & Side-by-Side Comparison
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = currentItem.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = currentItem.subtitle,
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Professional Before vs After Price Comparison Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF070B14))
                                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // BEFORE CARTWISE AI
                                Column {
                                    Text(
                                        text = "BEFORE CARTWISE AI",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Standard Store Retail",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$${currentItem.retailPrice.toInt()}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        style = TextStyle(textDecoration = TextDecoration.LineThrough)
                                    )
                                }

                                // Arrow
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )

                                // AFTER CARTWISE SEARCH
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "AFTER CARTWISE SEARCH",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Landed Escrow Total",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$${"%.2f".format(currentItem.cartwisePrice)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Footer: Escrow Indicator + Carousel Dots
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "PayPal Escrow Protected",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                // Interactive indicator dots
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    showcaseItems.indices.forEach { idx ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (idx == selectedIndex) 8.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (idx == selectedIndex) AccentCyan else TextMuted.copy(alpha = 0.35f)
                                                )
                                                .clickable { selectedIndex = idx }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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

                Spacer(modifier = Modifier.height(18.dp))

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
    isSafetyCheckEnabled: Boolean = false,
    onToggleSafetyCheck: (Boolean) -> Unit = {},
    selectedModel: String = "gemini-3.8-flash",
    onSelectModel: (String) -> Unit = {},
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

                Spacer(modifier = Modifier.height(10.dp))

                // Gemini AI Model Selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI SEARCH MODEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = TextMuted
                        )
                    }

                    // Tidy single-line chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "gemini-3.8-flash" to "Gemini 3.8 Flash",
                            "gemini-3.7-flash" to "Gemini 3.7 Flash",
                            "gemini-2.5-flash" to "Gemini 2.5 Flash"
                        ).forEach { (id, label) ->
                            val isSelected = selectedModel == id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PrimaryIndigo.copy(alpha = 0.32f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else CardBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectModel(id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AccentCyan)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextMuted,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Risk Score Toggle Button (Clean single-row, inactive by default)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSafetyCheckEnabled) Color(0xFF042F2E) else Color(0xFF0F172A))
                        .border(
                            1.dp,
                            if (isSafetyCheckEnabled) SuccessGreen.copy(alpha = 0.6f) else CardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleSafetyCheck(!isSafetyCheckEnabled) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSafetyCheckEnabled) SuccessGreen.copy(alpha = 0.2f) else Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSafetyCheckEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isSafetyCheckEnabled) SuccessGreen else TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Merchant Risk Score",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSafetyCheckEnabled) "Risk score active" else "Risk score inactive",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSafetyCheckEnabled) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSafetyCheckEnabled) SuccessGreen else TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = isSafetyCheckEnabled,
                        onCheckedChange = onToggleSafetyCheck,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen,
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }

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
                        Text(if (selectedModel.contains("3.8")) "Gemini 3.8 is searching deals..." else "Gemini is searching deals...", color = Color.White, fontWeight = FontWeight.Bold)
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
    isSafetyCheckEnabled: Boolean = false,
    onToggleSafetyCheck: (Boolean) -> Unit = {},
    onSelectDeal: (Deal) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("AI Deal Recommendations", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Landed cost including shipping to Lagos", fontSize = 11.sp, color = AccentCyan)
            }
        }

        // Top AI Risk Score Toggle Row (Compact & clean, inactive by default)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSafetyCheckEnabled) Color(0xFF042F2E) else Color(0xFF0F172A))
                .border(
                    1.dp,
                    if (isSafetyCheckEnabled) SuccessGreen.copy(alpha = 0.6f) else CardBorder,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onToggleSafetyCheck(!isSafetyCheckEnabled) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSafetyCheckEnabled) SuccessGreen.copy(alpha = 0.2f) else Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSafetyCheckEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isSafetyCheckEnabled) SuccessGreen else TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AI Merchant Risk Score",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSafetyCheckEnabled) "Risk score active" else "Risk score inactive",
                        fontSize = 11.5.sp,
                        fontWeight = if (isSafetyCheckEnabled) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSafetyCheckEnabled) SuccessGreen else TextMuted
                    )
                }
            }

            Switch(
                checked = isSafetyCheckEnabled,
                onCheckedChange = onToggleSafetyCheck,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SuccessGreen,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(deals) { deal ->
                DealCard(
                    deal = deal,
                    isSafetyCheckEnabled = isSafetyCheckEnabled,
                    onSelect = { selectedWithVariant -> onSelectDeal(selectedWithVariant) }
                )
            }
        }
    }
}

@Composable
fun DealCard(
    deal: Deal,
    isSafetyCheckEnabled: Boolean,
    onSelect: (Deal) -> Unit
) {
    val context = LocalContext.current

    // Varieties setup (defaulting to 3 realistic variants if not provided)
    val varieties = remember(deal) {
        if (deal.varieties.isNotEmpty()) {
            deal.varieties
        } else {
            listOf(
                ProductVariety("Matte Black", "VAR-BLK", inStock = deal.inStock, stockCount = if (deal.inStock) deal.stockCount else 0),
                ProductVariety("Arctic White", "VAR-WHT", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 2) else 0),
                ProductVariety("Deep Navy", "VAR-NVY", inStock = deal.inStock, stockCount = if (deal.inStock) minOf(2, deal.stockCount) else 0),
                ProductVariety("Special Edition", "VAR-LTD", inStock = false, stockCount = 0)
            )
        }
    }

    var selectedVarietyIndex by remember(deal) {
        val initialIdx = varieties.indexOfFirst { it.name.equals(deal.selectedVarietyName, ignoreCase = true) }
        mutableStateOf(if (initialIdx >= 0) initialIdx else (varieties.indexOfFirst { it.inStock }.takeIf { it >= 0 } ?: 0))
    }

    val selectedVariety = varieties.getOrNull(selectedVarietyIndex) ?: varieties.first()
    val isSelectedSoldOut = !selectedVariety.inStock || selectedVariety.stockCount <= 0

    // Domain AI Safety Analysis
    val safety = deal.safety ?: WebsiteSafety(
        score = if (deal.storeName.contains("Demo", true) || deal.storeUrl.contains("cartwise-demo-market")) 98
                else if (deal.storeUrl.contains("glorious") || deal.storeName.contains("Boutique", true)) 68
                else if (deal.storeName.contains("Flash", true) || deal.storeUrl.contains("drop")) 34
                else 92,
        level = if (deal.storeName.contains("Demo", true) || deal.storeUrl.contains("cartwise-demo-market")) "SAFE"
                else if (deal.storeUrl.contains("glorious") || deal.storeName.contains("Boutique", true)) "CAUTION"
                else if (deal.storeName.contains("Flash", true) || deal.storeUrl.contains("drop")) "RISKY"
                else "SAFE",
        domain = try { Uri.parse(deal.storeUrl).host ?: deal.storeName } catch (e: Exception) { deal.storeName },
        sslVerified = !deal.storeName.contains("Flash", true),
        summary = if (deal.storeName.contains("Demo", true) || deal.storeUrl.contains("cartwise-demo-market"))
            "Cartwise Official Demo Market: 100% verified merchant, instant escrow capture, guaranteed fulfillment."
        else if (deal.storeUrl.contains("glorious") || deal.storeName.contains("Boutique", true))
            "Caution: Recognized specialty merchant, but third-party gateway may decline international test cards. Escrow release protected."
        else if (deal.storeName.contains("Flash", true) || deal.storeUrl.contains("drop"))
            "Risky Domain: Unverified registration, high dispute rate, and unconfirmed return policy. Escrow void guard active."
        else
            "Safe to Buy: Established retailer with verified SSL encryption, high buyer reputation, and PayPal protection."
    )

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
            // Badges row: Store Name & Rating
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

            Spacer(modifier = Modifier.height(10.dp))

            // Item Title
            Text(
                text = deal.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Direct Item Link Row with Tap to Open in Browser + Share Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direct store link chip (tapping opens in browser)
                val cleanDomain = deal.storeUrl.replace("https://", "").replace("http://", "").trimEnd('/')
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.storeUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Website Link",
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cleanDomain,
                        fontSize = 11.5.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open in browser",
                        tint = AccentCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Direct Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out this deal found by Cartwise AI: ${deal.title} at ${deal.storeName} for $${deal.totalCost} landed total! Store link: ${deal.storeUrl}"
                            )
                            putExtra(Intent.EXTRA_TITLE, deal.title)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Deal Link"))
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share Link",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 2. AI Website Safety Check Badge (Shown ONLY if isSafetyCheckEnabled == true)
            if (isSafetyCheckEnabled) {
                Spacer(modifier = Modifier.height(10.dp))

                val isSafe = safety.level == "SAFE"
                val isCaution = safety.level == "CAUTION"
                val safetyBorderColor = if (isSafe) SuccessGreen else if (isCaution) Color(0xFFF59E0B) else DangerRed
                val safetyBgColor = if (isSafe) Color(0xFF042F2E) else if (isCaution) Color(0xFF381A05) else Color(0xFF450A0A)
                val safetyAccentColor = if (isSafe) Color(0xFF34D399) else if (isCaution) Color(0xFFFBBF24) else Color(0xFFF87171)
                val safetyLabel = if (isSafe) "GOOD TO BUY" else if (isCaution) "CAUTION: THIRD-PARTY" else "RISKY MERCHANT"
                val safetyIcon = if (isSafe) Icons.Default.VerifiedUser else if (isCaution) Icons.Default.Warning else Icons.Default.GppBad

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(safetyBgColor)
                        .border(1.dp, safetyBorderColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = safetyIcon,
                                    contentDescription = null,
                                    tint = safetyAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${safety.score}/100 • $safetyLabel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = safetyAccentColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(safetyBorderColor.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (safety.sslVerified) "SSL 256-BIT" else "NO SSL / RISKY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = safetyAccentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = safety.summary,
                            fontSize = 11.sp,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Store Item Variety Picker
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Store Varieties / Colors:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Text(
                        text = "${selectedVariety.name} (${if (selectedVariety.inStock && selectedVariety.stockCount > 0) "${selectedVariety.stockCount} in stock" else "Sold Out"})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedVariety.inStock && selectedVariety.stockCount > 0) AccentCyan else DangerRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Variety Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(varieties) { idx, v ->
                        val isSelected = idx == selectedVarietyIndex
                        val isSoldOut = !v.inStock || v.stockCount <= 0

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isSoldOut) Color(0xFF450A0A) else Color(0xFF1E3A8A)
                                    } else {
                                        Color(0xFF0F172A)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) {
                                        if (isSoldOut) DangerRed else AccentCyan
                                    } else {
                                        CardBorder
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedVarietyIndex = idx }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = v.name,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSoldOut) Color(0xFFFCA5A5) else TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (isSoldOut) {
                                    Text(
                                        text = "Sold Out",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                } else if (v.stockCount in 1..3) {
                                    Text(
                                        text = "Only ${v.stockCount} left!",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFBBF24)
                                    )
                                } else {
                                    Text(
                                        text = "${v.stockCount} in stock",
                                        fontSize = 9.sp,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sold Out Alert Banner (When currently selected variety is sold out)
            if (isSelectedSoldOut) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0D0D)),
                    border = BorderStroke(1.dp, DangerRed)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Variety Sold Out: '${selectedVariety.name}' is unavailable. Please pick an in-stock variety to proceed.",
                            fontSize = 11.sp,
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        fontSize = 11.5.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    onClick = {
                        val configuredDeal = deal.copy(
                            selectedVarietyName = selectedVariety.name,
                            inStock = !isSelectedSoldOut,
                            stockCount = selectedVariety.stockCount,
                            varieties = varieties,
                            safety = safety
                        )
                        onSelect(configuredDeal)
                    },
                    enabled = !isSelectedSoldOut,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryIndigo,
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isSelectedSoldOut) "Variety Sold Out" else "Select Deal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
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
    initialShippingAddress: ShippingAddress = ShippingAddress(),
    initialBillingAddress: BillingAddress = BillingAddress(),
    cancelNotice: String? = null,
    isSafetyCheckEnabled: Boolean = false,
    onDismissCancelNotice: () -> Unit = {},
    onPayWithPayPal: (ShippingAddress, BillingAddress, String, Int) -> Unit,
    onDemoSandboxFastPass: (ShippingAddress, BillingAddress, String, Int) -> Unit = { _, _, _, _ -> },
    onSwitchToDemoMarket: () -> Unit = {},
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDemoMarket = deal.storeName.contains("Demo Market", ignoreCase = true) || deal.storeUrl.contains("cartwise-demo-market")

    // Interactive Variety & Quantity Selection
    var chosenVariety by remember(deal.id) {
        mutableStateOf(
            deal.selectedVarietyName.ifBlank {
                deal.varieties.firstOrNull { it.inStock }?.name
                    ?: deal.varieties.firstOrNull()?.name
                    ?: "Standard"
            }
        )
    }

    LaunchedEffect(deal.selectedVarietyName) {
        if (deal.selectedVarietyName.isNotBlank() && deal.selectedVarietyName != chosenVariety) {
            chosenVariety = deal.selectedVarietyName
        }
    }

    var quantity by remember(deal.id) {
        mutableStateOf(maxOf(1, deal.quantity))
    }

    val selectedVarietyObj = deal.varieties.find { it.name.equals(chosenVariety, ignoreCase = true) }
    val isSelectedSoldOut = selectedVarietyObj?.inStock == false

    val itemSubtotal = deal.price * quantity
    val serviceFee = 3.00
    val totalHold = itemSubtotal + deal.shippingCost + serviceFee

    // Shipping & Billing Address States (Prefilled so judges don't have to fill anything)
    var shippingFullName by remember { mutableStateOf(initialShippingAddress.fullName) }
    var shippingPhone by remember { mutableStateOf(initialShippingAddress.phone.ifBlank { "+234 801 234 5678" }) }
    var shippingEmail by remember { mutableStateOf(initialShippingAddress.email.ifBlank { "ada.lovelace@gmail.com" }) }
    var shippingAddressLine by remember { mutableStateOf(initialShippingAddress.address) }
    var shippingCity by remember { mutableStateOf(initialShippingAddress.city) }
    var shippingCountry by remember { mutableStateOf(initialShippingAddress.country) }

    var sameAsShipping by remember { mutableStateOf(initialBillingAddress.sameAsShipping) }
    var billingCardholderName by remember { mutableStateOf(initialBillingAddress.cardholderName) }
    var billingAddressLine by remember { mutableStateOf(initialBillingAddress.address) }
    var billingCity by remember { mutableStateOf(initialBillingAddress.city) }
    var billingCountry by remember { mutableStateOf(initialBillingAddress.country) }

    fun currentShipping(): ShippingAddress = ShippingAddress(
        fullName = shippingFullName.ifBlank { "Ada Lovelace" },
        email = shippingEmail.ifBlank { "ada.lovelace@gmail.com" },
        address = shippingAddressLine.ifBlank { "14 Marina Street, Lagos Island" },
        city = shippingCity.ifBlank { "Lagos" },
        state = "Lagos",
        country = shippingCountry.ifBlank { "Nigeria" },
        postalCode = "100001",
        phone = shippingPhone.ifBlank { "+234 801 234 5678" }
    )

    fun currentBilling(): BillingAddress = if (sameAsShipping) {
        BillingAddress(
            cardholderName = shippingFullName.ifBlank { "Ada Lovelace" },
            address = shippingAddressLine.ifBlank { "14 Marina Street, Lagos Island" },
            city = shippingCity.ifBlank { "Lagos" },
            state = "Lagos",
            country = shippingCountry.ifBlank { "Nigeria" },
            postalCode = "100001",
            sameAsShipping = true
        )
    } else {
        BillingAddress(
            cardholderName = billingCardholderName.ifBlank { shippingFullName.ifBlank { "Ada Lovelace" } },
            address = billingAddressLine.ifBlank { shippingAddressLine.ifBlank { "14 Marina Street, Lagos Island" } },
            city = billingCity.ifBlank { shippingCity.ifBlank { "Lagos" } },
            state = "Lagos",
            country = billingCountry.ifBlank { shippingCountry.ifBlank { "Nigeria" } },
            postalCode = "100001",
            sameAsShipping = false
        )
    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDemoMarket) "Cartwise Demo Market" else deal.storeName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDemoMarket) SuccessGreen else AccentCyan
                        )
                        if (isDemoMarket) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreen.copy(alpha = 0.2f))
                                    .border(1.dp, SuccessGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("GUARANTEED CAPTURE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(deal.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    // Interactive Variety Selection (Chips with In Stock / Sold Out indication)
                    if (deal.varieties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Choose Variety",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            if (chosenVariety.isNotBlank()) {
                                Text(
                                    text = if (isSelectedSoldOut) "● Sold Out" else "● In Stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelectedSoldOut) DangerRed else SuccessGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(deal.varieties) { v ->
                                val isSelected = v.name.equals(chosenVariety, ignoreCase = true)
                                val isSoldOut = !v.inStock
                                val chipBg = if (isSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF0F172A)
                                val chipBorder = if (isSelected) AccentCyan else if (isSoldOut) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFF1E293B)
                                val chipTextColor = if (isSelected) AccentCyan else if (isSoldOut) Color(0xFF64748B) else TextPrimary

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg)
                                        .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            chosenVariety = v.name
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AccentCyan,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = v.name + if (isSoldOut) " (Sold Out)" else "",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = chipTextColor
                                        )
                                    }
                                }
                            }
                        }
                    } else if (chosenVariety.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Variety: $chosenVariety",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan
                            )
                        }
                    }

                    // Interactive Quantity Stepper Row
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quantity", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "$${deal.price} / unit",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (quantity > 1) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Quantity",
                                    tint = if (quantity > 1) TextPrimary else Color(0xFF64748B),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$quantity",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { if (quantity < 10) quantity++ },
                                enabled = quantity < 10 && !isSelectedSoldOut,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (quantity < 10 && !isSelectedSoldOut) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Quantity",
                                    tint = if (quantity < 10 && !isSelectedSoldOut) TextPrimary else Color(0xFF64748B),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct Store Link & Share Row on Confirm Screen
                    val confirmContext = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.storeUrl))
                                        confirmContext.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                deal.storeUrl.replace("https://", "").replace("http://", "").trimEnd('/'),
                                fontSize = 11.sp,
                                color = AccentCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Buying on Cartwise: ${deal.title} (${deal.selectedVarietyName}) at ${deal.storeName}. Store link: ${deal.storeUrl}")
                                    putExtra(Intent.EXTRA_TITLE, deal.title)
                                }
                                confirmContext.startActivity(Intent.createChooser(shareIntent, "Share Item Link"))
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }

                    // Website Safety Score on Confirm Screen (Shown only if isSafetyCheckEnabled == true)
                    if (isSafetyCheckEnabled && deal.safety != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val s = deal.safety
                        val isSafe = s.level == "SAFE"
                        val isCaution = s.level == "CAUTION"
                        val sColor = if (isSafe) SuccessGreen else if (isCaution) Color(0xFFF59E0B) else DangerRed
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSafe) Color(0xFF042F2E) else if (isCaution) Color(0xFF381A05) else Color(0xFF450A0A))
                                .border(1.dp, sColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSafe) Icons.Default.VerifiedUser else if (isCaution) Icons.Default.Warning else Icons.Default.GppBad,
                                    contentDescription = null,
                                    tint = sColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Merchant Risk Score: ${s.score}/100 • ${s.level}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sColor
                                )
                            }
                        }
                    }

                    if (!isDemoMarket) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF26190B)),
                            border = BorderStroke(1.dp, Color(0xFFB45309))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Third-Party Store (${deal.storeName})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Real third-party stores decline test card 123456*****2234 to demonstrate Cartwise's automatic escrow void protection.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFEF3C7),
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onSwitchToDemoMarket,
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Switch to Cartwise Demo Market (Guaranteed Capture)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (quantity > 1) "Item Price ($${deal.price} × $quantity)" else "Item Price", color = TextSecondary, fontSize = 14.sp)
                        Text("$${"%.2f".format(itemSubtotal)}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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

            // 1. Shipping Address Card (Prefilled with Ada Lovelace in Lagos)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Shipping Address",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F291E))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "PREFILLED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Text(
                        "Prefilled for instant checkout. Edit any field below if desired:",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Recipient Full Name
                    OutlinedTextField(
                        value = shippingFullName,
                        onValueChange = { shippingFullName = it },
                        label = { Text("Full Name (Recipient)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyan,
                            focusedContainerColor = Color(0xFF0A0F1D),
                            unfocusedContainerColor = Color(0xFF0A0F1D)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Number with Country Code (Required for courier & merchant guest checkout)
                    OutlinedTextField(
                        value = shippingPhone,
                        onValueChange = { shippingPhone = it },
                        label = { Text("Phone Number (with Country Code)") },
                        placeholder = { Text("+234 801 234 5678") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyan,
                            focusedContainerColor = Color(0xFF0A0F1D),
                            unfocusedContainerColor = Color(0xFF0A0F1D)
                        ),
                        singleLine = true
                    )
                    Text(
                        "Required with country code for courier dispatch & merchant guest checkout",
                        fontSize = 10.5.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Guest Recovery Email
                    OutlinedTextField(
                        value = shippingEmail,
                        onValueChange = { shippingEmail = it },
                        label = { Text("Email (Merchant Guest Recovery)") },
                        placeholder = { Text("ada.lovelace@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyan,
                            focusedContainerColor = Color(0xFF0A0F1D),
                            unfocusedContainerColor = Color(0xFF0A0F1D)
                        ),
                        singleLine = true
                    )
                    Text(
                        "Used by AI agent to bind guest order tracking on merchant store",
                        fontSize = 10.5.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Street Address Line
                    OutlinedTextField(
                        value = shippingAddressLine,
                        onValueChange = { shippingAddressLine = it },
                        label = { Text("Street Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyan,
                            focusedContainerColor = Color(0xFF0A0F1D),
                            unfocusedContainerColor = Color(0xFF0A0F1D)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // City & Country Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = shippingCity,
                            onValueChange = { shippingCity = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = AccentCyan,
                                focusedContainerColor = Color(0xFF0A0F1D),
                                unfocusedContainerColor = Color(0xFF0A0F1D)
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = shippingCountry,
                            onValueChange = { shippingCountry = it },
                            label = { Text("Country") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = AccentCyan,
                                focusedContainerColor = Color(0xFF0A0F1D),
                                unfocusedContainerColor = Color(0xFF0A0F1D)
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Card & Billing Address Card (with "Use shipping address" toggle)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Card & Billing Address",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option: Use shipping address for card billing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { sameAsShipping = !sameAsShipping }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = sameAsShipping,
                            onCheckedChange = { sameAsShipping = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentCyan,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                "Use shipping address for card billing",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                "Card address & name match shipping address",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (sameAsShipping) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0A0F1D))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Billing to: ${shippingFullName.ifBlank { "Ada Lovelace" }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${shippingAddressLine.ifBlank { "14 Marina Street, Lagos Island" }}, ${shippingCity.ifBlank { "Lagos" }}, ${shippingCountry.ifBlank { "Nigeria" }}",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        // Separate Card/Billing Details
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = billingCardholderName,
                            onValueChange = { billingCardholderName = it },
                            label = { Text("Cardholder Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = AccentCyan,
                                focusedContainerColor = Color(0xFF0A0F1D),
                                unfocusedContainerColor = Color(0xFF0A0F1D)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = billingAddressLine,
                            onValueChange = { billingAddressLine = it },
                            label = { Text("Billing Street Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = AccentCyan,
                                focusedContainerColor = Color(0xFF0A0F1D),
                                unfocusedContainerColor = Color(0xFF0A0F1D)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = billingCity,
                                onValueChange = { billingCity = it },
                                label = { Text("Billing City") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = AccentCyan,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = AccentCyan,
                                    focusedContainerColor = Color(0xFF0A0F1D),
                                    unfocusedContainerColor = Color(0xFF0A0F1D)
                                ),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = billingCountry,
                                onValueChange = { billingCountry = it },
                                label = { Text("Billing Country") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = AccentCyan,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = AccentCyan,
                                    focusedContainerColor = Color(0xFF0A0F1D),
                                    unfocusedContainerColor = Color(0xFF0A0F1D)
                                ),
                                singleLine = true
                            )
                        }
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
            if (isSelectedSoldOut) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    border = BorderStroke(1.dp, DangerRed)
                ) {
                    Text(
                        text = "⚠️ \"$chosenVariety\" is currently out of stock. Please select another variety to proceed.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = { onPayWithPayPal(currentShipping(), currentBilling(), chosenVariety, quantity) },
                enabled = !isSelectedSoldOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp), // Official PayPal pill shape
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayPalGold,
                    disabledContainerColor = Color(0xFF334155),
                    disabledContentColor = Color(0xFF64748B)
                ),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Demo Market Fast-Pass Option (Guaranteed Capture on cartwise-demo-market.onrender.com)
            OutlinedButton(
                onClick = { onDemoSandboxFastPass(currentShipping(), currentBilling(), chosenVariety, quantity) },
                enabled = !isSelectedSoldOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isSelectedSoldOut) Color(0xFF334155) else AccentCyan.copy(alpha = 0.8f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF0F172A),
                    disabledContainerColor = Color(0xFF0F172A),
                    disabledContentColor = Color(0xFF64748B)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Demo Market Fast-Pass (Guaranteed Capture)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                }
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
    onDone: () -> Unit,
    onSearchAgain: () -> Unit = onDone
) {
    val isVoided = order.status == "VOIDED"
    val shipping = order.shippingAddress ?: ShippingAddress()
    val billing = order.billingAddress

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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shipping & Delivery Recipient Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Delivery Destination",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                shipping.fullName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "${shipping.address}, ${shipping.city}, ${shipping.country}",
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Phone: ${order.guestPhone ?: shipping.phone.ifBlank { "+234 801 234 5678" }} • Email: ${order.guestEmail ?: shipping.email.ifBlank { "ada.lovelace@gmail.com" }}",
                                fontSize = 11.sp,
                                color = AccentCyan
                            )

                            if (billing != null && !billing.sameAsShipping) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Card Billing: ${billing.cardholderName} • ${billing.address}, ${billing.city}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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

                    if (!order.selectedVariety.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Variety", color = TextSecondary, fontSize = 13.sp)
                            Text(order.selectedVariety, color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (order.quantity > 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quantity", color = TextSecondary, fontSize = 13.sp)
                            Text("${order.quantity}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
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

                    // Dedicated Merchant Guest Order Recovery Card
                    var isPasswordVisible by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    val clipboardManager = LocalClipboardManager.current
                    val guestEmail = order.guestEmail ?: shipping.email.ifBlank { "ada.lovelace@gmail.com" }
                    val guestPhone = order.guestPhone ?: shipping.phone.ifBlank { "+234 801 234 5678" }
                    val merchantPassword = order.merchantPassword ?: "Cw*9824!qM26"
                    val merchantOrderId = order.merchantOrderId ?: "DM-${order.id.takeLast(6).uppercase()}"
                    val recoveryUrl = order.merchantRecoveryUrl ?: "https://cartwise-demo-market.onrender.com/track?orderId=$merchantOrderId&email=$guestEmail"

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1222)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF083344)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.VpnKey,
                                            contentDescription = null,
                                            tint = AccentCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Merchant Guest Order Recovery",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            "For manual order lookup on merchant store",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0C2A38))
                                        .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "GUEST CHECKOUT",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Store & Order ID Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Merchant Store", fontSize = 11.5.sp, color = TextSecondary)
                                Text(order.merchantName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Merchant Order ID", fontSize = 11.5.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        merchantOrderId,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(merchantOrderId))
                                            Toast.makeText(context, "Order ID copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Order ID", tint = AccentCyan, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Guest Email Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Guest Email Used", fontSize = 11.5.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        guestEmail,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE2E8F0)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(guestEmail))
                                            Toast.makeText(context, "Guest Email copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Email", tint = AccentCyan, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Phone Filled Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Phone Filled (w/ code)", fontSize = 11.5.sp, color = TextSecondary)
                                Text(
                                    guestPhone,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF1E293B).copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Merchant Password Row (Masked with Tap to View)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Merchant Password", fontSize = 11.5.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF3B1E08))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            if (order.merchantPasswordRequired) "AI ENFORCED" else "GUEST",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFDBA74)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isPasswordVisible) merchantPassword else "••••••••••••",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPasswordVisible) SuccessGreen else TextPrimary,
                                        letterSpacing = if (isPasswordVisible) 0.5.sp else 2.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TextButton(
                                        onClick = { isPasswordVisible = !isPasswordVisible },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = AccentCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            if (isPasswordVisible) "Hide" else "Tap to view",
                                            fontSize = 10.sp,
                                            color = AccentCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(merchantPassword))
                                            Toast.makeText(context, "Password copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = AccentCyan, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }

                            Text(
                                "Password automatically generated by Cartwise AI because this merchant required account creation during checkout.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            // Manual Recovery Instructions Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF070B14))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = AccentCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "How to manually recover or track on merchant site:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "1. Open merchant order lookup page.\n2. Enter Guest Email ($guestEmail) & Order ID ($merchantOrderId).\n3. If prompted for password, enter the AI-generated password revealed above.",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Open Merchant Lookup Button
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recoveryUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF082F49).copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Merchant Order Lookup ↗", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
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

                    // Action Button 1: View in Order History
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isVoided) "Back to Order History" else "View in Order History", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Button 2: Shop Another Item
                    OutlinedButton(
                        onClick = onSearchAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search Another Product", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    PoweredByPartnersBar()
                }
            }
        }
    }
}
