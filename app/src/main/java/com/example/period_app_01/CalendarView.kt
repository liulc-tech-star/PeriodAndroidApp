package com.example.period_app_01

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.period_app_01.data.PeriodRecord
import com.example.period_app_01.data.PeriodRecordDao
import com.example.period_app_01.data.CycleCalculator
import com.example.period_app_01.data.CycleAnalysis
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * 日历视图组件
 * 支持点击日期标记经期开始和结束
 */
@Composable
fun CalendarView(
    periodRecordDao: PeriodRecordDao,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val coroutineScope = rememberCoroutineScope()
    
    // 获取当前月份的所有记录
    val startOfMonth = currentMonth.atDay(1)
    val endOfMonth = currentMonth.atEndOfMonth()
    val records by periodRecordDao.getRecordsByMonth(startOfMonth, endOfMonth)
        .collectAsState(initial = emptyList())
    
    // 获取所有经期开始记录（用于预测）
    val allPeriodStarts by periodRecordDao.getAllPeriodStarts()
        .collectAsState(initial = emptyList())
    
    // 获取所有记录（用于检查最近经期是否有结束日期）
    val allRecords by periodRecordDao.getAllRecords()
        .collectAsState(initial = emptyList())
    
    // 计算所有周期的分析信息
    val allCycleAnalyses = remember(allPeriodStarts, allRecords) {
        if (allPeriodStarts.size >= 2) {
            val analyses = mutableListOf<CycleAnalysis>()
            
            // allPeriodStarts是降序排列（最新的在前）
            // 需要从后往前遍历，确保firstStart < secondStart
            for (i in allPeriodStarts.size - 1 downTo 1) {
                val firstStart = allPeriodStarts[i].date  // 更早的周期
                val secondStart = allPeriodStarts[i - 1].date  // 更晚的周期
                val secondPeriodId = allPeriodStarts[i - 1].periodId
                
                // 检查第二个周期是否有结束记录
                val endRecord = allRecords.firstOrNull { 
                    it.periodId == secondPeriodId && it.recordType == 3 
                }
                
                if (endRecord != null) {
                    val analysis = CycleCalculator.calculateCycle(
                        firstStart, 
                        secondStart, 
                        endRecord.date
                    )
                    if (analysis != null) {
                        analyses.add(analysis)
                    }
                }
            }
            
            analyses
        } else {
            emptyList()
        }
    }
    
    // 使用最新的完整周期分析（用于预测下一个周期）
    // 如果最新周期还没有结束记录，基于最新两个周期开始创建临时预测
    val latestCycleAnalysis = remember(allCycleAnalyses, allPeriodStarts, allRecords) {
        if (allPeriodStarts.size >= 2) {
            val latestPeriodId = allPeriodStarts[0].periodId
            val hasLatestEndRecord = allRecords.any { 
                it.periodId == latestPeriodId && it.recordType == 3 
            }
            
            if (hasLatestEndRecord && allCycleAnalyses.isNotEmpty()) {
                // 最新周期已完成，使用最新的完整分析
                allCycleAnalyses.lastOrNull()
            } else if (allPeriodStarts.size >= 2) {
                // 最新周期未完成，基于最新两个周期开始创建临时分析用于预测
                // allPeriodStarts[0] 是最新的，allPeriodStarts[1] 是倒数第二个
                val firstStart = allPeriodStarts[1].date  // 更早的周期
                val secondStart = allPeriodStarts[0].date  // 更晚的周期（最新）
                
                // 使用倒数第二个周期的结束日期（如果有）
                val prevPeriodId = allPeriodStarts[1].periodId
                val prevEndRecord = allRecords.firstOrNull { 
                    it.periodId == prevPeriodId && it.recordType == 3 
                }
                
                // 创建临时分析用于预测
                CycleCalculator.calculateCycle(firstStart, secondStart, prevEndRecord?.date)
            } else {
                null
            }
        } else {
            allCycleAnalyses.lastOrNull()
        }
    }
    
    // 创建日期到记录的映射
    val recordMap = remember(records) {
        records.associateBy { it.date }
    }
    
    // 待标记的开始日期（用于标记经期开始后等待标记结束）
    var pendingStartDate by remember { mutableStateOf<LocalDate?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 月份导航
        MonthNavigation(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 星期标题
        WeekdayHeader()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日历网格
        CalendarGrid(
            currentMonth = currentMonth,
            recordMap = recordMap,
            allCycleAnalyses = allCycleAnalyses,
            latestCycleAnalysis = latestCycleAnalysis,
            pendingStartDate = pendingStartDate,
            onDateClick = { date ->
                coroutineScope.launch {
                    handleDateClick(
                        date = date,
                        recordMap = recordMap,
                        pendingStartDate = pendingStartDate,
                        onPendingStartDateChange = { pendingStartDate = it },
                        periodRecordDao = periodRecordDao
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 图例说明
        CalendarLegend()
    }
}

/**
 * 月份导航栏
 */
@Composable
fun MonthNavigation(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "上个月",
                tint = Color(0xFFE91E63)
            )
        }
        
        Text(
            text = "${currentMonth.year}年${currentMonth.monthValue}月",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "下个月",
                tint = Color(0xFFE91E63)
            )
        }
    }
}

/**
 * 星期标题行
 */
@Composable
fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
        weekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

/**
 * 日历网格
 */
@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    recordMap: Map<LocalDate, PeriodRecord>,
    allCycleAnalyses: List<CycleAnalysis>,
    latestCycleAnalysis: CycleAnalysis?,
    pendingStartDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    // Java的DayOfWeek: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
    // 我们需要: SUNDAY=0, MONDAY=1, ..., SATURDAY=6
    val firstDayOfWeek = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
    
    // 创建日期列表（包含前置空白）
    val dates = buildList {
        // 添加空白天数
        repeat(firstDayOfWeek) { add(null) }
        // 添加实际日期
        for (day in 1..daysInMonth) {
            add(currentMonth.atDay(day))
        }
    }
    
    // 使用网格布局
    Column {
        dates.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 确保每行都有7个元素，不足的用空白填充
                val fullWeek = week + List(7 - week.size) { null }
                fullWeek.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                    ) {
                        if (date != null) {
                            CalendarDay(
                                date = date,
                                record = recordMap[date],
                                allCycleAnalyses = allCycleAnalyses,
                                latestCycleAnalysis = latestCycleAnalysis,
                                isPending = date == pendingStartDate,
                                onClick = { onDateClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个日期单元格
 */
@Composable
fun CalendarDay(
    date: LocalDate,
    record: PeriodRecord?,
    allCycleAnalyses: List<CycleAnalysis>,
    latestCycleAnalysis: CycleAnalysis?,
    isPending: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    
    // 遍历所有周期，检查日期是否属于任何周期的阶段
    var isInFollicularPhase = false
    var isInOvulationPhase = false
    var isInLutealPhase = false
    var isOvulationDay = false
    
    // 检查所有已记录的周期
    allCycleAnalyses.forEach { analysis ->
        // 当前周期
        if (!date.isBefore(analysis.follicularPhaseStart) && !date.isAfter(analysis.follicularPhaseEnd)) {
            isInFollicularPhase = true
        }
        if (!date.isBefore(analysis.ovulationStart) && !date.isAfter(analysis.ovulationEnd)) {
            isInOvulationPhase = true
        }
        if (!date.isBefore(analysis.lutealPhaseStart) && !date.isAfter(analysis.lutealPhaseEnd)) {
            isInLutealPhase = true
        }
        if (date == analysis.ovulationDay) {
            isOvulationDay = true
        }
        
        // 上一个周期
        if (!date.isBefore(analysis.prevFollicularPhaseStart) && !date.isAfter(analysis.prevFollicularPhaseEnd)) {
            isInFollicularPhase = true
        }
        if (!date.isBefore(analysis.prevOvulationStart) && !date.isAfter(analysis.prevOvulationEnd)) {
            isInOvulationPhase = true
        }
        if (!date.isBefore(analysis.prevLutealPhaseStart) && !date.isAfter(analysis.prevLutealPhaseEnd)) {
            isInLutealPhase = true
        }
        if (date == analysis.prevOvulationDay) {
            isOvulationDay = true
        }
    }
    
    // 检查预测的下一个周期（仅使用最新的周期分析）
    val isInPredictedPeriod = latestCycleAnalysis?.let {
        !date.isBefore(it.nextPeriodDate) && !date.isAfter(it.nextPeriodEnd)
    } ?: false
    
    val isInNextFollicularPhase = latestCycleAnalysis?.let {
        !date.isBefore(it.nextFollicularPhaseStart) && !date.isAfter(it.nextFollicularPhaseEnd)
    } ?: false
    
    val isInNextOvulationPhase = latestCycleAnalysis?.let {
        !date.isBefore(it.nextOvulationStart) && !date.isAfter(it.nextOvulationEnd)
    } ?: false
    
    val isInNextLutealPhase = latestCycleAnalysis?.let {
        !date.isBefore(it.nextLutealPhaseStart) && !date.isAfter(it.nextLutealPhaseEnd)
    } ?: false
    
    val isNextOvulationDay = latestCycleAnalysis?.let {
        date == it.nextOvulationDay
    } ?: false
    
    if (isInNextFollicularPhase) isInFollicularPhase = true
    if (isInNextOvulationPhase) isInOvulationPhase = true
    if (isInNextLutealPhase) isInLutealPhase = true
    if (isNextOvulationDay) isOvulationDay = true
    
    // 根据记录类型和周期阶段确定背景色
    val backgroundColor = when {
        isPending -> Color(0xFFFFF59D) // 待完成的开始日期（黄色）
        record != null && record.recordType == 1 -> Color(0xFFD32F2F) // 经期开始（红色）
        record != null && record.recordType == 2 -> Color(0xFFEF5350) // 经期中（红色）
        record != null && record.recordType == 3 -> Color(0xFFE57373) // 经期结束（浅红色）
        isInPredictedPeriod -> Color(0xFFF8BBD0) // 预测经期（浅粉红色）
        isInFollicularPhase -> Color(0xFFE1BEE7) // 卵泡期（浅紫色）
        isInOvulationPhase -> Color(0xFFFFE082) // 排卵期（浅黄色）
        isInLutealPhase -> Color(0xFFC5E1A5) // 黄体期（浅绿色）
        else -> Color.Transparent
    }
    
    val textColor = when {
        record != null && record.recordType in 1..3 -> Color.White
        isInPredictedPeriod -> Color(0xFFC2185B) // 预测日期的文字颜色（深粉红色）
        isInFollicularPhase || isInOvulationPhase || isInLutealPhase -> Color(0xFF424242) // 周期阶段的文字颜色（深灰色）
        isToday -> Color(0xFFE91E63)
        else -> Color.Black
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && backgroundColor == Color.Transparent) {
                    Modifier.border(2.dp, Color(0xFFE91E63), CircleShape)
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 如果是排卵日，只显示👶图标，不显示日期数字
        if (isOvulationDay) {
            Text(
                text = "👶",
                fontSize = 20.sp
            )
        } else {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

/**
 * 图例说明
 */
@Composable
fun CalendarLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            /*
            Spacer(modifier = Modifier.height(12.dp))
            
            // 经期记录部分
            Text(
                text = "经期记录",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE91E63)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(
                    color = Color(0xFFD32F2F),
                    text = "经期开始",
                    modifier = Modifier.weight(1f)
                )
                
                LegendItem(
                    color = Color(0xFFEF5350),
                    text = "经期中",
                    modifier = Modifier.weight(1f)
                )
                
                LegendItem(
                    color = Color(0xFFE57373),
                    text = "经期结束",
                    modifier = Modifier.weight(1f)
                )
            }
            */
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 周期阶段部分
            Text(
                text = "周期阶段",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                LegendItem(
                    color = Color(0xFFE1BEE7),
                    text = "卵泡期",
                    modifier = Modifier.weight(1f)
                )
                
                LegendItem(
                    color = Color(0xFFFFE082),
                    text = "排卵期",
                    modifier = Modifier.weight(1f)
                )
                
                /*
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "👶",
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "排卵日",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                } */
                
                Spacer(modifier = Modifier.weight(1f))

            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                LegendItem(
                    color = Color(0xFFC5E1A5),
                    text = "黄体期",
                    modifier = Modifier.weight(1f)
                )

                LegendItem(
                    color = Color(0xFFF8BBD0),
                    text = "预测经期",
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• 第三次点击可清除标记",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )

            Text(
                text = "• 周期阶段基于最近两次经期计算",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * 图例项
 */
@Composable
fun LegendItem(color: Color, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.DarkGray
        )
    }
}

/**
 * 处理日期点击逻辑
 */
suspend fun handleDateClick(
    date: LocalDate,
    recordMap: Map<LocalDate, PeriodRecord>,
    pendingStartDate: LocalDate?,
    onPendingStartDateChange: (LocalDate?) -> Unit,
    periodRecordDao: PeriodRecordDao
) {
    val existingRecord = recordMap[date]
    
    when {
        // 情况1：点击已有记录 - 删除整个经期
        existingRecord != null -> {
            periodRecordDao.deleteByPeriodId(existingRecord.periodId)
            if (pendingStartDate == date) {
                onPendingStartDateChange(null)
            }
        }
        
        // 情况2：已有待处理的开始日期，这次点击标记结束
        pendingStartDate != null -> {
            val startDate = pendingStartDate
            val endDate = date
            
            // 确保结束日期不早于开始日期
            if (!endDate.isBefore(startDate)) {
                val periodId = periodRecordDao.getMaxPeriodId() + 1
                val recordsToInsert = mutableListOf<PeriodRecord>()
                
                // 标记开始日期
                recordsToInsert.add(
                    PeriodRecord(
                        date = startDate,
                        recordType = 1,
                        periodId = periodId
                    )
                )
                
                // 标记中间日期
                var currentDate = startDate.plusDays(1)
                while (currentDate.isBefore(endDate)) {
                    recordsToInsert.add(
                        PeriodRecord(
                            date = currentDate,
                            recordType = 2,
                            periodId = periodId
                        )
                    )
                    currentDate = currentDate.plusDays(1)
                }
                
                // 标记结束日期（如果开始和结束不是同一天）
                if (!startDate.isEqual(endDate)) {
                    recordsToInsert.add(
                        PeriodRecord(
                            date = endDate,
                            recordType = 3,
                            periodId = periodId
                        )
                    )
                }
                
                periodRecordDao.insertRecords(recordsToInsert)
            }
            
            onPendingStartDateChange(null)
        }
        
        // 情况3：首次点击 - 标记为经期开始
        else -> {
            onPendingStartDateChange(date)
            val periodId = periodRecordDao.getMaxPeriodId() + 1
            periodRecordDao.insertRecord(
                PeriodRecord(
                    date = date,
                    recordType = 1,
                    periodId = periodId
                )
            )
        }
    }
}
