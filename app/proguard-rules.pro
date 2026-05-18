# AIAutoCreate ProGuard Rules (Final)

# Kotlin Serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class com.aiautocreate.** {
    *** Companion;
}
-keep class com.aiautocreate.data.datasource.remote.dto.** { *; }
-keep class com.aiautocreate.data.datasource.local.db.entities.** { *; }
-keep class com.aiautocreate.domain.model.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# Retrofit & OkHttp
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# FFmpegKit
-keep class com.arthenica.ffmpegkit.** { *; }

# Coil
-dontwarn coil.**