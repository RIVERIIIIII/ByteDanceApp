package com.example.firsttry.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firsttry.model.BannerItem
import com.example.firsttry.model.City
import com.example.firsttry.model.HotelSearchQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HotelSearchUiState(
    val selectedCity: String = "北京",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..1300f,
    val selectedStars: Set<Int> = emptySet(),
    val keyword: String = "",
    val popularTags: List<String> = listOf("含早", "免费取消", "近地铁", "大床房", "浴缸"),
    val cities: Map<Char, List<City>> = emptyMap(),
    val isLocating: Boolean = false,
    val banners: List<BannerItem> = emptyList()
)

class HotelSearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HotelSearchUiState())
    val uiState: StateFlow<HotelSearchUiState> = _uiState.asStateFlow()

    init {
        loadCities()
        loadBanners()
    }

    private fun loadBanners() {
        val banners = listOf(
            BannerItem("https://picsum.photos/800/400?random=101", "HOTEL_001"),
            BannerItem("https://picsum.photos/800/400?random=102", "HOTEL_002"),
            BannerItem("https://picsum.photos/800/400?random=103", "HOTEL_003")
        )
        _uiState.update { it.copy(banners = banners) }
    }

    private fun loadCities() {
        val rawCities = listOf(
            City("北京", 'B'), City("上海", 'S'), City("广州", 'G'), City("深圳", 'S'),
            City("杭州", 'H'), City("成都", 'C'), City("武汉", 'W'), City("南京", 'N'),
            City("重庆", 'C'), City("西安", 'X'), City("苏州", 'S'), City("天津", 'T'),
            City("长沙", 'C'), City("郑州", 'Z'), City("东莞", 'D'), City("青岛", 'Q'),
            City("沈阳", 'S'), City("宁波", 'N'), City("昆明", 'K')
        )
        val grouped = rawCities.groupBy { it.initial }.toSortedMap()
        _uiState.update { it.copy(cities = grouped) }
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(selectedCity = city) }
    }

    fun setLocating(isLocating: Boolean) {
        _uiState.update { it.copy(isLocating = isLocating) }
    }

    fun updateDateRange(start: Long?, end: Long?) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
    }

    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
    }
    
    fun toggleTag(tag: String) {
        // In this UI, we don't persist selected tags in the main screen state as they are quick filters,
        // but for consistency we might want to track them if we want to show them as selected.
        // For now, the UI uses chips that just trigger a search or are toggleable.
        // Let's assume we don't store them in uiState for the main screen, or we can add 'selectedTags'.
        // The previous code had:
        // onTagClick = { tag -> viewModel.toggleTag(tag); onSearch(...) }
        // Let's verify what the UI expects.
    }
    
    fun updatePriceRange(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(priceRange = range) }
    }
    
    fun clearFilters() {
        _uiState.update { it.copy(
            priceRange = 0f..1300f,
            selectedStars = emptySet()
        ) }
    }
    
    fun toggleStar(star: Int) {
        _uiState.update { 
            val newStars = if (it.selectedStars.contains(star)) {
                it.selectedStars - star
            } else {
                it.selectedStars + star
            }
            it.copy(selectedStars = newStars)
        }
    }

    fun getSearchQuery(): HotelSearchQuery {
        val state = _uiState.value
        return HotelSearchQuery(
            city = state.selectedCity,
            startDate = state.startDate,
            endDate = state.endDate,
            priceRangeStart = state.priceRange.start,
            priceRangeEnd = state.priceRange.endInclusive,
            starRatings = state.selectedStars.toList(),
            keyword = state.keyword,
            // tags are passed dynamically in the UI quick filter or can be added here
            tags = emptyList() 
        )
    }
}
