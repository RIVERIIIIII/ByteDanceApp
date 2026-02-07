package com.example.firsttry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firsttry.model.HotelDetail
import com.example.firsttry.model.RoomType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HotelDetailUiState(
    val isLoading: Boolean = true,
    val hotel: HotelDetail? = null,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
    val error: String? = null
)

class HotelDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HotelDetailUiState())
    val uiState: StateFlow<HotelDetailUiState> = _uiState.asStateFlow()

    fun loadHotelDetail(hotelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Simulate network delay
            delay(1000)
            
            // Mock Data
            val mockHotel = HotelDetail(
                id = hotelId,
                name = "北京王府井希尔顿酒店",
                nameEn = "Hilton Beijing Wangfujing",
                starRating = 5,
                address = "北京市东城区王府井东街8号",
                openingDate = "2008年开业",
                description = "位于王府井商业区，毗邻故宫和天安门广场。",
                facilities = listOf("免费WiFi", "健身房", "游泳池", "停车场", "会议室", "SPA", "餐厅"),
                imageUrls = listOf(
                    "https://picsum.photos/800/600?random=1",
                    "https://picsum.photos/800/600?random=2",
                    "https://picsum.photos/800/600?random=3",
                    "https://picsum.photos/800/600?random=4",
                    "https://picsum.photos/800/600?random=5"
                ),
                rooms = List(10) { i ->
                    RoomType(
                        id = "room_$i",
                        name = if (i % 2 == 0) "豪华大床房" else "行政双床房",
                        bedType = if (i % 2 == 0) "大床 2m" else "双床 1.2m",
                        size = "${30 + i}㎡",
                        imageUrl = "https://picsum.photos/300/200?random=${10+i}",
                        price = 800 + i * 100,
                        hasBreakfast = i % 3 == 0,
                        cancelPolicy = if (i % 2 == 0) "免费取消" else "不可取消"
                    )
                }
            )
            
            _uiState.update { it.copy(isLoading = false, hotel = mockHotel) }
        }
    }

    fun updateDateRange(start: Long, end: Long) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        // Reload room prices based on new dates if needed
        reloadRoomPrices()
    }

    private fun reloadRoomPrices() {
        // Simulate reloading room prices
        viewModelScope.launch {
             // Logic to update room list based on dates
        }
    }
}
