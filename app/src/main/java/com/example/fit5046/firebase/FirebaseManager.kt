package com.example.fit5046.firebase

import android.util.Log
import com.example.fit5046.UserPreference
import com.example.fit5046.model.QuizQuestion
import com.example.fit5046.model.QuizResult
import com.example.fit5046.model.ReadingRecord
import com.example.fit5046.daily.DailyReading
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class FirebaseManager {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Get current user ID or null if not logged in
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Reference to user's data root
    private fun getUserReference(): DatabaseReference? {
        val userId = getCurrentUserId() ?: return null
        return database.getReference("users").child(userId)
    }

    // User preferences
    suspend fun saveUserPreferences(userPreference: UserPreference): Boolean {
        return try {
            val userRef = getUserReference() ?: throw Exception("User not logged in")
            userRef.child("preferences").setValue(userPreference).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving preferences: ${e.message}")
            false
        }
    }

    fun getUserPreferencesFlow(): Flow<UserPreference?> = callbackFlow {
        val userRef = getUserReference()
        if (userRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val preferences = snapshot.child("preferences").getValue(UserPreference::class.java)
                trySend(preferences)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error getting preferences: ${error.message}")
                trySend(null)
            }
        }

        userRef.addValueEventListener(listener)

        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    // Form data
    suspend fun saveFormData(formData: Map<String, Any>): Boolean {
        return try {
            val userRef = getUserReference() ?: throw Exception("User not logged in")
            userRef.child("form_data").setValue(formData).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving form data: ${e.message}")
            false
        }
    }

    fun getFormDataFlow(): Flow<Map<String, Any>?> = callbackFlow {
        val userRef = getUserReference()
        if (userRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val formDataRef = userRef.child("form_data")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val formData = snapshot.getValue<Map<String, Any>>()
                    trySend(formData)
                } catch (e: Exception) {
                    Log.e("FirebaseManager", "Error parsing form data: ${e.message}")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error getting form data: ${error.message}")
                trySend(null)
            }
        }

        formDataRef.addValueEventListener(listener)

        awaitClose {
            formDataRef.removeEventListener(listener)
        }
    }

    // Daily reading
    suspend fun saveDailyReading(reading: DailyReading): Boolean {
        return try {
            val userRef = getUserReference() ?: throw Exception("User not logged in")
            val today = Calendar.getInstance().apply { 
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis.toString()
            
            userRef.child("daily_readings").child(today).setValue(reading).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving daily reading: ${e.message}")
            false
        }
    }

    fun getDailyReadingFlow(): Flow<DailyReading?> = callbackFlow {
        val userRef = getUserReference()
        if (userRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val today = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis.toString()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reading = snapshot.child("daily_readings").child(today).getValue(DailyReading::class.java)
                trySend(reading)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error getting daily reading: ${error.message}")
                trySend(null)
            }
        }

        userRef.addValueEventListener(listener)

        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    // Mark daily reading as read
    suspend fun markDailyReadingAsRead(): Boolean {
        return try {
            val userRef = getUserReference() ?: throw Exception("User not logged in")
            val today = Calendar.getInstance().apply { 
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis.toString()
            
            userRef.child("daily_readings").child(today).child("isRead").setValue(true).await()
            
            // Add reading record for reports
            val readingRecord = ReadingRecord(
                date = System.currentTimeMillis(),
                title = "Daily Reading",
                pagesRead = 1
            )
            saveReadingRecord(readingRecord)
            
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error marking daily reading as read: ${e.message}")
            false
        }
    }

    // Check if daily reading is read
    fun isDailyReadingReadFlow(): Flow<Boolean> = callbackFlow {
        val userRef = getUserReference()
        if (userRef == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val today = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis.toString()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isRead = snapshot.child("daily_readings").child(today).child("isRead").getValue(Boolean::class.java) ?: false
                trySend(isRead)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error checking if daily reading is read: ${error.message}")
                trySend(false)
            }
        }

        userRef.addValueEventListener(listener)

        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    // Quiz results
    suspend fun saveQuizResult(quizResult: QuizResult): Boolean {
        return try {
            val userRef = getUserReference() ?: throw Exception("User not logged in")
            val resultId = userRef.child("quiz_results").push().key ?: throw Exception("Could not get result ID")
            
            userRef.child("quiz_results").child(resultId).setValue(quizResult).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving quiz result: ${e.message}")
            false
        }
    }

    fun getQuizResultsFlow(): Flow<List<QuizResult>> = callbackFlow {
        val userRef = getUserReference()
        if (userRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<QuizResult>()
                for (childSnapshot in snapshot.child("quiz_results").children) {
                    val result = childSnapshot.getValue(QuizResult::class.java)
                    if (result != null) {
                        results.add(result)
                    }
                }
                trySend(results)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error getting quiz results: ${error.message}")
                trySend(emptyList())
            }
        }

        userRef.addValueEventListener(listener)

        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    // Reading records for reports
    suspend fun saveReadingRecord(record: ReadingRecord): Boolean {
        return try {
            val userRef = getUserReference() ?: throw Exception("User not logged in")
            val recordId = userRef.child("reading_records").push().key ?: throw Exception("Could not get record ID")
            
            userRef.child("reading_records").child(recordId).setValue(record).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving reading record: ${e.message}")
            false
        }
    }

    fun getReadingRecordsFlow(): Flow<List<ReadingRecord>> = callbackFlow {
        val userRef = getUserReference()
        if (userRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = mutableListOf<ReadingRecord>()
                for (childSnapshot in snapshot.child("reading_records").children) {
                    val record = childSnapshot.getValue(ReadingRecord::class.java)
                    if (record != null) {
                        records.add(record)
                    }
                }
                trySend(records)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error getting reading records: ${error.message}")
                trySend(emptyList())
            }
        }

        userRef.addValueEventListener(listener)

        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager().also { INSTANCE = it }
            }
        }
    }
} 