package com.fanyiadrien.ictu_ex.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
<<<<<<< Updated upstream
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
=======
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
>>>>>>> Stashed changes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.data.model.Notification
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyNotifications(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
<<<<<<< Updated upstream
                items(notifications, key = { it.notifId }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onRead       = { viewModel.markAsRead(notification.notifId) },
                        onDelete     = { viewModel.deleteNotification(notification.notifId) }
=======
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onRead = { viewModel.markAsRead(notification.notifId) },
                        onDelete = { viewModel.deleteNotification(notification.notifId) }
>>>>>>> Stashed changes
                    )
                }
            }
        }
    }
}

@Composable
<<<<<<< Updated upstream
private fun NotificationItem(
=======
fun NotificationItem(
>>>>>>> Stashed changes
    notification: Notification,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
<<<<<<< Updated upstream
    val dateString = remember(notification.createdAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            .format(Date(notification.createdAt))
    }

    val (icon, title) = when (notification.type) {
        "NEW_ORDER"    -> Icons.Rounded.ShoppingBag   to "New Order Received!"
        "NEW_LISTING"  -> Icons.Rounded.Storefront    to "New Item on Campus!"
        "ORDER_PLACED" -> Icons.Rounded.CheckCircle   to "Order Confirmed!"
        else           -> Icons.Rounded.Notifications to "Notification"
    }

    Card(
        onClick = onRead,
        shape   = RoundedCornerShape(16.dp),
        colors  = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
=======
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(notification.createdAt))

    Card(
        onClick = onRead,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
>>>>>>> Stashed changes
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
<<<<<<< Updated upstream
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotifIcon(icon = icon, read = notification.read)
=======
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (notification.read) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(notification.type) {
                        "NEW_ORDER" -> Icons.Rounded.ShoppingBag
                        else -> Icons.Rounded.Notifications
                    },
                    contentDescription = null,
                    tint = if (notification.read) MaterialTheme.colorScheme.outline 
                           else MaterialTheme.colorScheme.primary
                )
            }
>>>>>>> Stashed changes

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
<<<<<<< Updated upstream
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text     = notification.itemSummary,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notification.totalXaf > 0) {
                        Text(
                            text       = "XAF ${notification.totalXaf.toInt()}",
                            style      = MaterialTheme.typography.labelMedium,
                            color      = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text  = "• $dateString",
=======
                    text = if (notification.type == "NEW_ORDER") "New Order Received!" else "Notification",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = notification.itemSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total: XAF ${notification.totalXaf.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $dateString",
>>>>>>> Stashed changes
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
<<<<<<< Updated upstream
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete",
                    tint     = MaterialTheme.colorScheme.outline,
=======
                    Icons.Rounded.DeleteOutline, 
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
>>>>>>> Stashed changes
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
<<<<<<< Updated upstream
private fun NotifIcon(icon: ImageVector, read: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (read) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = if (read) MaterialTheme.colorScheme.outline
                                 else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyNotifications(modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = Icons.Rounded.NotificationsNone,
            contentDescription = null,
            modifier           = Modifier.size(80.dp),
            tint               = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text  = "No notifications yet",
=======
fun EmptyNotifications(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
>>>>>>> Stashed changes
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
<<<<<<< Updated upstream
            text      = "Orders and new listings will appear here.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
=======
            text = "When you receive orders, they will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
>>>>>>> Stashed changes
        )
    }
}
