import 'package:supabase_flutter/supabase_flutter.dart';

class ProductRepository {
  final SupabaseClient _client = Supabase.instance.client;

  // ── READ ────────────────────────────────────────────────────────────────────

  /// All products across all stores — for the admin catalogue view.
  /// Joins: store name, menu_category name, app_category name.
  Future<List<Map<String, dynamic>>> getAllProducts() async {
    final response = await _client
        .from('products')
        .select('''
          id, name, description, base_price, stock_quantity,
          is_active, is_veg, image_url, created_at, updated_at,
          store_id, category_id, menu_category_id,
          store:stores!products_store_id_fkey(id, name),
          menu_cat:menu_categories!products_menu_category_id_fkey(id, name),
          app_cat:app_categories!products_category_id_fkey(id, name)
        ''')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Products filtered by store.
  Future<List<Map<String, dynamic>>> getProductsByStore(String storeId) async {
    final response = await _client
        .from('products')
        .select('''
          id, name, description, base_price, stock_quantity,
          is_active, is_veg, image_url, created_at, updated_at,
          store_id, category_id, menu_category_id,
          menu_cat:menu_categories!products_menu_category_id_fkey(id, name),
          app_cat:app_categories!products_category_id_fkey(id, name)
        ''')
        .eq('store_id', storeId)
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  // ── Lookups for form dropdowns ───────────────────────────────────────────────

  Future<List<Map<String, dynamic>>> getAllStores() async {
    final r = await _client
        .from('stores')
        .select('id, name')
        .order('name', ascending: true);
    return List<Map<String, dynamic>>.from(r);
  }

  Future<List<Map<String, dynamic>>> getMenuCategoriesByStore(
      String storeId) async {
    final r = await _client
        .from('menu_categories')
        .select('id, name')
        .eq('store_id', storeId)
        .eq('is_active', true)
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(r);
  }

  Future<List<Map<String, dynamic>>> getAppCategories() async {
    final r = await _client
        .from('app_categories')
        .select('id, name')
        .eq('is_active', true)
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(r);
  }

  // ── WRITE ───────────────────────────────────────────────────────────────────

  /// Maps exactly to the `products` schema columns.
  Future<void> createProduct({
    required String storeId,
    required String? categoryId,       // app_categories FK
    required String? menuCategoryId,   // menu_categories FK
    required String name,
    required String? description,
    required double basePrice,
    required int stockQuantity,
    required bool isVeg,
    required bool isActive,
    required String? imageUrl,
  }) async {
    await _client.from('products').insert({
      'store_id': storeId,
      'category_id': categoryId,
      'menu_category_id': menuCategoryId,
      'name': name,
      'description': description,
      'base_price': basePrice,
      'stock_quantity': stockQuantity,
      'is_veg': isVeg,
      'is_active': isActive,
      'image_url': imageUrl,
    });
  }

  Future<void> updateProduct(
      String id, {
      required String storeId,
      required String? categoryId,
      required String? menuCategoryId,
      required String name,
      required String? description,
      required double basePrice,
      required int stockQuantity,
      required bool isVeg,
      required bool isActive,
      required String? imageUrl,
  }) async {
    await _client.from('products').update({
      'store_id': storeId,
      'category_id': categoryId,
      'menu_category_id': menuCategoryId,
      'name': name,
      'description': description,
      'base_price': basePrice,
      'stock_quantity': stockQuantity,
      'is_veg': isVeg,
      'is_active': isActive,
      'image_url': imageUrl,
    }).eq('id', id);
  }

  Future<void> toggleActive(String id, bool isActive) async {
    await _client
        .from('products')
        .update({'is_active': isActive}).eq('id', id);
  }

  Future<void> deleteProduct(String id) async {
    await _client.from('products').delete().eq('id', id);
  }

  // ── Menu Category helpers (kept for store_menu_screen) ───────────────────────

  Future<void> createMenuCategory(String storeId, String name) async {
    await _client.from('menu_categories').insert({
      'store_id': storeId,
      'name': name,
      'is_active': true,
      'sort_order': 0,
    });
  }

  Future<void> deleteMenuCategory(String id) async {
    await _client.from('menu_categories').delete().eq('id', id);
  }
}
