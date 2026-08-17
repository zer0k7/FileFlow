package com.salik.fileflow.ui.screens.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salik.fileflow.core.model.HistoryItem
import com.salik.fileflow.core.model.ToolType
import com.salik.fileflow.ui.components.FloatingTopAppBar
import com.salik.fileflow.ui.components.FolderPickerBanner
import com.salik.fileflow.ui.components.ToolCard
import com.salik.fileflow.ui.components.getToolIcon
import com.salik.fileflow.ui.theme.ToolCardShape

@Composable
fun HomeScreen(
    folderName: String?,
    favoriteToolIds: Set<String>,
    recentToolIds: List<String>,
    recentHistory: List<HistoryItem>,
    onPickFolder: () -> Unit,
    onOpenTool: (ToolType) -> Unit,
    onToggleFavorite: (ToolType) -> Unit,
    onOpenHistoryFile: (HistoryItem) -> Unit,
    onShareHistoryFile: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        FloatingTopAppBar(
            title = "FileFlow",
            subtitle = "Offline document & image tools"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FolderPickerBanner(
                    currentFolder = folderName,
                    onPickFolder = onPickFolder
                )
            }

            val favoriteTools = ToolType.entries.filter { favoriteToolIds.contains(it.id) }
            if (favoriteTools.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(favoriteTools) { tool ->
                    ToolCard(
                        tool = tool,
                        isFavorite = true,
                        onToolClick = onOpenTool,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            val recentTools = recentToolIds.mapNotNull { ToolType.fromId(it) }
            if (recentTools.isNotEmpty()) {
                item {
                    Text(
                        text = "Recently Used",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recentTools) { tool ->
                            Surface(
                                shape = ToolCardShape,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .width(140.dp)
                                    .clip(ToolCardShape)
                                    .clickable { onOpenTool(tool) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = getToolIcon(tool.iconName),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tool.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (recentHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Output Files",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(recentHistory.take(5)) { item ->
                    Surface(
                        shape = ToolCardShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getToolIcon(item.toolType.iconName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.outputFileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.toolType.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onOpenHistoryFile(item) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Rounded.OpenInNew, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onShareHistoryFile(item) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
