package com.example.period_app_01.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

// interface, so declares functions/methods without implementation
interface DatesRepository {
    // can add more functions later, when adding more features
    // for single date, plural for consistency
    suspend fun insertDates(dates: Dates)

    suspend fun deleteLast()

    fun getLastEntry(): Flow<Dates?>

    fun getLastId(): Flow<Int>

    fun getLastPeriod(): Flow<Long>

    fun getLastDate(): Flow<LocalDate?>
}