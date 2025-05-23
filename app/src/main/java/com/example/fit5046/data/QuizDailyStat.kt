package com.example.fit5046.data



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_daily_stats")
data class QuizDailyStat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val date: String,
    val category: String,
    val totalQuestions: Int,
    val correctAnswers: Int
)

data class DailyStat(
    val date: String,
    val correctAnswers: Int,
    val totalQuestions: Int
)

data class CategoryStat(
    val category: String,
    val correctAnswers: Int
)
