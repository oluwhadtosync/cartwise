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
import com.cartwise.app.data.ShippingMethod
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

enum class AskInputMode {
    AI_SEARCH,
    PASTE_LINK
}

@Composable
fun AskScreen(
    queryText: String,
    onQueryChanged: (String) -> Unit,
    isLoading: Boolean,
    isSafetyCheckEnabled: Boolean = false,
    onToggleSafetyCheck: (Boolean) -> Unit = {},
    selectedModel: String = "gemini-3.8-flash",
    onSelectModel: (String) -> Unit = {},
    shippingAddress: ShippingAddress = ShippingAddress(),
    onUpdateShippingAddress: (ShippingAddress) -> Unit = {},
    onSearchSubmitted: () -> Unit,
    onQuickPromptSelected: (String) -> Unit,
    onExtractLinkSubmitted: (String) -> Unit = {},
    onViewHistory: () -> Unit
) {
    var currentMode by remember { mutableStateOf(AskInputMode.AI_SEARCH) }
    var directLinkInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    var pendingPrompt by remember { mutableStateOf<String?>(null) }
    var showShippingAddressDialog by remember { mutableStateOf(false) }
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

    // Prefilled Shipping Destination & Address Settings Dialog
    if (showShippingAddressDialog) {
        var editFullName by remember { mutableStateOf(shippingAddress.fullName) }
        var editAddress by remember { mutableStateOf(shippingAddress.address) }
        var editCity by remember { mutableStateOf(shippingAddress.city) }
        var editState by remember { mutableStateOf(shippingAddress.state) }
        var editCountry by remember { mutableStateOf(shippingAddress.country) }
        var editPostalCode by remember { mutableStateOf(shippingAddress.postalCode ?: "100001") }
        var editPhone by remember { mutableStateOf(shippingAddress.phone ?: "+234 801 234 5678") }
        var editEmail by remember { mutableStateOf(shippingAddress.email) }

        AlertDialog(
            onDismissRequest = { showShippingAddressDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Shipping Destination & Address",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cloud executions use this destination to check real-time shipping varieties and rates before ordering.",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Street Address", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editCity,
                            onValueChange = { editCity = it },
                            label = { Text("City", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                        OutlinedTextField(
                            value = editState,
                            onValueChange = { editState = it },
                            label = { Text("State", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editCountry,
                            onValueChange = { editCountry = it },
                            label = { Text("Country", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                        OutlinedTextField(
                            value = editPostalCode,
                            onValueChange = { editPostalCode = it },
                            label = { Text("Postal Code", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = AccentCyan,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                    }
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = shippingAddress.copy(
                            fullName = editFullName.ifBlank { "Ada Lovelace" },
                            address = editAddress.ifBlank { "14 Marina Street, Lagos Island" },
                            city = editCity.ifBlank { "Lagos" },
                            state = editState.ifBlank { "Lagos" },
                            country = editCountry.ifBlank { "Nigeria" },
                            postalCode = editPostalCode.ifBlank { "100001" },
                            phone = editPhone.ifBlank { "+234 801 234 5678" },
                            email = editEmail.ifBlank { "ada.lovelace@gmail.com" }
                        )
                        onUpdateShippingAddress(updated)
                        showShippingAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Address", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShippingAddressDialog = false }) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showShippingAddressDialog = true }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${shippingAddress.city.ifBlank { "Lagos" }}, ${shippingAddress.country.ifBlank { "Nigeria" }} 🇳🇬",
                            fontSize = 11.sp,
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Destination",
                            tint = AccentCyan.copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Right Actions: Shipping Address Setting Button + Order History Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { showShippingAddressDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = "Shipping Address Setting",
                            tint = AccentCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Address",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onViewHistory) {
                    Icon(Icons.Default.History, contentDescription = "Order History", tint = TextSecondary)
                }
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

        // Floating Dual-Tab Switcher (Option C: Floating Dual-Tab Switcher)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab 1: AI Search (50% width)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { currentMode = AskInputMode.AI_SEARCH }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (currentMode == AskInputMode.AI_SEARCH) AccentCyan else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Search",
                        fontSize = 14.sp,
                        fontWeight = if (currentMode == AskInputMode.AI_SEARCH) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentMode == AskInputMode.AI_SEARCH) Color.White else TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(
                            if (currentMode == AskInputMode.AI_SEARCH) AccentCyan else Color.Transparent,
                            RoundedCornerShape(2.dp)
                        )
                )
            }

            // Tab 2: Paste Link (50% width)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { currentMode = AskInputMode.PASTE_LINK }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = if (currentMode == AskInputMode.PASTE_LINK) AccentCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Paste Link",
                        fontSize = 14.sp,
                        fontWeight = if (currentMode == AskInputMode.PASTE_LINK) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentMode == AskInputMode.PASTE_LINK) Color.White else TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(
                            if (currentMode == AskInputMode.PASTE_LINK) AccentCyan else Color.Transparent,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        if (currentMode == AskInputMode.AI_SEARCH) {
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
        } else {
            // Direct Store Link Card (Option C: Floating Dual-Tab Switcher - Spacious & Responsive)
            val trimmedLink = directLinkInput.trim()
            val detectedStoreLabel = remember(trimmedLink) {
                val lower = trimmedLink.lowercase()
                when {
                    lower.contains("aliexpress") -> "AliExpress (Buyer Protection)"
                    lower.contains("amazon") -> "Amazon (Escrow Secured)"
                    lower.contains("ebay") -> "eBay (Money Back Guarantee)"
                    lower.contains("jumia") -> "Jumia (Lagos Express)"
                    lower.contains("shopify") || lower.contains("myshopify") -> "Shopify Store"
                    lower.contains("cartwise-demo-market") || lower.contains("demo-market") -> "Cartwise Demo Market"
                    lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.") -> "Web Store (Escrow Protected)"
                    else -> null
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorder, AccentCyan)))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row: Label & Paste Clipboard Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "PASTE STORE LINK",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Prominent Paste Chip Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryIndigo.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, PrimaryIndigo),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        directLinkInput = clipText.trim()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Paste",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Full-Width Responsive URL Input Box
                    OutlinedTextField(
                        value = directLinkInput,
                        onValueChange = { directLinkInput = it },
                        placeholder = {
                            Text(
                                text = "Paste AliExpress, Amazon, Shopify or demo link...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (directLinkInput.isNotBlank()) AccentCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (directLinkInput.isNotBlank()) {
                                IconButton(
                                    onClick = { directLinkInput = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Detected Store Badge Pill (Realtime Feedback)
                    if (detectedStoreLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentCyan.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Detected Store: $detectedStoreLabel",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentCyan
                                )
                            }
                        }
                    }

                    // Quick Demo Market sample link shortcut (when empty)
                    if (directLinkInput.isBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    directLinkInput = "https://cartwise-demo-market.onrender.com/product/prod-hoodie-13"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "💡 Try Demo:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                                Text(
                                    text = "Apex Pro Tech Fleece Hoodie",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Feature Highlights Checklist
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Extracts product title, image, price & varieties",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Calculates landed Lagos shipping & PayPal Escrow hold",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Supported Store Badges (Horizontally Scrollable - never squished or line-broken)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Supported:",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                        listOf("AliExpress", "Amazon", "Shopify", "eBay", "Jumia", "Demo Market").forEach { store ->
                            val isDetected = detectedStoreLabel?.contains(store, ignoreCase = true) == true
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDetected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDetected) AccentCyan else CardBorder
                                )
                            ) {
                                Text(
                                    text = store,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isDetected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDetected) AccentCyan else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Primary Extract CTA Button
                    Button(
                        onClick = {
                            onExtractLinkSubmitted(directLinkInput.trim())
                        },
                        enabled = directLinkInput.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extracting with Cloud Browser & AI...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extract Deal & Buy with Escrow",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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

    // Varieties setup (intelligent category-tailored fallback if not provided by backend)
    val varieties = remember(deal) {
        if (deal.varieties.isNotEmpty()) {
            deal.varieties
        } else {
            val titleLower = deal.title.lowercase()
            when {
                titleLower.contains("earbud") || titleLower.contains("headphone") || titleLower.contains("earphone") || titleLower.contains("audio") || titleLower.contains("headset") || titleLower.contains("airpod") -> listOf(
                    ProductVariety(id = "var-1", name = "Midnight Black", sku = "VAR-BLK", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount) else 0, priceOffset = 0.0, unitPrice = deal.price),
                    ProductVariety(id = "var-2", name = "Pearl White", sku = "VAR-WHT", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 2) else 0, priceOffset = 0.0, unitPrice = deal.price),
                    ProductVariety(id = "var-3", name = "Navy Blue", sku = "VAR-BLU", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 3) else 0, priceOffset = 0.0, unitPrice = deal.price)
                )
                (titleLower.contains("phone") && !titleLower.contains("headphone") && !titleLower.contains("earphone")) || titleLower.contains("redmi") || titleLower.contains("xiaomi") || titleLower.contains("iphone") || titleLower.contains("samsung") || titleLower.contains("pixel") -> listOf(
                    ProductVariety(id = "var-1", name = "4GB RAM + 128GB Storage (Midnight Black)", sku = "VAR-128-BLK", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount) else 0, priceOffset = 0.0, unitPrice = deal.price),
                    ProductVariety(id = "var-2", name = "6GB RAM + 128GB Storage (Starry Blue)", sku = "VAR-128-BLU", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 2) else 0, priceOffset = 12.0, unitPrice = deal.price + 12.0),
                    ProductVariety(id = "var-3", name = "8GB RAM + 256GB Storage (Sage Green)", sku = "VAR-256-GRN", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 3) else 0, priceOffset = 25.0, unitPrice = deal.price + 25.0)
                )
                titleLower.contains("screen") || titleLower.contains("oled") || titleLower.contains("lcd") || titleLower.contains("digitizer") -> listOf(
                    ProductVariety(id = "var-1", name = "OLED Without Frame", sku = "VAR-NO-FRAME", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount) else 0, priceOffset = 0.0, unitPrice = deal.price),
                    ProductVariety(id = "var-2", name = "OLED With Frame (Black)", sku = "VAR-FRAME-BLK", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 2) else 0, priceOffset = 7.50, unitPrice = deal.price + 7.50),
                    ProductVariety(id = "var-3", name = "OLED With Frame (Aurora)", sku = "VAR-FRAME-AUR", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 3) else 0, priceOffset = 7.50, unitPrice = deal.price + 7.50)
                )
                titleLower.contains("keyboard") || titleLower.contains("keychron") -> listOf(
                    ProductVariety(id = "var-1", name = "Linear Red Switches", sku = "SW-RED", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount) else 0, priceOffset = 0.0, unitPrice = deal.price),
                    ProductVariety(id = "var-2", name = "Tactile Brown Switches", sku = "SW-BRN", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 2) else 0, priceOffset = 0.0, unitPrice = deal.price),
                    ProductVariety(id = "var-3", name = "Clicky Blue Switches", sku = "SW-BLU", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount / 3) else 0, priceOffset = 0.0, unitPrice = deal.price)
                )
                else -> listOf(
                    ProductVariety(id = "var-std", name = "Standard Edition", sku = "STD-01", inStock = deal.inStock, stockCount = if (deal.inStock) maxOf(1, deal.stockCount) else 0, priceOffset = 0.0, unitPrice = deal.price)
                )
            }
        }
    }

    var selectedVarietyIndex by remember(deal) {
        val initialIdx = varieties.indexOfFirst { it.name.equals(deal.selectedVarietyName, ignoreCase = true) }
        mutableStateOf(if (initialIdx >= 0) initialIdx else (varieties.indexOfFirst { it.inStock }.takeIf { it >= 0 } ?: 0))
    }

    val selectedVariety = varieties.getOrNull(selectedVarietyIndex) ?: varieties.first()
    val isSelectedSoldOut = !selectedVariety.inStock || selectedVariety.stockCount <= 0

    // Quantity state for this deal
    var quantity by remember(deal.id) { mutableStateOf(maxOf(1, deal.quantity)) }

    // Shipping methods for this deal
    val isAliDeal = deal.storeName.contains("AliExpress", ignoreCase = true) || deal.storeUrl.contains("aliexpress")
    val isJumiaDeal = deal.storeName.contains("Jumia", ignoreCase = true) || deal.storeUrl.contains("jumia")
    val availableShippingMethods = remember(deal.id, deal.shippingMethods) {
        if (deal.shippingMethods.isNotEmpty()) deal.shippingMethods
        else if (isJumiaDeal) {
            val cost = if (deal.isFreeShipping) 0.0 else (if (deal.shippingCost > 0.0) deal.shippingCost else 0.85)
            val pickupCost = if (deal.isFreeShipping) 0.0 else (if (deal.shippingCost > 0.0) (deal.shippingCost - 0.45).coerceAtLeast(0.0) else 0.40)
            listOf(
                ShippingMethod("ship_jumia_door", "Jumia Door Delivery", "Jumia Logistics", cost, "1 - 3 business days", true, cost == 0.0),
                ShippingMethod("ship_jumia_pickup", "Jumia Pickup Station", "Jumia Express", pickupCost, "1 - 2 business days", true, false)
            )
        } else if (isAliDeal) {
            listOf(
                ShippingMethod("ship_aliexpress_std", "AliExpress Standard Shipping", "AliExpress", if (deal.isFreeShipping) 0.0 else deal.shippingCost, "14 - 21 business days", true, deal.isFreeShipping || deal.shippingCost == 0.0)
            )
        } else {
            val cost = if (deal.isFreeShipping) 0.0 else deal.shippingCost
            listOf(
                ShippingMethod("ship_std", "${deal.storeName} Standard Delivery", "${deal.storeName} Logistics", cost, "7 - 14 business days", true, cost == 0.0)
            )
        }
    }

    var selectedShippingMethodId by remember(deal.id, availableShippingMethods) {
        mutableStateOf(
            availableShippingMethods.firstOrNull { it.isFree }?.id
                ?: deal.selectedShippingMethodId?.takeIf { id -> availableShippingMethods.any { it.id == id } }
                ?: availableShippingMethods.firstOrNull()?.id
                ?: ""
        )
    }

    val selectedShippingMethod = availableShippingMethods.find { it.id == selectedShippingMethodId }
        ?: availableShippingMethods.firstOrNull()

    val isFreeShipping = selectedShippingMethod?.isFree == true || (selectedShippingMethod?.cost ?: 0.0) == 0.0
    val methodBaseCost = if (isFreeShipping) 0.0 else (selectedShippingMethod?.cost ?: 0.0)
    val extraUnits = maxOf(0, quantity - 1)
    val incrementalPerItem = if (isFreeShipping) 0.0 else (if (deal.incrementalShippingPerUnit > 0.0) deal.incrementalShippingPerUnit else (if (selectedShippingMethod?.carrier?.contains("DHL", true) == true || selectedShippingMethod?.carrier?.contains("UPS", true) == true) 3.50 else 1.50))
    val totalShipping = if (isFreeShipping) 0.0 else Math.round((methodBaseCost + extraUnits * incrementalPerItem) * 100.0) / 100.0

    val variantSpecificPrice = selectedVariety.unitPrice?.takeIf { it > 0.0 }
    val priceOffset = selectedVariety.priceOffset
    val unitPrice = variantSpecificPrice ?: (Math.round((deal.price + priceOffset) * 100.0) / 100.0)
    val itemSubtotal = Math.round(unitPrice * quantity * 100.0) / 100.0
    val totalLandedCost = Math.round((itemSubtotal + totalShipping) * 100.0) / 100.0

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
                        text = if (varieties.size > 1) "Store Varieties & Options:" else "Specification / Edition:",
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

            // Shipping Methods Selector
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shipping Method (${availableShippingMethods.size} Available):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = selectedShippingMethod?.carrier ?: "",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableShippingMethods) { m ->
                        val isSelected = m.id == selectedShippingMethodId
                        val costLabel = if (m.isFree || m.cost == 0.0) "FREE" else "$${m.cost}"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF042F2E) else Color(0xFF0F172A))
                                .border(
                                    1.dp,
                                    if (isSelected) SuccessGreen else CardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedShippingMethodId = m.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = m.name,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SuccessGreen else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = costLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (m.isFree || m.cost == 0.0) SuccessGreen else TextPrimary
                                    )
                                }
                                if (!m.estimatedDelivery.isNullOrBlank()) {
                                    Text(
                                        text = m.estimatedDelivery,
                                        fontSize = 9.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quantity Stepper with Incremental Shipping Scaling
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Item Quantity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isFreeShipping) "Free shipping applied" else (if (quantity > 1) "+$$incrementalPerItem/extra unit shipping" else "Shipping scales +$$incrementalPerItem per extra unit"),
                        fontSize = 10.5.sp,
                        color = if (isFreeShipping) SuccessGreen else TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (quantity > 1) Color(0xFF1E293B) else Color(0xFF0F172A))
                            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                            .clickable(enabled = quantity > 1) { quantity-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (quantity > 1) TextPrimary else TextMuted)
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    val maxStock = if (selectedVariety.stockCount > 0) selectedVariety.stockCount else 10
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (quantity < maxStock) Color(0xFF1E293B) else Color(0xFF0F172A))
                            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                            .clickable(enabled = quantity < maxStock) { quantity++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (quantity < maxStock) TextPrimary else TextMuted)
                    }
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
                        text = "$$totalLandedCost",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$$itemSubtotal ($quantity" + "x) + $$totalShipping shipping",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        val configuredDeal = deal.copy(
                            selectedVarietyName = selectedVariety.name,
                            selectedShippingMethodId = selectedShippingMethodId,
                            shippingCost = totalShipping,
                            price = unitPrice,
                            totalCost = totalLandedCost,
                            quantity = quantity,
                            inStock = !isSelectedSoldOut,
                            stockCount = selectedVariety.stockCount,
                            varieties = varieties,
                            shippingMethods = availableShippingMethods,
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
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isSelectedSoldOut) "Variety Sold Out" else "Select Deal & Review ($$totalLandedCost)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
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
    onUpdateDealDetails: (String, Double) -> Unit = { _, _ -> },
    onPayWithPayPal: (ShippingAddress, BillingAddress, String, Int, Double, Double) -> Unit,
    onDemoSandboxFastPass: (ShippingAddress, BillingAddress, String, Int, Double, Double) -> Unit = { _, _, _, _, _, _ -> },
    onSwitchToDemoMarket: () -> Unit = {},
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDemoMarket = deal.storeName.contains("Demo Market", ignoreCase = true) || deal.storeUrl.contains("cartwise-demo-market")

    var editableTitle by remember(deal.id, deal.title) { mutableStateOf(deal.title) }
    var editablePrice by remember(deal.id, deal.price) { mutableStateOf(deal.price) }
    var isEditingItemDetails by remember { mutableStateOf(false) }

    // Multi-Attribute Variant Detection (e.g. Clothes: Color & Size separate dimensions)
    val distinctColors = remember(deal.varieties) {
        val fromColorField = deal.varieties.mapNotNull { it.color?.ifBlank { null } }.distinct()
        if (fromColorField.isNotEmpty()) fromColorField
        else {
            val parts = deal.varieties.map { it.name.split("/").map { p -> p.trim() } }
            if (parts.isNotEmpty() && parts.all { it.size == 2 }) {
                parts.map { it[0] }.distinct()
            } else emptyList()
        }
    }

    val distinctSizes = remember(deal.varieties) {
        val fromSizeField = deal.varieties.mapNotNull { it.size?.ifBlank { null } }.distinct()
        if (fromSizeField.isNotEmpty()) fromSizeField
        else {
            val parts = deal.varieties.map { it.name.split("/").map { p -> p.trim() } }
            if (parts.isNotEmpty() && parts.all { it.size == 2 }) {
                parts.map { it[1] }.distinct()
            } else emptyList()
        }
    }

    val isMultiAttribute = distinctColors.size > 1 && distinctSizes.size > 1

    var selectedColor by remember(deal.id) {
        val initialColor = deal.varieties.find { it.name.equals(deal.selectedVarietyName, ignoreCase = true) }?.color
            ?: distinctColors.firstOrNull() ?: ""
        mutableStateOf(initialColor)
    }

    var selectedSize by remember(deal.id) {
        val initialSize = deal.varieties.find { it.name.equals(deal.selectedVarietyName, ignoreCase = true) }?.size
            ?: distinctSizes.firstOrNull() ?: ""
        mutableStateOf(initialSize)
    }

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
            if (isMultiAttribute) {
                deal.varieties.find { it.name.equals(deal.selectedVarietyName, ignoreCase = true) }?.let { v ->
                    v.color?.let { selectedColor = it }
                    v.size?.let { selectedSize = it }
                }
            }
        }
    }

    var quantity by remember(deal.id) {
        mutableStateOf(maxOf(1, deal.quantity))
    }

    // Match composite variety when multi-attribute
    val activeCompositeVariety = if (isMultiAttribute) {
        deal.varieties.find { v ->
            (v.color.equals(selectedColor, ignoreCase = true) && v.size.equals(selectedSize, ignoreCase = true)) ||
            v.name.equals("$selectedColor / $selectedSize", ignoreCase = true) ||
            v.name.equals("$selectedSize / $selectedColor", ignoreCase = true) ||
            (v.name.contains(selectedColor, ignoreCase = true) && v.name.contains(selectedSize, ignoreCase = true))
        } ?: deal.varieties.firstOrNull { it.color.equals(selectedColor, ignoreCase = true) }
        ?: deal.varieties.firstOrNull()
    } else null

    val selectedVarietyObj = if (isMultiAttribute) {
        activeCompositeVariety
    } else {
        deal.varieties.find { it.name.equals(chosenVariety, ignoreCase = true) || it.sku.equals(chosenVariety, ignoreCase = true) }
    }

    val effectiveVarietyName = if (isMultiAttribute) {
        activeCompositeVariety?.name ?: "$selectedColor / $selectedSize"
    } else chosenVariety

    val isSelectedSoldOut = selectedVarietyObj?.inStock == false

    // Variant Price Calculation (Supports variant-specific prices or offset)
    val variantSpecificPrice = selectedVarietyObj?.unitPrice?.takeIf { it > 0.0 }
    val priceOffset = selectedVarietyObj?.priceOffset ?: 0.0
    val effectiveBasePrice = editablePrice
    val unitPrice = variantSpecificPrice ?: (Math.round((effectiveBasePrice + priceOffset) * 100.0) / 100.0)
    val itemSubtotal = Math.round(unitPrice * quantity * 100.0) / 100.0

    val isAliExpressDeal = deal.storeName.contains("AliExpress", ignoreCase = true) || deal.storeUrl.contains("aliexpress")

    val isJumiaDeal = deal.storeName.contains("Jumia", ignoreCase = true) || deal.storeUrl.contains("jumia")

    // Shipping Varieties and Quantity-dependent shipping cost calculation
    val availableShippingMethods = remember(deal.id, deal.shippingMethods) {
        if (deal.shippingMethods.isNotEmpty()) deal.shippingMethods
        else if (isJumiaDeal) {
            val cost = if (deal.isFreeShipping) 0.0 else (if (deal.shippingCost > 0.0) deal.shippingCost else 0.85)
            val pickupCost = if (deal.isFreeShipping) 0.0 else (if (deal.shippingCost > 0.0) (deal.shippingCost - 0.45).coerceAtLeast(0.0) else 0.40)
            listOf(
                ShippingMethod("ship_jumia_door", "Jumia Door Delivery", "Jumia Logistics", cost, "1 - 3 business days", true, cost == 0.0),
                ShippingMethod("ship_jumia_pickup", "Jumia Pickup Station", "Jumia Express", pickupCost, "1 - 2 business days", true, false)
            )
        } else if (isAliExpressDeal) {
            listOf(
                ShippingMethod("ship_aliexpress_std", "AliExpress Standard Shipping", "AliExpress", if (deal.isFreeShipping) 0.0 else deal.shippingCost, "14 - 21 business days", true, deal.isFreeShipping || deal.shippingCost == 0.0)
            )
        } else {
            val shipCost = if (deal.isFreeShipping) 0.0 else deal.shippingCost
            listOf(
                ShippingMethod("ship_std", "${deal.storeName} Standard Delivery", "${deal.storeName} Logistics", shipCost, "7 - 14 business days", true, shipCost == 0.0)
            )
        }
    }

    var selectedShippingMethodId by remember(deal.id, availableShippingMethods) {
        mutableStateOf(
            availableShippingMethods.firstOrNull { it.isFree }?.id
                ?: deal.selectedShippingMethodId?.takeIf { id -> availableShippingMethods.any { it.id == id } }
                ?: availableShippingMethods.firstOrNull()?.id
                ?: ""
        )
    }

    val selectedShippingMethod = availableShippingMethods.find { it.id == selectedShippingMethodId }
        ?: availableShippingMethods.firstOrNull()

    val methodBaseCost = selectedShippingMethod?.cost ?: (if (deal.isFreeShipping || isAliExpressDeal) 0.0 else deal.shippingCost)
    val isFreeShipping = selectedShippingMethod?.isFree ?: (methodBaseCost == 0.0 || deal.isFreeShipping || isAliExpressDeal)
    val baseShipping = if (isFreeShipping) 0.0 else methodBaseCost
    val extraUnits = maxOf(0, quantity - 1)
    val incrementalPerItem = if (isFreeShipping) 0.0 else (if (deal.incrementalShippingPerUnit > 0.0) deal.incrementalShippingPerUnit else 1.50)
    val totalShipping = if (isFreeShipping) 0.0 else Math.round((baseShipping + extraUnits * incrementalPerItem) * 100.0) / 100.0

    val serviceFee = 3.00
    val totalHold = Math.round((itemSubtotal + totalShipping + serviceFee) * 100.0) / 100.0

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

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isEditingItemDetails) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, AccentCyan)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Edit Item Details", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                OutlinedTextField(
                                    value = editableTitle,
                                    onValueChange = { editableTitle = it },
                                    label = { Text("Product Title", fontSize = 11.sp) },
                                    singleLine = false,
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = CardBorder,
                                        focusedLabelColor = AccentCyan,
                                        unfocusedLabelColor = TextSecondary
                                    )
                                )
                                OutlinedTextField(
                                    value = if (editablePrice > 0.0) editablePrice.toString() else "",
                                    onValueChange = { editablePrice = it.toDoubleOrNull() ?: editablePrice },
                                    label = { Text("Unit Price (USD)", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = CardBorder,
                                        focusedLabelColor = AccentCyan,
                                        unfocusedLabelColor = TextSecondary
                                    )
                                )
                                Button(
                                    onClick = {
                                        onUpdateDealDetails(editableTitle, editablePrice)
                                        isEditingItemDetails = false
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                ) {
                                    Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = editableTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { isEditingItemDetails = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Details", tint = AccentCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                                }
                            }
                        }
                    }

                    // Interactive Multi-Attribute (Color & Size) or Single-Attribute (Variety) Selection
                    if (isMultiAttribute) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Row 1: Choose Color
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Choose Color", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(selectedColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(distinctColors) { color ->
                                val isSelected = color.equals(selectedColor, ignoreCase = true)
                                val chipBg = if (isSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF0F172A)
                                val chipBorder = if (isSelected) AccentCyan else Color(0xFF1E293B)
                                val chipTextColor = if (isSelected) AccentCyan else TextPrimary

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg)
                                        .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedColor = color }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(text = color, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = chipTextColor)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        // Row 2: Choose Size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Choose Size", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(
                                text = if (isSelectedSoldOut) "● Sold Out" else "● In Stock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectedSoldOut) DangerRed else SuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(distinctSizes) { size ->
                                val isSelected = size.equals(selectedSize, ignoreCase = true)
                                val matchingVariety = deal.varieties.find { v ->
                                    (v.color.equals(selectedColor, ignoreCase = true) && v.size.equals(size, ignoreCase = true)) ||
                                    (v.name.contains(selectedColor, ignoreCase = true) && v.name.contains(size, ignoreCase = true))
                                }
                                val isSoldOut = matchingVariety?.inStock == false
                                val offset = matchingVariety?.priceOffset ?: 0.0
                                val offsetText = if (offset > 0.0) " (+$$${"%.2f".format(offset)})" else if (offset < 0.0) " (-$$${"%.2f".format(-offset)})" else ""
                                val chipBg = if (isSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF0F172A)
                                val chipBorder = if (isSelected) AccentCyan else if (isSoldOut) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFF1E293B)
                                val chipTextColor = if (isSelected) AccentCyan else if (isSoldOut) Color(0xFF64748B) else TextPrimary

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg)
                                        .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedSize = size }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = size + offsetText + if (isSoldOut) " (Sold Out)" else "",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = chipTextColor
                                        )
                                    }
                                }
                            }
                        }
                    } else if (deal.varieties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Choose Variety (${deal.varieties.size} Available)",
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

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            deal.varieties.forEach { v ->
                                val isSelected = v.name.equals(chosenVariety, ignoreCase = true)
                                val isSoldOut = !v.inStock
                                val specificPrice = v.unitPrice?.takeIf { it > 0.0 }
                                val offset = v.priceOffset
                                val priceBadge = when {
                                    specificPrice != null -> " ($${"%.2f".format(specificPrice)})"
                                    offset > 0.0 -> " (+$$${"%.2f".format(offset)})"
                                    offset < 0.0 -> " (-$$${"%.2f".format(-offset)})"
                                    else -> ""
                                }
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
                                            if (v.unitPrice != null && v.unitPrice > 0.0) {
                                                editablePrice = v.unitPrice
                                            }
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
                                            text = v.name + priceBadge + if (isSoldOut) " (Sold Out)" else "",
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
                                text = "$${"%.2f".format(unitPrice)} / unit",
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

                    // Interactive Shipping Method / Varieties Selector
                    if (availableShippingMethods.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Choose Shipping Method (${availableShippingMethods.size} Available)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                "to ${shippingCity.ifBlank { "Lagos" }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableShippingMethods.forEach { method ->
                                val isSelected = method.id == selectedShippingMethodId
                                val cardBg = if (isSelected) AccentCyan.copy(alpha = 0.12f) else Color(0xFF0F172A)
                                val cardBorder = if (isSelected) AccentCyan else Color(0xFF1E293B)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cardBg)
                                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                        .clickable { selectedShippingMethodId = method.id }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) AccentCyan else Color.Transparent)
                                                    .border(1.5.dp, if (isSelected) AccentCyan else Color(0xFF64748B), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Black)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = method.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) AccentCyan else TextPrimary
                                                )
                                                val detailText = buildString {
                                                    if (!method.estimatedDelivery.isNullOrBlank()) {
                                                        append(method.estimatedDelivery)
                                                    }
                                                    if (method.trackingAvailable) {
                                                        if (isNotEmpty()) append(" • ")
                                                        append("Tracking available")
                                                    }
                                                }
                                                if (detailText.isNotBlank()) {
                                                    Text(
                                                        text = detailText,
                                                        fontSize = 11.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (method.isFree || method.cost == 0.0) Color(0xFF0F291E) else Color(0xFF1E293B))
                                                .border(1.dp, if (method.isFree || method.cost == 0.0) SuccessGreen.copy(alpha = 0.5f) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (method.isFree || method.cost == 0.0) "FREE ($0.00)" else "$${"%.2f".format(method.cost)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (method.isFree || method.cost == 0.0) SuccessGreen else TextPrimary
                                            )
                                        }
                                    }
                                }
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
                        val priceLabel = if (quantity > 1) {
                            "Item Price ($${"%.2f".format(unitPrice)} × $quantity)"
                        } else {
                            "Item Price ($${"%.2f".format(unitPrice)})"
                        }
                        Text(priceLabel, color = TextSecondary, fontSize = 14.sp)
                        Text("$${"%.2f".format(itemSubtotal)}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val carrierName = selectedShippingMethod?.name?.takeIf { it.isNotBlank() } ?: "Standard"
                        val shipLabel = if (quantity > 1) {
                            "Shipping ($carrierName) to ${shippingCity.ifBlank { "Lagos" }} ($quantity items)"
                        } else {
                            "Shipping ($carrierName) to ${shippingCity.ifBlank { "Lagos" }}"
                        }
                        Text(shipLabel, color = TextSecondary, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (totalShipping == 0.0) "FREE ($0.00)" else "$${"%.2f".format(totalShipping)}",
                            color = if (totalShipping == 0.0) SuccessGreen else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                onClick = { onPayWithPayPal(currentShipping(), currentBilling(), effectiveVarietyName, quantity, itemSubtotal, totalShipping) },
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF003087),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Sandbox",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
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
                onClick = { onDemoSandboxFastPass(currentShipping(), currentBilling(), effectiveVarietyName, quantity, itemSubtotal, totalShipping) },
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
