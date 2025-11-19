package com.example.period_app_01.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Delete
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * 经期记录数据访问对象
 */
@Dao
interface PeriodRecordDao {
    
    /**
     * 插入或更新记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PeriodRecord)
    
    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<PeriodRecord>)
    
    /**
     * 更新记录
     */
    @Update
    suspend fun updateRecord(record: PeriodRecord)
    
    /**
     * 删除记录
     */
    @Delete
    suspend fun deleteRecord(record: PeriodRecord)
    
    /**
     * 根据日期删除记录
     */
    @Query("DELETE FROM period_records WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
    
    /**
     * 根据经期ID删除所有相关记录
     */
    @Query("DELETE FROM period_records WHERE periodId = :periodId")
    suspend fun deleteByPeriodId(periodId: Long)
    
    /**
     * 获取指定日期的记录
     */
    @Query("SELECT * FROM period_records WHERE date = :date")
    fun getRecordByDate(date: LocalDate): Flow<PeriodRecord?>
    
    /**
     * 获取指定日期的记录（非Flow）
     */
    @Query("SELECT * FROM period_records WHERE date = :date")
    suspend fun getRecordByDateSync(date: LocalDate): PeriodRecord?
    
    /**
     * 获取某个月份的所有记录
     */
    @Query("SELECT * FROM period_records WHERE date >= :startDate AND date <= :endDate ORDER BY date")
    fun getRecordsByMonth(startDate: LocalDate, endDate: LocalDate): Flow<List<PeriodRecord>>
    
    /**
     * 获取所有经期开始日期
     */
    @Query("SELECT * FROM period_records WHERE recordType = 1 ORDER BY date DESC")
    fun getAllPeriodStarts(): Flow<List<PeriodRecord>>
    
    /**
     * 获取最近的经期开始日期
     */
    @Query("SELECT * FROM period_records WHERE recordType = 1 ORDER BY date DESC LIMIT 1")
    fun getLastPeriodStart(): Flow<PeriodRecord?>
    
    /**
     * 获取最近的两次经期开始记录
     */
    @Query("SELECT * FROM period_records WHERE recordType = 1 ORDER BY date DESC LIMIT 2")
    fun getLastTwoPeriodStarts(): Flow<List<PeriodRecord>>
    
    /**
     * 获取指定经期ID的所有记录
     */
    @Query("SELECT * FROM period_records WHERE periodId = :periodId ORDER BY date")
    fun getRecordsByPeriodId(periodId: Long): Flow<List<PeriodRecord>>
    
    /**
     * 获取最大的经期ID
     */
    @Query("SELECT COALESCE(MAX(periodId), 0) FROM period_records")
    suspend fun getMaxPeriodId(): Long
    
    /**
     * 获取所有记录
     */
    @Query("SELECT * FROM period_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<PeriodRecord>>
    
    /**
     * 删除所有记录
     */
    @Query("DELETE FROM period_records")
    suspend fun deleteAll()
}
