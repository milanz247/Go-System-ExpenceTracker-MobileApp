package com.example.network

import android.content.Context
import com.example.BuildConfig
import com.example.data.DataStoreManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    fun getApiService(context: Context, dataStoreManager: DataStoreManager): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(ApiService::class.java)
    }

    /** Best-effort extraction of the Go backend's `{"error": "..."}` body from a failed call. */
    fun parseErrorBody(exception: HttpException): String? {
        val raw = exception.response()?.errorBody()?.string()
        if (raw.isNullOrBlank()) return null
        return runCatching { errorAdapter.fromJson(raw)?.error }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}

/** Turns any exception from an [ApiService] call into a message safe to show a user. */
fun Throwable.toUserMessage(fallback: String = "Something went wrong. Please try again."): String =
    when (this) {
        is HttpException -> NetworkClient.parseErrorBody(this) ?: fallback
        is IOException -> "Can't reach the server. Check your connection and try again."
        else -> fallback
    }

/** Throws [HttpException] on a non-2xx response so callers can use [toUserMessage] uniformly. */
fun <T> retrofit2.Response<T>.requireSuccess(): retrofit2.Response<T> {
    if (!isSuccessful) throw HttpException(this)
    return this
}
