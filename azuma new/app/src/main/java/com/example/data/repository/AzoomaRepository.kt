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
                val token = response.body()?.supabaseToken
                if (dto != null) {
                    ApiResult.Success(
                        UserSession(
                            id = dto.id,
                            email = dto.email,
                            fullName = dto.fullName ?: "",
                            phone = dto.phone,
                            role = dto.role,
                            avatarUrl = dto.avatarUrl,
                            supabaseToken = token
                        )
                    )
                } else {
                    ApiResult.Error("Invalid server response")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                ApiResult.Error("Auth failed (${response.code()}) $errorBody", response.code())
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
                        deliveryFee = dto.deliveryRadiusKm ?: 5.0,
                        actualCommissionRate = dto.commissionRate ?: 0.0
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

    suspend fun getBanners(): ApiResult<List<AppBanner>> {
        return try {
            val response = api.getBanners()
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()
                ApiResult.Success(dtos.map { dto ->
                    AppBanner(
                        id        = dto.id,
                        imageUrl  = dto.imageUrl,
                        actionUrl = dto.actionUrl,
                        storeId   = dto.storeId
                    )
                })
            } else {
                Log.e(TAG, "getBanners HTTP ${response.code()}")
                ApiResult.Success(emptyList()) // non-fatal: show no banners
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBanners error", e)
            ApiResult.Success(emptyList()) // non-fatal
        }
    }
    
    // ── Orders ────────────────────────────────────────────────────────────────
    
    suspend fun createOrder(request: com.example.data.api.CreateOrderRequest): ApiResult<com.example.data.api.OrderDto> {
        return try {
            val response = api.createOrder(request)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    ApiResult.Success(data)
                } else {
                    ApiResult.Error("Order created but no data returned")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                ApiResult.Error("Order creation failed (${response.code()}) $errorBody", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "createOrder error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    // ── User Profile ─────────────────────────────────────────────────────────

    suspend fun getMe(): ApiResult<UserSession> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    ApiResult.Success(
                        UserSession(
                            id = dto.id,
                            email = dto.email,
                            fullName = dto.fullName ?: "",
                            phone = dto.phone,
                            role = dto.role,
                            avatarUrl = dto.avatarUrl,
                            supabaseToken = null // token unchanged
                        )
                    )
                } else {
                    ApiResult.Error("No data in response")
                }
            } else {
                ApiResult.Error("Fetch profile failed (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMe error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        email: String? = null
    ): ApiResult<UserSession> {
        return try {
            val response = api.updateProfile(
                com.example.data.api.UpdateProfileRequest(fullName = fullName, phone = phone, email = email)
            )
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    ApiResult.Success(
                        UserSession(
                            id = dto.id,
                            email = dto.email,
                            fullName = dto.fullName ?: "",
                            phone = dto.phone,
                            role = dto.role,
                            avatarUrl = dto.avatarUrl,
                            supabaseToken = null // token unchanged
                        )
                    )
                } else {
                    ApiResult.Error("No data in response")
                }
            } else {
                ApiResult.Error("Profile update failed (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateProfile error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }
    // ── Addresses ────────────────────────────────────────────────────────────

    suspend fun getAddresses(): ApiResult<List<com.example.model.Address>> {
        return try {
            val response = api.getAddresses()
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()
                ApiResult.Success(dtos.map { dto ->
                    com.example.model.Address(
                        id = dto.id,
                        name = dto.title,
                        details = dto.fullAddress + (if (dto.buildingDetails != null) ", ${dto.buildingDetails}" else ""),
                        isDefault = dto.isDefault,
                        lat = dto.latitude,
                        lng = dto.longitude
                    )
                })
            } else {
                ApiResult.Error("Failed to fetch addresses (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAddresses error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun createAddress(request: com.example.data.api.CreateAddressRequest): ApiResult<com.example.model.Address> {
        return try {
            val response = api.createAddress(request)
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    ApiResult.Success(
                        com.example.model.Address(
                            id = dto.id,
                            name = dto.title,
                            details = dto.fullAddress + (if (dto.buildingDetails != null) ", ${dto.buildingDetails}" else ""),
                            isDefault = dto.isDefault,
                            lat = dto.latitude,
                            lng = dto.longitude
                        )
                    )
                } else {
                    ApiResult.Error("Address created but no data returned")
                }
            } else {
                ApiResult.Error("Failed to create address (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "createAddress error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteAddress(id: String): ApiResult<Unit> {
        return try {
            val response = api.deleteAddress(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete address (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteAddress error", e)
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}
