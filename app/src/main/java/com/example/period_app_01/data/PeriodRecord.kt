package com.example.period_app_01.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 经期记录实体类 - 用于日历视图
 * 每条记录代表一个日期，可以标记为经期开始或结束
 */
@Entity(tableName = "period_records")
data class PeriodRecord(
    @PrimaryKey
    val date: LocalDate,
    
    /**
     * 日期类型：
     * 0 = 未标记
     * 1 = 经期开始日
     * 2 = 经期中（开始和结束之间的日期）
     * 3 = 经期结束日
     */
    val recordType: Int = 0,
    
    /**
     * 关联的经期ID，同一次经期的所有日期共享相同的periodId
     */
    val periodId: Long = 0,
    
    /**
     * 记录创建时间
     */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 日期点击状态枚举
 */
enum class DateClickState {
    NONE,           // 未标记
    PERIOD_START,   // 经期开始
    PERIOD_END      // 经期结束
}
