import 'package:supabase_flutter/supabase_flutter.dart';

class StoreRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getStores() async {
    final response = await _client
        .from('stores')
        .select('*')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Fetches only stores that belong to a specific [appCategoryId].
  Future<List<Map<String, dynamic>>> getStoresByCategory(
      String appCategoryId) async {
    final response = await _client
        .from('stores')
        .select('*')
        .eq('app_category_id', appCategoryId)
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<void> createStore(Map<String, dynamic> data) async {
    await _client.from('stores').insert(data);
  }

  Future<void> updateStore(String id, Map<String, dynamic> data) async {
    await _client.from('stores').update(data).eq('id', id);
  }

  Future<void> deleteStore(String id) async {
    await _client.from('stores').delete().eq('id', id);
  }
}
