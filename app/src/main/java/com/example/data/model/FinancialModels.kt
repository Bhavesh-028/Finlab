package com.example.data.model

enum class NewsCategory(val displayName: String, val iconName: String) {
    FOR_YOU("For You", "sparkles"),
    ALL("All News", "newspaper"),
    MACRO("Macro & Fed", "account_balance"),
    TECH_AI("Tech & AI", "memory"),
    EQUITIES("Equities", "trending_up"),
    CRYPTO("Crypto & DeFi", "currency_bitcoin"),
    COMMODITIES("Commodities", "local_gas_station"),
    CENTRAL_BANKS("Central Banks", "domain"),
    VENTURE_IPOS("Venture & IPO", "rocket_launch")
}

enum class Sentiment(val label: String, val score: Float) {
    BULLISH("Bullish", 1.0f),
    NEUTRAL("Neutral", 0.0f),
    BEARISH("Bearish", -1.0f)
}

enum class MarketImpact(val label: String) {
    CRITICAL("Critical Impact"),
    HIGH("High Impact"),
    MEDIUM("Moderate"),
    LOW("Low")
}

enum class InvestmentHorizon(val label: String) {
    DAY_TRADER("Day Trading (Intraday)"),
    SWING("Swing Trading (Days-Weeks)"),
    LONG_TERM_GROWTH("Long-Term Growth (1-5 Yrs)"),
    VALUE_DIVIDEND("Value & Dividend Focus"),
    MACRO_GLOBAL("Global Macro Strategist")
}

enum class RiskTolerance(val label: String) {
    CONSERVATIVE("Conservative Capital Preservation"),
    MODERATE("Balanced Growth & Income"),
    AGGRESSIVE("High Growth & Volatility")
}

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val fullContent: String,
    val category: NewsCategory,
    val tickers: List<String>,
    val source: String,
    val sourceUrl: String,
    val publishedTimeAgo: String,
    val timestamp: Long,
    val sentiment: Sentiment,
    val impactRating: MarketImpact,
    val keyTakeaways: List<String>,
    val readTimeMinutes: Int,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val viewCount: Int = 0,
    val algorithmicFitScore: Int = 75, // 0-100%
    val recommendationReason: String = ""
)

data class InstitutionalReport(
    val id: String,
    val title: String,
    val organization: String,          // e.g., "US Bureau of Labor Statistics (BLS)"
    val dataMetricsExpected: String,    // "Core CPI MoM & YoY, Shelter Index"
    val wherePublished: String,         // "bls.gov/cpi"
    val officialPortalUrl: String,      // https://www.bls.gov/cpi/
    val releaseDate: String,            // "Wed, Oct 14, 2026"
    val releaseTime: String,            // "08:30 AM EDT"
    val frequency: String,              // "Monthly"
    val marketImpact: MarketImpact,
    val previousValue: String,          // "2.9% YoY"
    val consensusForecast: String,      // "2.7% YoY"
    val actualValue: String?,           // null if upcoming, or e.g. "2.6% YoY"
    val keyImplication: String,         // "Higher print reduces FOMC 50bps rate cut odds; yields rise."
    val aiBrief: String,                // Deep Gemini explanation of what traders are watching
    val isAlertSet: Boolean = false
)

enum class AssetType(val label: String) {
    EQUITY("Stock"),
    ETF("ETF"),
    CRYPTO("Crypto"),
    CASH("Cash / Yield")
}

data class PortfolioPosition(
    val id: String,
    val symbol: String,
    val name: String,
    val shares: Double,
    val avgPrice: Double,
    val currentPrice: Double,
    val assetType: AssetType,
    val sector: String
) {
    val totalValue: Double get() = shares * currentPrice
    val totalCost: Double get() = shares * avgPrice
    val unrealizedPnL: Double get() = totalValue - totalCost
    val unrealizedPnLPercent: Double get() = if (totalCost > 0) (unrealizedPnL / totalCost) * 100.0 else 0.0
}

data class BrokerageAccount(
    val brokerId: String,
    val brokerName: String,
    val accountMaskedNumber: String,
    val isConnected: Boolean,
    val encryptionStandard: String = "AES-256-GCM + Hardware Keystore",
    val cashBalance: Double,
    val lastSyncTimestamp: Long
)

data class MarketShiftAlert(
    val id: String,
    val title: String,
    val message: String,
    val category: String,
    val timestamp: Long,
    val timeAgo: String,
    val isUrgent: Boolean,
    val ticker: String? = null,
    val changePercent: Double? = null,
    val isRead: Boolean = false
)

data class UserPreferences(
    val favoriteCategories: Set<NewsCategory> = setOf(NewsCategory.MACRO, NewsCategory.TECH_AI, NewsCategory.EQUITIES),
    val watchlistTickers: Set<String> = setOf("NVDA", "AAPL", "BTC", "SPY", "TSLA"),
    val investmentHorizon: InvestmentHorizon = InvestmentHorizon.LONG_TERM_GROWTH,
    val riskTolerance: RiskTolerance = RiskTolerance.MODERATE,
    val pushNotificationsEnabled: Boolean = true,
    val breakingAlertsOnly: Boolean = false,
    val alertThresholdPercent: Double = 3.0,
    val biometricLockEnabled: Boolean = true,
    // Dynamic algorithmic weights learned from reading behaviors:
    val categoryEngagementWeights: Map<String, Int> = mapOf(
        "MACRO" to 38,
        "TECH_AI" to 42,
        "EQUITIES" to 20,
        "CRYPTO" to 15,
        "COMMODITIES" to 5
    ),
    val tickerEngagementWeights: Map<String, Int> = mapOf(
        "NVDA" to 45,
        "AAPL" to 30,
        "BTC" to 22,
        "SPY" to 35
    )
)
