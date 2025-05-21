package com.example.fit5046

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.*

@Composable
fun MainScreen(parentNavController: NavHostController) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(innerNavController) }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(innerNavController) }
            composable(BottomNavItem.Form.route) { FormScreen(innerNavController) }
            composable(BottomNavItem.Report.route) { ReportScreen(innerNavController) }
            composable(BottomNavItem.Quiz.route) { GenerateQuizScreen() }
            composable("test_db") { TestDatabaseScreen() }

            composable("quiz_english") { GenerateQuizScreen() }
            composable("science") { ScienceQuizScreen() }
            composable("math") { MathQuizScreen() }
        }
    }
}










