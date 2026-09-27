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

  Future<void> updateUserRole(String id, String newRole) async {
    await _client.from('users').update({
      'role': newRole,
      'updated_at': DateTime.now().toIso8601String(),
    }).eq('id', id);
  }
}
