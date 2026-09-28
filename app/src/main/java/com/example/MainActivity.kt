package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddHoldingDialog
import com.example.ui.screens.*
import com.example.ui.theme.FinPulseTheme
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinPulseTheme(darkTheme = true) {
                FinPulseApp()
            }
        }
    }
}

@Composable
fun FinPulseApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                // Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = "FinPulse Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = "FINPULSE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "MARKET INTELLIGENCE TERMINAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Right actions
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.selectTab(AppNavTab.AI_INTEL) },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("top_ai_intel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        val navItems = listOf(
                            Triple(AppNavTab.FEED, Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
                            Triple(AppNavTab.REPORTS, Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote),
                            Triple(AppNavTab.PORTFOLIO, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                            Triple(AppNavTab.ALGORITHM, Icons.Filled.Tune, Icons.Outlined.Tune),
                            Triple(AppNavTab.ALERTS, Icons.Filled.Notifications, Icons.Outlined.Notifications),
                            Triple(AppNavTab.AI_INTEL, Icons.Filled.Psychology, Icons.Outlined.Psychology)
                        )

                        navItems.forEach { (tab, filledIcon, outlinedIcon) ->
                            val isSelected = uiState.selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Wide Screen Navigation Rail
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        val navItems = listOf(
                            Triple(AppNavTab.FEED, Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
                            Triple(AppNavTab.REPORTS, Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote),
                            Triple(AppNavTab.PORTFOLIO, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                            Triple(AppNavTab.ALGORITHM, Icons.Filled.Tune, Icons.Outlined.Tune),
                            Triple(AppNavTab.ALERTS, Icons.Filled.Notifications, Icons.Outlined.Notifications),
                            Triple(AppNavTab.AI_INTEL, Icons.Filled.Psychology, Icons.Outlined.Psychology)
                        )

                        navItems.forEach { (tab, filledIcon, outlinedIcon) ->
                            val isSelected = uiState.selectedTab == tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Active Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.selectedTab) {
                        AppNavTab.FEED -> FeedScreen(
                            uiState = uiState,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onToggleBookmarkFilter = viewModel::toggleBookmarkedFilter,
                            onToggleHighImpactFilter = viewModel::toggleHighImpactFilter,
                            onArticleClick = viewModel::openArticleDetail,
                            onBookmarkClick = { viewModel.toggleBookmark(it.id, it.isBookmarked) },
                            onDismissUrgentAlert = viewModel::dismissUrgentBanner,
                            onOpenSearchGrounding = { query ->
                                viewModel.executeGroundedSearch(query)
                                viewModel.selectTab(AppNavTab.AI_INTEL)
                            }
                        )

                        AppNavTab.REPORTS -> ReportsCalendarScreen(
                            reports = uiState.reports,
                            onToggleReportAlert = viewModel::toggleReportAlert
                        )

                        AppNavTab.PORTFOLIO -> PortfolioScreen(
                            uiState = uiState,
                            onSyncPrices = viewModel::syncBrokerageData,
                            onAddPositionClick = { viewModel.toggleAddHoldingDialog(true) },
                            onRemovePosition = viewModel::removePosition,
                            onConnectBrokerClick = { viewModel.toggleConnectBrokerDialog(true) },
                            onArticleClick = viewModel::openArticleDetail
                        )

                        AppNavTab.ALGORITHM -> AlgorithmProfileScreen(
                            uiState = uiState,
                            onToggleWatchlist = viewModel::toggleWatchlistTicker,
                            onToggleCategoryInterest = viewModel::toggleCategoryInterest,
                            onSelectHorizon = viewModel::updateInvestmentHorizon,
                            onSelectRisk = viewModel::updateRiskTolerance,
                            onResetLearning = viewModel::resetAlgorithmicWeights
                        )

                        AppNavTab.ALERTS -> AlertsScreen(
                            uiState = uiState,
                            onTriggerSimulatedShift = viewModel::triggerSimulatedShift
                        )

                        AppNavTab.AI_INTEL -> AiIntelScreen(
                            uiState = uiState,
                            onExecuteQuery = viewModel::executeGroundedSearch
                        )
                    }
                }
            }

            // Article Detail Dialog / Sheet
            uiState.activeArticleDetail?.let { article ->
                ArticleDetailDialog(
                    article = article,
                    aiDeepDiveText = uiState.currentAiDeepDiveText,
                    isAiLoading = uiState.aiDeepDiveLoading,
                    onGenerateAiDeepDive = { viewModel.generateArticleDeepDive(article) },
                    onDismiss = viewModel::closeArticleDetail,
                    onBookmarkToggle = { viewModel.toggleBookmark(article.id, article.isBookmarked) }
                )
            }

            // Add Holding Dialog
            if (uiState.showAddHoldingDialog) {
                AddHoldingDialog(
                    onDismiss = { viewModel.toggleAddHoldingDialog(false) },
                    onConfirm = { symbol, name, shares, price, assetType ->
                        viewModel.addNewPosition(symbol, name, shares, price, assetType)
                    }
                )
            }
        }
    }
}
