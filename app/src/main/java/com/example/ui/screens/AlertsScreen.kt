package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.MarketShiftAlert
import com.example.ui.theme.*
import com.example.viewmodel.MainUiState

@Composable
fun AlertsScreen(
    uiState: MainUiState,
    onTriggerSimulatedShift: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var breakingOnly by remember { mutableStateOf(false) }
    var thresholdSlider by remember { mutableFloatStateOf(3.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = FinGoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Instant Market Shift Alerts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Push notification engine • Real-time volatility & economic release triggers",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("alerts_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Push Notification Simulation Interactive Hub
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = null,
                                tint = FinGoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "TRIGGER INSTANT MARKET SHIFT (SIMULATOR)",
                                color = FinGoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Test instant push notifications and in-app emergency alert banners across three market scenario triggers:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onTriggerSimulatedShift("CPI_SURPRISE") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_cpi_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF7F1D1D).copy(alpha = 0.7f),
                                    contentColor = Color(0xFFFEE2E2)
                                )
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🚨 Simulate CPI Inflation Print Surprise (-0.2% MoM)", fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = { onTriggerSimulatedShift("NVDA_BREAKOUT") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_nvda_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF065F46).copy(alpha = 0.7f),
                                    contentColor = Color(0xFFD1FAE5)
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ Simulate NVDA Breakout Surge (+5.4%)", fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = { onTriggerSimulatedShift("FED_EMERGENCY") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_fed_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.7f),
                                    contentColor = Color(0xFFDBEAFE)
                                )
                            ) {
                                Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🏛️ Simulate FOMC Emergency Rate Policy Cut", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Notification Rules Configuration
            item {
                Text(
                    text = "PUSH NOTIFICATION PREFERENCES",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Real-Time Push Notifications", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Receive instant OS alerts for high-priority shifts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = pushEnabled, onCheckedChange = { pushEnabled = it })
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Breaking News & Critical Only", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Filter out low-impact routine news articles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = breakingOnly, onCheckedChange = { breakingOnly = it })
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Portfolio Volatility Trigger Threshold", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("±${String.format("%.1f", thresholdSlider)}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                            }
                            Slider(
                                value = thresholdSlider,
                                onValueChange = { thresholdSlider = it },
                                valueRange = 1.0f..10.0f,
                                steps = 8
                            )
                        }
                    }
                }
            }

            // Real-Time Alert Log
            item {
                Text(
                    text = "REAL-TIME ALERTS TIMELINE (${uiState.alerts.size})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            items(uiState.alerts, key = { it.id }) { alert ->
                AlertItemCard(alert = alert)
            }
        }
    }
}

@Composable
fun AlertItemCard(alert: MarketShiftAlert) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_item_${alert.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (alert.isUrgent) Color(0xFFEF4444).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (alert.isUrgent) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alert.isUrgent) Icons.Filled.Warning else Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = if (alert.isUrgent) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = alert.timeAgo,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                alert.ticker?.let { ticker ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$$ticker",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        alert.changePercent?.let { change ->
                            val isPositive = change >= 0
                            val color = if (isPositive) FinGreenGain else FinRedLoss
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.2f", change)}%",
                                color = color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
