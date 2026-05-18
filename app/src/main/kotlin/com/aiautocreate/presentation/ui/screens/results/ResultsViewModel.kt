package com.aiautocreate.presentation.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.domain.pipeline.PipelineEvent
import com.aiautocreate.domain.pipeline.PipelineOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val orchestrator: PipelineOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsState())
    val state: StateFlow<ResultsState> = _state.asStateFlow()

    init {
        // الاستماع المستمر لأحداث البايبلاين حتى بدون startTests()
        listenToPipeline()
    }

    private fun listenToPipeline() {
        viewModelScope.launch {
            orchestrator.events.collect { event ->
                when (event) {
                    is PipelineEvent.Progress -> handleProgress(event)
                    is PipelineEvent.Log -> handleLog(event)
                    is PipelineEvent.Error -> handleError(event)
                    is PipelineEvent.FinalResult -> handleFinalResult(event)
                }
            }
        }
    }

    private suspend fun handleProgress(event: PipelineEvent.Progress) {
        val stage = event.stage.lowercase()
        val title = titleForStage(stage)
        val status = if (event.percent >= 100) "completed" else "in_progress"

        _state.update { s ->
            val existingOps = s.operations.toMutableList()
            val existingIndex = existingOps.indexOfFirst { it.id == stage }
            val op = OperationResult(
                id = stage,
                title = title,
                status = status,
                progress = event.percent / 100f,
                detailText = event.message
            )
            if (existingIndex >= 0) {
                existingOps[existingIndex] = op
            } else {
                existingOps.add(op)
            }
            // تحديث التقدم العام بناءً على عدد العمليات المكتملة
            val completedCount = existingOps.count { it.status == "completed" }
            val overallProgress = if (existingOps.isNotEmpty()) completedCount.toFloat() / existingOps.size else 0f
            s.copy(
                operations = existingOps,
                overallProgress = overallProgress,
                overallStep = completedCount + 1,
                overallStatusText = event.message,
                isProcessing = overallProgress < 1f
            )
        }
    }

    private suspend fun handleLog(event: PipelineEvent.Log) {
        _state.update { it.copy(logs = it.logs + event.message) }
    }

    private suspend fun handleError(event: PipelineEvent.Error) {
        // تحديث حالة العملية إلى failed إن وجدت
        val stage = event.stage.lowercase()
        _state.update { s ->
            val ops = s.operations.toMutableList()
            val idx = ops.indexOfFirst { it.id == stage }
            if (idx >= 0) {
                ops[idx] = ops[idx].copy(status = "failed", detailText = event.message)
            }
            s.copy(operations = ops, errorMessage = event.message, isProcessing = false)
        }
    }

    private suspend fun handleFinalResult(event: PipelineEvent.FinalResult) {
        _state.update { it.copy(overallProgress = 1f, overallStatusText = "اكتملت العملية", isProcessing = false) }
    }

    private fun titleForStage(stage: String): String = when (stage) {
        "script" -> "Script"
        "image" -> "Image Generation"
        "tts" -> "Audio"
        "video" -> "Video Assembly"
        else -> stage.replaceFirstChar { it.uppercase() }
    }

    // يمكن استدعاؤها من زر "تشغيل الاختبارات" أو من HomeScreen
    fun startTests() {
        // لا حاجة لعمل شيء، البايبلاين يديره HomeViewModel
        // لكن يمكن إعادة تعيين الحالة
        _state.update { ResultsState(isProcessing = true, overallStatusText = "جاري التهيئة...") }
    }
}