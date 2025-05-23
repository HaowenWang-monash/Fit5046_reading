package com.example.fit5046

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.background
import com.example.fit5046.firebase.FirebaseManager
import com.example.fit5046.model.ReadingRecord
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseManager = remember { FirebaseManager.getInstance() }
    
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var readingLevel by remember { mutableStateOf("") }
    var dailyGoal by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Error states
    var nameError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf("") }
    var readingLevelError by remember { mutableStateOf("") }
    var dailyGoalError by remember { mutableStateOf("") }

    // Reading level options
    val readingLevels = listOf("Beginner", "Intermediate", "Advanced")

    // Date picker
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            birthDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            birthDateError = ""
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

        if (age.isEmpty()) {
            ageError = "Please enter your age"
            isValid = false
        } else if (age.toIntOrNull() == null || age.toInt() !in 1..120) {
            ageError = "Please enter a valid age (1-120)"
            isValid = false
        } else {
            ageError = ""
        }

        if (birthDate.isEmpty()) {
            birthDateError = "Please select your birth date"
            isValid = false
        } else {
            birthDateError = ""
        }

        if (readingLevel.isEmpty()) {
            readingLevelError = "Please select your reading level"
            isValid = false
        } else {
            readingLevelError = ""
        }

        if (dailyGoal.isEmpty()) {
            dailyGoalError = "Please enter your daily reading goal"
            isValid = false
        } else if (dailyGoal.toIntOrNull() == null || dailyGoal.toInt() < 1) {
            dailyGoalError = "Please enter a valid number of pages (minimum 1)"
            isValid = false
        } else {
            dailyGoalError = ""
        }

        return isValid
    }
    
    // Load existing form data from Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            if (firebaseManager.getCurrentUserId() != null) {
                // 尝试从Firebase加载数据，使用first()获取单次数据
                val formData = firebaseManager.getFormDataFlow().first()
                if (formData != null) {
                    formData.let { data ->
                        name = (data["name"] as? String) ?: ""
                        age = (data["age"] as? Number)?.toString() ?: ""
                        birthDate = (data["birthDate"] as? String) ?: ""
                        readingLevel = (data["readingLevel"] as? String) ?: ""
                        dailyGoal = (data["dailyGoal"] as? Number)?.toString() ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "加载数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📋 Personal Information",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading your information...", style = MaterialTheme.typography.bodyMedium)
        } else {
            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = "" },
                label = { Text("Name") },
                placeholder = { Text("Enter your name") },
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

            // Age input
            OutlinedTextField(
                value = age,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d{0,3}$"))) {
                        age = it
                        ageError = ""
                    }
                },
                label = { Text("Age") },
                placeholder = { Text("Enter your age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ageError.isNotEmpty(),
                supportingText = { 
                    if (ageError.isNotEmpty()) {
                        Text(ageError, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Numbers, "Age icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Birth date selection
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
                        Text(
                            text = "Birth Date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (birthDate.isEmpty()) "Select your birth date" else birthDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (birthDate.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant 
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (birthDateError.isNotEmpty()) {
                Text(
                    text = birthDateError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reading level selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = "Reading Level",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Reading Level",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (readingLevel.isEmpty()) "Select your reading level" else readingLevel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (readingLevel.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant 
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                if (expanded) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            readingLevels.forEach { level ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            readingLevel = level
                                            readingLevelError = ""
                                            expanded = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = level,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (level != readingLevels.last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }

            if (readingLevelError.isNotEmpty()) {
                Text(
                    text = readingLevelError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily reading goal
            OutlinedTextField(
                value = dailyGoal,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d{0,4}$"))) {
                        dailyGoal = it
                        dailyGoalError = ""
                    }
                },
                label = { Text("Daily Reading Goal (pages)") },
                placeholder = { Text("Enter your daily reading goal") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = dailyGoalError.isNotEmpty(),
                supportingText = { 
                    if (dailyGoalError.isNotEmpty()) {
                        Text(dailyGoalError, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Book, "Reading goal icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = {
                    if (validateForm()) {
                        scope.launch {
                            isLoading = true
                            try {
                                // 创建表单数据
                                val formData = mapOf(
                                    "name" to name,
                                    "age" to age.toInt(),
                                    "birthDate" to birthDate,
                                    "readingLevel" to readingLevel,
                                    "dailyGoal" to dailyGoal.toInt(),
                                    "lastUpdated" to System.currentTimeMillis()
                                )

                                // 保存到Firebase
                                if (firebaseManager.getCurrentUserId() != null) {
                                    val success = firebaseManager.saveFormData(formData)
                                    if (success) {
                                        Toast.makeText(context, "数据已保存到云端", Toast.LENGTH_SHORT).show()
                                        
                                        // 同时保存阅读记录
                                        val readingRecord = ReadingRecord(
                                            date = System.currentTimeMillis(),
                                            title = "每日目标设置",
                                            pagesRead = dailyGoal.toIntOrNull() ?: 0
                                        )
                                        firebaseManager.saveReadingRecord(readingRecord)
                                        
                                        navController.navigate("home")
                                    } else {
                                        Toast.makeText(context, "保存到云端失败", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "未登录，无法保存到云端", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("保存信息")
                }
            }
        }
    }
}

