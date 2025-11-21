package com.example.period_app_01.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 周期分析数据类
 */
data class CycleAnalysis(
    val cycleLength: Long,                    // 月经周期长度（天数）
    val nextPeriodDate: LocalDate,            // 下次月经预测日期
    
    // 当前周期（最近完成的周期）
    val periodStart: LocalDate,               // 经期开始日期
    val periodEnd: LocalDate,                 // 经期结束日期
    val follicularPhaseStart: LocalDate,      // 卵泡期开始日期
    val follicularPhaseEnd: LocalDate,        // 卵泡期结束日期
    val ovulationStart: LocalDate,            // 排卵期开始日期
    val ovulationEnd: LocalDate,              // 排卵期结束日期
    val ovulationDay: LocalDate,              // 排卵日
    val lutealPhaseStart: LocalDate,          // 黄体期开始日期
    val lutealPhaseEnd: LocalDate,            // 黄体期结束日期
    
    // 上一个周期
    val prevPeriodStart: LocalDate,           // 上周期经期开始日期
    val prevPeriodEnd: LocalDate,             // 上周期经期结束日期
    val prevFollicularPhaseStart: LocalDate,  // 上周期卵泡期开始日期
    val prevFollicularPhaseEnd: LocalDate,    // 上周期卵泡期结束日期
    val prevOvulationStart: LocalDate,        // 上周期排卵期开始日期
    val prevOvulationEnd: LocalDate,          // 上周期排卵期结束日期
    val prevOvulationDay: LocalDate,          // 上周期排卵日
    val prevLutealPhaseStart: LocalDate,      // 上周期黄体期开始日期
    val prevLutealPhaseEnd: LocalDate,        // 上周期黄体期结束日期
    
    // 下一个周期（预测）
    val nextPeriodEnd: LocalDate,             // 下周期预测经期结束日期
    val nextFollicularPhaseStart: LocalDate,  // 下周期预测卵泡期开始日期
    val nextFollicularPhaseEnd: LocalDate,    // 下周期预测卵泡期结束日期
    val nextOvulationStart: LocalDate,        // 下周期预测排卵期开始日期
    val nextOvulationEnd: LocalDate,          // 下周期预测排卵期结束日期
    val nextOvulationDay: LocalDate,          // 下周期预测排卵日
    val nextLutealPhaseStart: LocalDate,      // 下周期预测黄体期开始日期
    val nextLutealPhaseEnd: LocalDate         // 下周期预测黄体期结束日期
)

/**
 * 周期计算工具类
 */
object CycleCalculator {
    
    /**
     * 计算月经周期分析
     * @param firstPeriodDate 第一次月经开始日期
     * @param secondPeriodDate 第二次月经开始日期（也是预测中"第三次"的位置）
     * @param secondPeriodEndDate 第二次月经结束日期
     * @param isSecondPeriodActual 第二次经期是否是实际记录（而非预测）
     * @param lutealPhaseDays 黄体期天数（默认14天）
     * @return 周期分析结果，如果输入无效则返回null
     */
    fun calculateCycle(
        firstPeriodDate: LocalDate,
        secondPeriodDate: LocalDate,
        secondPeriodEndDate: LocalDate? = null,
        isSecondPeriodActual: Boolean = true,
        lutealPhaseDays: Int = 14
    ): CycleAnalysis? {
        // 验证日期有效性
        if (secondPeriodDate.isBefore(firstPeriodDate) || secondPeriodDate.isEqual(firstPeriodDate)) {
            return null
        }
        
        // 如果有经期结束日期，验证其合法性
        if (secondPeriodEndDate != null && secondPeriodEndDate.isBefore(secondPeriodDate)) {
            return null
        }
        
        // 1. 计算月经周期长度
        val cycleLength = ChronoUnit.DAYS.between(firstPeriodDate, secondPeriodDate)
        
        // 2. 计算经期持续天数
        val periodDuration = if (secondPeriodEndDate != null) {
            ChronoUnit.DAYS.between(secondPeriodDate, secondPeriodEndDate)
        } else {
            4L // 默认5天（0-4天，共5天）
        }
        
        // ========== 当前周期（第二次经期 → 第三次经期）==========
        // 3. 计算/预测第三次月经日期
        val nextPeriodDate = secondPeriodDate.plusDays(cycleLength)
        
        // 4. 第二次经期的开始和结束日期
        val periodStart = secondPeriodDate
        val periodEnd = secondPeriodEndDate ?: secondPeriodDate.plusDays(periodDuration)
        
        // 5. 当前周期的阶段计算：
        // - 如果第二次经期是实际记录（isSecondPeriodActual=true），使用实际周期方法
        // - 如果第二次经期是预测（isSecondPeriodActual=false），使用预测周期方法
        val ovulationDay: LocalDate
        val follicularPhaseStart: LocalDate
        val follicularPhaseEnd: LocalDate
        val ovulationStart: LocalDate
        val ovulationEnd: LocalDate
        val lutealPhaseStart: LocalDate
        val lutealPhaseEnd: LocalDate
        
        if (isSecondPeriodActual) {
            // 实际周期方法：排卵日从实际的第三次经期（secondPeriodDate）往回推算
            ovulationDay = secondPeriodDate.minusDays(lutealPhaseDays.toLong())
            
            // 卵泡期从第一次经期结束后开始
            follicularPhaseStart = firstPeriodDate.plusDays(periodDuration + 1)
            ovulationStart = ovulationDay.minusDays(3)
            follicularPhaseEnd = ovulationStart.minusDays(1)
            
            // 排卵期
            ovulationEnd = ovulationDay.plusDays(4)
            
            // 黄体期（排卵期结束后到第三次经期前一天）
            lutealPhaseStart = ovulationEnd.plusDays(1)
            lutealPhaseEnd = secondPeriodDate.minusDays(1)
        } else {
            // 预测周期方法：排卵日从预测的第三次经期（nextPeriodDate）往回推算
            ovulationDay = nextPeriodDate.minusDays(lutealPhaseDays.toLong())
            
            // 卵泡期从第二次经期结束后开始
            follicularPhaseStart = periodEnd.plusDays(1)
            ovulationStart = ovulationDay.minusDays(3)
            follicularPhaseEnd = ovulationStart.minusDays(1)
            
            // 排卵期（排卵日前3天到排卵日后4天，共8天易孕期）
            ovulationEnd = ovulationDay.plusDays(4)
            
            // 黄体期（排卵期结束后到预测的第三次经期前一天）
            lutealPhaseStart = ovulationEnd.plusDays(1)
            lutealPhaseEnd = nextPeriodDate.minusDays(1)
        }
        
        // ========== 上一个周期（实际周期：第一次经期 → 第二次经期）==========
        val prevPeriodStart = firstPeriodDate
        val prevPeriodEnd = firstPeriodDate.plusDays(periodDuration)
        
        // 实际周期的阶段计算：基于实际的第二次经期日期
        // 第二次经期日期是已知的，所以这是实际记录的阶段
        val prevNextPeriodDate = secondPeriodDate
        
        // 排卵日从第二次经期开始往回推算
        val prevOvulationDay = prevNextPeriodDate.minusDays(lutealPhaseDays.toLong())
        
        // 卵泡期从第一次经期结束后开始
        val prevFollicularPhaseStart = prevPeriodEnd.plusDays(1)
        val prevOvulationStart = prevOvulationDay.minusDays(3)
        val prevFollicularPhaseEnd = prevOvulationStart.minusDays(1)
        
        // 排卵期
        val prevOvulationEnd = prevOvulationDay.plusDays(4)
        
        // 黄体期（排卵期结束后到第二次经期前一天）
        val prevLutealPhaseStart = prevOvulationEnd.plusDays(1)
        val prevLutealPhaseEnd = prevNextPeriodDate.minusDays(1)
        
        // ========== 下一个周期（下个预测周期：第三次经期 → 第四次经期）==========
        val nextPeriodEnd = nextPeriodDate.plusDays(periodDuration)
        
        // 预测第四次月经日期
        val nextNextPeriodDate = nextPeriodDate.plusDays(cycleLength)
        
        // 下个预测周期的阶段计算：基于预测的第四次经期
        val nextOvulationDay = nextNextPeriodDate.minusDays(lutealPhaseDays.toLong())
        
        // 卵泡期从第三次经期结束后开始
        val nextFollicularPhaseStart = nextPeriodEnd.plusDays(1)
        val nextOvulationStart = nextOvulationDay.minusDays(3)
        val nextFollicularPhaseEnd = nextOvulationStart.minusDays(1)
        
        // 排卵期
        val nextOvulationEnd = nextOvulationDay.plusDays(4)
        
        // 黄体期
        val nextLutealPhaseStart = nextOvulationEnd.plusDays(1)
        val nextLutealPhaseEnd = nextNextPeriodDate.minusDays(1)
        
        return CycleAnalysis(
            cycleLength = cycleLength,
            nextPeriodDate = nextPeriodDate,
            
            // 当前周期
            periodStart = periodStart,
            periodEnd = periodEnd,
            follicularPhaseStart = follicularPhaseStart,
            follicularPhaseEnd = follicularPhaseEnd,
            ovulationStart = ovulationStart,
            ovulationEnd = ovulationEnd,
            ovulationDay = ovulationDay,
            lutealPhaseStart = lutealPhaseStart,
            lutealPhaseEnd = lutealPhaseEnd,
            
            // 上一个周期
            prevPeriodStart = prevPeriodStart,
            prevPeriodEnd = prevPeriodEnd,
            prevFollicularPhaseStart = prevFollicularPhaseStart,
            prevFollicularPhaseEnd = prevFollicularPhaseEnd,
            prevOvulationStart = prevOvulationStart,
            prevOvulationEnd = prevOvulationEnd,
            prevOvulationDay = prevOvulationDay,
            prevLutealPhaseStart = prevLutealPhaseStart,
            prevLutealPhaseEnd = prevLutealPhaseEnd,
            
            // 下一个周期
            nextPeriodEnd = nextPeriodEnd,
            nextFollicularPhaseStart = nextFollicularPhaseStart,
            nextFollicularPhaseEnd = nextFollicularPhaseEnd,
            nextOvulationStart = nextOvulationStart,
            nextOvulationEnd = nextOvulationEnd,
            nextOvulationDay = nextOvulationDay,
            nextLutealPhaseStart = nextLutealPhaseStart,
            nextLutealPhaseEnd = nextLutealPhaseEnd
        )
    }
    
    /**
     * 根据多次记录计算平均周期
     */
    fun calculateAverageCycle(dates: List<LocalDate>): Long? {
        if (dates.size < 2) return null
        
        val sortedDates = dates.sorted()
        var totalDays = 0L
        
        for (i in 0 until sortedDates.size - 1) {
            totalDays += ChronoUnit.DAYS.between(sortedDates[i], sortedDates[i + 1])
        }
        
        return totalDays / (sortedDates.size - 1)
    }
}
