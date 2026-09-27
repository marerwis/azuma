import 'package:supabase_flutter/supabase_flutter.dart';

class HomeRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getBanners() async {
    final response = await _client
        .from('banners')
        .select('*')
        .eq('is_active', true)
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<List<Map<String, dynamic>>> getCategories() async {
    final response = await _client
        .from('categories')
        .select('*')
        .eq('is_active', true)
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<List<Map<String, dynamic>>> getTopStores() async {
    final response = await _client
        .from('stores')
        .select('*')
        .eq('is_active', true)
        .limit(4);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<List<Map<String, dynamic>>> getOffers() async {
    // Assuming we fetch stores that are active and we limit to 3 for offers for now.
    final response = await _client
        .from('stores')
        .select('*')
        .eq('is_active', true)
        .limit(3);
    return List<Map<String, dynamic>>.from(response);
  }
}
