package com.example.firsttry.activity.hotel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firsttry.activity.message.MessageActivity
import com.example.firsttry.model.City
import com.example.firsttry.ui.theme.FirstTryTheme
import com.example.firsttry.utils.LocationHelper
import com.example.firsttry.ui.component.CityPickerDialog
import com.example.firsttry.ui.component.DateRangePickerDialog
import com.example.firsttry.viewmodel.HotelSearchUiState
import com.example.firsttry.viewmodel.HotelSearchViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HotelSearchComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstTryTheme {
                HotelSearchScreen(
                    onNavigateToMessage = {
                        startActivity(Intent(this, MessageActivity::class.java))
                    },
                    onSearch = { query ->
                        val intent = Intent(this, HotelListActivity::class.java)
                        intent.putExtra("query", query)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelSearchScreen(
    viewModel: HotelSearchViewModel = viewModel(),
    onNavigateToMessage: () -> Unit,
    onSearch: (com.example.firsttry.model.HotelSearchQuery) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showCityPicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setLocating(true)
            locationHelper.getCurrentCity(
                onSuccess = { city ->
                    viewModel.updateCity(city)
                    viewModel.setLocating(false)
                },
                onFailure = {
                    viewModel.setLocating(false)
                    Toast.makeText(context, "定位失败，请手动选择", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "需要位置权限才能获取当前位置", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("酒店搜索") },
                actions = {
                    IconButton(onClick = onNavigateToMessage) {
                        Icon(Icons.Default.Email, contentDescription = "Message")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            // 1. Banner
            BannerSection()

            Spacer(modifier = Modifier.height(16.dp))

            // 2. City Picker
            CityPickerSection(
                city = uiState.selectedCity,
                isLocating = uiState.isLocating,
                onClick = { showCityPicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 3. Date Range
            DateRangeSection(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onClick = { showDateRangePicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 4. Keyword Search
            KeywordSearchSection(
                keyword = uiState.keyword,
                onValueChange = viewModel::updateKeyword
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Quick Filter Chips
            QuickFiltersSection(
                tags = uiState.popularTags,
                onTagClick = { tag ->
                    viewModel.toggleTag(tag)
                    val currentQuery = viewModel.getSearchQuery()
                    onSearch(currentQuery.copy(tags = listOf(tag)))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Filter Button (opens BottomSheet)
            Button(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("更多筛选")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Search Button
            Button(
                onClick = { onSearch(viewModel.getSearchQuery()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("查询", fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            cities = uiState.cities,
            onDismiss = { showCityPicker = false },
            onCitySelected = { city ->
                viewModel.updateCity(city.name)
                showCityPicker = false
            },
            onLocateClick = {
                // Check Permission
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    viewModel.setLocating(true)
                    showCityPicker = false // Close dialog to show loading on main screen
                    locationHelper.getCurrentCity(
                        onSuccess = { city ->
                            viewModel.updateCity(city)
                            viewModel.setLocating(false)
                        },
                        onFailure = {
                            viewModel.setLocating(false)
                            Toast.makeText(context, "定位失败，请手动选择", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    showCityPicker = false // Close dialog to handle permission
                }
            }
        )
    }

    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.updateDateRange(start, end)
                showDateRangePicker = false
            }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onApply = { priceRange ->
                viewModel.updatePriceRange(priceRange)
                showFilterSheet = false
            },
            onClear = { 
                viewModel.clearFilters() 
                // Don't close sheet immediately, let user see it cleared
            },
            onToggleStar = viewModel::toggleStar
        )
    }
}

@Composable
fun BannerSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)) {
        HorizontalPager(state = pagerState) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (page) {
                            0 -> Color(0xFFE0E0E0)
                            1 -> Color(0xFFBDBDBD)
                            else -> Color(0xFF9E9E9E)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Banner Ad ${page + 1}", fontSize = 24.sp, color = Color.White)
            }
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun CityPickerSection(city: String, isLocating: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("目的地", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            if (isLocating) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     CircularProgressIndicator(
                         modifier = Modifier.size(16.dp),
                         strokeWidth = 2.dp,
                         color = MaterialTheme.colorScheme.primary
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("定位中...", style = MaterialTheme.typography.titleMedium)
                 }
            } else {
                Text(city, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Select")
    }
}

@Composable
fun DateRangeSection(startDate: Long?, endDate: Long?, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())
    val startStr = startDate?.let { formatter.format(Date(it)) } ?: "入住日期"
    val endStr = endDate?.let { formatter.format(Date(it)) } ?: "离店日期"
    val nights = if (startDate != null && endDate != null) {
        val diff = endDate - startDate
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } else {
        0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("入住", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(startStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("共 $nights 晚", style = MaterialTheme.typography.bodySmall, modifier = Modifier
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(4.dp))
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text("离店", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(endStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KeywordSearchSection(keyword: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        TextField(
            value = keyword,
            onValueChange = onValueChange,
            placeholder = { Text("位置/品牌/定位") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuickFiltersSection(tags: List<String>, onTagClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            SuggestionChip(
                onClick = { onTagClick(tag) },
                label = { Text(tag) }
            )
        }
    }
}

// Helper for FlowRow if not available in older Compose (ExperimentalLayoutApi)
// Renamed to avoid conflict with top-level or other file definitions
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowHelper(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: HotelSearchUiState,
    onDismiss: () -> Unit,
    onApply: (ClosedFloatingPointRange<Float>) -> Unit,
    onClear: () -> Unit,
    onToggleStar: (Int) -> Unit
) {
    // 修复 Bug：每次打开 BottomSheet 时，使用 remember(uiState.priceRange) 确保状态同步
    var priceRange by remember(uiState.priceRange) { mutableStateOf(uiState.priceRange) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("价格区间", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // 修复：范围 0-1300
            RangeSlider(
                value = priceRange,
                onValueChange = { priceRange = it },
                valueRange = 0f..1300f,
                steps = 12 // (1300-0)/100 - 1 = 12
            )
            
            // 交互：动态价格文本
            val startText = priceRange.start.toInt()
            val endText = priceRange.endInclusive.toInt()
            val endDisplay = if (endText >= 1300) "1300+" else endText.toString()
            
            Text(
                text = "￥$startText - ￥$endDisplay",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("星级", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRowHelper(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val stars = listOf(2, 3, 4, 5)
                val labels = mapOf(2 to "二星及以下", 3 to "三星", 4 to "四星", 5 to "五星")
                
                stars.forEach { star ->
                    val selected = uiState.selectedStars.contains(star)
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleStar(star) },
                        label = { Text(labels[star] ?: "") },
                        leadingIcon = if (selected) {
                            @Composable { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("清空", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { onApply(priceRange) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("完成")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
