package com.example.data.repository

import android.util.Log
import com.example.data.api.AzoomaApiService
import com.example.data.api.VerifyRequest
import com.example.model.AppBanner
import com.example.model.AppCategory
import com.example.model.MenuCategoryWithProducts
import com.example.model.MenuItem
import com.example.model.Store
import com.example.model.UserSession

private const val TAG = "AzoomaRepository"

// ── Result wrapper ────────────────────────────────────────────────────────────
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

class AzoomaRepository(private val api: AzoomaApiService) {

    // ── Auth ─────────────────────────────────────────────────────────────────

    suspend fun verifyFirebaseToken(
        idToken: String,
        fcmToken: String? = null,
        fullName: String? = null
    ): ApiResult<UserSession> {
        return try {
            val response = api.verifyToken(VerifyRequest(idToken, fcmToken, fullName))
            if (response.isSuccessful) {
                val dto = response.body()?.user
                if (dto != null) {
                    ApiResult.Success(
                        UserSession(
                            id = dto.id,
                            email = dto.email,
                            fullName = dto.fullName ?: "",
                            phone = dto.phone,
                            role = dto.role,
                            avatarUrl = dto.avatarUrl
                        )
                    )
                } else {
                    ApiResult.Error("Invalid server response")
                }
            } else {
                ApiResult.Error("Auth failed (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyFirebaseToken error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // ── Categories ────────────────────────────────────────────────────────────

    suspend fun getCategories(): ApiResult<List<AppCategory>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()
                ApiResult.Success(dtos.map { dto ->
                    AppCategory(
                        id = dto.id,
                        name = dto.name,
                        imageUrl = dto.imageUrl ?: "",
                        isActive = dto.isActive,
                        sortOrder = dto.sortOrder
                    )
                })
            } else {
                ApiResult.Error("Failed to fetch categories (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // ── Stores ────────────────────────────────────────────────────────────────

    suspend fun getStores(categoryId: String? = null, isOpen: Boolean? = null): ApiResult<List<Store>> {
        return try {
            val response = api.getStores(categoryId, isOpen)
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()
                ApiResult.Success(dtos.map { dto ->
                    Store(
                        id = dto.id,
                        name = dto.name,
                        // categoryId field in Store maps to vendor_id column — we repurpose it
                        // to hold the app_category name for display
                        categoryId = dto.appCategory?.name ?: "restaurants",
                        isOpen = dto.isOpen,
                        isFeatured = dto.isActive,
                        address = dto.description ?: dto.address ?: "بنغازي",
                        logoText = dto.imageUrl ?: "",
                        // Compute delivery fee / time from API data if available
                        deliveryFee = dto.deliveryRadiusKm ?: 5.0
                    )
                })
            } else {
                ApiResult.Error("Failed to fetch stores (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getStores error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // ── Store Menu ────────────────────────────────────────────────────────────

    suspend fun getStoreMenu(storeId: String): ApiResult<List<MenuCategoryWithProducts>> {
        return try {
            // 1. Menu categories
            val catResp = api.getMenuCategories(storeId)
            if (!catResp.isSuccessful) {
                return ApiResult.Error("Menu fetch failed (${catResp.code()})", catResp.code())
            }
            val menuCats = catResp.body()?.data ?: emptyList()

            // 2. All products for this store
            val prodResp = api.getProducts(storeId)
            if (!prodResp.isSuccessful) {
                return ApiResult.Error("Products fetch failed (${prodResp.code()})", prodResp.code())
            }
            val allProducts = prodResp.body()?.data ?: emptyList()

            // 3. Group products under their categories
            val menu = menuCats.map { cat ->
                val products = allProducts
                    .filter { it.menuCategoryId == cat.id }
                    .map { p ->
                        MenuItem(
                            id = p.id,
                            storeId = storeId,
                            name = p.name,
                            description = p.description ?: "",
                            price = p.basePrice,
                            imageUrl = p.imageUrl ?: "",
                            menuCategoryId = p.menuCategoryId
                        )
                    }
                MenuCategoryWithProducts(
                    id = cat.id,
                    storeId = storeId,
                    name = cat.name,
                    sortOrder = cat.sortOrder,
                    products = products
                )
            }
            ApiResult.Success(menu)
        } catch (e: Exception) {
            Log.e(TAG, "getStoreMenu error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // ── Banners ───────────────────────────────────────────────────────────────
    // Banners endpoint not yet built — return empty until added
    suspend fun getBanners(): ApiResult<List<AppBanner>> = ApiResult.Success(emptyList())
}
