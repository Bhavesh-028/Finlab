package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.NewsArticle
import com.example.data.model.PortfolioPosition
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainUiState
import java.util.Locale

@Composable
fun PortfolioScreen(
    uiState: MainUiState,
    onSyncPrices: () -> Unit,
    onAddPositionClick: () -> Unit,
    onRemovePosition: (String) -> Unit,
    onConnectBrokerClick: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPositionsValue = uiState.positions.sumOf { it.totalValue }
    val totalCash = uiState.connectedBrokers.sumOf { it.cashBalance }
    val totalPortfolioValue = totalPositionsValue + totalCash
    val totalUnrealizedPnL = uiState.positions.sumOf { it.unrealizedPnL }
    val totalCost = uiState.positions.sumOf { it.totalCost }
    val totalPnLPct = if (totalCost > 0) (totalUnrealizedPnL / totalCost) * 100.0 else 0.0

    // News linked to user's held symbols
    val heldSymbols = remember(uiState.positions) { uiState.positions.map { it.symbol }.toSet() }
    val linkedNews = remember(uiState.articles, heldSymbols) {
        uiState.articles.filter { article ->
            article.tickers.any { heldSymbols.contains(it) }
        }.take(3)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Brokerage & Portfolio Terminal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Live Brokerage API Integration • Encrypted Vault",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onSyncPrices,
                            enabled = !uiState.isSyncingBrokerage,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("sync_brokerage_button")
                        ) {
                            if (uiState.isSyncingBrokerage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = "Sync Prices",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onAddPositionClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_position_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Holding", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("portfolio_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Security & Hardware Encryption Notice
            item {
                SecurityEncryptionBadge(
                    standard = "AES-256-GCM • Android Keystore Enclave Active • TLS 1.3"
                )
            }

            // Portfolio Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOTAL NET LIQUIDATION VALUE",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", totalPortfolioValue)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val isGain = totalUnrealizedPnL >= 0
                        val pnlColor = if (isGain) FinGreenGain else FinRedLoss
                        val sign = if (isGain) "+" else ""

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = pnlColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Total Unrealized: $sign$${String.format(Locale.US, "%,.2f", totalUnrealizedPnL)} ($sign${String.format(Locale.US, "%.2f", totalPnLPct)}%)",
                                color = pnlColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Asset Allocation Bar
                        AssetAllocationBar(positions = uiState.positions)
                    }
                }
            }

            // Connected Live Brokerages
            item {
                Text(
                    text = "CONNECTED BROKERAGES & APIS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            items(uiState.connectedBrokers) { broker ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = broker.brokerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Account: ${broker.accountMaskedNumber} • Cash: $${String.format(Locale.US, "%,.2f", broker.cashBalance)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Surface(
                            color = FinGreenGain.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "CONNECTED",
                                color = FinGreenGain,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Positions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LIVE HOLDINGS & ASSETS (${uiState.positions.size})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Position Items
            items(uiState.positions, key = { it.id }) { position ->
                PositionHoldingCard(
                    position = position,
                    onDelete = { onRemovePosition(position.id) }
                )
            }

            // Linked News to Holdings
            if (linkedNews.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "NEWS LINKED TO YOUR HOLDINGS",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                items(linkedNews, key = { "linked_${it.id}" }) { article ->
                    ArticleCard(
                        article = article,
                        onCardClick = { onArticleClick(article) },
                        onBookmarkClick = { }
                    )
                }
            }
        }
    }
}
