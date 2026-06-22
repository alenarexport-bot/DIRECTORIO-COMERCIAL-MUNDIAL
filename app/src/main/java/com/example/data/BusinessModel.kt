package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Restaurantes", "Servicios", "Tiendas", "Salud", "Educación", "Moda"
    val rating: Float,
    val address: String,
    val description: String,
    val phone: String,
    val hours: String,
    val isFavorite: Boolean = false,
    val imageResName: String = "general",
    val isSynced: Boolean = false, // Cloud indicator sync with "LOPII"
    val isCustom: Boolean = false // Track businesses added locally by the user
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int,
    val author: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val businessName: String,
    val description: String,
    val city: String,
    val address: String,
    val discountPercent: Int, // e.g. 50%
    val imageResName: String, // e.g. "img_offer_food", "img_offer_shopping", "img_offer_services"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_streams")
data class LiveStream(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessName: String,
    val title: String,
    val socialPlatform: String, // "TikTok", "Facebook", "Instagram"
    val socialHandle: String,   // e.g. "@latrattoria", "facebook.com/elcarboncito"
    val viewerCount: Int = 120, // Simulated active viewers
    val description: String = "",
    val isStreaming: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fair_products")
data class FairProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int = 0,
    val businessName: String,     // La marca automáticamente heredada
    val businessAddress: String,  // La ubicación automáticamente heredada
    val businessPhone: String,    // El contacto automáticamente heredado
    val productName: String,
    val productPrice: String,
    val productDescription: String,
    val productImageResName: String, // "pizza", "tacos", "clases", "cafe", "herramientas", "dientes", "gimnasio"
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)


