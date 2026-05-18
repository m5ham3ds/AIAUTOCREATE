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
        @Body request: HfImageRequestDto
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun generateSpeech(
        @Path("model") model: String,
        @Body request: HfTtsRequestDto
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun generateClonedSpeech(
        @Path("model") model: String,
        @Body request: HfTtsRequestDto
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun postRawAudio(
        @Path("model") model: String,
        @Body audio: RequestBody
    ): Response<ResponseBody>

    @POST("models/{model}")
    suspend fun imageToText(
        @Path("model") model: String,
        @Body image: ByteArray?
    ): Response<ResponseBody>

    @GET("api/models/{modelId}")
    suspend fun getModelInfo(
        @Path("modelId") modelId: String
    ): Response<HfModelInfo>
    
    @GET("api/models")
    suspend fun searchModelsByCategory(
        @Query("pipeline_tag") pipelineTag: String,
        @Query("limit") limit: Int = 50,
        @Query("sort") sort: String = "downloads",
        @Query("direction") direction: String = "-1"
    ): Response<List<HfModelInfo>>
}