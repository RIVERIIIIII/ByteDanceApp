package com.example.firsttry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firsttry.model.City
import com.example.firsttry.model.Hotel
import com.example.firsttry.model.HotelSearchQuery
import com.example.firsttry.model.HotelSortMode
import com.example.firsttry.remote.Http.HttpClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

data class HotelListUiState(
    val hotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val query: HotelSearchQuery = HotelSearchQuery(),
    val cities: Map<Char, List<City>> = emptyMap(),
    val error: String? = null
)

class HotelListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HotelListUiState())
    val uiState: StateFlow<HotelListUiState> = _uiState.asStateFlow()

    private val client = HttpClient.getClient()
    private val gson = Gson()

    init {
        loadCities()
    }

    fun initQuery(query: HotelSearchQuery) {
        _uiState.update { it.copy(query = query) }
        searchHotels()
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
        _uiState.update { it.copy(query = it.query.copy(city = city)) }
        searchHotels()
    }

    fun updateDateRange(start: Long?, end: Long?) {
        _uiState.update { it.copy(query = it.query.copy(startDate = start, endDate = end)) }
        searchHotels()
    }

    fun updateSortMode(mode: HotelSortMode) {
        _uiState.update { it.copy(query = it.query.copy(sortMode = mode)) }
        searchHotels()
    }
    
    fun updateLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(query = it.query.copy(latitude = lat, longitude = lng)) }
        // If sorting by distance, refresh
        if (_uiState.value.query.sortMode == HotelSortMode.DISTANCE) {
            searchHotels()
        }
    }

    fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(query = it.query.copy(tags = tags)) }
        searchHotels()
    }

    fun searchHotels() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val query = _uiState.value.query
                // Fallback to mock directly for now since backend is not guaranteed
                // In real implementation, uncomment below:
                /*
                val json = gson.toJson(query)
                val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(HttpClient.BASE_URL + "hotel/search")
                    .post(body)
                    .build()
                client.newCall(request).execute().use { ... }
                */
                
                // Simulate network delay
                Thread.sleep(1000)
                
                val mockHotels = getMockHotels(query)
                _uiState.update { it.copy(hotels = mockHotels, isLoading = false) }
                
            } catch (e: Exception) {
                 val mockHotels = getMockHotels(_uiState.value.query)
                 _uiState.update { it.copy(hotels = mockHotels, isLoading = false) }
            }
        }
    }
    
    private fun getMockHotels(query: HotelSearchQuery): List<Hotel> {
        val baseHotels = List(20) { i ->
            Hotel(
                id = "$i",
                name = "${query.city.ifEmpty { "City" }} Hotel $i",
                imageUrl = "https://picsum.photos/200/200?random=$i",
                price = (200..1000).random(),
                distance = (1..10).random() + 0.5f,
                tags = listOf("浴缸", "大床房", "近地铁", "含早", "免费取消").shuffled().take((1..3).random())
            )
        }
        
        // Filter by tags
        var result = if (query.tags.isNotEmpty()) {
            baseHotels.filter { hotel ->
                hotel.tags.any { it in query.tags }
            }
        } else {
            baseHotels
        }

        // Sort
        result = when(query.sortMode) {
            HotelSortMode.DISTANCE -> result.sortedBy { it.distance }
            HotelSortMode.PRICE_LOW_TO_HIGH -> result.sortedBy { it.price }
        }
        
        return result
    }
}
