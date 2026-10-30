package com.cartwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartwise.app.data.CartwiseOrder
import com.cartwise.app.ui.theme.*

@Composable
fun OrdersHistoryScreen(
    orders: List<CartwiseOrder>,
    onBack: () -> Unit,
    onOrderClick: (CartwiseOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Order History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No past orders yet.", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(order.merchantName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                Text(
                                    order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status == "CAPTURED") SuccessGreen else DangerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(order.itemTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Card: ${order.cardUsed}", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(order.createdAt.take(10).ifEmpty { "Today" }, fontSize = 12.sp, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$${order.totalAmount}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = "View Receipt", tint = AccentCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
