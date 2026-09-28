package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory
import com.example.ui.components.ArticleCard
import com.example.ui.components.UrgentAlertBanner
import com.example.ui.theme.*
import com.example.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    uiState: MainUiState,
    onSelectCategory: (NewsCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleBookmarkFilter: () -> Unit,
    onToggleHighImpactFilter: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkClick: (NewsArticle) -> Unit,
    onDismissUrgentAlert: () -> Unit,
    onOpenSearchGrounding: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchExpanded by remember { mutableStateOf(false) }

    // Filter articles based on active criteria
    val displayedArticles = remember(
        uiState.articles,
        uiState.selectedCategory,
        uiState.searchQuery,
        uiState.showBookmarkedOnly,
        uiState.showHighImpactOnly
    ) {
        uiState.articles.filter { article ->
            val matchesCategory = when (uiState.selectedCategory) {
                NewsCategory.FOR_YOU -> true
                NewsCategory.ALL -> true
                else -> article.category == uiState.selectedCategory
            }
            val matchesSearch = uiState.searchQuery.isBlank() ||
                    article.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    article.summary.contains(uiState.searchQuery, ignoreCase = true) ||
                    article.tickers.any { it.contains(uiState.searchQuery, ignoreCase = true) }
            val matchesBookmark = !uiState.showBookmarkedOnly || article.isBookmarked
            val matchesImpact = !uiState.showHighImpactOnly ||
                    article.impactRating.name == "CRITICAL" || article.impactRating.name == "HIGH"

            matchesCategory && matchesSearch && matchesBookmark && matchesImpact
        }.let { list ->
            if (uiState.selectedCategory == NewsCategory.FOR_YOU) {
                list.sortedByDescending { it.algorithmicFitScore }
            } else {
                list
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ticker Tape Bar
        MarketTickerTape()

        // Urgent Alert Banner if present
        uiState.latestUrgentAlert?.let { alert ->
            UrgentAlertBanner(
                alert = alert,
                onDismiss = onDismissUrgentAlert
            )
        }

        // Top Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed_search_input"),
                placeholder = {
                    Text(
                        "Search tickers (\$NVDA), topics, inflation...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Bookmark Filter Toggle
            FilterIconButton(
                icon = if (uiState.showBookmarkedOnly) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                isActive = uiState.showBookmarkedOnly,
                onClick = onToggleBookmarkFilter,
                contentDescription = "Saved Filter",
                testTag = "filter_bookmark_toggle"
            )

            // High Impact Filter Toggle
            FilterIconButton(
                icon = Icons.Filled.FlashOn,
                isActive = uiState.showHighImpactOnly,
                onClick = onToggleHighImpactFilter,
                contentDescription = "High Impact Only",
                testTag = "filter_impact_toggle"
            )
        }

        // Categories Scrollable Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(NewsCategory.values()) { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(category) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (category == NewsCategory.FOR_YOU) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color(0xFFA5B4FC),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = category.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("category_chip_${category.name}")
                )
            }
        }

        // Google Search Grounding Banner
        SearchGroundingHeroCard(
            onQueryClick = onOpenSearchGrounding,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Articles Feed List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("articles_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.selectedCategory == NewsCategory.FOR_YOU) {
                item {
                    AlgorithmicFeedHeader(userWatchlistCount = uiState.userPreferences.watchlistTickers.size)
                }
            }

            if (displayedArticles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = "No articles",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No news matching current filters",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(displayedArticles, key = { it.id }) { article ->
                    ArticleCard(
                        article = article,
                        onCardClick = { onArticleClick(article) },
                        onBookmarkClick = { onBookmarkClick(article) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarketTickerTape() {
    val items = listOf(
        "S&P 500: 5,742.80 (+0.45%)" to FinGreenGain,
        "NASDAQ: 18,124.50 (+0.82%)" to FinGreenGain,
        "BTC/USD: $64,850 (+1.42%)" to FinGreenGain,
        "10Y Yield: 3.92% (-4 bps)" to FinCyanAccent,
        "WTI Crude: $78.20 (-0.35%)" to FinRedLoss,
        "DXY Index: 101.40 (-0.18%)" to Color(0xFF94A3B8)
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(FinGreenGain)
                )
                Text(
                    text = "LIVE",
                    color = FinGreenGain,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items.forEach { (text, color) ->
                Text(
                    text = text,
                    color = color,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AlgorithmicFeedHeader(userWatchlistCount: Int) {
    Surface(
        color = Color(0xFF312E81).copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F46E5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Algorithmic Personalized Feed",
                    color = Color(0xFFE0E7FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ranked dynamically from your $userWatchlistCount watchlist assets, reading duration & sector weights.",
                    color = Color(0xFFA5B4FC),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun SearchGroundingHeroCard(
    onQueryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = "Search Grounding",
                    tint = FinCyanAccent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "GEMINI LIVE SEARCH GROUNDING",
                    color = FinCyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap any topic for real-time market synthesis with Google Search grounding:",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = listOf(
                    "Latest Fed Rate Cut Odds" to "Federal Reserve interest rate expectations and Treasury yields",
                    "Today's AI Capex Shift" to "Latest datacenter semiconductor earnings guidance NVDA MSFT",
                    "BLS CPI Consensus Preview" to "Upcoming Bureau of Labor Statistics inflation report consensus"
                )

                suggestions.forEach { (chipLabel, fullQuery) ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { onQueryClick(fullQuery) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = chipLabel,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .testTag(testTag)
            .background(
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
