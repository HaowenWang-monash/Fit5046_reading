package com.example.fit5046.daily

data class DailyReading(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val difficulty: String = "",
    val isRead: Boolean = false
) {
    // Empty constructor for Firebase
    constructor() : this(0, "", "", "", false)
}

// 预设的阅读段落数据
object ReadingDataSet {
    val readings = listOf(
        DailyReading(
            1,
            "The Power of Reading",
            "Reading is a fundamental skill that opens doors to knowledge and imagination. It helps us learn, grow, and understand the world around us. Through reading, we can explore new ideas and perspectives.",
            "Beginner"
        ),
        DailyReading(
            2,
            "The Solar System",
            "Our solar system consists of the Sun and everything that orbits around it. This includes eight planets, dozens of moons, asteroids, comets, and other celestial objects.",
            "Intermediate"
        ),
        DailyReading(
            3,
            "Mathematics in Nature",
            "Mathematics can be found everywhere in nature. The Fibonacci sequence appears in flower petals, pine cones, and shells. The golden ratio is present in galaxy spirals and hurricane patterns.",
            "Advanced"
        ),
        // 添加更多阅读段落...
    )
} 