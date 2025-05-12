package com.example.fit5046

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun FormScreen() {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Beginner") }
    var goalMinutes by remember { mutableStateOf("") }

    val options = listOf("Beginner", "Intermediate", "Advanced")
    var expanded by remember { mutableStateOf(false) }

    // DatePickerDialog 设置
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            birthDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📋 Reading Preferences",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Your Age") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    label = { Text("Birth Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box {
                    OutlinedTextField(
                        value = level,
                        onValueChange = {},
                        label = { Text("Your Reading Level") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expanded = true }
                            )
                        }
                    )

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    level = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = goalMinutes,
                    onValueChange = { goalMinutes = it },
                    label = { Text("Daily Reading Goal (mins)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Form submitted! 🎉", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Preferences", fontSize = 16.sp)
                }
            }
        }
    }
}

