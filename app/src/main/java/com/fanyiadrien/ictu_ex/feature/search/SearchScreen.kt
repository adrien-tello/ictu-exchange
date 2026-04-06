package com.fanyiadrien.ictu_ex.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.ui.components.IctuBottomNav
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import java.util.*

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Default to buyer layout for SearchScreen or ideally pass true user state
            IctuBottomNav(navController = navController, isSeller = false)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Search Header ──────────────────────────────────────────────
            SearchHeader(
                query = uiState.searchQuery,
                onQueryChanged = viewModel::onSearchQueryChanged,
                onBackClick = { navController.popBackStack() }
            )

            // ── Categories ─────────────────────────────────────────────────
            CategoryChips(
                selected = uiState.selectedCategory,
                onSelected = viewModel::onCategorySelected
            )

            // ── Results Grid ───────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredListings.isEmpty()) {
                EmptySearchState(uiState.searchQuery)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "${uiState.filteredListings.size} items found",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(uiState.filteredListings) { listing ->
                        SearchProductCard(
                            listing = listing,
                            isSaved = uiState.wishlistedIds.contains(listing.id),
                            onSave = { viewModel.toggleWishlist(listing.id) },
                            onClick = { navController.navigate(Screen.ItemDetail.createRoute(listing.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }

        TextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search on ICTU-Ex...") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(Icons.Rounded.Close, null)
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
private fun CategoryChips(
    selected: ListingCategory,
    onSelected: (ListingCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ListingCategory.values().forEach { category ->
            val isSelected = selected == category
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(category) },
                label = { Text(category.displayName) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = null
            )
        }
    }
}

@Composable
private fun SearchProductCard(
    listing: Listing,
    isSaved: Boolean,
    onSave: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )

                IconButton(
                    onClick = onSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isSaved) Color.Red else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(Modifier.padding(12.dp)) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "XAF ${String.format(Locale.US, "%,.0f", listing.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.SearchOff,
            null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No results found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "We couldn't find anything for \"$query\". Try a different keyword or category.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
