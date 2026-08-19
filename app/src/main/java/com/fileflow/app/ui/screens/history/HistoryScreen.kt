package com.fileflow.app.ui.screens.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.app.core.history.HistoryRepository
import com.fileflow.app.core.model.HistoryItem
import com.fileflow.app.ui.components.FloatingTopAppBar
import com.fileflow.app.ui.components.getToolIcon
import com.fileflow.app.ui.components.rememberAppHaptics
import com.fileflow.app.ui.theme.ToolCardShape
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyItems: List<HistoryItem>,
    historyRepository: HistoryRepository,
    floatingTopBar: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAppHaptics()
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredItems = remember(historyItems, searchQuery) {
        if (searchQuery.isBlank()) historyItems
        else historyItems.filter {
            it.outputFileName.contains(searchQuery, ignoreCase = true) ||
                    it.inputFileName.contains(searchQuery, ignoreCase = true) ||
                    it.toolType.title.contains(searchQuery, ignoreCase = true)
        }
    }

    fun openFile(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun shareFile(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = context.contentResolver.getType(uri) ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share File"))
        } catch (_: Exception) {
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FloatingTopAppBar(
            title = "Files & History",
            subtitle = "${historyItems.size} processed items",
            isFloating = floatingTopBar,
            actions = {
                if (historyItems.isNotEmpty()) {
                    IconButton(onClick = {
                        haptics.tap()
                        showClearDialog = true
                    }) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear History?") },
                text = { Text("This will clear the processing records. Output files in your storage will remain untouched.") },
                confirmButton = {
                    Button(
                        onClick = {
                            haptics.heavyTap()
                            scope.launch { historyRepository.clearHistory() }
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (historyItems.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search file history...") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No processing history yet" else "No matching files found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Files you convert or process will appear here." else "Try a different search query.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                    val sizeFormatted = formatFileSize(item.sizeBytes)

                    Surface(
                        shape = ToolCardShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(ToolCardShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getToolIcon(item.toolType.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.outputFileName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${item.toolType.title} • $sizeFormatted • $dateFormatted",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    haptics.tap()
                                    openFile(item.outputUriString)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.OpenInNew, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(
                                onClick = {
                                    haptics.tap()
                                    shareFile(item.outputUriString)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            IconButton(
                                onClick = {
                                    haptics.heavyTap()
                                    scope.launch { historyRepository.removeItem(item.id) }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
        else -> "$bytes B"
    }
}
