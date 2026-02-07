package com.example.firsttry.activity.hotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.firsttry.model.HotelSearchQuery
import com.example.firsttry.ui.theme.FirstTryTheme

class HotelListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val query = intent.getSerializableExtra("query") as? HotelSearchQuery
        
        setContent {
            FirstTryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Search Results:\n" +
                                "City: ${query?.city}\n" +
                                "Date: ${query?.startDate} - ${query?.endDate}\n" +
                                "Price: ${query?.priceRangeStart} - ${query?.priceRangeEnd}\n" +
                                "Stars: ${query?.starRatings}\n" +
                                "Keyword: ${query?.keyword}\n" +
                                "Tags: ${query?.tags}")
                    }
                }
            }
        }
    }
}
