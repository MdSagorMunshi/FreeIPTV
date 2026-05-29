package com.ryanshelby.iptv.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ryanshelby.iptv.R
import com.ryanshelby.iptv.data.Channel
import com.ryanshelby.iptv.ui.theme.*
import com.ryanshelby.iptv.viewmodel.AppState

/**
 * Maps a group name to its corresponding category icon drawable resource ID.
 * Falls back to the "all" icon for unrecognized groups.
 */
private fun getCategoryIconRes(group: String): Int {
    return when {
        group.equals("All", ignoreCase = true) -> R.drawable.ic_category_all
        group.equals("Favorites", ignoreCase = true) -> 0 // Use vector icon for favorites
        group.contains("sport", ignoreCase = true) -> R.drawable.ic_category_sports
        group.contains("news", ignoreCase = true) -> R.drawable.ic_category_news
        group.contains("entertain", ignoreCase = true) -> R.drawable.ic_category_entertainment
        group.contains("music", ignoreCase = true) -> R.drawable.ic_category_music
        group.contains("kid", ignoreCase = true) || group.contains("child", ignoreCase = true) -> R.drawable.ic_category_kids
        group.contains("movie", ignoreCase = true) || group.contains("film", ignoreCase = true) || group.contains("cinema", ignoreCase = true) -> R.drawable.ic_category_movies
        else -> R.drawable.ic_category_all
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AppState,
    onChannelClick: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onGroupSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Purple40.copy(alpha = 0.15f), Color.Transparent),
        startY = 0f, endY = 400f
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with gradient
            Box(
                modifier = Modifier.fillMaxWidth().background(gradientBrush)
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo — uses the app icon foreground
                        Box(
                            modifier = Modifier.size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Purple40, Pink40))),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_notification),
                                contentDescription = "FreeIPTV",
                                modifier = Modifier.size(26.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("FREE IPTV", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "${state.filteredChannels.size} channels available",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        // Live badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = LiveRed.copy(alpha = 0.15f),
                            modifier = Modifier.border(1.dp, LiveRed.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 1f, targetValue = 0.3f,
                                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha"
                                )
                                Box(Modifier.size(6.dp).clip(CircleShape).background(LiveRed.copy(alpha = alpha)))
                                Spacer(Modifier.width(5.dp))
                                Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiveRed)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        // About button
                        IconButton(
                            onClick = onOpenAbout,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "About", tint = TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Search bar - Material 3 style
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearch,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search channels...", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearch("") }) {
                                    Icon(Icons.Default.Close, null, tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCard.copy(alpha = 0.6f),
                            unfocusedContainerColor = DarkCard.copy(alpha = 0.4f),
                            focusedBorderColor = Purple40.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Purple40
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Category chips with icons
            val allGroups = remember(state.groups, state.favorites) {
                if (state.favorites.isNotEmpty()) listOf("Favorites") + state.groups else state.groups
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(allGroups) { group ->
                    val selected = state.selectedGroup == group
                    val iconRes = getCategoryIconRes(group)
                    FilterChip(
                        selected = selected,
                        onClick = { onGroupSelect(group) },
                        label = { Text(group, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        leadingIcon = if (group == "Favorites") {
                            { Icon(Icons.Default.Favorite, null, Modifier.size(14.dp)) }
                        } else if (iconRes != 0) {
                            {
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = group,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(
                                        if (selected) Color.White else TextSecondary
                                    )
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple40,
                            selectedLabelColor = Color.White,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = selected
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Loading state — with illustration
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.empty_loading),
                            contentDescription = "Loading",
                            modifier = Modifier.size(160.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(color = Purple40, strokeWidth = 2.dp)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading channels...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (state.filteredChannels.isEmpty()) {
                // Empty state — different illustrations depending on context
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val isFavoritesEmpty = state.selectedGroup == "Favorites"
                        Image(
                            painter = painterResource(
                                if (isFavoritesEmpty) R.drawable.empty_no_favorites
                                else R.drawable.empty_no_results
                            ),
                            contentDescription = "No channels",
                            modifier = Modifier.size(180.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (isFavoritesEmpty) "No favorites yet"
                            else "No channels found",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (isFavoritesEmpty) "Tap the heart icon on any channel to add it here"
                            else "Try a different search term or category",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                }
            } else {
                // Channel grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.filteredChannels) { index, channel ->
                        ChannelCard(
                            channel = channel,
                            isActive = index == state.currentIndex,
                            isFavorite = state.favorites.contains(channel.url),
                            onClick = { onChannelClick(index) },
                            onFavoriteClick = { onToggleFavorite(channel.url) }
                        )
                    }
                }
            }
        }

        // Mini player bar at bottom
        AnimatedVisibility(
            visible = state.currentIndex >= 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val channel = if (state.currentIndex in state.filteredChannels.indices)
                state.filteredChannels[state.currentIndex] else null
            if (channel != null) {
                MiniPlayerBar(channel = channel, onClick = onOpenPlayer)
            }
        }
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    isActive: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val borderColor = if (isActive) Purple40 else Color.Transparent
    val bgColor = if (isActive) Purple40.copy(alpha = 0.1f) else DarkCard.copy(alpha = 0.6f)

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.9f)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(10.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (channel.logo.isNotBlank()) {
                    AsyncImage(
                        model = channel.logo, contentDescription = channel.name,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Use channel_placeholder image instead of generic icon
                    Image(
                        painter = painterResource(R.drawable.channel_placeholder),
                        contentDescription = channel.name,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    channel.name, style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }

            // Active indicator
            if (isActive) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(8.dp)
                        .clip(CircleShape).background(OnlineGreen)
                )
            }

            // Favorite button
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                modifier = Modifier.align(Alignment.TopStart).size(16.dp)
                    .clickable(onClick = onFavoriteClick),
                tint = if (isFavorite) Pink40 else TextTertiary
            )
        }
    }
}

@Composable
private fun MiniPlayerBar(channel: Channel, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(12.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = DarkCard,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (channel.logo.isNotBlank()) {
                AsyncImage(
                    model = channel.logo, contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.channel_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(OnlineGreen))
                    Spacer(Modifier.width(4.dp))
                    Text("Playing now", style = MaterialTheme.typography.labelSmall, color = OnlineGreen)
                }
            }
            FilledIconButton(
                onClick = onClick,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Purple40),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Fullscreen, null, tint = Color.White)
            }
        }
    }
}
