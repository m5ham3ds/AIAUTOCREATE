package com.aiautocreate.domain.model

data class AgentTask(
    val id: String,
    val name: String,
    val description: String,
    val assignedModelId: Long? = null,       // معرف النموذج من ModelConfig
    val assignedModelName: String? = null    // اسم النموذج (للعرض)
)