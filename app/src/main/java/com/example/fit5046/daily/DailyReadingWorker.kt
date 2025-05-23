package com.example.fit5046.daily

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import java.util.*
import com.example.fit5046.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyReadingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val sharedPreferences: SharedPreferences = appContext.getSharedPreferences(
        "daily_reading_prefs",
        Context.MODE_PRIVATE
    )
    
    private val firebaseManager = FirebaseManager.getInstance()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 随机选择一篇阅读
                val reading = ReadingDataSet.readings.random()
                
                // 将阅读内容保存到 SharedPreferences
                sharedPreferences.edit().apply {
                    putString("current_reading", Gson().toJson(reading))
                    putLong("last_update", System.currentTimeMillis())
                    putBoolean("is_read", false)
                    apply()
                }
                
                // 同步到Firebase
                if (firebaseManager.getCurrentUserId() != null) {
                    try {
                        firebaseManager.saveDailyReading(reading)
                    } catch (e: Exception) {
                        // Firebase保存失败，但不影响本地保存
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    companion object {
        // 检查是否需要更新每日阅读
        fun needsUpdate(context: Context): Boolean {
            val prefs = context.getSharedPreferences("daily_reading_prefs", Context.MODE_PRIVATE)
            val lastUpdate = prefs.getLong("last_update", 0)
            
            if (lastUpdate == 0L) return true

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = lastUpdate
            val lastUpdateDay = calendar.get(Calendar.DAY_OF_YEAR)

            calendar.timeInMillis = System.currentTimeMillis()
            val today = calendar.get(Calendar.DAY_OF_YEAR)

            return lastUpdateDay != today
        }

        // 获取当前的每日阅读
        suspend fun getCurrentReading(context: Context): DailyReading? {
            return withContext(Dispatchers.IO) {
                val firebaseManager = FirebaseManager.getInstance()
                
                // 尝试从Firebase获取
                if (firebaseManager.getCurrentUserId() != null) {
                    var firebaseReading: DailyReading? = null
                    try {
                        firebaseManager.getDailyReadingFlow().collect { reading ->
                            if (reading != null) {
                                firebaseReading = reading
                            }
                        }
                    } catch (e: Exception) {
                        // Firebase获取失败，回退到本地
                    }
                    if (firebaseReading != null) {
                        return@withContext firebaseReading
                    }
                }
                
                // 从本地SharedPreferences获取
                val prefs = context.getSharedPreferences("daily_reading_prefs", Context.MODE_PRIVATE)
                val readingJson = prefs.getString("current_reading", null)
                if (readingJson != null) {
                    Gson().fromJson(readingJson, DailyReading::class.java)
                } else null
            }
        }

        // 标记阅读为已读
        suspend fun markAsRead(context: Context) {
            withContext(Dispatchers.IO) {
                val prefs = context.getSharedPreferences("daily_reading_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_read", true).apply()
                
                // 同步到Firebase
                val firebaseManager = FirebaseManager.getInstance()
                if (firebaseManager.getCurrentUserId() != null) {
                    try {
                        firebaseManager.markDailyReadingAsRead()
                    } catch (e: Exception) {
                        // Firebase操作失败，但不影响本地
                    }
                }
            }
        }

        // 检查是否已读
        suspend fun isRead(context: Context): Boolean {
            return withContext(Dispatchers.IO) {
                val firebaseManager = FirebaseManager.getInstance()
                
                // 尝试从Firebase获取状态
                if (firebaseManager.getCurrentUserId() != null) {
                    var isReadInFirebase = false
                    try {
                        firebaseManager.isDailyReadingReadFlow().collect { isRead ->
                            isReadInFirebase = isRead
                        }
                    } catch (e: Exception) {
                        // Firebase获取失败，回退到本地
                    }
                    if (isReadInFirebase) {
                        return@withContext true
                    }
                }
                
                // 从本地获取
                val prefs = context.getSharedPreferences("daily_reading_prefs", Context.MODE_PRIVATE)
                prefs.getBoolean("is_read", false)
            }
        }
    }
} 