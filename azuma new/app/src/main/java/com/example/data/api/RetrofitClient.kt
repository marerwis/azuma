package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ────────────────────────────────────────────────────────────────────────────
// Base URL — swap to your production URL before release
// ────────────────────────────────────────────────────────────────────────────
// For Android Emulator: 10.0.2.2 maps to host machine localhost
// Using Production Render URL for physical device testing
private const val BASE_URL = "https://azuma.onrender.com/"

// ── Retrofit Service Interface ──────────────────────────────────────────────
interface AzoomaApiService {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("api/v1/auth/verify")
    suspend fun verifyToken(@Body request: VerifyRequest): Response<VerifyResponse>

    // ── Categories ────────────────────────────────────────────────────────
    @GET("api/v1/categories")
    suspend fun getCategories(): Response<ApiResponse<List<AppCategoryDto>>>

    @GET("api/v1/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: String): Response<ApiResponse<AppCategoryDto>>

    // ── Stores ────────────────────────────────────────────────────────────
    @GET("api/v1/stores")
    suspend fun getStores(
        @Query("categoryId") categoryId: String? = null,
        @Query("isOpen") isOpen: Boolean? = null
    ): Response<ApiResponse<List<StoreDto>>>

    @GET("api/v1/stores/{id}")
    suspend fun getStoreById(@Path("id") id: String): Response<ApiResponse<StoreDto>>

    @GET("api/v1/stores/{id}/menu-categories")
    suspend fun getMenuCategories(
        @Path("id") storeId: String
    ): Response<ApiResponse<List<MenuCategoryDto>>>

    @GET("api/v1/stores/{id}/products")
    suspend fun getProducts(
        @Path("id") storeId: String,
        @Query("menuCategoryId") menuCategoryId: String? = null
    ): Response<ApiResponse<List<ProductDto>>>

    // ── Banners ───────────────────────────────────────────────────────
    @GET("api/v1/banners")
    suspend fun getBanners(): Response<ApiResponse<List<BannerDto>>>

    // ── Orders ────────────────────────────────────────────────────────
    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderDto>>

    // ── User Profile ──────────────────────────────────────────────────
    @GET("api/v1/users/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>

    @PATCH("api/v1/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>

    // ── Addresses ─────────────────────────────────────────────────────
    @GET("api/v1/addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<AddressDto>>>

    @POST("api/v1/addresses")
    suspend fun createAddress(@Body request: CreateAddressRequest): Response<ApiResponse<AddressDto>>

    @DELETE("api/v1/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<ApiResponse<Unit>>
}

// ── Auth Interceptor — injects Bearer token on every request ────────────────
class AuthInterceptor(private val tokenProvider: () -> String?) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val token = tokenProvider()
        
        android.util.Log.d("AuthInterceptor", "Token: ${token?.take(15)}")
        
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

// ── Retrofit Singleton ──────────────────────────────────────────────────────
object RetrofitClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Call this once at app start with a lambda that returns the current Firebase ID token
    fun create(tokenProvider: () -> String?): AzoomaApiService {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AzoomaApiService::class.java)
    }
}
