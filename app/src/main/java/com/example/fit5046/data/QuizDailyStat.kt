package com.example.fit5046.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_stats")
data class QuizDailyStat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val subject: String,
    val total: Int,
    val correct: Int
)