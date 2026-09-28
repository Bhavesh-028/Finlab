package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.ArticleEntity
import com.example.data.local.FinPulseDatabase
import com.example.data.model.*
import com.example.data.remote.GeminiMarketService
import com.example.data.remote.GroundedMarketInsight
import com.example.data.repository.FinPulseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String, val iconName: String) {
    FEED("Feed", "newspaper"),
    REPORTS("Reports", "analytics"),
    PORTFOLIO("Portfolio", "account_balance_wallet"),
    ALGORITHM("Algorithm", "tune"),
    ALERTS("Alerts", "notifications"),
    AI_INTEL("AI Intel", "psychology")
}

data class MainUiState(
    val selectedTab: AppNavTab = AppNavTab.FEED,
    val selectedCategory: NewsCategory = NewsCategory.FOR_YOU,
    val searchQuery: String = "",
    val showBookmarkedOnly: Boolean = false,
    val showHighImpactOnly: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences(),
    val articles: List<NewsArticle> = emptyList(),
    val reports: List<InstitutionalReport> = emptyList(),
    val positions: List<PortfolioPosition> = emptyList(),
    val alerts: List<MarketShiftAlert> = emptyList(),
    val activeArticleDetail: NewsArticle? = null,
    val activeReportDetail: InstitutionalReport? = null,
    val latestUrgentAlert: MarketShiftAlert? = null,
    val isGroundedSearching: Boolean = false,
    val groundedSearchResult: GroundedMarketInsight? = null,
    val groundedSearchQuery: String = "",
    val isSyncingBrokerage: Boolean = false,
    val isEncryptedVaultUnlocked: Boolean = true,
    val showAddHoldingDialog: Boolean = false,
    val showConnectBrokerDialog: Boolean = false,
    val connectedBrokers: List<BrokerageAccount> = listOf(
        BrokerageAccount(
            brokerId = "ibkr_01",
            brokerName = "Interactive Brokers (IBKR)",
            accountMaskedNumber = "U***9421",
            isConnected = true,
            encryptionStandard = "AES-256-GCM / Hardware Keystore Enclave",
            cashBalance = 14250.80,
            lastSyncTimestamp = System.currentTimeMillis()
        ),
        BrokerageAccount(
            brokerId = "alpaca_02",
            brokerName = "Alpaca Securities Direct API",
            accountMaskedNumber = "ALP-***682",
            isConnected = true,
            encryptionStandard = "Hardware Token Enclave • TLS 1.3",
            cashBalance = 8400.00,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    ),
    val aiDeepDiveLoading: Boolean = false,
    val currentAiDeepDiveText: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        FinPulseDatabase::class.java,
        "finpulse.db"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    private val repository = FinPulseRepository(db)
    private val geminiService = GeminiMarketService()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
            observeDatabaseStreams()
        }
    }

    private fun observeDatabaseStreams() {
        // Observe articles
        viewModelScope.launch {
            repository.getArticlesStream(_uiState.value.userPreferences).collect { articleList ->
                _uiState.update { it.copy(articles = articleList) }
            }
        }

        // Observe reports
        viewModelScope.launch {
            repository.getReportsStream().collect { reportList ->
                _uiState.update { it.copy(reports = reportList) }
            }
        }

        // Observe portfolio
        viewModelScope.launch {
            repository.getPortfolioPositionsStream().collect { posList ->
                _uiState.update { it.copy(positions = posList) }
            }
        }

        // Observe alerts
        viewModelScope.launch {
            repository.getAlertsStream().collect { alertList ->
                val urgent = alertList.firstOrNull { it.isUrgent && !it.isRead }
                _uiState.update { it.copy(alerts = alertList, latestUrgentAlert = urgent) }
            }
        }
    }

    fun selectTab(tab: AppNavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectCategory(category: NewsCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleBookmarkedFilter() {
        _uiState.update { it.copy(showBookmarkedOnly = !it.showBookmarkedOnly) }
    }

    fun toggleHighImpactFilter() {
        _uiState.update { it.copy(showHighImpactOnly = !it.showHighImpactOnly) }
    }

    fun openArticleDetail(article: NewsArticle) {
        _uiState.update {
            it.copy(
                activeArticleDetail = article,
                currentAiDeepDiveText = null,
                aiDeepDiveLoading = false
            )
        }
        viewModelScope.launch {
            repository.recordArticleRead(article.id)
            refreshEngagementWeights()
        }
    }

    fun closeArticleDetail() {
        _uiState.update { it.copy(activeArticleDetail = null, currentAiDeepDiveText = null) }
    }

    fun openReportDetail(report: InstitutionalReport) {
        _uiState.update { it.copy(activeReportDetail = report) }
    }

    fun closeReportDetail() {
        _uiState.update { it.copy(activeReportDetail = null) }
    }

    fun toggleBookmark(articleId: String, currentVal: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId, currentVal)
            refreshEngagementWeights()
        }
    }

    fun toggleReportAlert(reportId: String, currentAlert: Boolean) {
        viewModelScope.launch {
            repository.toggleReportAlert(reportId, currentAlert)
        }
    }

    fun dismissUrgentBanner() {
        val currentUrgent = _uiState.value.latestUrgentAlert
        if (currentUrgent != null) {
            viewModelScope.launch {
                repository.markAlertRead(currentUrgent.id)
            }
        }
        _uiState.update { it.copy(latestUrgentAlert = null) }
    }

    // ----------------- GEMINI SEARCH GROUNDING -----------------

    fun executeGroundedSearch(query: String) {
        if (query.isBlank()) return
        _uiState.update {
            it.copy(
                isGroundedSearching = true,
                groundedSearchQuery = query
            )
        }
        viewModelScope.launch {
            val result = geminiService.queryMarketIntelligence(query, enableSearchGrounding = true)
            _uiState.update {
                it.copy(
                    isGroundedSearching = false,
                    groundedSearchResult = result
                )
            }
        }
    }

    fun generateArticleDeepDive(article: NewsArticle) {
        _uiState.update { it.copy(aiDeepDiveLoading = true) }
        viewModelScope.launch {
            val prompt = "Provide a deep dive institutional impact analysis on this financial news: '${article.title}'. Summary: ${article.summary}. Mention potential impact on tickers ${article.tickers.joinToString(", ")} and investor recommendation."
            val result = geminiService.queryMarketIntelligence(prompt, enableSearchGrounding = true)
            _uiState.update {
                it.copy(
                    aiDeepDiveLoading = false,
                    currentAiDeepDiveText = result.summary
                )
            }
        }
    }

    // ----------------- BROKERAGE & PORTFOLIO -----------------

    fun syncBrokerageData() {
        _uiState.update { it.copy(isSyncingBrokerage = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(800) // Realistic secure API latency
            repository.simulateLiveMarketTick()
            _uiState.update { it.copy(isSyncingBrokerage = false) }
        }
    }

    fun addNewPosition(symbol: String, name: String, shares: Double, price: Double, assetType: AssetType) {
        viewModelScope.launch {
            val pos = PortfolioPosition(
                id = "pos_${System.currentTimeMillis()}",
                symbol = symbol.uppercase().trim(),
                name = name.ifBlank { "${symbol.uppercase()} Asset" },
                shares = shares,
                avgPrice = price,
                currentPrice = price,
                assetType = assetType,
                sector = "User Added"
            )
            repository.addPosition(pos)
            _uiState.update { it.copy(showAddHoldingDialog = false) }
        }
    }

    fun removePosition(id: String) {
        viewModelScope.launch {
            repository.deletePosition(id)
        }
    }

    fun toggleAddHoldingDialog(show: Boolean) {
        _uiState.update { it.copy(showAddHoldingDialog = show) }
    }

    fun toggleConnectBrokerDialog(show: Boolean) {
        _uiState.update { it.copy(showConnectBrokerDialog = show) }
    }

    // ----------------- ALERTS & SHIFTS SIMULATION -----------------

    fun triggerSimulatedShift(type: String) {
        viewModelScope.launch {
            when (type) {
                "CPI_SURPRISE" -> {
                    repository.triggerMarketShiftAlert(
                        title = "🚨 Flash Alert: BLS CPI Prints at 2.4% YoY (Below 2.6% Est)",
                        message = "Accelerated disinflation trigger! 10-Year Treasury Yield dumps 12 bps to 3.88%. S&P futures surge +1.2%.",
                        ticker = "CPI",
                        changePercent = -0.2,
                        isUrgent = true
                    )
                }
                "NVDA_BREAKOUT" -> {
                    repository.triggerMarketShiftAlert(
                        title = "⚡ NVDA Spikes +5.4% in Heavy Volume",
                        message = "Tier-1 sovereign compute contract confirmed with $12B enterprise allocation backlog.",
                        ticker = "NVDA",
                        changePercent = 5.4,
                        isUrgent = true
                    )
                }
                "FED_EMERGENCY" -> {
                    repository.triggerMarketShiftAlert(
                        title = "🏛️ FOMC Statement: Rate Corridor Reduced by 50 bps",
                        message = "Federal Reserve announces recalibration to preserve maximum employment while inflation moderates.",
                        ticker = "SPY",
                        changePercent = 2.1,
                        isUrgent = true
                    )
                }
                else -> {
                    repository.triggerMarketShiftAlert(
                        title = "🔔 Market Shift Alert: Unusual Volume Detected",
                        message = "Heavy institutional block trades in spot BTC ETFs and semiconductor equities.",
                        ticker = "BTC",
                        changePercent = 3.2,
                        isUrgent = false
                    )
                }
            }
        }
    }

    // ----------------- USER PREFERENCES & ALGORITHM TUNING -----------------

    fun toggleWatchlistTicker(ticker: String) {
        val current = _uiState.value.userPreferences.watchlistTickers.toMutableSet()
        if (current.contains(ticker)) current.remove(ticker) else current.add(ticker)
        val updated = _uiState.value.userPreferences.copy(watchlistTickers = current)
        _uiState.update { it.copy(userPreferences = updated) }
    }

    fun toggleCategoryInterest(category: NewsCategory) {
        val current = _uiState.value.userPreferences.favoriteCategories.toMutableSet()
        if (current.contains(category)) current.remove(category) else current.add(category)
        val updated = _uiState.value.userPreferences.copy(favoriteCategories = current)
        _uiState.update { it.copy(userPreferences = updated) }
    }

    fun updateInvestmentHorizon(horizon: InvestmentHorizon) {
        val updated = _uiState.value.userPreferences.copy(investmentHorizon = horizon)
        _uiState.update { it.copy(userPreferences = updated) }
    }

    fun updateRiskTolerance(risk: RiskTolerance) {
        val updated = _uiState.value.userPreferences.copy(riskTolerance = risk)
        _uiState.update { it.copy(userPreferences = updated) }
    }

    fun resetAlgorithmicWeights() {
        val updated = _uiState.value.userPreferences.copy(
            categoryEngagementWeights = mapOf(
                "MACRO" to 30,
                "TECH_AI" to 35,
                "EQUITIES" to 20,
                "CRYPTO" to 15
            ),
            tickerEngagementWeights = mapOf("NVDA" to 30, "AAPL" to 20, "BTC" to 20, "SPY" to 25)
        )
        _uiState.update { it.copy(userPreferences = updated) }
    }

    private suspend fun refreshEngagementWeights() {
        val catScores = repository.getCategoryEngagementScores()
        val tickerScores = repository.getTickerEngagementScores()
        if (catScores.isNotEmpty() || tickerScores.isNotEmpty()) {
            val updated = _uiState.value.userPreferences.copy(
                categoryEngagementWeights = catScores,
                tickerEngagementWeights = tickerScores
            )
            _uiState.update { it.copy(userPreferences = updated) }
        }
    }
}
