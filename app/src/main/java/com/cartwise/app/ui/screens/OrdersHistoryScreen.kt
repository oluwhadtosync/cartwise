package com.cartwise.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartwise.app.data.CartwiseOrder
import com.cartwise.app.ui.theme.*

@Composable
fun OrdersHistoryScreen(
    orders: List<CartwiseOrder>,
    onBack: () -> Unit,
    onOrderClick: (CartwiseOrder) -> Unit,
    onClearHistory: () -> Unit = {}
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear Order History?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will remove all saved purchases from your device's persistent storage.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearHistory()
                    }
                ) {
                    Text("Clear All", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Order History", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (orders.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${orders.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                }
            }

            if (orders.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Clear History",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            // Elegant Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF111827)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "No Purchases Yet",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Purchases made in Cartwise are saved to your device automatically. Search for an item and place an order to see it here!",
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Deals", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(orders) { order ->
                    val isCaptured = order.status == "CAPTURED"
                    val isVoided = order.status == "VOIDED"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(
                            1.dp,
                            if (isCaptured) SuccessGreen.copy(alpha = 0.4f)
                            else if (isVoided) DangerRed.copy(alpha = 0.4f)
                            else CardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Store & Status Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        order.merchantName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isCaptured) SuccessBg else if (isVoided) DangerBg else Color(0xFF1E293B)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isCaptured) "CAPTURED • PAID"
                                        else if (isVoided) "VOIDED • $0 CHARGED"
                                        else order.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCaptured) SuccessGreen else if (isVoided) DangerRed else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Item Title
                            Text(
                                text = order.itemTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!order.selectedVariety.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E293B))
                                            .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "Variety: ${order.selectedVariety}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AccentCyan
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Recipient & Destination line
                            val shipping = order.shippingAddress
                            val recipientText = if (shipping != null && shipping.fullName.isNotBlank()) {
                                "${shipping.fullName} • ${shipping.city}, ${shipping.country}"
                            } else {
                                "Ada Lovelace • Lagos, Nigeria"
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    recipientText,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Purchasing Card line
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Card: ${order.cardUsed}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom Row: Date & Amount & Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    order.createdAt.ifEmpty { "Recent Order" },
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "$${order.totalAmount}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isVoided) TextMuted else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "View Receipt",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
