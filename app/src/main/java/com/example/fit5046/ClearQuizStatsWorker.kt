package com.example.fit5046.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.fit5046.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ClearQuizStatsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val calendar = Calendar.getInstance()
            val isSundayMidnight = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
                    calendar.get(Calendar.HOUR_OF_DAY) == 0

            if (isSundayMidnight) {
                val dao = AppDatabase.getDatabase(applicationContext).quizStatDao()
                dao.clearAll()
                Log.d("ClearQuizStatsWorker", "✅ Quiz stats cleared")
            } else {
                Log.d("ClearQuizStatsWorker", "⏰ Skipped — not Sunday midnight")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("ClearQuizStatsWorker", "❌ Error during cleanup", e)
            Result.failure()
        }
    }
}


fun scheduleMidnightCleanup(context: Context) {
    val delay = getInitialDelayUntilMidnight()

    val request = PeriodicWorkRequestBuilder<ClearQuizStatsWorker>(
        1, TimeUnit.DAYS
    ).setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "midnight_quiz_cleanup",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

fun getInitialDelayUntilMidnight(): Long {
    val now = Calendar.getInstance()

    val midnight = Calendar.getInstance().apply {
        add(Calendar.DATE, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return midnight.timeInMillis - now.timeInMillis
}


