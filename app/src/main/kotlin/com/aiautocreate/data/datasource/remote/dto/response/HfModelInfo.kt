package com.aiautocreate.data.datasource.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HfModelInfo(
    val id: String,
    val modelId: String? = null,
    val pipelineTag: String? = null,
    val tags: List<String>? = null,
    val cardData: CardData? = null,
    val config: Config? = null
) {
    @Serializable
    data class CardData(
        val title: String? = null,
        val description: String? = null
    )

    @Serializable
    data class Config(
        val modelType: String? = null
    )
}