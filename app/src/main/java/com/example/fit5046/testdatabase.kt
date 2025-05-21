package com.example.fit5046

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fit5046.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TestDatabaseScreen() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).quizStatDao()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧪 Test Room Database", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                val startDate = "2025-01-01"

                // ✅ 替代 LocalDate.now()
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())

                dao.getStatsBetweenDates(startDate, endDate).collect { stats ->
                    if (stats.isEmpty()) {
                        Log.d("QuizStats", "⚠️ No records found.")
                    } else {
                        stats.forEach {
                            Log.d("QuizStats", "✅ ${it.date} | ${it.subject} | total: ${it.total}, correct: ${it.correct}")
                        }
                    }
                }
            }
        }) {
            Text("🔍 Print All Stats to Logcat")
        }
    }
}
