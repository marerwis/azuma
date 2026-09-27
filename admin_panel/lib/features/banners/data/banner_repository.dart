import 'package:supabase_flutter/supabase_flutter.dart';

class BannerRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getBanners() async {
    final response = await _client
        .from('banners')
        .select('*')
        .order('sort_order', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<void> createBanner(Map<String, dynamic> data) async {
    await _client.from('banners').insert(data);
  }

  Future<void> updateBanner(String id, Map<String, dynamic> data) async {
    await _client.from('banners').update(data).eq('id', id);
  }

  Future<void> deleteBanner(String id) async {
    await _client.from('banners').delete().eq('id', id);
  }
}
