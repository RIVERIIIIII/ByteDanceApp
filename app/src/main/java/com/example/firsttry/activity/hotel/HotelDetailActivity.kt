package com.example.firsttry.activity.hotel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.firsttry.ui.component.DateRangePickerDialog
import com.example.firsttry.ui.component.hoteldetail.HotelBanner
import com.example.firsttry.ui.component.hoteldetail.HotelInfoSection
import com.example.firsttry.ui.component.hoteldetail.RoomListItem
import com.example.firsttry.ui.component.hoteldetail.StickyDateBar
import com.example.firsttry.ui.theme.FirstTryTheme
import com.example.firsttry.viewmodel.HotelDetailViewModel

class HotelDetailActivity : ComponentActivity() {
    private val viewModel: HotelDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val hotelId = intent.getStringExtra("hotelId") ?: ""
        
        setContent {
            FirstTryTheme {
                HotelDetailScreen(
                    viewModel = viewModel,
                    hotelId = hotelId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HotelDetailScreen(
    viewModel: HotelDetailViewModel,
    hotelId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(hotelId) {
        if (hotelId.isNotEmpty()) {
            viewModel.loadHotelDetail(hotelId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.hotel?.name ?: "酒店详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.hotel != null) {
                val hotel = uiState.hotel!!
                
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
                    // 1. Banner
                    item {
                        HotelBanner(
                            imageUrls = hotel.imageUrls,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                    }
                    
                    // 2. Info
                    item {
                        Box(modifier = Modifier.background(Color.White)) {
                            HotelInfoSection(hotel = hotel)
                        }
                    }
                    
                    // 3. Sticky Date Bar
                    stickyHeader {
                        StickyDateBar(
                            startDate = uiState.startDate,
                            endDate = uiState.endDate,
                            onDateClick = { showDatePicker = true }
                        )
                    }
                    
                    // 4. Room List
                    items(hotel.rooms) { room ->
                        RoomListItem(
                            room = room,
                            onBookClick = { 
                                Toast.makeText(context, "预定: ${room.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        // Divider
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                    }
                }
            } else {
                 Text("无法加载酒店信息", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
    
    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateRangeSelected = { start, end ->
                if (start != null && end != null) {
                    viewModel.updateDateRange(start, end)
                }
                showDatePicker = false
            }
        )
    }
}
