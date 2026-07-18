package com.example.network

import android.content.Context
import com.example.data.DataStoreManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val name: String? = null,
    val password: String? = null,
    val currency: String? = "USD"
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val name: String,
    val email: String,
    val currency: String
)

@JsonClass(generateAdapter = true)
data class Wallet(
    val id: String,
    val name: String,
    val balance: Double,
    val currency: String,
    val type: String
)

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // INCOME or EXPENSE
    val date: String,
    val walletId: String
)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    val wallets: List<Wallet>,
    val transactions: List<Transaction>
)

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: LoginRequest): AuthResponse

    @GET("api/dashboard")
    suspend fun getDashboardData(): DashboardResponse
}

object NetworkClient {
    private const val BASE_URL = "https://finance-api.example.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun getApiService(context: Context, dataStoreManager: DataStoreManager): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { dataStoreManager.tokenFlow.firstOrNull() }
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        // Beautiful Mock Response Fallback Interceptor
        val mockInterceptor = Interceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath

            try {
                chain.proceed(request)
            } catch (e: Exception) {
                val jsonResponse = when {
                    path.endsWith("api/auth/login") || path.endsWith("api/auth/register") -> {
                        """
                        {
                            "token": "mock_jwt_token_for_user_auth_linear_aesthetic",
                            "name": "Jane Doe",
                            "email": "jane.doe@example.com",
                            "currency": "USD"
                        }
                        """.trimIndent()
                    }
                    path.endsWith("api/dashboard") -> {
                        """
                        {
                            "wallets": [
                                { "id": "w1", "name": "Main Checking", "balance": 12840.50, "currency": "USD", "type": "Cash" },
                                { "id": "w2", "name": "Investment Account", "balance": 45120.00, "currency": "USD", "type": "Investment" },
                                { "id": "w3", "name": "Business Ledger", "balance": 3400.00, "currency": "USD", "type": "Bank" }
                            ],
                            "transactions": [
                                { "id": "t1", "title": "SaaS Payout", "amount": 4200.00, "category": "Revenue", "type": "INCOME", "date": "2026-07-18", "walletId": "w1" },
                                { "id": "t2", "title": "Equinox Membership", "amount": -180.00, "category": "Wellness", "type": "EXPENSE", "date": "2026-07-17", "walletId": "w1" },
                                { "id": "t3", "title": "Google Cloud Platform", "amount": -340.50, "category": "DevOps", "type": "EXPENSE", "date": "2026-07-16", "walletId": "w1" },
                                { "id": "t4", "title": "Stock Dividends", "amount": 125.00, "category": "Investing", "type": "INCOME", "date": "2026-07-15", "walletId": "w2" },
                                { "id": "t5", "title": "Intelligentsia Espresso", "amount": -6.50, "category": "Food", "type": "EXPENSE", "date": "2026-07-15", "walletId": "w1" }
                            ]
                        }
                        """.trimIndent()
                    }
                    else -> "{}"
                }

                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_2)
                    .code(200)
                    .message("OK (Mocked)")
                    .body(jsonResponse.toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(mockInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
