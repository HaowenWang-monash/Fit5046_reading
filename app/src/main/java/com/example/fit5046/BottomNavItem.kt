package com.example.fit5046



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Form : BottomNavItem("form", "Form", Icons.Default.List)
    object Report : BottomNavItem("report", "Report", Icons.Default.BarChart)
    object Quiz : BottomNavItem("test", "test", Icons.Default.School)
}