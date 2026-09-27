import 'package:supabase_flutter/supabase_flutter.dart';

class CategoryRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getGlobalCategories() async {
    final response = await _client
        .from('categories')
        .select('*')
        .is_('store_id', null) // Only global categories
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<void> createCategory(Map<String, dynamic> data) async {
    await _client.from('categories').insert(data);
  }

  Future<void> updateCategory(String id, Map<String, dynamic> data) async {
    await _client.from('categories').update(data).eq('id', id);
  }

  Future<void> deleteCategory(String id) async {
    await _client.from('categories').delete().eq('id', id);
  }
}
