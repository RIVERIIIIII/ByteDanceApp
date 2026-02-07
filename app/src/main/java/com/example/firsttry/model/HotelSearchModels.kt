package com.example.firsttry.model

import java.io.Serializable

data class HotelSearchQuery(
    val city: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val priceRangeStart: Float = 0f,
    val priceRangeEnd: Float = 1300f,
    val starRatings: List<Int> = emptyList(), // 2 (<=2), 3, 4, 5
    val keyword: String = "",
    val tags: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sortMode: HotelSortMode = HotelSortMode.DISTANCE
) : Serializable

enum class HotelSortMode {
    DISTANCE, PRICE_LOW_TO_HIGH
}

data class Hotel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val price: Int,
    val distance: Float, // km
    val tags: List<String>
)

data class City(
    val name: String,
    val initial: Char
)
