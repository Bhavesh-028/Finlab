package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

// ----------------- ENTITIES -----------------

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val fullContent: String,
    val category: String,
    val tickersRaw: String, // Comma-separated: "NVDA,TSLA"
    val source: String,
    val sourceUrl: String,
    val publishedTimeAgo: String,
    val timestamp: Long,
    val sentiment: String, // BULLISH, BEARISH, NEUTRAL
    val impactRating: String, // CRITICAL, HIGH, MEDIUM, LOW
    val keyTakeawaysRaw: String, // Pipe-separated: "Point 1|Point 2|Point 3"
    val readTimeMinutes: Int,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val viewCount: Int = 0
)

@Entity(tableName = "institutional_reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organization: String,
    val dataMetricsExpected: String,
    val wherePublished: String,
    val officialPortalUrl: String,
    val releaseDate: String,
    val releaseTime: String,
    val frequency: String,
    val marketImpact: String,
    val previousValue: String,
    val consensusForecast: String,
    val actualValue: String?,
    val keyImplication: String,
    val aiBrief: String,
    val isAlertSet: Boolean = false
)

@Entity(tableName = "portfolio_positions")
data class PortfolioEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val shares: Double,
    val avgPrice: Double,
    val currentPrice: Double,
    val assetType: String,
    val sector: String
)

@Entity(tableName = "market_alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val category: String,
    val timestamp: Long,
    val timeAgo: String,
    val isUrgent: Boolean,
    val ticker: String?,
    val changePercent: Double?,
    val isRead: Boolean
)

@Entity(tableName = "engagement_events")
data class EngagementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: String,
    val category: String,
    val ticker: String,
    val actionType: String, // VIEW, BOOKMARK, LIKE, SHARE
    val scoreDelta: Int,
    val timestamp: Long
)

// ----------------- DAOS -----------------

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY timestamp DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY timestamp DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Query("UPDATE articles SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, bookmarked: Boolean)

    @Query("UPDATE articles SET isRead = 1, viewCount = viewCount + 1 WHERE id = :id")
    suspend fun markArticleRead(id: String)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM institutional_reports ORDER BY id ASC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>)

    @Query("UPDATE institutional_reports SET isAlertSet = :isAlertSet WHERE id = :id")
    suspend fun toggleReportAlert(id: String, isAlertSet: Boolean)
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_positions ORDER BY symbol ASC")
    fun getAllPositions(): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPositions(positions: List<PortfolioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: PortfolioEntity)

    @Query("DELETE FROM portfolio_positions WHERE id = :id")
    suspend fun deletePosition(id: String)

    @Query("UPDATE portfolio_positions SET currentPrice = :newPrice WHERE symbol = :symbol")
    suspend fun updatePrice(symbol: String, newPrice: Double)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM market_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Query("UPDATE market_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("DELETE FROM market_alerts")
    suspend fun clearAlerts()
}

@Dao
interface EngagementDao {
    @Query("SELECT * FROM engagement_events ORDER BY timestamp DESC LIMIT 200")
    fun getRecentEvents(): Flow<List<EngagementEntity>>

    @Insert
    suspend fun logEvent(event: EngagementEntity)

    @Query("SELECT category, SUM(scoreDelta) as totalScore FROM engagement_events GROUP BY category")
    suspend fun getCategoryScores(): List<CategoryScoreTuple>

    @Query("SELECT ticker, SUM(scoreDelta) as totalScore FROM engagement_events WHERE ticker != '' GROUP BY ticker")
    suspend fun getTickerScores(): List<TickerScoreTuple>
}

data class CategoryScoreTuple(val category: String, val totalScore: Int)
data class TickerScoreTuple(val ticker: String, val totalScore: Int)

// ----------------- DATABASE -----------------

@Database(
    entities = [
        ArticleEntity::class,
        ReportEntity::class,
        PortfolioEntity::class,
        AlertEntity::class,
        EngagementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinPulseDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun reportDao(): ReportDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun alertDao(): AlertDao
    abstract fun engagementDao(): EngagementDao
}
