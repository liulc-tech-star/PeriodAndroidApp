package com.example.period_app_01.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 周期分析数据类
 */
data class CycleAnalysis(
    val cycleLength: Long,                    // 月经周期长度（天数）
    val nextPeriodDate: LocalDate,            // 下次月经预测日期
    val periodStart: LocalDate,               // 经期开始日期
    val periodEnd: LocalDate,                 // 经期结束日期
    val follicularPhaseStart: LocalDate,      // 卵泡期开始日期
    val follicularPhaseEnd: LocalDate,        // 卵泡期结束日期
    val ovulationStart: LocalDate,            // 排卵期开始日期
    val ovulationEnd: LocalDate,              // 排卵期结束日期
    val ovulationDay: LocalDate,              // 排卵日
    val lutealPhaseStart: LocalDate,          // 黄体期开始日期
    val lutealPhaseEnd: LocalDate             // 黄体期结束日期
)

/**
 * 周期计算工具类
 */
object CycleCalculator {
    
    /**
     * 计算月经周期分析
     * @param firstPeriodDate 第一次月经开始日期
     * @param secondPeriodDate 第二次月经开始日期
     * @param secondPeriodEndDate 第二次月经结束日期
     * @param lutealPhaseDays 黄体期天数（默认14天）
     * @return 周期分析结果，如果输入无效则返回null
     */
    fun calculateCycle(
        firstPeriodDate: LocalDate,
        secondPeriodDate: LocalDate,
        secondPeriodEndDate: LocalDate? = null,
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
        
        // 2. 预测下一次月经日期
        val nextPeriodDate = secondPeriodDate.plusDays(cycleLength)
        
        // 3. 计算经期（最后一次月经的开始和结束日期）
        val periodStart = secondPeriodDate
        val periodEnd = secondPeriodEndDate ?: secondPeriodDate.plusDays(4) // 默认5天
        
        // 4. 计算排卵日（下次月经日期减去黄体期天数）
        val ovulationDay = nextPeriodDate.minusDays(lutealPhaseDays.toLong())
        
        // 5. 计算卵泡期（本次月经结束后到排卵期前一天）
        val follicularPhaseStart = periodEnd.plusDays(1)
        val ovulationStart = ovulationDay.minusDays(3)
        val follicularPhaseEnd = ovulationStart.minusDays(1)
        
        // 6. 计算排卵期（排卵日前3天到排卵日后4天，共8天易孕期）
        val ovulationEnd = ovulationDay.plusDays(4)
        
        // 7. 计算黄体期（排卵期结束后到下次月经前一天）
        val lutealPhaseStart = ovulationEnd.plusDays(1)
        val lutealPhaseEnd = nextPeriodDate.minusDays(1)
        
        return CycleAnalysis(
            cycleLength = cycleLength,
            nextPeriodDate = nextPeriodDate,
            periodStart = periodStart,
            periodEnd = periodEnd,
            follicularPhaseStart = follicularPhaseStart,
            follicularPhaseEnd = follicularPhaseEnd,
            ovulationStart = ovulationStart,
            ovulationEnd = ovulationEnd,
            ovulationDay = ovulationDay,
            lutealPhaseStart = lutealPhaseStart,
            lutealPhaseEnd = lutealPhaseEnd
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
