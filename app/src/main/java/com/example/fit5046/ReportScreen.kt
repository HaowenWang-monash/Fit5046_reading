package com.example.fit5046

import android.graphics.Color
import android.widget.Toast
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
import com.example.fit5046.data.DailyStat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
    var dailyStats by remember { mutableStateOf(listOf<DailyStat>()) }

    // Fetch data once
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val stats = dao.getStatsForUser(userId)
            correctAnswers = stats.sumOf { it.correctAnswers }
            totalQuestions = stats.sumOf { it.totalQuestions }
            dailyStats = dao.getDailyCorrectAnswers(userId)
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
            AndroidView(
                factory = { ctx ->
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

                        data = PieData(dataSet)
                        description.isEnabled = false
                        centerText = "Your Accuracy"
                        setEntryLabelTextSize(12f)
                        setCenterTextSize(16f)
                        setUsePercentValues(true)
                        setDrawEntryLabels(false)
                        legend.isEnabled = true
                        invalidate()

                        // Listener to handle click events
                        var currentToast: Toast? = null
                        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                if (e is PieEntry) {
                                    val label = e.label
                                    val message = when (label) {
                                        "Correct" -> "Correct Answers: $correctAnswers"
                                        "Incorrect" -> "Incorrect Answers: ${totalQuestions - correctAnswers}"
                                        else -> "Unknown selection"
                                    }
                                    currentToast?.cancel() // Cancel previous toast
                                    currentToast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT)
                                    currentToast?.show()

                                }
                            }

                            override fun onNothingSelected() {
                                // Exception: handle when nothing is selected
                            }
                        })

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        } else {
            Text("Sorry, no quiz data available.", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("📅 Daily Correct Answers", fontSize = 20.sp)
        if (dailyStats.isNotEmpty()) {
            AndroidView(
                factory = { ctx ->
                    BarChart(ctx).apply {
                        val entries = dailyStats.mapIndexed { index, stat ->
                            BarEntry(index.toFloat(), stat.correctAnswers.toFloat())
                        }

                        val labels = dailyStats.map { it.date }

                        val dataSet = BarDataSet(entries, "Correct Answers Per Day").apply {
                            colors = ColorTemplate.MATERIAL_COLORS.toList()
                            valueTextSize = 12f
                        }
                        val barData = BarData(dataSet)
                        barData.barWidth = 0.4f // set width for each bar

                        data = barData

                        xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(labels)
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawLabels(true)
                            labelRotationAngle = -45f
                            setCenterAxisLabels(false)
                        }
                        axisLeft.axisMinimum = 0f
                        axisRight.isEnabled = false
                        description.isEnabled = false
                        legend.isEnabled = false

                        setFitBars(true)
                        invalidate()
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

        } else {
            Text("No daily progress data to display.", fontSize = 16.sp)
        }
    }
}
