package com.fanyiadrien.ictu_ex.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.ui.components.IctuBottomNav
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode

@Composable
fun HomeScreen(
    navController: NavController,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    
    LaunchedEffect(Unit) {
        viewModel.fetchListings()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            IctuBottomNav(
                navController = navController,
                isSeller = uiState.isSeller
            )
        },
        floatingActionButton = {
            // Dashboard FAB for both Buyer & Seller
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.MyActivity.route) },
                icon = { Icon(Icons.Rounded.Dashboard, contentDescription = null) },
                text = { Text(if (uiState.isSeller) "Seller Panel" else "My Activity") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            item {
                HomeTopBar(
                    userName = uiState.currentUser?.displayName ?: "Student",
                    themeMode = themeMode,
                    onThemeModeToggle = onThemeModeChange,
                    onNotificationClick = { /* TODO */ }
                )
            }

            item {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                CategoryChips(
                    selected = uiState.selectedCategory,
                    onSelected = viewModel::onCategorySelected
                )
            }

            if (!uiState.isLoading && uiState.allListings.isNotEmpty()) {
                item { SectionHeader(title = "New on Campus") }
                item {
                    NewOnCampusRow(
                        listings = uiState.allListings.take(5),
                        onItemClick = { listing ->
                            navController.navigate(Screen.ItemDetail.createRoute(listing.id))
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                item { ErrorBanner(message = error, onRetry = viewModel::fetchListings) }
            }

            if (uiState.isEmpty) {
                item { EmptyFeedState(isSeller = uiState.isSeller) }
            }

            if (uiState.filteredListings.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = if (uiState.selectedCategory == ListingCategory.ALL)
                            "All Listings" else uiState.selectedCategory.displayName
                    )
                }
            }

            val chunked = uiState.filteredListings.chunked(2)
            items(chunked) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { listing ->
                        ProductCard(
                            listing  = listing,
                            modifier = Modifier.weight(1f),
                            onClick  = { navController.navigate(Screen.ItemDetail.createRoute(listing.id)) }
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    userName: String,
    themeMode: ThemeMode,
    onThemeModeToggle: (ThemeMode) -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Welcome back 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(userName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val themeIcon = when (themeMode) {
                ThemeMode.AUTO -> Icons.Rounded.BrightnessAuto
                ThemeMode.LIGHT -> Icons.Rounded.LightMode
                ThemeMode.DARK -> Icons.Rounded.DarkMode
            }
            IconButton(
                onClick = {
                    val nextMode = when (themeMode) {
                        ThemeMode.AUTO -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.AUTO
                    }
                    onThemeModeToggle(nextMode)
                },
                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
            ) { Icon(themeIcon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
            ) { Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query, onValueChange = onQueryChanged,
        placeholder = { Text("Search textbooks, electronics…") },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun CategoryChips(selected: ListingCategory, onSelected: (ListingCategory) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ListingCategory.values().forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text(category.displayName) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("See all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun NewOnCampusRow(listings: List<Listing>, onItemClick: (Listing) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(listings) { NewOnCampusCard(it, { onItemClick(it) }) }
    }
}

@Composable
private fun ProductCard(listing: Listing, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = listing.imageUrl, contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )
            Column(Modifier.padding(12.dp)) {
                Text(listing.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("XAF ${listing.price.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun NewOnCampusCard(listing: Listing, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(155.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = listing.imageUrl, contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )
            Column(Modifier.padding(10.dp)) {
                Text(listing.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("XAF ${listing.price.toInt()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun EmptyFeedState(isSeller: Boolean) {
    Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.Inventory2, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
        Text("No listings yet", style = MaterialTheme.typography.titleMedium)
        Text(if (isSeller) "Post your first item!" else "Check back soon!", textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Button(onRetry) { Text("Retry") }
    }
}
