import 'package:supabase_flutter/supabase_flutter.dart';

class UserRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getUsers() async {
    final response = await _client
        .from('users')
        .select('*')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<void> updateUserData(String id, Map<String, dynamic> data) async {
    data['updated_at'] = DateTime.now().toIso8601String();
    await _client.from('users').update(data).eq('id', id);
  }
}
