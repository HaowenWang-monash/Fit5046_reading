package com.example.fit5046

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun ReportScreen(navController: NavHostController) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {

        Text("📊 Weekly Progress", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    val entries = listOf(
                        BarEntry(0f, 2f),
                        BarEntry(1f, 3f),
                        BarEntry(2f, 5f),
                        BarEntry(3f, 1f),
                        BarEntry(4f, 4f)
                    )

                    val dataSet = BarDataSet(entries, "Pages Read")
                    dataSet.color = Color.parseColor("#FFA726")
                    dataSet.valueTextColor = Color.BLACK
                    dataSet.valueTextSize = 12f

                    val barData = BarData(dataSet)
                    data = barData

                    xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri"))
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    legend.isEnabled = true

                    invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

