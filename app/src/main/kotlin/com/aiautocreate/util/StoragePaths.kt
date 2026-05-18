package com.aiautocreate.util

import android.content.Context
import java.io.File

object StoragePaths {
    fun getProjectRoot(context: Context): File {
        val dir = File(context.filesDir, "projects")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getProjectDir(context: Context, projectId: Long): File {
        val dir = File(getProjectRoot(context), projectId.toString())
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCacheDir(context: Context, subDir: String = ""): File {
        val dir = if (subDir.isNotEmpty()) File(context.cacheDir, subDir)
        else context.cacheDir
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}