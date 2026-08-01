package com.adamtri.mynoteapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.adamtri.mynoteapp.navigation.Screen
import com.adamtri.mynoteapp.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: NoteViewModel,
    noteId: Long,
    onNavigateBack: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableLongStateOf(0xFFFFF9C4) } // Default Yellow

    val colors = listOf(
        0xFFFFF9C4, // Yellow
        0xFFFFCCBC, // Peach
        0xFFC8E6C9, // Green
        0xFFD1C4E9, // Purple
        0xFFB3E5FC, // Blue
        0xFFF5F5F5, // Grey
        0xFFFFFFFF  // White
    )
    
    var createdAt by remember { mutableLongStateOf(0L) }
    var updatedAt by remember { mutableLongStateOf(0L) }

    LaunchedEffect(noteId) {
        if (noteId != Screen.Editor.NO_ID) {
            viewModel.getNoteById(noteId)?.let { note ->
                content = note.content
                isPinned = note.isPinned
                selectedColor = note.color
                createdAt = note.createdAt
                updatedAt = note.updatedAt
            }
        }
    }

    val backgroundColor = Color(selectedColor)
    val contentColor = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == Screen.Editor.NO_ID) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                        )
                    }
                    if (noteId != Screen.Editor.NO_ID) {
                        IconButton(onClick = {
                            viewModel.deleteNote(noteId)
                            onNavigateBack()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.saveNote(
                            id = if (noteId == Screen.Editor.NO_ID) null else noteId,
                            content = content,
                            color = selectedColor,
                            isPinned = isPinned
                        )
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Mulai menulis...", color = contentColor.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor,
                    cursorColor = contentColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            if (noteId != Screen.Editor.NO_ID && createdAt != 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Terakhir diubah: ${formatEditorTimestamp(updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Warna Catatan",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(colors) { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .border(
                                width = if (selectedColor == colorHex) 2.dp else 1.dp,
                                color = if (selectedColor == colorHex) contentColor else contentColor.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorHex }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    }
}

fun formatEditorTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
