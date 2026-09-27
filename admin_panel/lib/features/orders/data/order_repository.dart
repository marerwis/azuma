import 'package:supabase_flutter/supabase_flutter.dart';

class OrderRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getOrders() async {
    final response = await _client
        .from('orders')
        .select('*, customer:users(*), store:stores(*)')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<void> updateOrderStatus(String orderId, String newStatus) async {
    await _client.from('orders').update({'status': newStatus}).eq('id', orderId);
  }
}
