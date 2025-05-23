package com.example.fit5046

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import com.example.fit5046.firebase.FirebaseManager
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(navController: NavHostController, isEditing: Boolean = false) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()
    val firebaseManager = remember { FirebaseManager.getInstance() }
    
    var isLoading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var educationalBackground by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var subjectsOfInterest by remember { mutableStateOf("") }

    // 加载数据
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 尝试从Firebase加载
            if (firebaseManager.getCurrentUserId() != null) {
                val prefs = firebaseManager.getUserPreferencesFlow().first()
                if (prefs != null) {
                    // 如果Firebase中有数据，使用Firebase数据
                    name = prefs.name
                    gender = prefs.gender
                    educationalBackground = prefs.educationalBackground
                    dateOfBirth = prefs.dateOfBirth
                    school = prefs.school
                    subjectsOfInterest = prefs.subjectsOfInterest
                } else {
                    // 如果Firebase中没有数据，尝试从本地加载
                    val localPrefs = preferenceManager.userPreferencesFlow.first()
                    name = localPrefs.name
                    gender = localPrefs.gender
                    educationalBackground = localPrefs.educationalBackground
                    dateOfBirth = localPrefs.dateOfBirth
                    school = localPrefs.school
                    subjectsOfInterest = localPrefs.subjectsOfInterest
                }
            } else {
                // 未登录时从本地加载
                val localPrefs = preferenceManager.userPreferencesFlow.first()
                name = localPrefs.name
                gender = localPrefs.gender
                educationalBackground = localPrefs.educationalBackground
                dateOfBirth = localPrefs.dateOfBirth
                school = localPrefs.school
                subjectsOfInterest = localPrefs.subjectsOfInterest
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load preferences: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // Error states
    var nameError by remember { mutableStateOf("") }
    var schoolError by remember { mutableStateOf("") }

    // Dropdowns state
    var genderExpanded by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }
    var subjectsExpanded by remember { mutableStateOf(false) }

    // Options
    val genderOptions = listOf("Male", "Female")
    val educationOptions = listOf("Primary School", "Junior Secondary", "Senior Secondary")
    val subjectOptions = listOf("English", "Math", "Science")

    // Date picker
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dateOfBirth = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
        },
        year,
        month,
        day
    )

    // Form validation
    fun validateForm(): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameError = "Please enter your name"
            isValid = false
        } else {
            nameError = ""
        }

        if (gender.isEmpty()) {
            isValid = false
        }

        if (educationalBackground.isEmpty()) {
            isValid = false
        }

        if (dateOfBirth.isEmpty()) {
            isValid = false
        }

        if (subjectsOfInterest.isEmpty()) {
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditing) "✏️ Edit Preferences" else "👋 Welcome!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Text(
                text = "Loading preferences...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = "" },
                label = { Text("Name") },
                singleLine = true,
                isError = nameError.isNotEmpty(),
                supportingText = { 
                    if (nameError.isNotEmpty()) {
                        Text(nameError, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Person, "Name icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { genderExpanded = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Gender", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Gender")
                                Text(
                                    text = gender.ifEmpty { "Select gender" },
                                    color = if (gender.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant 
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, "Expand")
                    }
                }
            }

            if (genderExpanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Educational Background selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { educationExpanded = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, "Education", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Educational Background")
                                Text(
                                    text = educationalBackground.ifEmpty { "Select education level" },
                                    color = if (educationalBackground.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant 
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, "Expand")
                    }
                }
            }

            if (educationExpanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        educationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    educationalBackground = option
                                    educationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Date of Birth")
                        Text(
                            text = if (dateOfBirth.isEmpty()) "Select birth date" else dateOfBirth,
                            color = if (dateOfBirth.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // School (Optional)
            OutlinedTextField(
                value = school,
                onValueChange = { school = it; schoolError = "" },
                label = { Text("School (Optional)") },
                singleLine = true,
                isError = schoolError.isNotEmpty(),
                supportingText = { 
                    if (schoolError.isNotEmpty()) {
                        Text(schoolError, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.LocationCity, "School icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subjects of Interest
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { subjectsExpanded = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, "Subjects", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Subjects of Interest")
                                Text(
                                    text = subjectsOfInterest.ifEmpty { "Select subjects" },
                                    color = if (subjectsOfInterest.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant 
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, "Expand")
                    }
                }
            }

            if (subjectsExpanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        subjectOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    subjectsOfInterest = option
                                    subjectsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    if (validateForm()) {
                        scope.launch {
                            val userPreference = UserPreference(
                                name = name,
                                gender = gender,
                                educationalBackground = educationalBackground,
                                dateOfBirth = dateOfBirth,
                                school = school,
                                subjectsOfInterest = subjectsOfInterest
                            )
                            
                            try {
                                if (firebaseManager.getCurrentUserId() != null) {
                                    // 已登录，保存到Firebase
                                    val success = firebaseManager.saveUserPreferences(userPreference)
                                    if (success) {
                                        Toast.makeText(context, "已保存到云端", Toast.LENGTH_SHORT).show()
                                        // 保存成功后导航
                                        if (isEditing) {
                                            navController.popBackStack()
                                        } else {
                                            navController.navigate(BottomNavItem.Home.route) {
                                                popUpTo("preference") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "保存到云端失败", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // 未登录，保存到本地
                                    preferenceManager.saveUserPreferences(userPreference)
                                    Toast.makeText(context, "已保存到本地", Toast.LENGTH_SHORT).show()
                                    // 保存成功后导航
                                    if (isEditing) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(BottomNavItem.Home.route) {
                                            popUpTo("preference") { inclusive = true }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEditing) "保存修改" else "保存设置")
            }
        }
    }
} 