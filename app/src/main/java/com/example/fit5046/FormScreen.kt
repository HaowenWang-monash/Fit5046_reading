
package com.example.fit5046

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavHostController) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()

    // 状态管理
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var educationalBackground by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var subjectsOfInterest by remember { mutableStateOf("") }

    // 下拉展开状态
    var genderExpanded by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }
    var subjectsExpanded by remember { mutableStateOf(false) }

    // 下拉选项
    val genderOptions = listOf("Male", "Female")
    val educationOptions = listOf("Primary School", "Junior Secondary", "Senior Secondary")
    val subjectOptions = listOf("English", "Math", "Science")

    // 日期选择器
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> dateOfBirth = "%04d-%02d-%02d".format(year, month + 1, day) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    // 加载本地数据
    LaunchedEffect(Unit) {
        val prefs = preferenceManager.userPreferencesFlow.first()
        name = prefs.name
        gender = prefs.gender
        educationalBackground = prefs.educationalBackground
        dateOfBirth = prefs.dateOfBirth
        school = prefs.school
        subjectsOfInterest = prefs.subjectsOfInterest
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👤 User Info", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator()
            return@Column
        }

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            readOnly = !isEditing,
            enabled = isEditing,
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Gender
        ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                enabled = isEditing,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        gender = it
                        genderExpanded = false
                    })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Education
        ExposedDropdownMenuBox(expanded = educationExpanded, onExpandedChange = { educationExpanded = !educationExpanded }) {
            OutlinedTextField(
                value = educationalBackground,
                onValueChange = {},
                readOnly = true,
                enabled = isEditing,
                label = { Text("Educational Background") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(educationExpanded, onDismissRequest = { educationExpanded = false }) {
                educationOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        educationalBackground = it
                        educationExpanded = false
                    })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Date of Birth
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = {},
            readOnly = true,
            enabled = isEditing,
            label = { Text("Date of Birth") },
            trailingIcon = {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.clickable(enabled = isEditing) {
                    datePickerDialog.show()
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isEditing) { datePickerDialog.show() }
        )

        Spacer(Modifier.height(16.dp))

        // School
        OutlinedTextField(
            value = school,
            onValueChange = { school = it },
            readOnly = !isEditing,
            enabled = isEditing,
            label = { Text("School (Optional)") },
            leadingIcon = { Icon(Icons.Default.LocationCity, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Subjects
        ExposedDropdownMenuBox(expanded = subjectsExpanded, onExpandedChange = { subjectsExpanded = !subjectsExpanded }) {
            OutlinedTextField(
                value = subjectsOfInterest,
                onValueChange = {},
                readOnly = true,
                enabled = isEditing,
                label = { Text("Subjects of Interest") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectsExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(subjectsExpanded, onDismissRequest = { subjectsExpanded = false }) {
                subjectOptions.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        subjectsOfInterest = it
                        subjectsExpanded = false
                    })
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Bottom Button
        if (isEditing) {
            Button(
                onClick = {
                    scope.launch {
                        if (name.isEmpty() || gender.isEmpty() || educationalBackground.isEmpty() || dateOfBirth.isEmpty() || subjectsOfInterest.isEmpty()) {
                            Toast.makeText(context, "Please complete all required fields", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val prefs = UserPreference(name, gender, educationalBackground, dateOfBirth, school, subjectsOfInterest)
                        preferenceManager.saveUserPreferences(prefs)
                        isEditing = false
                        Toast.makeText(context, "Saved locally", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💾 Save")
            }
        } else {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("✏️ Edit")
            }
        }
    }
}
