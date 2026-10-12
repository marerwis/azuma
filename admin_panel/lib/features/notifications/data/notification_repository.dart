import 'package:supabase_flutter/supabase_flutter.dart';

class NotificationRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch all sent notifications history, newest first.
  Future<List<Map<String, dynamic>>> getNotifications() async {
    final response = await _client
        .from('notifications')
        .select('*, sender:users!sent_by(full_name)')
        .order('sent_at', ascending: false)
        .limit(200);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Save a notification record to the history table.
  Future<void> saveNotification({
    required String title,
    required String body,
    required String targetType, // 'all' | 'customer' | 'driver'
    String? targetUserId,
    required String sentBy,
  }) async {
    await _client.from('notifications').insert({
      'title': title,
      'body': body,
      'target_type': targetType,
      'target_user_id': targetUserId,
      'sent_by': sentBy,
      'sent_at': DateTime.now().toIso8601String(),
      'status': 'sent',
    });
  }

  /// Fetch all customers (role='customer') for the target selector.
  Future<List<Map<String, dynamic>>> getCustomers() async {
    final response = await _client
        .from('users')
        .select('id, full_name, phone, email')
        .eq('role', 'customer')
        .eq('is_active', true)
        .order('full_name', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Fetch all drivers (role='driver') for the target selector.
  Future<List<Map<String, dynamic>>> getDrivers() async {
    final response = await _client
        .from('users')
        .select('id, full_name, phone, email')
        .eq('role', 'driver')
        .eq('is_active', true)
        .order('full_name', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }
}
