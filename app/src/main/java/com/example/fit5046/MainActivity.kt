package com.example.fit5046
import androidx.navigation.NavHostController

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fit5046.ui.theme.Fit5046Theme
import com.example.fit5046.work.scheduleMidnightCleanup

import com.example.fit5046.LoginScreen
import com.example.fit5046.HomeScreen
import com.example.fit5046.FormScreen
import com.example.fit5046.ReportScreen
import com.example.fit5046.GenerateQuizScreen
import com.example.fit5046.TodayRecommendationScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleMidnightCleanup(this)
        setContent {
            Fit5046Theme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}



@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
    }
}



