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
import com.ryanshelby.iptv.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldScreen(
    state: AppState,
    viewModel: MainViewModel,
    onChannelClick: (Int) -> Unit,
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadWorldChannels()
    }

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
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier.size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Purple40, Pink40))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = "World",
                                modifier = Modifier.size(26.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("WORLD TV", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (state.isWorldLoaded) {
                                Text(
                                    "${state.filteredWorldChannels.size} channels",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            } else {
                                Text(
                                    "Loading...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Search bar
                    OutlinedTextField(
                        value = state.worldSearchQuery,
                        onValueChange = { viewModel.searchWorld(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search world channels...", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                        trailingIcon = {
                            if (state.worldSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchWorld("") }) {
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

            // Category chips
            if (state.isWorldLoaded) {
                val allGroups = remember(state.worldGroups, state.favorites) {
                    if (state.favorites.isNotEmpty()) listOf("Favorites") + state.worldGroups else state.worldGroups
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(allGroups) { group ->
                        val selected = state.selectedWorldGroup == group
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectWorldGroup(group) },
                            label = { Text(group, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = if (group == "Favorites") {
                                { Icon(Icons.Default.Favorite, null, Modifier.size(14.dp)) }
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
            }

            // Loading state
            if (state.isWorldLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Purple40, strokeWidth = 2.dp)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading massive global TV list...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (state.isWorldLoaded && state.filteredWorldChannels.isEmpty()) {
                // Empty state
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val isFavoritesEmpty = state.selectedWorldGroup == "Favorites"
                        Text(
                            if (isFavoritesEmpty) "No world favorites yet"
                            else "No channels found",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (state.isWorldLoaded) {
                // Channel grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.filteredWorldChannels) { index, channel ->
                        WorldChannelCard(
                            channel = channel,
                            isActive = index == state.currentWorldIndex && state.isViewingWorld,
                            isFavorite = state.favorites.contains(channel.url),
                            onClick = { onChannelClick(index) },
                            onFavoriteClick = { viewModel.toggleFavorite(channel.url) }
                        )
                    }
                }
            }
        }

        // Mini player bar at bottom
        AnimatedVisibility(
            visible = state.currentWorldIndex >= 0 && state.isViewingWorld,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val channel = if (state.currentWorldIndex in state.filteredWorldChannels.indices)
                state.filteredWorldChannels[state.currentWorldIndex] else null
            if (channel != null) {
                WorldMiniPlayerBar(channel = channel, onClick = onOpenPlayer)
            }
        }
    }
}

@Composable
private fun WorldChannelCard(
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
                        contentScale = ContentScale.Fit,
                        error = painterResource(R.drawable.channel_placeholder)
                    )
                } else {
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

            if (isActive) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(8.dp)
                        .clip(CircleShape).background(OnlineGreen)
                )
            }

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
private fun WorldMiniPlayerBar(channel: Channel, onClick: () -> Unit) {
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
                    contentScale = ContentScale.Fit,
                    error = painterResource(R.drawable.channel_placeholder)
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
