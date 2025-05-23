package com.example.fit5046

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.work.*
import com.example.fit5046.daily.*
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentReading by remember { mutableStateOf<DailyReading?>(null) }
    
    // Check and set up daily reading work
    LaunchedEffect(Unit) {
        if (DailyReadingWorker.needsUpdate(context)) {
            val workRequest = OneTimeWorkRequestBuilder<DailyReadingWorker>()
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }

        // Set up daily scheduled task
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReadingWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "daily_reading",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        
        // Load current reading
        scope.launch {
            currentReading = DailyReadingWorker.getCurrentReading(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏠 Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(
                onClick = { navController.navigate("edit_preference") }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit preferences",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Display daily reading card
        DailyReadingCard(currentReading)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose a subject:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        HomeCardButton(
            label = "📘 English Reading",
            onClick = { navController.navigate("quiz_english") }
        )

        HomeCardButton(
            label = "🔬 Science Knowledge",
            onClick = { navController.navigate("science") }
        )

        HomeCardButton(
            label = "➗ Math Practice",
            onClick = { navController.navigate("math") }
        )

        // Add bottom padding to ensure the last button is fully visible
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HomeCardButton(label: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(label, fontSize = 16.sp)
        }
    }
}
