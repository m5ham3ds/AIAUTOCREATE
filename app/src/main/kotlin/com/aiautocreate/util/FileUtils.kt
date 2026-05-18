package com.aiautocreate.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun copyUriToFile(context: Context, uri: Uri, targetFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }

    fun getFileExtension(path: String): String {
        return path.substringAfterLast('.', "")
    }

    fun getFileNameWithoutExtension(path: String): String {
        return File(path).nameWithoutExtension
    }
}