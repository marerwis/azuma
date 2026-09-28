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
    
    // If we're updating the role, we MUST use the secure RPC to sync auth.users metadata
    if (data.containsKey('role')) {
      final newRole = data['role'];
      await _client.rpc('set_user_role', params: {
        'target_user_id': id,
        'new_role': newRole,
      });
      // Remove role from data so normal update doesn't overwrite it redundantly
      data.remove('role'); 
    }
    
    // Update the rest of the profile if there's anything left
    if (data.isNotEmpty && data.length > 1) { // >1 because updated_at is there
      await _client.from('users').update(data).eq('id', id);
    }
  }

  Future<void> createUser({
    required String email,
    required String password,
    required String fullName,
    required String phone,
    required String role,
  }) async {
    // Uses the secure RPC function to create Auth and Profile records seamlessly
    await _client.rpc('admin_create_user', params: {
      'p_email': email,
      'p_password': password,
      'p_full_name': fullName,
      'p_phone': phone,
      'p_role': role,
    });
  }
}
