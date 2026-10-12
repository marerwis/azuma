import 'package:supabase_flutter/supabase_flutter.dart';

class CustomerRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch all users with role = 'customer'
  Future<List<Map<String, dynamic>>> getCustomers() async {
    final response = await _client
        .from('users')
        .select('id, full_name, email, phone, is_active, created_at, profile_image_url')
        .eq('role', 'customer')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Toggle customer active status
  Future<void> toggleActive(String userId, bool isActive) async {
    await _client
        .from('users')
        .update({'is_active': isActive}).eq('id', userId);
  }
}
