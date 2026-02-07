package com.example.firsttry.model

data class RoomType(
    val id: String,
    val name: String,
    val bedType: String, // e.g., "大床", "双床"
    val size: String, // e.g., "25-30㎡"
    val imageUrl: String,
    val price: Int,
    val hasBreakfast: Boolean = false,
    val cancelPolicy: String = "不可取消"
)

data class HotelDetail(
    val id: String,
    val name: String,
    val nameEn: String,
    val starRating: Int,
    val address: String,
    val openingDate: String,
    val description: String,
    val facilities: List<String>,
    val imageUrls: List<String>,
    val rooms: List<RoomType>
)
