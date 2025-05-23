package com.example.fit5046

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreference(
    val name: String = "",
    val gender: String = "",
    val educationalBackground: String = "",
    val dateOfBirth: String = "",
    val school: String = "",
    val subjectsOfInterest: String = ""
)

class PreferenceManager(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "user_preferences")
        
        private val NAME = stringPreferencesKey("name")
        private val GENDER = stringPreferencesKey("gender")
        private val EDUCATIONAL_BACKGROUND = stringPreferencesKey("educational_background")
        private val DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
        private val SCHOOL = stringPreferencesKey("school")
        private val SUBJECTS_OF_INTEREST = stringPreferencesKey("subjects_of_interest")
    }

    // Get user preferences
    val userPreferencesFlow: Flow<UserPreference> = context.dataStore.data
        .map { preferences ->
            UserPreference(
                name = preferences[NAME] ?: "",
                gender = preferences[GENDER] ?: "",
                educationalBackground = preferences[EDUCATIONAL_BACKGROUND] ?: "",
                dateOfBirth = preferences[DATE_OF_BIRTH] ?: "",
                school = preferences[SCHOOL] ?: "",
                subjectsOfInterest = preferences[SUBJECTS_OF_INTEREST] ?: ""
            )
        }

    // Save user preferences
    suspend fun saveUserPreferences(userPreference: UserPreference) {
        context.dataStore.edit { preferences ->
            preferences[NAME] = userPreference.name
            preferences[GENDER] = userPreference.gender
            preferences[EDUCATIONAL_BACKGROUND] = userPreference.educationalBackground
            preferences[DATE_OF_BIRTH] = userPreference.dateOfBirth
            preferences[SCHOOL] = userPreference.school
            preferences[SUBJECTS_OF_INTEREST] = userPreference.subjectsOfInterest
        }
    }

    // Clear user preferences
    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 