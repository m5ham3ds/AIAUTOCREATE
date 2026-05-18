package com.aiautocreate.util

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode

/**
 * مشغل أوامر FFmpeg مبسط مع دالة مساعدة.
 */
object FFmpegRunner {

    fun execute(command: String): Result<FFmpegSession> {
        val session = FFmpegKit.execute(command)
        return if (ReturnCode.isSuccess(session.returnCode)) {
            Result.success(session)
        } else {
            Result.failure(RuntimeException("FFmpeg failed: ${session.failStackTrace}"))
        }
    }

    fun executeAsync(command: String, onComplete: (FFmpegSession) -> Unit) {
        FFmpegKit.executeAsync(command) { session ->
            onComplete(session)
        }
    }

    fun buildDrawTextFilter(text: String, fontSize: Int = 24, fontColor: String = "white"): String =
        "drawtext=text='$text':fontcolor=$fontColor:fontsize=$fontSize:x=(w-text_w)/2:y=(h-text_h)/2"
}