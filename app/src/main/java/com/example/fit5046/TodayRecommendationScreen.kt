package com.example.fit5046



import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TodayRecommendationScreen() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("ai_storage", Context.MODE_PRIVATE)
    var todayText by remember { mutableStateOf("") }
    var showThankYou by remember { mutableStateOf(false) }

    // 默认推荐内容（如无保存记录）
    val defaultText = """
        Once upon a time, in a quiet village surrounded by mountains, lived a curious young fox named Kiko. 
        Unlike other foxes, Kiko loved listening to stories more than hunting. One day, he found an old book 
        hidden under a tree, and from that moment on, his life changed forever.
    """.trimIndent()

    // 从 SharedPreferences 加载推荐内容
    LaunchedEffect(Unit) {
        todayText = sharedPrefs.getString("today_ai_text", defaultText) ?: defaultText
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌟 Today’s Recommendation",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .background(Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Text(
                text = todayText,
                modifier = Modifier.padding(20.dp),
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showThankYou = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("✅ I’ve read it!", fontSize = 16.sp)
        }

        if (showThankYou) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Great job! Come back tomorrow for a new story. 🎉", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

