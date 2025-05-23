package com.example.fit5046.model

data class ReadingRecord(
    val date: Long = System.currentTimeMillis(), // 时间戳
    val title: String = "", // 标题
    val pagesRead: Int = 0, // 阅读页数
    val duration: Int = 0, // 阅读时长（分钟）
    val notes: String = "" // 笔记
) {
    // Empty constructor for Firebase
    constructor() : this(0, "", 0, 0, "")
} 