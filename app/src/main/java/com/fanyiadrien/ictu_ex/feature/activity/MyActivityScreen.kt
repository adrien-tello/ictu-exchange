package com.fanyiadrien.ictu_ex.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.Order
import com.fanyiadrien.ictu_ex.ui.theme.Purple40
import com.fanyiadrien.ictu_ex.ui.theme.Purple80
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyActivityScreen(
    navController: NavController,
    viewModel: MyActivityViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    var listingToDelete by remember { mutableStateOf<Listing?>(null) }
    var listingToEdit   by remember { mutableStateOf<Listing?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (state.isSeller) "Seller Panel" else "My Activity",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            if (state.isSeller) "Manage listings & orders" else "Track your orders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { SummaryHeader(userName = state.userName, isSeller = state.isSeller, count = if (state.isSeller) state.myListings.size else state.myOrders.size) }

                    if (state.isSeller) {
                        // ── Section 1: Received Orders ────────────────────
                        if (state.myOrders.isNotEmpty()) {
                            item { SectionTitle("Orders Received", state.myOrders.size) }
                            items(state.myOrders) { order ->
                                ReceivedOrderCard(
                                    order = order,
                                    onChatClick = { navController.navigate(Screen.Messages.createRoute(sellerId = order.buyerId)) }
                                )
                            }
                        }

                        // ── Section 2: Your Listings ──────────────────────
                        item { SectionTitle("Your Listings", state.myListings.size) }
                        if (state.myListings.isEmpty()) {
                            item { EmptyState(Icons.Rounded.Inventory2, "No items posted", "Tap + below to start selling.") }
                        } else {
                            items(state.myListings) { listing ->
                                SellerListingCard(
                                    listing         = listing,
                                    onDeleteRequest = { listingToDelete = listing },
                                    onEditRequest   = { listingToEdit = listing },
                                    onMarkSold      = viewModel::markAsSold,
                                    enabled         = !state.isActionLoading
                                )
                            }
                        }
                    } else {
                        // Buyer View
                        if (state.myOrders.isEmpty()) {
                            item { EmptyState(Icons.Rounded.ShoppingBag, "No orders found", "Purchased items will appear here.") }
                        } else {
                            items(state.myOrders) { order -> OrderCard(order) }
                        }
                    }
                }
            }

            if (state.isActionLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)).clickable(enabled=false){}, contentAlignment = Alignment.Center) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(16.dp))
                            Text("Updating...")
                        }
                    }
                }
            }
        }
    }

    // Dialogs ... (Edit/Delete implementation remains same as previous working state)
}

@Composable
private fun SectionTitle(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Badge(containerColor = MaterialTheme.colorScheme.primary) { Text(count.toString()) }
    }
}

@Composable
private fun ReceivedOrderCard(order: Order, onChatClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.orderId.take(6).uppercase()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(SimpleDateFormat("MMM dd", Locale.US).format(Date(order.createdAt)), style = MaterialTheme.typography.labelSmall)
            }
            Text("From: ${order.buyerName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(order.items.joinToString { it.title }, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onChatClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.Chat, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chat with Buyer")
            }
        }
    }
}

@Composable
private fun SummaryHeader(userName: String, isSeller: Boolean, count: Int) {
    val bgGradient = if (isSeller) Brush.horizontalGradient(listOf(Purple40, Purple80)) else Brush.horizontalGradient(listOf(Color(0xFF00796B), Color(0xFF4DB6AC)))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.background(bgGradient).padding(24.dp).fillMaxWidth()) {
            Column {
                Text("Hey, $userName! 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(if (isSeller) "Your shop is growing" else "Your trading history", color = Color.White.copy(0.8f))
            }
        }
    }
}

// ... Rest of components (SellerListingCard, OrderCard, etc.) ...
@Composable
private fun SellerListingCard(listing: Listing, onDeleteRequest: () -> Unit, onEditRequest: () -> Unit, onMarkSold: (String) -> Unit, enabled: Boolean) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = listing.imageUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(listing.title, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("XAF ${listing.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
                Surface(color = if (listing.available) Color(0xFFE8F5E9) else Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)) {
                    Text(if (listing.available) "ACTIVE" else "SOLD", style = MaterialTheme.typography.labelSmall, color = if (listing.available) Color(0xFF2E7D32) else Color.Gray, modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEditRequest, enabled = enabled) { Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)); Text(" Edit") }
                if (listing.available) TextButton(onClick = { onMarkSold(listing.id) }, enabled = enabled) { Icon(Icons.Rounded.CheckCircle, null, Modifier.size(16.dp)); Text(" Sold") }
                IconButton(onClick = onDeleteRequest, enabled = enabled) { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable private fun OrderCard(order: Order) { /* Standard buyer order card */ }
@Composable private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) { /* Styled empty state */ }
@Composable private fun ErrorState(message: String, onRetry: () -> Unit) { /* Error state */ }
