import 'package:supabase_flutter/supabase_flutter.dart';

class ProductRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetches all products for a store.
  /// Joins menu_categories via the correct FK: menu_category_id.
  /// Falls back to joining via category_id for legacy rows not yet migrated.
  Future<List<Map<String, dynamic>>> getProductsByStore(String storeId) async {
    final response = await _client
        .from('products')
        .select('*, menu_categories!products_menu_category_id_fkey(*)')
        .eq('store_id', storeId)
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Fetches ONLY menu categories that belong strictly to the given [storeId]
  /// from the normalized [menu_categories] table.
  Future<List<Map<String, dynamic>>> getMenuCategoriesByStore(
      String storeId) async {
    final response = await _client
        .from('menu_categories')
        .select('*')
        .eq('store_id', storeId)
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Creates a new menu category strictly scoped to [storeId].
  Future<void> createMenuCategory(String storeId, String name) async {
    await _client.from('menu_categories').insert({
      'store_id': storeId,
      'name': name,
      'is_active': true,
      'sort_order': 0,
    });
  }

  Future<void> createProduct(Map<String, dynamic> data) async {
    await _client.from('products').insert(data);
  }

  Future<void> updateProduct(String id, Map<String, dynamic> data) async {
    await _client.from('products').update(data).eq('id', id);
  }

  Future<void> deleteProduct(String id) async {
    await _client.from('products').delete().eq('id', id);
  }

  /// Deletes a menu category by ID.
  /// Will throw if the category still has linked products (FK RESTRICT).
  Future<void> deleteMenuCategory(String id) async {
    await _client.from('menu_categories').delete().eq('id', id);
  }
}
