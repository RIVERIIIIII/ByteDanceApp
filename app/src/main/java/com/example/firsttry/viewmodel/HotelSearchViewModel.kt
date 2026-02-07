package com.example.firsttry.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firsttry.model.City
import com.example.firsttry.model.HotelSearchQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HotelSearchUiState(
    val selectedCity: String = "北京",
    val isLocating: Boolean = false, // 定位加载状态
    val startDate: Long? = null,
    val endDate: Long? = null,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..1300f,
    val selectedStars: List<Int> = emptyList(), // 保持 List 以兼容数据结构，但逻辑上限制为单选
    val keyword: String = "",
    val selectedTags: List<String> = emptyList(),
    val cities: Map<Char, List<City>> = emptyMap(),
    val popularTags: List<String> = listOf("免费停车场", "亲子", "健身房", "游泳池", "免费取消")
)

class HotelSearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HotelSearchUiState())
    val uiState: StateFlow<HotelSearchUiState> = _uiState.asStateFlow()

    init {
        loadCities()
    }

    private fun loadCities() {
        // Mock data
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

    fun updatePriceRange(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(priceRange = range) }
    }

    // 互斥单选逻辑
    fun toggleStar(star: Int) {
        _uiState.update { currentState ->
            val newStars = if (currentState.selectedStars.contains(star)) {
                emptyList() // 已选中则取消
            } else {
                listOf(star) // 未选中则选中该项，并清除其他
            }
            currentState.copy(selectedStars = newStars)
        }
    }

    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
    }

    fun toggleTag(tag: String) {
        _uiState.update { currentState ->
            val newTags = if (currentState.selectedTags.contains(tag)) {
                currentState.selectedTags - tag
            } else {
                currentState.selectedTags + tag
            }
            currentState.copy(selectedTags = newTags)
        }
    }
    
    fun clearFilters() {
        _uiState.update {
            it.copy(
                priceRange = 0f..1300f,
                selectedStars = emptyList(),
                selectedTags = emptyList()
                // 城市不重置，保持当前选择或定位
            )
        }
    }

    fun getSearchQuery(): HotelSearchQuery {
        val s = _uiState.value
        return HotelSearchQuery(
            city = s.selectedCity,
            startDate = s.startDate,
            endDate = s.endDate,
            priceRangeStart = s.priceRange.start,
            priceRangeEnd = s.priceRange.endInclusive,
            starRatings = s.selectedStars,
            keyword = s.keyword,
            tags = s.selectedTags
        )
    }
}
