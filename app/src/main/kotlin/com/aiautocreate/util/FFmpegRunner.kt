package com.aiautocreate.util

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ✅ FIXED: execute() now runs on Dispatchers.IO to prevent UI freezing.
 * ✅ FIXED: buildDrawTextFilter now properly escapes special characters (' and :).
 * 
 * FFmpegRunner provides synchronous and asynchronous FFmpeg command execution
 * with proper coroutine dispatching for Android UI thread safety.
 */
object FFmpegRunner {

    /**
     * ✅ FIXED: Now executes on Dispatchers.IO to avoid blocking the main thread.
     * FFmpeg operations are I/O heavy and can freeze the UI if run on Main dispatcher.
     * 
     * @param command The FFmpeg command string to execute
     * @return Result containing the FFmpegSession on success, or exception on failure
     */
    suspend fun execute(command: String): Result<FFmpegSession> = withContext(Dispatchers.IO) {
        val session = FFmpegKit.execute(command)
        if (ReturnCode.isSuccess(session.returnCode)) {
            Result.success(session)
        } else {
            Result.failure(
                RuntimeException(
                    "FFmpeg failed with code ${session.returnCode}: ${session.failStackTrace ?: "Unknown error"}"
                )
            )
        }
    }

    /**
     * Asynchronous execution with callback (runs on IO thread, callback on provided thread).
     */
    fun executeAsync(command: String, onComplete: (FFmpegSession) -> Unit) {
        FFmpegKit.executeAsync(command) { session ->
            onComplete(session)
        }
    }

    /**
     * ✅ FIXED: Now escapes special characters that break FFmpeg drawtext syntax.
     * Characters that MUST be escaped in drawtext text parameter:
     * - Single quote (') → \'
     * - Colon (:) → \: (can break filter syntax)
     * - Backslash (\) → \\
     * - Percent (%) → %% (interpreted as format specifier)
     * 
     * @param text The raw text to display
     * @param fontSize Font size in pixels
     * @param fontColor Color name or hex value
     * @return Properly escaped drawtext filter string
     */
    fun buildDrawTextFilter(
        text: String,
        fontSize: Int = 24,
        fontColor: String = "white"
    ): String {
        val escaped = text
            .replace("\\", "\\\\")   // Escape backslashes first
            .replace("'", "\\'")          // Escape single quotes
            .replace(":", "\\:")          // Escape colons
            .replace("%", "%%")              // Escape percent signs
        return "drawtext=text='$escaped':fontcolor=$fontColor:fontsize=$fontSize:x=(w-text_w)/2:y=(h-text_h)/2"
    }
}
