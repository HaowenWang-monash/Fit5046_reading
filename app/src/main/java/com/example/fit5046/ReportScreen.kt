package com.example.fit5046

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.example.fit5046.firebase.FirebaseManager
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(navController: NavHostController) {
    val firebaseManager = remember { FirebaseManager.getInstance() }
    var isLoading by remember { mutableStateOf(true) }
    var quizResults by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Load quiz data
    LaunchedEffect(Unit) {
        try {
            val results = firebaseManager.getQuizResultsFlow().first()
            quizResults = results.map { result ->
                mapOf(
                    "subject" to (result.subject),
                    "score" to (result.score),
                    "correctAnswers" to (result.correctAnswers),
                    "totalQuestions" to (result.totalQuestions),
                    "timestamp" to (result.timestamp)
                )
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "📊 Quiz Report",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (quizResults.isEmpty()) {
            Text(
                text = "No quiz records yet",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            // Subject performance bar chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Subject Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    AndroidView(
                        factory = { context ->
                            BarChart(context).apply {
                                // Calculate average scores by subject
                                val subjectStats = quizResults.groupBy { it["subject"] as String }
                                val entries = ArrayList<BarEntry>()
                                val labels = ArrayList<String>()
                                
                                subjectStats.entries.forEachIndexed { index, entry ->
                                    val scores = entry.value.map { it["score"] as Int }
                                    val avgScore = scores.average().toFloat()
                                    entries.add(BarEntry(index.toFloat(), avgScore))
                                    labels.add(entry.key)
                                }

                                val dataSet = BarDataSet(entries, "Average Score").apply {
                                    color = Color.parseColor("#4CAF50")
                                    valueTextColor = Color.BLACK
                                    valueTextSize = 12f
                                    valueFormatter = PercentFormatter()
                                }

                                data = BarData(dataSet)
                                
                                xAxis.apply {
                                    valueFormatter = IndexAxisValueFormatter(labels)
                                    position = XAxis.XAxisPosition.BOTTOM
                                    granularity = 1f
                                    setDrawGridLines(false)
                                }
                                
                                axisLeft.apply {
                                    axisMinimum = 0f
                                    axisMaximum = 100f
                                    setDrawGridLines(true)
                                }
                                
                                axisRight.isEnabled = false
                                description.isEnabled = false
                                legend.isEnabled = true
                                
                                animateY(1000)
                                invalidate()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent quiz trend line chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Recent Quiz Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    AndroidView(
                        factory = { context ->
                            LineChart(context).apply {
                                // Get last 10 quiz results
                                val recentQuizzes = quizResults
                                    .sortedBy { it["timestamp"] as Long }
                                    .takeLast(10)
                                
                                val entries = ArrayList<Entry>()
                                val dates = ArrayList<String>()
                                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                                
                                recentQuizzes.forEachIndexed { index, result ->
                                    entries.add(Entry(index.toFloat(), (result["score"] as Int).toFloat()))
                                    dates.add(dateFormat.format(Date(result["timestamp"] as Long)))
                                }

                                val dataSet = LineDataSet(entries, "Quiz Score").apply {
                                    color = Color.parseColor("#2196F3")
                                    setCircleColor(Color.parseColor("#2196F3"))
                                    setDrawCircleHole(true)
                                    circleHoleColor = Color.WHITE
                                    lineWidth = 2.5f
                                    circleRadius = 5f
                                    valueTextSize = 11f
                                    valueFormatter = PercentFormatter()
                                    mode = LineDataSet.Mode.CUBIC_BEZIER
                                    setDrawFilled(true)
                                    fillColor = Color.parseColor("#2196F3")
                                    fillAlpha = 50
                                    setDrawHighlightIndicators(true)
                                    highLightColor = Color.parseColor("#FF4081")
                                    highlightLineWidth = 1.5f
                                }

                                data = LineData(dataSet)

                                xAxis.apply {
                                    valueFormatter = IndexAxisValueFormatter(dates)
                                    position = XAxis.XAxisPosition.BOTTOM
                                    granularity = 1f
                                    setDrawGridLines(false)
                                    labelRotationAngle = -45f
                                    textSize = 10f
                                    setAvoidFirstLastClipping(true)
                                }

                                axisLeft.apply {
                                    axisMinimum = 0f
                                    axisMaximum = 100f
                                    setDrawGridLines(true)
                                    gridColor = Color.parseColor("#E0E0E0")
                                    textSize = 12f
                                    valueFormatter = PercentFormatter()
                                }

                                axisRight.isEnabled = false
                                description.isEnabled = false
                                legend.apply {
                                    isEnabled = true
                                    textSize = 12f
                                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                                    formSize = 12f
                                    xEntrySpace = 10f
                                    yEntrySpace = 5f
                                }

                                setTouchEnabled(true)
                                isDragEnabled = true
                                setScaleEnabled(true)
                                setPinchZoom(true)
                                setDrawMarkers(true)
                                setDrawBorders(true)
                                setBorderWidth(1f)
                                setBorderColor(Color.parseColor("#E0E0E0"))
                                
                                animateX(1500)
                                invalidate()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)  // Increased height for better visibility
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accuracy statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Answer Accuracy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val subjectStats = quizResults.groupBy { it["subject"] as String }
                    subjectStats.forEach { (subject, results) ->
                        val totalCorrect = results.sumOf { it["correctAnswers"] as Int }
                        val totalQuestions = results.sumOf { it["totalQuestions"] as Int }
                        val correctRate = totalCorrect.toFloat() / totalQuestions

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(subject)
                                Text("${(correctRate * 100).toInt()}%")
                            }
                            LinearProgressIndicator(
                                progress = correctRate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

