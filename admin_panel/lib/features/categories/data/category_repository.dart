import 'package:supabase_flutter/supabase_flutter.dart';

class CategoryRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetches all Main App Categories from the normalized [app_categories] table.
  /// These are platform-wide tiles shown on the Home Screen (Restaurants, Pharmacies, etc.).
  Future<List<Map<String, dynamic>>> getGlobalCategories() async {
    final response = await _client
        .from('app_categories')         // ← normalized table
        .select('*')
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Creates a new App Category. [store_id] no longer exists on this table.
  Future<void> createCategory(Map<String, dynamic> data) async {
    await _client.from('app_categories').insert(data);
  }

  /// Updates an existing App Category by [id].
  Future<void> updateCategory(String id, Map<String, dynamic> data) async {
    await _client.from('app_categories').update(data).eq('id', id);
  }

  /// Permanently deletes an App Category by [id].
  Future<void> deleteCategory(String id) async {
    await _client.from('app_categories').delete().eq('id', id);
  }
}
