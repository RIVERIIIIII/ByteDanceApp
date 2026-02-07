package com.example.firsttry.ui.component.hoteldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StickyDateBar(
    startDate: Long,
    endDate: Long,
    onDateClick: () -> Unit
) {
    val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())
    val startStr = formatter.format(Date(startDate))
    val endStr = formatter.format(Date(endDate))
    val nights = ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onDateClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text("入住", fontSize = 10.sp, color = Color.Gray)
            Text(startStr, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Text("$nights 晚", fontSize = 12.sp, modifier = Modifier.background(Color(0xFFEEEEEE)).padding(4.dp))
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text("离店", fontSize = 10.sp, color = Color.Gray)
            Text(endStr, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
}
