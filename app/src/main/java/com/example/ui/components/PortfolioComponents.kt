package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.PortfolioPosition
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SecurityEncryptionBadge(
    standard: String = "AES-256-GCM / Hardware Keystore Enclave Active",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF064E3B).copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Secured",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SECURE CLIENT ENCRYPTION",
                        color = Color(0xFFA7F3D0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                    Text(
                        text = "ZERO-KNOWLEDGE VAULT",
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = standard,
                    color = Color(0xFFD1FAE5),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AssetAllocationBar(
    positions: List<PortfolioPosition>,
    modifier: Modifier = Modifier
) {
    val totalEquity = positions.filter { it.assetType == AssetType.EQUITY }.sumOf { it.totalValue }
    val totalCrypto = positions.filter { it.assetType == AssetType.CRYPTO }.sumOf { it.totalValue }
    val totalEtf = positions.filter { it.assetType == AssetType.ETF }.sumOf { it.totalValue }
    val totalValue = positions.sumOf { it.totalValue }

    val equityPct = if (totalValue > 0) (totalEquity / totalValue).toFloat() else 0.5f
    val cryptoPct = if (totalValue > 0) (totalCrypto / totalValue).toFloat() else 0.3f
    val etfPct = if (totalValue > 0) (totalEtf / totalValue).toFloat() else 0.2f

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "PORTFOLIO ALLOCATION DIVERSIFICATION",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Visual multi-color bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            if (equityPct > 0) {
                Box(
                    modifier = Modifier
                        .weight(equityPct.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(FinCyanAccent)
                )
            }
            if (cryptoPct > 0) {
                Box(
                    modifier = Modifier
                        .weight(cryptoPct.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(FinGoldAccent)
                )
            }
            if (etfPct > 0) {
                Box(
                    modifier = Modifier
                        .weight(etfPct.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF8B5CF6))
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Legends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(color = FinCyanAccent, label = "Equities", percent = (equityPct * 100).toInt())
            LegendItem(color = FinGoldAccent, label = "Crypto", percent = (cryptoPct * 100).toInt())
            LegendItem(color = Color(0xFF8B5CF6), label = "ETFs / Bonds", percent = (etfPct * 100).toInt())
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percent: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label ($percent%)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PositionHoldingCard(
    position: PortfolioPosition,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGain = position.unrealizedPnL >= 0
    val pnlColor = if (isGain) FinGreenGain else FinRedLoss

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("position_card_${position.symbol}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Symbol, Name, Shares
            Column(modifier = Modifier.weight(1.2f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = position.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = position.assetType.label,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = position.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${String.format(Locale.US, "%.2f", position.shares)} shares @ $${String.format(Locale.US, "%.2f", position.avgPrice)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Right: Current Price, Total Value & P&L
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", position.totalValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$${String.format(Locale.US, "%.2f", position.currentPrice)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = "P&L",
                        tint = pnlColor,
                        modifier = Modifier.size(12.dp)
                    )
                    val sign = if (isGain) "+" else ""
                    Text(
                        text = "$sign$${String.format(Locale.US, "%.2f", position.unrealizedPnL)} ($sign${String.format(Locale.US, "%.1f", position.unrealizedPnLPercent)}%)",
                        color = pnlColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
