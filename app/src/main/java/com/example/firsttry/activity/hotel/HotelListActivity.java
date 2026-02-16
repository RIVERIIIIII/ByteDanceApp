package com.example.firsttry.activity.hotel;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firsttry.activity.hotel.model.HotelSearchQuery;

import java.util.List;

public class HotelListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 使用 TextView 显示调试信息
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        textView.setGravity(Gravity.START);
        
        // 接收 Intent 参数
        HotelSearchQuery query = (HotelSearchQuery) getIntent().getSerializableExtra("search_query");
        
        if (query != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("===== 酒店搜索参数 =====\n\n");
            sb.append("城市: ").append(query.getCity()).append("\n");
            sb.append("关键词: ").append(query.getKeyword() != null ? query.getKeyword() : "无").append("\n");
            sb.append("入住日期: ").append(query.getCheckInDate()).append("\n");
            sb.append("离店日期: ").append(query.getCheckOutDate()).append("\n");
            sb.append("价格区间: ¥").append(query.getMinPrice()).append(" - ¥").append(query.getMaxPrice()).append("\n");
            sb.append("星级要求: ").append(query.getStarRating() == 0 ? "不限" : query.getStarRating() + "星").append("\n");
            
            sb.append("标签: ");
            List<String> tags = query.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    sb.append(tag).append("  ");
                }
            } else {
                sb.append("无");
            }
            
            textView.setText(sb.toString());
        } else {
            textView.setText("未接收到搜索参数");
        }
        
        setContentView(textView);
    }
}
