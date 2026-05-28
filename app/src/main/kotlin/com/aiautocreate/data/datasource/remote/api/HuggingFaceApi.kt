package com.aiautocreate.data.datasource.remote.api

import com.aiautocreate.data.datasource.remote.dto.request.HfImageRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.HfTtsRequestDto
import com.aiautocreate.data.datasource.remote.dto.response.HfModelInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * HuggingFace Inference API Interface
 *
 * Fixes applied:
 * 1. imageToText: ByteArray changed to RequestBody (Retrofit requires RequestBody for raw binary)
 * 2. Added generateVideoFromImage: Dedicated endpoint for img2vid models requiring binary image upload
 * 3. Added generateVideoFromImageMultipart: Alternative multipart upload for video generation
 * 4. All endpoints properly support Authorization header injection
 */
interface HuggingFaceApi {

    // ================== IMAGE GENERATION ==================

    @POST("models/{model}")
    suspend fun generateImage(
        @Path("model") model: String,
        @Body request: HfImageRequestDto,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    // ================== SPEECH / TTS ==================

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

    // ================== VISION / IMAGE-TO-TEXT ==================

    /**
     * FIXED: ByteArray changed to RequestBody
     * Retrofit does NOT support nullable ByteArray as @Body directly.
     * Use RequestBody.create(MediaType.parse("image/jpeg"), byteArray) when calling.
     */
    @POST("models/{model}")
    suspend fun imageToText(
        @Path("model") model: String,
        @Body image: RequestBody,
        @Header("Authorization") authorization: String = ""
    ): Response<ResponseBody>

    // ================== VIDEO GENERATION (IMG2VID) ==================

    /**
     * NEW: Dedicated video generation endpoint for img2vid models.
     * Models like stable-video-diffusion-img2vid require raw binary image in body,
     * NOT base64 JSON payload. Sends image as application/octet-stream.
     */
    @POST("models/{model}")
    @Headers("Content-Type: application/octet-stream")
    suspend fun generateVideoFromImage(
        @Path("model") model: String,
        @Body image: RequestBody,
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>

    /**
     * NEW: Multipart alternative for video generation (fallback).
     * Some HF Spaces/img2vid endpoints accept multipart/form-data with image file.
     */
    @Multipart
    @POST("models/{model}")
    suspend fun generateVideoFromImageMultipart(
        @Path("model") model: String,
        @Part image: MultipartBody.Part,
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>

    // ================== MODEL INFO & SEARCH ==================

    @GET("https://huggingface.co/api/models/{modelId}")
    suspend fun getModelInfo(
        @Path("modelId") modelId: String,
        @Header("Authorization") authorization: String = ""
    ): Response<HfModelInfo>

    @GET("https://huggingface.co/api/models")
    suspend fun searchModelsByCategory(
        @Query("pipeline_tag") pipelineTag: String,
        @Query("limit") limit: Int = 50,
        @Query("sort") sort: String = "downloads",
        @Query("direction") direction: String = "-1",
        @Header("Authorization") authorization: String = ""
    ): Response<List<HfModelInfo>>

    // ================== TEXT GENERATION ==================

    @POST("models/{modelId}")
    suspend fun generateText(
        @Path("modelId") modelId: String,
        @Body request: Map<String, Any>,
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>
}
