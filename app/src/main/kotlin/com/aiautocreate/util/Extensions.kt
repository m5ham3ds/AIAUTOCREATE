package com.aiautocreate.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

fun <T> Flow<T>.catchAndLog(tag: String = Constants.APP_TAG): Flow<T> =
    this.catch { e -> Timber.tag(tag).e(e, "Flow error") }

fun Uri.getFileName(context: Context): String {
    var name = "unknown"
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

fun Uri.getFileSize(context: Context): Long {
    var size = 0L
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst() && sizeIndex >= 0) {
            size = cursor.getLong(sizeIndex)
        }
    }
    return size
}