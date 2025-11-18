package com.example.period_app_01.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

// passing DAO as argument, extending DatesRepository interface
class OfflineDatesRepository(private val datesDao: DatesDao) : DatesRepository {
    // since function body is a single line, using = symbol rather than {} brackets
    override suspend fun insertDates(dates: Dates) = datesDao.insert(dates)

    override suspend fun deleteLast() = datesDao.deleteLast()

    override fun getLastEntry(): Flow<Dates?> = datesDao.getLastEntry()

    override fun getLastId(): Flow<Int> = datesDao.getLastId()

    override fun getLastPeriod(): Flow<Long> = datesDao.getLastPeriod()

    override fun getLastDate(): Flow<LocalDate?> = datesDao.getLastDate()
}