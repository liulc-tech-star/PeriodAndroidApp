package com.example.period_app_01

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnterDate(
    modifier: Modifier = Modifier, 
    periodRecordDao: com.example.period_app_01.data.PeriodRecordDao
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5E5),
                        Color(0xFFFFF0F5),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        
        // 应用标题
        Text(
            text = "月期知纪",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )
        
        Spacer(modifier = Modifier.height(7.dp))
        
        Text(
            text = "陪伴记录每个周期",
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 日历视图
        CalendarView(
            periodRecordDao = periodRecordDao,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
