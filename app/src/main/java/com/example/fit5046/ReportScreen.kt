package com.example.fit5046

import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.fit5046.data.AppDatabase
import com.example.fit5046.data.CategoryStat
import com.example.fit5046.data.DailyStat
import com.example.fit5046.data.QuizDailyStat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun ReportScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).quizStatDao()
    val userId = Firebase.auth.currentUser?.uid ?: return

    var overallCorrectAnswers by remember { mutableStateOf(0) }
    var overallTotalQuestions by remember { mutableStateOf(0) }
    var dailyStats by remember { mutableStateOf(listOf<DailyStat>()) }
    //var categoryStats by remember { mutableStateOf(listOf<CategoryStat>()) } // haven't used
    var selectedCategory by remember { mutableStateOf("Total") }
    val categories = listOf("Total", "English", "Science", "Math")
    var allStats by remember { mutableStateOf(listOf<QuizDailyStat>()) }


    // Fetch data once
    LaunchedEffect(userId) {
        snapshotFlow { selectedCategory }
            .collect { category ->
                withContext(Dispatchers.IO) {
                    val stats = dao.getStatsForUser(userId)
                    overallCorrectAnswers = stats.sumOf { it.correctAnswers }
                    overallTotalQuestions = stats.sumOf { it.totalQuestions }
                    dailyStats = if (selectedCategory == "Total") {
                        dao.getDailyCorrectAnswers(userId)
                    } else {
                        dao.getDailyCorrectAnswersForCategory(userId, selectedCategory)
                    }
                    Log.d("DAILY_STATS", dailyStats.joinToString("\n") {
                        "Category: $selectedCategory, Date: ${it.date}, Correct: ${it.correctAnswers}, Total: ${it.totalQuestions}"
                    }) //print the content of dailyStats, comment off after debugging
                    allStats = stats
                }
            }
    }

    val categoryStatsMap = remember(allStats) {
        allStats.groupBy { it.category }
            .mapValues { entry ->
                Pair(
                    entry.value.sumOf { it.correctAnswers },
                    entry.value.sumOf { it.totalQuestions }
                )
            }
    }
    val (correctAnswers, totalQuestions) = when (selectedCategory) {
        "Total" -> Pair(
            overallCorrectAnswers,
            overallTotalQuestions
        )
        else -> categoryStatsMap[selectedCategory] ?: Pair(0, 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("📊 Your Progress", fontSize = 24.sp)

        CategorySelector( // calls selector bar
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (totalQuestions > 0) {
            key(selectedCategory) {
            AndroidView(
                factory = { ctx ->
                    PieChart(ctx).apply {
                        val incorrectAnswers = totalQuestions - correctAnswers

                        val entries = listOf(
                            PieEntry(correctAnswers.toFloat(), "Correct"),
                            PieEntry(incorrectAnswers.toFloat(), "Incorrect")
                        )

                        val dataSet = PieDataSet(entries, "$selectedCategory Performance")
                        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                        dataSet.valueTextSize = 14f
                        dataSet.valueTextColor = Color.BLACK

                        data = PieData(dataSet)
                        description.isEnabled = false
                        centerText = "Your Accuracy"
                        setEntryLabelTextSize(12f)
                        setCenterTextSize(16f)
                        setUsePercentValues(true)
                        setDrawEntryLabels(true)
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
                }
        } else {
            Text("Sorry, no quiz data available.", fontSize = 16.sp)
        }
        //bar chart
        Spacer(modifier = Modifier.height(32.dp))
        Text("📅 Daily Correct Answers", fontSize = 20.sp)
        if (dailyStats.isNotEmpty()) {
            key(selectedCategory) {
                AndroidView(
                    factory = { ctx ->
                        BarChart(ctx).apply {
                            val correctEntries = mutableListOf<BarEntry>()
                            val totalEntries = mutableListOf<BarEntry>()
                            dailyStats.forEachIndexed { index, stat ->
                                correctEntries.add(
                                    BarEntry(
                                        index.toFloat(),
                                        stat.correctAnswers.toFloat()
                                    )
                                )
                                totalEntries.add(
                                    BarEntry(
                                        index.toFloat(),
                                        stat.totalQuestions.toFloat()
                                    )
                                )
                            }

                            val labels = dailyStats.map { it.date }

                            val correctDataSet =
                                BarDataSet(correctEntries, "Correct Answers").apply {
                                    color = ColorTemplate.MATERIAL_COLORS[0]
                                    valueTextSize = 12f
                                }

                            val totalDataSet = BarDataSet(totalEntries, "Total Questions").apply {
                                color = Color.rgb(52, 152, 219)
                                valueTextSize = 12f
                            }

                            val barData = BarData(correctDataSet, totalDataSet)
                            barData.barWidth = 0.3f // narrow bars

// Grouping setup
                            val groupSpace = 0.2f
                            val barSpace = 0.05f // space between bars within group
                            barData.groupBars(0f, groupSpace, barSpace)

                            data = barData
                            setVisibleXRangeMaximum(5f) //shows limited range
                            invalidate()

                            xAxis.apply {
                                valueFormatter = IndexAxisValueFormatter(labels)
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                labelRotationAngle = 0f
                                setCenterAxisLabels(true)
                                axisMinimum = 0f
                                axisMaximum = dailyStats.size.toFloat()
                            }
                            axisLeft.axisMinimum = 0f
                            axisRight.isEnabled = false
                            description.isEnabled = false
                            legend.isEnabled = true

                            setFitBars(true)
                            invalidate()
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "correct vs total per day",
                fontSize = 12.sp,
                color = ComposeColor.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )

        } else {
            Text("No daily progress data to display.", fontSize = 16.sp)
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Total", "English", "Science", "Math")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        categories.forEach {
                category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) ComposeColor(0xFFD6E8FF) else ComposeColor.White)
                    .border(1.dp, ComposeColor.LightGray, RoundedCornerShape(24.dp))
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    color = ComposeColor.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}
