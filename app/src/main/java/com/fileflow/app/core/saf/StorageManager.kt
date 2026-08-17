package com.fileflow.app.core.saf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StorageManager(private val context: Context) {

    fun persistFolderPermission(treeUri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            context.contentResolver.takePersistableUriPermission(treeUri, flags)
        } catch (_: SecurityException) {
        }
    }

    fun getFileName(uri: Uri): String {
        var name = "file"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = it.getString(nameIndex) ?: "file"
                    }
                }
            }
        } catch (_: Exception) {
            name = uri.lastPathSegment ?: "file"
        }
        return name
    }

    fun getFileSize(uri: Uri): Long {
        var size = 0L
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        size = it.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return size
    }

    fun generateFileName(prefix: String, extension: String): String {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
        val cleanPrefix = prefix.ifBlank { "FileFlow" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cleanExt = extension.removePrefix(".")
        return "${cleanPrefix}_$timeStamp.$cleanExt"
    }

    fun createTempFile(prefix: String = "temp", suffix: String = ".tmp"): File {
        val dir = File(context.cacheDir, "processing")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File.createTempFile(prefix, suffix, dir)
    }

    fun clearTempFiles() {
        try {
            val dir = File(context.cacheDir, "processing")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (_: Exception) {
        }
    }

    fun saveToTarget(
        sourceFile: File,
        targetFilename: String,
        mimeType: String,
        targetFolderUriString: String?,
        askBeforeReplace: Boolean = true
    ): Uri {
        if (!targetFolderUriString.isNullOrBlank()) {
            try {
                val treeUri = Uri.parse(targetFolderUriString)
                val pickedDir = DocumentFile.fromTreeUri(context, treeUri)
                if (pickedDir != null && pickedDir.canWrite()) {
                    var finalName = targetFilename
                    if (askBeforeReplace) {
                        finalName = resolveUniqueFileName(pickedDir, targetFilename)
                    } else {
                        val existing = pickedDir.findFile(targetFilename)
                        existing?.delete()
                    }

                    val newFile = pickedDir.createFile(mimeType, finalName)
                    if (newFile != null) {
                        context.contentResolver.openOutputStream(newFile.uri)?.use { out ->
                            sourceFile.inputStream().use { input ->
                                input.copyTo(out)
                            }
                        }
                        return newFile.uri
                    }
                }
            } catch (_: Exception) {
            }
        }

        val exportDir = File(context.filesDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val destFile = File(exportDir, targetFilename)
        sourceFile.copyTo(destFile, overwrite = true)
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destFile
        )
    }

    private fun resolveUniqueFileName(dir: DocumentFile, filename: String): String {
        val existing = dir.findFile(filename) ?: return filename
        val dotIndex = filename.lastIndexOf('.')
        val baseName = if (dotIndex != -1) filename.substring(0, dotIndex) else filename
        val extension = if (dotIndex != -1) filename.substring(dotIndex) else ""

        var counter = 1
        while (true) {
            val candidate = "$baseName ($counter)$extension"
            if (dir.findFile(candidate) == null) {
                return candidate
            }
            counter++
        }
    }

    fun copyUriToLocalTemp(uri: Uri, extension: String = "tmp"): File {
        val temp = createTempFile("input_", ".$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(temp).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot read file from Uri")
        return temp
    }
}
