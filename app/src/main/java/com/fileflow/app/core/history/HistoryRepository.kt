package com.fileflow.app.core.history

import android.content.Context
import com.fileflow.app.core.model.HistoryItem
import com.fileflow.app.core.model.ToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class HistoryRepository(private val context: Context) {

    private val historyFile = File(context.filesDir, "processing_history.json")
    private val _historyFlow = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyFlow: Flow<List<HistoryItem>> = _historyFlow.asStateFlow()

    suspend fun init() {
        loadHistory()
    }

    private suspend fun loadHistory() = withContext(Dispatchers.IO) {
        if (!historyFile.exists()) {
            _historyFlow.value = emptyList()
            return@withContext
        }
        try {
            val content = historyFile.readText()
            val array = JSONArray(content)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val toolId = obj.getString("toolType")
                val toolType = ToolType.fromId(toolId) ?: continue
                list.add(
                    HistoryItem(
                        id = obj.getString("id"),
                        toolType = toolType,
                        inputFileName = obj.getString("inputFileName"),
                        outputFileName = obj.getString("outputFileName"),
                        outputUriString = obj.getString("outputUriString"),
                        timestamp = obj.getLong("timestamp"),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        success = obj.optBoolean("success", true)
                    )
                )
            }
            _historyFlow.value = list.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            _historyFlow.value = emptyList()
        }
    }

    suspend fun recordItem(
        toolType: ToolType,
        inputFileName: String,
        outputFileName: String,
        outputUriString: String,
        sizeBytes: Long,
        success: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val newItem = HistoryItem(
            id = UUID.randomUUID().toString(),
            toolType = toolType,
            inputFileName = inputFileName,
            outputFileName = outputFileName,
            outputUriString = outputUriString,
            timestamp = System.currentTimeMillis(),
            sizeBytes = sizeBytes,
            success = success
        )
        val current = _historyFlow.value.toMutableList()
        current.add(0, newItem)
        val trimmed = current.take(100)
        _historyFlow.value = trimmed
        saveToFile(trimmed)
    }

    suspend fun removeItem(id: String) = withContext(Dispatchers.IO) {
        val updated = _historyFlow.value.filter { it.id != id }
        _historyFlow.value = updated
        saveToFile(updated)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        _historyFlow.value = emptyList()
        if (historyFile.exists()) {
            historyFile.delete()
        }
    }

    private fun saveToFile(list: List<HistoryItem>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("toolType", item.toolType.id)
                    put("inputFileName", item.inputFileName)
                    put("outputFileName", item.outputFileName)
                    put("outputUriString", item.outputUriString)
                    put("timestamp", item.timestamp)
                    put("sizeBytes", item.sizeBytes)
                    put("success", item.success)
                }
                array.put(obj)
            }
            historyFile.writeText(array.toString())
        } catch (_: Exception) {
        }
    }
}
