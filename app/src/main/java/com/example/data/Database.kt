package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY rating DESC, name ASC")
    fun getAllBusinesses(): Flow<List<Business>>

    @Query("SELECT * FROM businesses WHERE category = :category ORDER BY rating DESC")
    fun getBusinessesByCategory(category: String): Flow<List<Business>>

    @Query("SELECT * FROM businesses WHERE isFavorite = 1")
    fun getFavoriteBusinesses(): Flow<List<Business>>

    @Query("SELECT * FROM businesses WHERE id = :id")
    suspend fun getBusinessById(id: Int): Business?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBusinesses(businesses: List<Business>)

    @Update
    suspend fun updateBusiness(business: Business)

    @Delete
    suspend fun deleteBusiness(business: Business)

    @Query("UPDATE businesses SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("UPDATE businesses SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, isSynced: Boolean)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getReviewsForBusiness(businessId: Int): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviews(reviews: List<Review>)

    @Delete
    suspend fun deleteReview(review: Review)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers ORDER BY timestamp DESC")
    fun getAllOffers(): Flow<List<Offer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOffers(offers: List<Offer>)

    @Delete
    suspend fun deleteOffer(offer: Offer)
}

@Dao
interface LiveStreamDao {
    @Query("SELECT * FROM live_streams WHERE isStreaming = 1 ORDER BY timestamp DESC")
    fun getActiveLiveStreams(): Flow<List<LiveStream>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveStream(liveStream: LiveStream): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLiveStreams(liveStreams: List<LiveStream>)

    @Update
    suspend fun updateLiveStream(liveStream: LiveStream)

    @Delete
    suspend fun deleteLiveStream(liveStream: LiveStream)
}

@Dao
interface FairProductDao {
    @Query("SELECT * FROM fair_products ORDER BY timestamp DESC")
    fun getAllFairProducts(): Flow<List<FairProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFairProduct(product: FairProduct): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFairProducts(products: List<FairProduct>)

    @Query("UPDATE fair_products SET likesCount = likesCount + 1 WHERE id = :productId")
    suspend fun likeProduct(productId: Int)

    @Delete
    suspend fun deleteFairProduct(product: FairProduct)
}

@Database(entities = [Business::class, Review::class, Offer::class, LiveStream::class, FairProduct::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun reviewDao(): ReviewDao
    abstract fun offerDao(): OfferDao
    abstract fun liveStreamDao(): LiveStreamDao
    abstract fun fairProductDao(): FairProductDao
}
