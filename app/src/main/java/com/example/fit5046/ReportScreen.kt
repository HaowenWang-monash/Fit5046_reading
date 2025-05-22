package com.example.fit5046

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.fit5046.data.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReportScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).quizStatDao()
    val userId = Firebase.auth.currentUser?.uid ?: return

    var correctAnswers by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }

    // Fetch data once
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val stats = dao.getStatsForUser(userId)
            correctAnswers = stats.sumOf { it.correctAnswers }
            totalQuestions = stats.sumOf { it.totalQuestions }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("📊 Weekly Progress", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (totalQuestions > 0) {
            AndroidView(factory = { ctx ->
                PieChart(ctx).apply {
                    val incorrectAnswers = totalQuestions - correctAnswers

                    val entries = listOf(
                        PieEntry(correctAnswers.toFloat(), "Correct"),
                        PieEntry(incorrectAnswers.toFloat(), "Incorrect")
                    )

                    val dataSet = PieDataSet(entries, "Quiz Performance")
                    dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                    dataSet.valueTextSize = 14f
                    dataSet.valueTextColor = Color.BLACK

                    val data = PieData(dataSet)
                    this.data = data
                    this.description.isEnabled = false
                    this.centerText = "Your Accuracy"
                    this.setUsePercentValues(true)
                    this.animateY(1000)
                    this.invalidate()
                }
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp))
        } else {
            Text("Sorry, no quiz data available.", fontSize = 16.sp)
        }
    }
}
