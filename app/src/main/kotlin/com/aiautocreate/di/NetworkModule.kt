package com.aiautocreate.di

import com.aiautocreate.BuildConfig
import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.datasource.remote.interceptor.ApiKeyInterceptor
import com.aiautocreate.data.datasource.remote.interceptor.ErrorHandlingInterceptor
import com.aiautocreate.data.datasource.remote.interceptor.RetryInterceptor
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL_GEMINI = "https://generativelanguage.googleapis.com/"
    private const val BASE_URL_HF = "https://api-inference.huggingface.co/"
    private const val TIMEOUT_CONNECT_SECONDS = 30L
    private const val TIMEOUT_READ_SECONDS = 60L
    private const val TIMEOUT_WRITE_SECONDS = 60L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        // ✅ تعطيل pinning مؤقتاً لحل مشكلة الشهادة
        return CertificatePinner.Builder().build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        apiKeyInterceptor: ApiKeyInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor,
        retryInterceptor: RetryInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(loggingInterceptor)
            .certificatePinner(certificatePinner)  // الآن لا يوجد pinning
            .connectTimeout(TIMEOUT_CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_WRITE_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("gemini")
    fun provideRetrofitGemini(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_GEMINI)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @Named("huggingface")
    fun provideRetrofitHuggingFace(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_HF)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGeminiApi(@Named("gemini") retrofit: Retrofit): GeminiApi = retrofit.create(GeminiApi::class.java)

    @Provides
    @Singleton
    fun provideHuggingFaceApi(@Named("huggingface") retrofit: Retrofit): HuggingFaceApi = retrofit.create(HuggingFaceApi::class.java)

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val config = FirebaseRemoteConfig.getInstance()
        config.setDefaultsAsync(mapOf("default_key" to "default_value"))
        return config
    }
}
