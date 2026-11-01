package com.example.lifesync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifesync.ui.theme.PlusJakartaSansFont
import com.example.lifesync.ui.theme.Primary
import com.example.lifesync.ui.theme.SoftLilac
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalAutoComplete(
    placeholder: String,
    searchType: MedicalSearchType,
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    // Smart typing debounce to prevent spamming the API
    LaunchedEffect(query) {
        if (query.length >= 2) {
            isSearching = true
            delay(400) // wait for the user to stop typing
            val data = MedicalApiHelper.search(query, searchType)
            results = data
            expanded = data.isNotEmpty()
            isSearching = false
        } else {
            results = emptyList()
            expanded = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded && results.isNotEmpty() },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    placeholder = { Text(placeholder, fontFamily = PlusJakartaSansFont, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Primary)
                        } else if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Primary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Primary.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = PlusJakartaSansFont, color = Primary)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    results.forEach { result ->
                        DropdownMenuItem(
                            text = { Text(result, fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Primary) },
                            onClick = {
                                query = ""
                                expanded = false
                                onItemSelected(result)
                            }
                        )
                    }
                    if (query.length >= 2 && results.isEmpty() && !isSearching) {
                        DropdownMenuItem(
                            text = { Text("Add custom: \"$query\"", fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Primary, fontWeight = FontWeight.Bold) },
                            onClick = {
                                val custom = query
                                query = ""
                                expanded = false
                                onItemSelected(custom)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.background(Color(0xFFFFF0F0), CircleShape).size(48.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFFD32F2F))
            }
        }
    }
}
