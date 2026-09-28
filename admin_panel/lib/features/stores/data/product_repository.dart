import 'package:supabase_flutter/supabase_flutter.dart';

class ProductRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getProductsByStore(String storeId) async {
    final response = await _client
        .from('products')
        .select('*, categories(*)')
        .eq('store_id', storeId)
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  // REMOVED: getCategories() was fetching all rows with no filter — replaced by getMenuCategoriesByStore.

  /// Fetches ONLY menu categories that belong strictly to the given [storeId]
  /// from the normalized [menu_categories] table.
  Future<List<Map<String, dynamic>>> getMenuCategoriesByStore(String storeId) async {
    final response = await _client
        .from('menu_categories')         // ← normalized table
        .select('*')
        .eq('store_id', storeId)
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Creates a new menu category strictly scoped to [storeId].
  /// [image_url] is intentionally omitted — menu_categories has no image column.
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
}
