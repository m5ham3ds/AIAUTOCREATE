package com.aiautocreate.data.datasource.remote.api

import com.aiautocreate.data.datasource.remote.dto.request.HfImageRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.HfTtsRequestDto
import com.aiautocreate.data.datasource.remote.dto.response.HfModelInfo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface HuggingFaceApi {

    @POST("models/{model}")
    suspend fun generateImage(
        @Path("model") model: String,
        @Body request: HfImageRequestDto,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun generateSpeech(
        @Path("model") model: String,
        @Body request: HfTtsRequestDto,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun generateClonedSpeech(
        @Path("model") model: String,
        @Body request: HfTtsRequestDto,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun postRawAudio(
        @Path("model") model: String,
        @Body audio: RequestBody,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun imageToText(
        @Path("model") model: String,
        @Body image: ByteArray?,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    // ✅ معلومات النموذج من HuggingFace API
    @GET("https://huggingface.co/api/models/{modelId}")
    suspend fun getModelInfo(
        @Path("modelId") modelId: String,
        @Header("Authorization") authorization: String = ""
    ): Response<HfModelInfo>

    // ✅ البحث حسب الفئة
    @GET("https://huggingface.co/api/models")
    suspend fun searchModelsByCategory(
        @Query("pipeline_tag") pipelineTag: String,
        @Query("limit") limit: Int = 50,
        @Query("sort") sort: String = "downloads",
        @Query("direction") direction: String = "-1",
        @Header("Authorization") authorization: String = ""
    ): Response<List<HfModelInfo>>

    // ✅ توليد نص من نموذج HuggingFace (لنماذج الدردشة)
    @POST("models/{modelId}")
    suspend fun generateText(
        @Path("modelId") modelId: String,
        @Body request: Map<String, Any>,
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>
}
