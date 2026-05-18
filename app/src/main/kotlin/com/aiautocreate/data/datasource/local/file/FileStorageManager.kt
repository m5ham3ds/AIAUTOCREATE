package com.aiautocreate.data.datasource.local.file

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getProjectDir(projectId: Long): File {
        val dir = File(context.filesDir, "projects/$projectId")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveBytesToProject(projectId: Long, fileName: String, bytes: ByteArray): File {
        val file = File(getProjectDir(projectId), fileName)
        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    fun deleteProjectFiles(projectId: Long) {
        getProjectDir(projectId).deleteRecursively()
    }

    fun getCacheDir(subDir: String = ""): File {
        val dir = if (subDir.isNotEmpty()) File(context.cacheDir, subDir)
        else context.cacheDir
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun copyUriToCache(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(getCacheDir(), fileName)
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}