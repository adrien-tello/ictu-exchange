package com.fanyiadrien.ictu_ex.feature.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.ui.theme.*
import java.util.*

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    LaunchedEffect(state.checkoutOrderId) {
        state.checkoutOrderId?.let { orderId ->
            navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                popUpTo(Screen.Cart.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar         = {
            CartTopBar(
                itemCount = state.itemCount,
                onClose   = { navController.popBackStack() },
                onShare   = { /* TODO: share cart */ }
            )
        },
        bottomBar = {
            CartCheckoutBar(
                total       = state.total,
                itemCount   = state.items.size,
                isLoading   = state.isCheckingOut,
                onClick     = { viewModel.checkout() }
            )
        }
    ) { padding ->

        if (state.items.isEmpty() && state.checkoutOrderId == null) {
            EmptyCartState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                itemsIndexed(
                    items = state.items,
                    key   = { _, item -> item.id }
                ) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 6 }
                    ) {
                        CartItemCard(
                            item        = item,
                            onIncrement = { viewModel.increment(item.id) },
                            onDecrement = { viewModel.decrement(item.id) },
                            onRemove    = { viewModel.removeItem(item.id) }
                        )
                    }

                    if (index < state.items.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.8.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.8.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    PromoCodeSection(
                        code         = state.promoCode,
                        applied      = state.promoApplied,
                        onCodeChange = viewModel::onPromoCodeChange,
                        onApply      = viewModel::applyPromoCode
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.8.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                item {
                    PriceBreakdown(
                        subtotal        = state.subtotal,
                        discount        = state.discount,
                        total           = state.total,
                        discountPercent = state.discountPercent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartTopBar(
    itemCount: Int,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text      = "$itemCount ITEMS",
                style     = MaterialTheme.typography.titleMedium.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color     = MaterialTheme.colorScheme.onBackground,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, "Close", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(Icons.Rounded.IosShare, "Share", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}

@Composable
private fun PromoCodeSection(
    code: String,
    applied: Boolean,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.LocalOffer, null, tint = if (applied) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = code, onValueChange = onCodeChange,
            placeholder = { Text("Promo code") },
            singleLine = true, modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onApply() })
        )
        TextButton(onClick = onApply) { Text("Apply", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun PriceBreakdown(subtotal: Double, discount: Double, total: Double, discountPercent: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PriceRow("Subtotal", formatXaf(subtotal), false)
        if (discountPercent > 0) PriceRow("Discount ($discountPercent%)", "− ${formatXaf(discount)}", false, MaterialTheme.colorScheme.tertiary)
        HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        PriceRow("Total", formatXaf(total), true)
    }
}

@Composable
private fun PriceRow(label: String, value: String, bold: Boolean, color: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (bold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium)
        Text(value, style = if (bold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold) else MaterialTheme.typography.bodyMedium, color = if (bold) MaterialTheme.colorScheme.primary else color)
    }
}

@Composable
private fun CartCheckoutBar(total: Double, itemCount: Int, isLoading: Boolean, onClick: () -> Unit) {
    Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$itemCount items", style = MaterialTheme.typography.labelSmall)
                Text(formatXaf(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            Button(onClick = onClick, enabled = !isLoading, modifier = Modifier.height(50.dp).padding(horizontal = 12.dp)) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), Color.White)
                else Text("Checkout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyCartState(modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Rounded.ShoppingCartCheckout, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f))
        Text("Your cart is empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
