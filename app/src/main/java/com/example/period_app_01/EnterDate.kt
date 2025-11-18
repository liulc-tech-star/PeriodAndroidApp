package com.example.period_app_01

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import com.example.period_app_01.data.Dates
import com.example.period_app_01.data.DatesDao
import com.example.period_app_01.data.CycleCalculator
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterDate(messageDate: String, modifier: Modifier = Modifier, datesDao: DatesDao) {
    val textDate = remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAddRecordDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    val lastEntry by datesDao.getLastEntry().collectAsState(initial = null)
    val lastIdEntry by datesDao.getLastId().collectAsState(initial = 0)
    val lastTwoEntries by datesDao.getLastTwoEntries().collectAsState(initial = emptyList())
    
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // 计算周期分析
    val cycleAnalysis = remember(lastTwoEntries) {
        if (lastTwoEntries.size >= 2) {
            val first = lastTwoEntries[1].date
            val second = lastTwoEntries[0].date
            val secondEnd = lastTwoEntries[0].endDate
            if (first != null && second != null) {
                CycleCalculator.calculateCycle(first, second, secondEnd)
            } else null
        } else null
    }

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
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "月期知纪",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "记录您的月经周期",
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 主操作按钮卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "记录管理",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE91E63)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 添加记录按钮
                Button(
                    onClick = { 
                        showAddRecordDialog = true
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "添加记录",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "添加记录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 删除记录按钮
                if (lastEntry != null) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                datesDao.deleteLast()
                                errorMessage = "已删除最近一条记录"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text(
                            text = "删除最近一条记录",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        color = if (errorMessage.contains("错误")) Color(0xFFD32F2F) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // 添加记录对话框
        if (showAddRecordDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { 
                    showAddRecordDialog = false
                    selectedDate = null
                    selectedEndDate = null
                }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "添加月经记录",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE91E63)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 月经开始日期选择按钮
                        Text(
                            text = "月经开始日期",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFFFF5F8)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择开始日期",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = selectedDate?.toString() ?: "点击选择开始日期",
                                fontSize = 16.sp,
                                color = if (selectedDate != null) Color.Black else Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 月经结束日期选择按钮
                        Text(
                            text = "月经结束日期（可选）",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFFFF5F8)
                            ),
                            enabled = selectedDate != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择结束日期",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = selectedEndDate?.toString() ?: "点击选择结束日期",
                                fontSize = 16.sp,
                                color = if (selectedEndDate != null) Color.Black else Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 对话框操作按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAddRecordDialog = false
                                    selectedDate = null
                                    selectedEndDate = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("取消")
                            }
                            
                            Button(
                                onClick = {
                                    selectedDate?.let { date ->
                                        val newDate = Dates(
                                            id = 0,
                                            date = date,
                                            endDate = selectedEndDate,
                                            period = 0
                                        )
                                        coroutineScope.launch {
                                            datesDao.insert(newDate)
                                            errorMessage = "日期已保存：$date" + 
                                                (selectedEndDate?.let { " 至 $it" } ?: "")
                                            showAddRecordDialog = false
                                            selectedDate = null
                                            selectedEndDate = null
                                        }
                                    } ?: run {
                                        errorMessage = "请先选择开始日期"
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE91E63)
                                ),
                                enabled = selectedDate != null
                            ) {
                                Text(
                                    text = "确认保存",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 日期选择器对话框
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        // 经期结束日期选择器对话框
        if (showEndDatePicker) {
            val endDatePickerState = rememberDatePickerState()
            
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            endDatePickerState.selectedDateMillis?.let { millis ->
                                val endDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                // 验证结束日期不早于开始日期
                                if (selectedDate != null && !endDate.isBefore(selectedDate)) {
                                    selectedEndDate = endDate
                                } else {
                                    errorMessage = "结束日期不能早于开始日期"
                                }
                            }
                            showEndDatePicker = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 上次记录卡片
        lastEntry?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF0F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "上次记录",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            text = "上次月经日期",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${it.date}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )
                    }
                }
            }
        }
        
        // 周期分析卡片
        cycleAnalysis?.let { analysis ->
            Spacer(modifier = Modifier.height(16.dp))
            
            // 周期长度和下次月经预测
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "周期分析",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "月经周期",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${analysis.cycleLength} 天",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "下次月经预测",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${analysis.nextPeriodDate}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 经期卡片
            PhaseCard(
                title = "经期",
                description = "最后一次月经持续时间",
                startDate = analysis.periodStart,
                endDate = analysis.periodEnd,
                color = Color(0xFFE91E63)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 卵泡期
            PhaseCard(
                title = "卵泡期",
                description = "月经结束后至排卵前",
                startDate = analysis.follicularPhaseStart,
                endDate = analysis.follicularPhaseEnd,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 排卵期（易孕期）
            PhaseCard(
                title = "排卵期（易孕期）",
                description = "排卵日: ${analysis.ovulationDay}",
                startDate = analysis.ovulationStart,
                endDate = analysis.ovulationEnd,
                color = Color(0xFFFF9800),
                highlight = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 黄体期
            PhaseCard(
                title = "黄体期",
                description = "排卵后至下次月经前",
                startDate = analysis.lutealPhaseStart,
                endDate = analysis.lutealPhaseEnd,
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提示信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9C4)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "提示",
                        tint = Color(0xFFF57C00),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(20.dp)
                    )
                    Text(
                        text = "该推测基于生理规律，实际可能受情绪、作息等因素影响，仅供参考。",
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        // 如果只有一条记录，提示需要更多数据
        if (lastEntry != null && cycleAnalysis == null) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "提示",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = "请再输入一次月经日期，系统将为您分析周期规律",
                        fontSize = 15.sp,
                        color = Color(0xFF1976D2),
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PhaseCard(
    title: String,
    description: String,
    startDate: LocalDate,
    endDate: LocalDate,
    color: Color,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) color.copy(alpha = 0.15f) else color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlight) 3.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (highlight) {
                    Text(
                        text = "👶⚠️",
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$startDate 至 $endDate",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
