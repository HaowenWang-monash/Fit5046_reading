package com.example.fit5046.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizStatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: QuizDailyStat)

    @Query("SELECT * FROM quiz_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getStatsBetweenDates(startDate: String, endDate: String): Flow<List<QuizDailyStat>>

    @Query("SELECT * FROM quiz_stats WHERE date = :date AND subject = :subject LIMIT 1")
    suspend fun getStatByDateAndSubject(date: String, subject: String): QuizDailyStat?

    @Update
    suspend fun update(stat: QuizDailyStat)
}
