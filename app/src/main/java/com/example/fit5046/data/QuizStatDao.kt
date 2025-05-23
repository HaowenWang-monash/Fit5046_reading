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
    suspend fun getStatsForUser(userId: String): List<QuizDailyStat>

    @Query("SELECT date, SUM(correctAnswers) as correctAnswers, SUM(totalQuestions) as totalQuestions FROM quiz_daily_stats WHERE userId = :userId GROUP BY date")
    suspend fun getDailyCorrectAnswers(userId: String): List<DailyStat>

    @Query("SELECT date, SUM(correctAnswers) as correctAnswers, SUM(totalQuestions) as totalQuestions FROM quiz_daily_stats WHERE userId = :userId AND category = :category GROUP BY date")
    suspend fun getDailyCorrectAnswersForCategory(userId: String, category: String): List<DailyStat>





    @Query("SELECT category, SUM(correctAnswers) as correctAnswers FROM quiz_daily_stats WHERE userId = :userId GROUP BY category")
    suspend fun getCategoryCorrectAnswers(userId: String): List<CategoryStat>

}