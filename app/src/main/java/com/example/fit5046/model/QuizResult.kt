package com.example.fit5046.model

data class QuizResult(
    val timestamp: Long = System.currentTimeMillis(), // 完成时间
    val subject: String = "", // 科目：英语、数学、科学等
    val difficulty: String = "", // 难度级别
    val totalQuestions: Int = 0, // 题目总数
    val correctAnswers: Int = 0, // 正确回答数
    val score: Int = 0, // 分数
    val timeTaken: Long = 0 // 完成时间（毫秒）
) {
    // Empty constructor for Firebase
    constructor() : this(0, "", "", 0, 0, 0, 0)
} 