package com.example.firsttry.ui.component.hoteldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firsttry.model.RoomType
import com.example.firsttry.ui.component.GlideImage

@Composable
fun RoomListItem(room: RoomType, onBookClick: (RoomType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Left: Image
        GlideImage(
            imageUrl = room.imageUrl,
            modifier = Modifier
                .size(80.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            cornerRadius = 16 // approx 8dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Middle: Info
        Column(modifier = Modifier.weight(1f)) {
            Text(room.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${room.bedType} | ${room.size}", fontSize = 12.sp, color = Color.Gray)
            Text(if(room.hasBreakfast) "含早" else "无早", fontSize = 12.sp, color = Color.Gray)
            Text(room.cancelPolicy, fontSize = 12.sp, color = if(room.cancelPolicy == "免费取消") Color(0xFF4CAF50) else Color.Gray)
        }
        
        // Right: Price & Button
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(80.dp)
        ) {
            Text(
                text = "¥${room.price}",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Button(
                onClick = { onBookClick(room) },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("预定", fontSize = 12.sp)
            }
        }
    }
}
