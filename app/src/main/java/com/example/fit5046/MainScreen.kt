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
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }
            composable(BottomNavItem.Form.route) { FormScreen(navController) }
            composable(BottomNavItem.Report.route) { ReportScreen(navController) }
            composable(BottomNavItem.Quiz.route) { GenerateQuizScreen() }
            composable("test_db") { TestDatabaseScreen() }

            composable("quiz_english") { GenerateQuizScreen() }
            composable("science") { ScienceQuizScreen() }
            composable("math") { MathQuizScreen() }


        }
    }
}







