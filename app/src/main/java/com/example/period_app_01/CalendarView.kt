package com.example.period_app_01

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
 * 支持左右滑动切换月份
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(
    periodRecordDao: PeriodRecordDao,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // 使用 HorizontalPager，初始显示当前月份
    // 设置一个较大的页面数量，允许向前向后滑动多个月
    val initialPage = 1200 // 从中间位置开始
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 2400 } // 总共支持2400个月的范围
    )
    
    // 根据当前页面计算当前月份
    val currentMonth = remember(pagerState.currentPage) {
        YearMonth.now().plusMonths((pagerState.currentPage - initialPage).toLong())
    }
    
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
        // 月份显示（不再需要导航按钮）
        Text(
            text = "${currentMonth.year}年${currentMonth.monthValue}月",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 星期标题
        WeekdayHeader()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 使用 HorizontalPager 实现滑动切换
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val pageMonth = YearMonth.now().plusMonths((page - initialPage).toLong())
            
            // 获取该页面月份的记录
            val pageStartOfMonth = pageMonth.atDay(1)
            val pageEndOfMonth = pageMonth.atEndOfMonth()
            val pageRecords by periodRecordDao.getRecordsByMonth(pageStartOfMonth, pageEndOfMonth)
                .collectAsState(initial = emptyList())
            
            val pageRecordMap = remember(pageRecords) {
                pageRecords.associateBy { it.date }
            }
            
            // 日历网格
            CalendarGrid(
                currentMonth = pageMonth,
                recordMap = pageRecordMap,
                allCycleAnalyses = allCycleAnalyses,
                latestCycleAnalysis = latestCycleAnalysis,
                pendingStartDate = pendingStartDate,
                onDateClick = { date ->
                    coroutineScope.launch {
                        handleDateClick(
                            date = date,
                            recordMap = pageRecordMap,
                            pendingStartDate = pendingStartDate,
                            onPendingStartDateChange = { pendingStartDate = it },
                            periodRecordDao = periodRecordDao
                        )
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 图例说明
        CalendarLegend()
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 回到今天按钮
        Button(
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = initialPage,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 800,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF8BBD0)
            )
        ) {
            Text(
                text = "回到今天",
                color = Color(0xFFC2185B)
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
        val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
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
    // 我们需要: MONDAY=0, TUESDAY=1, ..., SUNDAY=6
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value - 1
    
    // 创建日期列表（包含前置空白）
    val dates = buildList {
        // 添加空白天数
        repeat(firstDayOfWeek) { add(null) }
        // 添加实际日期
        for (day in 1..daysInMonth) {
            add(currentMonth.atDay(day))
        }
    }
    
    // 使用网格布局，固定高度为6行
    Column(
        modifier = Modifier.height(280.dp)
    ) {
        // 确保总是显示6行，不足的用空白填充
        val totalCells = 42 // 6行 x 7列
        val paddedDates = dates + List(totalCells - dates.size) { null }
        
        paddedDates.chunked(7).forEachIndexed { weekIndex, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEachIndexed { dayIndex, date ->
                    val prevDate = date?.minusDays(1)
                    val nextDate = date?.plusDays(1)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (date != null) {
                            CalendarDay(
                                date = date,
                                prevDate = prevDate!!,
                                nextDate = nextDate!!,
                                record = recordMap[date],
                                prevRecord = recordMap[prevDate],
                                nextRecord = recordMap[nextDate],
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
    prevDate: LocalDate,
    nextDate: LocalDate,
    record: PeriodRecord?,
    prevRecord: PeriodRecord?,
    nextRecord: PeriodRecord?,
    allCycleAnalyses: List<CycleAnalysis>,
    latestCycleAnalysis: CycleAnalysis?,
    isPending: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    
    // 检查前后日期是否是同一经期
    val hasPrevSamePeriod = record != null && prevRecord != null && 
                            record.periodId == prevRecord.periodId &&
                            record.recordType in 1..3 && prevRecord.recordType in 1..3
    
    val hasNextSamePeriod = record != null && nextRecord != null && 
                            record.periodId == nextRecord.periodId &&
                            record.recordType in 1..3 && nextRecord.recordType in 1..3
    
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
    
    // 检查前一天和后一天是否在相同阶段
    fun isSamePhase(checkDate: LocalDate): Boolean {
        var prevInFollicular = false
        var prevInOvulation = false
        var prevInLuteal = false
        var prevInPredicted = false
        
        allCycleAnalyses.forEach { analysis ->
            if (!checkDate.isBefore(analysis.follicularPhaseStart) && !checkDate.isAfter(analysis.follicularPhaseEnd)) {
                prevInFollicular = true
            }
            if (!checkDate.isBefore(analysis.ovulationStart) && !checkDate.isAfter(analysis.ovulationEnd)) {
                prevInOvulation = true
            }
            if (!checkDate.isBefore(analysis.lutealPhaseStart) && !checkDate.isAfter(analysis.lutealPhaseEnd)) {
                prevInLuteal = true
            }
            if (!checkDate.isBefore(analysis.prevFollicularPhaseStart) && !checkDate.isAfter(analysis.prevFollicularPhaseEnd)) {
                prevInFollicular = true
            }
            if (!checkDate.isBefore(analysis.prevOvulationStart) && !checkDate.isAfter(analysis.prevOvulationEnd)) {
                prevInOvulation = true
            }
            if (!checkDate.isBefore(analysis.prevLutealPhaseStart) && !checkDate.isAfter(analysis.prevLutealPhaseEnd)) {
                prevInLuteal = true
            }
        }
        
        latestCycleAnalysis?.let {
            if (!checkDate.isBefore(it.nextPeriodDate) && !checkDate.isAfter(it.nextPeriodEnd)) {
                prevInPredicted = true
            }
            if (!checkDate.isBefore(it.nextFollicularPhaseStart) && !checkDate.isAfter(it.nextFollicularPhaseEnd)) {
                prevInFollicular = true
            }
            if (!checkDate.isBefore(it.nextOvulationStart) && !checkDate.isAfter(it.nextOvulationEnd)) {
                prevInOvulation = true
            }
            if (!checkDate.isBefore(it.nextLutealPhaseStart) && !checkDate.isAfter(it.nextLutealPhaseEnd)) {
                prevInLuteal = true
            }
        }
        
        // 检查是否在相同的阶段
        return (isInFollicularPhase && prevInFollicular) ||
               (isInOvulationPhase && prevInOvulation) ||
               (isInLutealPhase && prevInLuteal) ||
               (isInPredictedPeriod && prevInPredicted)
    }
    
    val hasPrevSamePhase = isSamePhase(prevDate)
    val hasNextSamePhase = isSamePhase(nextDate)
    
    // 判断是否需要连续显示（经期记录或周期阶段）
    val hasPrevContinuous = hasPrevSamePeriod || hasPrevSamePhase
    val hasNextContinuous = hasNextSamePeriod || hasNextSamePhase
    
    // 根据连续状态确定是否需要间距
    val horizontalPadding = when {
        !hasPrevContinuous && !hasNextContinuous -> 2.dp // 单独的日期，四周都有间距
        !hasPrevContinuous && hasNextContinuous -> 2.dp // 开始日期，左侧有间距
        hasPrevContinuous && !hasNextContinuous -> 2.dp // 结束日期，右侧有间距
        else -> 0.dp // 中间日期，左右无间距
    }
    
    val verticalPadding = 2.dp // 上下始终保持间距
    
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
        else -> Color.Black
    }
    
    // 根据连续状态确定圆角
    val cornerRadius = 20.dp
    val shape = when {
        !hasPrevContinuous && !hasNextContinuous -> RoundedCornerShape(cornerRadius) // 单独的日期，四角都圆
        !hasPrevContinuous && hasNextContinuous -> RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius) // 开始日期，左侧圆角
        hasPrevContinuous && !hasNextContinuous -> RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius) // 结束日期，右侧圆角
        else -> RoundedCornerShape(0.dp) // 中间日期，无圆角
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = if (hasPrevContinuous) 0.dp else horizontalPadding,
                end = if (hasNextContinuous) 0.dp else horizontalPadding,
                top = verticalPadding,
                bottom = verticalPadding
            )
            .clip(shape)
            .background(backgroundColor)
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
            
            Spacer(modifier = Modifier.height(6.dp))
            
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
