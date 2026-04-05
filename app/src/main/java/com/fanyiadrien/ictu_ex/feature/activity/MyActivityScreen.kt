package com.fanyiadrien.ictu_ex.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.Order
import com.fanyiadrien.ictu_ex.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
                            if (state.isSeller) "Manage your campus shop" else "Track your orders",
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
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null) {
                ErrorState(state.errorMessage) { viewModel.loadData() }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SummaryHeader(
                            userName = state.userName,
                            isSeller = state.isSeller,
                            count = if (state.isSeller) state.myListings.size else state.myOrders.size
                        )
                    }

                    if (state.isSeller) {
                        if (state.myListings.isEmpty()) {
                            item { EmptyState(Icons.Rounded.Inventory2, "No items posted yet", "Tap + to start selling!") }
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
                        if (state.myOrders.isEmpty()) {
                            item { EmptyState(Icons.Rounded.ShoppingBag, "No orders found", "Your purchases will appear here.") }
                        } else {
                            items(state.myOrders) { order ->
                                OrderCard(order)
                            }
                        }
                    }
                }
            }

            // Action Loading Overlay
            if (state.isActionLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
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

    // ── Dialogs ───────────────────────────────────────────────────────────
    
    listingToEdit?.let { listing ->
        EditListingDialog(
            listing = listing,
            onConfirm = { updated ->
                viewModel.updateListing(updated)
                listingToEdit = null
            },
            onDismiss = { listingToEdit = null }
        )
    }

    listingToDelete?.let { listing ->
        DeleteVerificationDialog(
            listingName = listing.title,
            onConfirm = {
                viewModel.deleteListing(listing.id)
                listingToDelete = null
            },
            onDismiss = { listingToDelete = null }
        )
    }
}

@Composable
private fun SummaryHeader(userName: String, isSeller: Boolean, count: Int) {
    val bgGradient = if (isSeller)
        Brush.horizontalGradient(listOf(Purple40, Purple80))
    else
        Brush.horizontalGradient(listOf(Color(0xFF00796B), Color(0xFF4DB6AC)))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.background(bgGradient).padding(24.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = "Hey, $userName! 👋",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isSeller) "You have $count total listings" else "You've made $count orders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = if (isSeller) Icons.Rounded.Storefront else Icons.Rounded.LocalMall,
                contentDescription = null,
                modifier = Modifier.size(48.dp).align(Alignment.CenterEnd).alpha(0.2f),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SellerListingCard(
    listing: Listing,
    onDeleteRequest: () -> Unit,
    onEditRequest: () -> Unit,
    onMarkSold: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("XAF ${listing.price.toInt()}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    
                    Surface(
                        color = if (listing.available) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (listing.available) "ACTIVE" else "SOLD",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (listing.available) Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditRequest, enabled = enabled) {
                    Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }

                if (listing.available) {
                    TextButton(onClick = { onMarkSold(listing.id) }, enabled = enabled) {
                        Icon(Icons.Rounded.CheckCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Sold")
                    }
                }

                TextButton(onClick = onDeleteRequest, enabled = enabled, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun EditListingDialog(
    listing: Listing,
    onConfirm: (Listing) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(listing.title) }
    var price by remember { mutableStateOf(listing.price.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Listing") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (XAF)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val p = price.toDoubleOrNull() ?: 0.0
                    onConfirm(listing.copy(title = name, price = p)) 
                },
                enabled = name.isNotBlank() && price.isNotBlank()
            ) { Text("Save Changes") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DeleteVerificationDialog(
    listingName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    val isMatch = inputName.trim().equals(listingName.trim(), ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Listing?") },
        text = {
            Column {
                Text("This action cannot be undone. To confirm, please type the item name: \"$listingName\"")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    placeholder = { Text("Product name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isMatch,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete Permanently") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OrderCard(order: Order) {
    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(order.createdAt))
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ORDER #${order.orderId.take(8).uppercase()}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Text(order.items.joinToString { it.title }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Total Paid:", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                Text("XAF ${order.totalXaf.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = { /* View details */ },
                    label = { Text(order.status) },
                    leadingIcon = { Icon(Icons.Rounded.History, null, Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Rounded.CloudOff, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error.copy(0.5f))
        Spacer(Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onRetry) { Text("Retry Connection") }
    }
}
