package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinPulseRepository(private val db: FinPulseDatabase) {

    private val articleDao = db.articleDao()
    private val reportDao = db.reportDao()
    private val portfolioDao = db.portfolioDao()
    private val alertDao = db.alertDao()
    private val engagementDao = db.engagementDao()

    suspend fun ensureInitialData() {
        // Pre-populate articles if empty
        val currentArticles = articleDao.getArticleById(PrepopulateData.sampleArticles.first().id)
        if (currentArticles == null) {
            articleDao.insertArticles(PrepopulateData.sampleArticles)
        }

        // Pre-populate reports
        val reportEntities = PrepopulateData.sampleReports.map { r ->
            ReportEntity(
                id = r.id,
                title = r.title,
                organization = r.organization,
                dataMetricsExpected = r.dataMetricsExpected,
                wherePublished = r.wherePublished,
                officialPortalUrl = r.officialPortalUrl,
                releaseDate = r.releaseDate,
                releaseTime = r.releaseTime,
                frequency = r.frequency,
                marketImpact = r.marketImpact.name,
                previousValue = r.previousValue,
                consensusForecast = r.consensusForecast,
                actualValue = r.actualValue,
                keyImplication = r.keyImplication,
                aiBrief = r.aiBrief,
                isAlertSet = r.isAlertSet
            )
        }
        reportDao.insertReports(reportEntities)

        // Pre-populate portfolio positions
        portfolioDao.insertPositions(PrepopulateData.samplePositions)

        // Pre-populate alerts
        alertDao.insertAlerts(PrepopulateData.sampleAlerts)
    }

    // ----------------- ARTICLES & ALGORITHMIC ENGINE -----------------

    fun getArticlesStream(userPrefs: UserPreferences): Flow<List<NewsArticle>> {
        return articleDao.getAllArticles().map { entities ->
            entities.map { entity ->
                val tickers = entity.tickersRaw.split(",").filter { it.isNotBlank() }
                val takeaways = entity.keyTakeawaysRaw.split("|").filter { it.isNotBlank() }
                val categoryEnum = try {
                    NewsCategory.valueOf(entity.category)
                } catch (e: Exception) {
                    NewsCategory.ALL
                }

                // Compute algorithmic fit score
                val (score, reason) = computeAlgorithmicScore(entity, tickers, categoryEnum, userPrefs)

                NewsArticle(
                    id = entity.id,
                    title = entity.title,
                    summary = entity.summary,
                    fullContent = entity.fullContent,
                    category = categoryEnum,
                    tickers = tickers,
                    source = entity.source,
                    sourceUrl = entity.sourceUrl,
                    publishedTimeAgo = entity.publishedTimeAgo,
                    timestamp = entity.timestamp,
                    sentiment = try { Sentiment.valueOf(entity.sentiment) } catch (e: Exception) { Sentiment.NEUTRAL },
                    impactRating = try { MarketImpact.valueOf(entity.impactRating) } catch (e: Exception) { MarketImpact.MEDIUM },
                    keyTakeaways = takeaways,
                    readTimeMinutes = entity.readTimeMinutes,
                    isBookmarked = entity.isBookmarked,
                    isRead = entity.isRead,
                    viewCount = entity.viewCount,
                    algorithmicFitScore = score,
                    recommendationReason = reason
                )
            }
        }
    }

    private fun computeAlgorithmicScore(
        entity: ArticleEntity,
        tickers: List<String>,
        category: NewsCategory,
        prefs: UserPreferences
    ): Pair<Int, String> {
        var score = 50
        val reasons = mutableListOf<String>()

        // 1. Explicit Category Preference
        if (prefs.favoriteCategories.contains(category)) {
            score += 20
            reasons.add("Matches your saved '${category.displayName}' sector interest")
        }

        // 2. Dynamic Engagement Weight in Category
        val catEngage = prefs.categoryEngagementWeights[category.name] ?: 0
        if (catEngage > 0) {
            val boost = (catEngage / 2).coerceAtMost(15)
            score += boost
            if (boost > 8) reasons.add("High reading activity in ${category.displayName}")
        }

        // 3. Watchlist Ticker Overlap
        val matchingTickers = tickers.filter { prefs.watchlistTickers.contains(it) }
        if (matchingTickers.isNotEmpty()) {
            score += 25
            reasons.add("Features your watchlist ticker(s): ${matchingTickers.joinToString(", ") { "$$it" }}")
        }

        // 4. Critical Impact Priority
        if (entity.impactRating == MarketImpact.CRITICAL.name) {
            score += 10
            reasons.add("Critical market impact release")
        }

        // Normalize
        val finalScore = score.coerceIn(40, 99)
        val finalReason = if (reasons.isNotEmpty()) reasons.joinToString(" • ") else "Trending general market intelligence"
        return Pair(finalScore, finalReason)
    }

    suspend fun toggleBookmark(articleId: String, currentBookmarked: Boolean) {
        articleDao.updateBookmark(articleId, !currentBookmarked)
        if (!currentBookmarked) {
            // Log positive engagement
            val article = articleDao.getArticleById(articleId)
            if (article != null) {
                val ticker = article.tickersRaw.split(",").firstOrNull() ?: ""
                engagementDao.logEvent(
                    EngagementEntity(
                        articleId = articleId,
                        category = article.category,
                        ticker = ticker,
                        actionType = "BOOKMARK",
                        scoreDelta = 10,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun recordArticleRead(articleId: String) {
        articleDao.markArticleRead(articleId)
        val article = articleDao.getArticleById(articleId)
        if (article != null) {
            val ticker = article.tickersRaw.split(",").firstOrNull() ?: ""
            engagementDao.logEvent(
                EngagementEntity(
                    articleId = articleId,
                    category = article.category,
                    ticker = ticker,
                    actionType = "VIEW",
                    scoreDelta = 5,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addCustomArticle(article: ArticleEntity) {
        articleDao.insertArticle(article)
    }

    // ----------------- INSTITUTIONAL REPORTS -----------------

    fun getReportsStream(): Flow<List<InstitutionalReport>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { entity ->
                InstitutionalReport(
                    id = entity.id,
                    title = entity.title,
                    organization = entity.organization,
                    dataMetricsExpected = entity.dataMetricsExpected,
                    wherePublished = entity.wherePublished,
                    officialPortalUrl = entity.officialPortalUrl,
                    releaseDate = entity.releaseDate,
                    releaseTime = entity.releaseTime,
                    frequency = entity.frequency,
                    marketImpact = try { MarketImpact.valueOf(entity.marketImpact) } catch (e: Exception) { MarketImpact.MEDIUM },
                    previousValue = entity.previousValue,
                    consensusForecast = entity.consensusForecast,
                    actualValue = entity.actualValue,
                    keyImplication = entity.keyImplication,
                    aiBrief = entity.aiBrief,
                    isAlertSet = entity.isAlertSet
                )
            }
        }
    }

    suspend fun toggleReportAlert(reportId: String, isAlertSet: Boolean) {
        reportDao.toggleReportAlert(reportId, !isAlertSet)
    }

    // ----------------- PORTFOLIO & LIVE BROKERAGE -----------------

    fun getPortfolioPositionsStream(): Flow<List<PortfolioPosition>> {
        return portfolioDao.getAllPositions().map { entities ->
            entities.map { entity ->
                PortfolioPosition(
                    id = entity.id,
                    symbol = entity.symbol,
                    name = entity.name,
                    shares = entity.shares,
                    avgPrice = entity.avgPrice,
                    currentPrice = entity.currentPrice,
                    assetType = try { AssetType.valueOf(entity.assetType) } catch (e: Exception) { AssetType.EQUITY },
                    sector = entity.sector
                )
            }
        }
    }

    suspend fun addPosition(position: PortfolioPosition) {
        portfolioDao.insertPosition(
            PortfolioEntity(
                id = position.id,
                symbol = position.symbol,
                name = position.name,
                shares = position.shares,
                avgPrice = position.avgPrice,
                currentPrice = position.currentPrice,
                assetType = position.assetType.name,
                sector = position.sector
            )
        )
    }

    suspend fun deletePosition(positionId: String) {
        portfolioDao.deletePosition(positionId)
    }

    suspend fun simulateLiveMarketTick() {
        val positions = PrepopulateData.samplePositions
        for (pos in positions) {
            // Apply small realistic +/- 0.5% - 2.0% fluctuation
            val deltaPercent = (Math.random() - 0.48) * 0.03
            val newPrice = (pos.currentPrice * (1.0 + deltaPercent)).let { Math.round(it * 100.0) / 100.0 }
            portfolioDao.updatePrice(pos.symbol, newPrice)
        }
    }

    // ----------------- ALERTS & NOTIFICATIONS -----------------

    fun getAlertsStream(): Flow<List<MarketShiftAlert>> {
        return alertDao.getAllAlerts().map { entities ->
            entities.map { entity ->
                MarketShiftAlert(
                    id = entity.id,
                    title = entity.title,
                    message = entity.message,
                    category = entity.category,
                    timestamp = entity.timestamp,
                    timeAgo = entity.timeAgo,
                    isUrgent = entity.isUrgent,
                    ticker = entity.ticker,
                    changePercent = entity.changePercent,
                    isRead = entity.isRead
                )
            }
        }
    }

    suspend fun markAlertRead(alertId: String) {
        alertDao.markRead(alertId)
    }

    suspend fun clearAlerts() {
        alertDao.clearAlerts()
    }

    suspend fun triggerMarketShiftAlert(
        title: String,
        message: String,
        ticker: String? = null,
        changePercent: Double? = null,
        isUrgent: Boolean = true
    ) {
        val alert = AlertEntity(
            id = "alert_${System.currentTimeMillis()}",
            title = title,
            message = message,
            category = if (isUrgent) "URGENT_SHIFT" else "MARKET_PULSE",
            timestamp = System.currentTimeMillis(),
            timeAgo = "Just now",
            isUrgent = isUrgent,
            ticker = ticker,
            changePercent = changePercent,
            isRead = false
        )
        alertDao.insertAlert(alert)
    }

    // ----------------- ENGAGEMENT TELEMETRY -----------------

    suspend fun getCategoryEngagementScores(): Map<String, Int> {
        val tuples = engagementDao.getCategoryScores()
        return tuples.associate { it.category to it.totalScore }
    }

    suspend fun getTickerEngagementScores(): Map<String, Int> {
        val tuples = engagementDao.getTickerScores()
        return tuples.associate { it.ticker to it.totalScore }
    }
}
