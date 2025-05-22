package com.example.fit5046.data



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface QuizStatDao {

    @Query("SELECT * FROM quiz_daily_stats WHERE date = :date AND category = :category AND userId = :userId LIMIT 1")
    suspend fun getStatByDateAndCategory(date: String, category: String, userId: String): QuizDailyStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: QuizDailyStat)

    @Update
    suspend fun update(stat: QuizDailyStat)

    @Query("SELECT * FROM quiz_daily_stats ORDER BY date DESC")
    suspend fun getAllStats(): List<QuizDailyStat>

    @Query("SELECT * FROM quiz_daily_stats WHERE userId = :userId")
    fun getStatsForUser(userId: String): List<QuizDailyStat>
}


