package com.example.firsttry.activity.hotel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.firsttry.model.Hotel
import com.example.firsttry.model.HotelSearchQuery
import com.example.firsttry.model.HotelSortMode
import com.example.firsttry.ui.component.CityPickerDialog
import com.example.firsttry.ui.component.GlideImage
import com.example.firsttry.ui.theme.FirstTryTheme
import com.example.firsttry.utils.LocationHelper
import com.example.firsttry.viewmodel.HotelListViewModel
import java.text.SimpleDateFormat
import java.util.*

class HotelListActivity : ComponentActivity() {
    private val viewModel: HotelListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val query = intent.getSerializableExtra("query") as? HotelSearchQuery
        if (query != null) {
            viewModel.initQuery(query)
        }

        setContent {
            FirstTryTheme {
                HotelListScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(
    viewModel: HotelListViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // UI States for overlays
    var showCityPicker by remember { mutableStateOf(false) }
    var showDateDrawer by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // BackHandler
    BackHandler(enabled = showCityPicker || showDateDrawer || showFilterMenu) {
        when {
            showCityPicker -> showCityPicker = false
            showDateDrawer -> showDateDrawer = false
            showFilterMenu -> showFilterMenu = false
        }
    }

    val locationHelper = remember { LocationHelper(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
             locationHelper.getCurrentLocation(
                 onSuccess = { loc ->
                     viewModel.updateLocation(loc.latitude, loc.longitude)
                     viewModel.updateSortMode(HotelSortMode.DISTANCE)
                 },
                 onFailure = {
                     Toast.makeText(context, "无法获取位置", Toast.LENGTH_SHORT).show()
                 }
             )
        } else {
            Toast.makeText(context, "需要位置权限才能按距离排序", Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
             Column {
                 HotelListHeader(
                     query = uiState.query,
                     onCityClick = { 
                         showCityPicker = true
                         showDateDrawer = false
                         showFilterMenu = false
                     },
                     onDateClick = { 
                         showDateDrawer = !showDateDrawer
                         showCityPicker = false
                         showFilterMenu = false
                     },
                     onSearchClick = { /* Refresh or Search */ viewModel.searchHotels() }
                 )
                 // Date Drawer Overlay attached to Header
                 AnimatedVisibility(
                    visible = showDateDrawer,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        shadowElevation = 8.dp
                    ) {
                         DateRangeDrawerContent(
                             initialStartDate = uiState.query.startDate,
                             initialEndDate = uiState.query.endDate,
                             onDateSelected = { start, end -> 
                                 viewModel.updateDateRange(start, end)
                                 showDateDrawer = false
                             }
                         )
                    }
                }
             }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Sub-filter Bar
                SubFilterBar(
                    currentSortMode = uiState.query.sortMode,
                    onSortChange = { mode -> 
                        if (mode == HotelSortMode.DISTANCE) {
                             if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                 locationHelper.getCurrentLocation(
                                     onSuccess = { loc ->
                                         viewModel.updateLocation(loc.latitude, loc.longitude)
                                         viewModel.updateSortMode(mode)
                                     },
                                     onFailure = {
                                         Toast.makeText(context, "无法获取位置", Toast.LENGTH_SHORT).show()
                                     }
                                 )
                             } else {
                                 locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                             }
                        } else {
                            viewModel.updateSortMode(mode)
                        }
                    },
                    onFilterClick = { 
                        showFilterMenu = !showFilterMenu
                        showDateDrawer = false
                        showCityPicker = false
                    }
                )
                
                // Filter Menu Overlay (Below Sub-filter)
                AnimatedVisibility(
                    visible = showFilterMenu,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        FilterMenuContent(
                            currentTags = uiState.query.tags,
                            onApply = { tags -> 
                                viewModel.updateTags(tags)
                                showFilterMenu = false
                            },
                            onReset = { 
                                viewModel.updateTags(emptyList())
                                // showFilterMenu = false // Optional: close or keep open
                            }
                        )
                    }
                }

                // List
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.hotels) { hotel ->
                            HotelListItem(hotel)
                        }
                    }
                }
            }
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
                 // Reuse location logic or implement new one
                 // For now just close
                 showCityPicker = false
            }
        )
    }
}

@Composable
fun HotelListHeader(
    query: HotelSearchQuery,
    onCityClick: () -> Unit,
    onDateClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())
    val startStr = query.startDate?.let { formatter.format(Date(it)) } ?: "入住"
    val endStr = query.endDate?.let { formatter.format(Date(it)) } ?: "离店"
    val nights = if (query.startDate != null && query.endDate != null) {
        ((query.endDate - query.startDate) / (1000 * 60 * 60 * 24)).toInt()
    } else 0

    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City
            Row(
                modifier = Modifier.clickable(onClick = onCityClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(query.city.ifEmpty { "城市" }, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            VerticalDivider(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.width(16.dp))

            // Date
            Row(
                modifier = Modifier.weight(1f).clickable(onClick = onDateClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$startStr - $endStr", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                if (nights > 0) {
                    Text("($nights 晚)", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            // Search Icon
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SubFilterBar(
    currentSortMode: HotelSortMode,
    onSortChange: (HotelSortMode) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort Options
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = currentSortMode == HotelSortMode.DISTANCE,
                onClick = { onSortChange(HotelSortMode.DISTANCE) },
                label = { Text("直线距离") },
                leadingIcon = { if (currentSortMode == HotelSortMode.DISTANCE) Icon(Icons.Default.Check, null) }
            )
            FilterChip(
                selected = currentSortMode == HotelSortMode.PRICE_LOW_TO_HIGH,
                onClick = { onSortChange(HotelSortMode.PRICE_LOW_TO_HIGH) },
                label = { Text("低价优先") },
                leadingIcon = { if (currentSortMode == HotelSortMode.PRICE_LOW_TO_HIGH) Icon(Icons.Default.Check, null) }
            )
        }
        
        // Filter Button
        TextButton(onClick = onFilterClick) {
            Text("筛选")
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

@Composable
fun HotelListItem(hotel: Hotel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Left: Image
            GlideImage(
                imageUrl = hotel.imageUrl,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                cornerRadius = 16 // approx 8dp in pixels (simplified)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Middle: Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hotel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Tags
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    hotel.tags.forEach { tag ->
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Right: Price & Distance
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(100.dp)
            ) {
                Text(
                    text = "¥${hotel.price}起",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = String.format("%.1fkm", hotel.distance),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDrawerContent(
    initialStartDate: Long?,
    initialEndDate: Long?,
    onDateSelected: (Long?, Long?) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate,
        initialSelectedEndDateMillis = initialEndDate
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        DateRangePicker(
            state = state,
            modifier = Modifier.weight(1f),
            title = null,
            headline = null,
            showModeToggle = false
        )
        Button(
            onClick = { onDateSelected(state.selectedStartDateMillis, state.selectedEndDateMillis) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("确定")
        }
    }
}

@Composable
fun FilterMenuContent(
    currentTags: List<String>,
    onApply: (List<String>) -> Unit,
    onReset: () -> Unit
) {
    val availableTags = listOf("浴缸", "大床房", "近地铁", "含早", "免费取消", "健身房", "游泳池")
    var selectedTags by remember(currentTags) { mutableStateOf(currentTags.toMutableList()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("设施服务", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableTags.forEach { tag ->
                val selected = selectedTags.contains(tag)
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) selectedTags.remove(tag) else selectedTags.add(tag)
                        // Force recompose hack if needed, but SnapshotStateList handles it
                        selectedTags = selectedTags.toMutableList() 
                    },
                    label = { Text(tag) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.Check, null) }
                    } else null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    onReset()
                    selectedTags.clear()
                    selectedTags = mutableListOf()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("重置", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onApply(selectedTags) },
                modifier = Modifier.weight(1f)
            ) {
                Text("查看")
            }
        }
    }
}

// Helper for FlowRow if not available in older Compose (ExperimentalLayoutApi)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
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
