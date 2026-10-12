import 'package:supabase_flutter/supabase_flutter.dart';

class ReviewRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch all reviews with joined order, customer, and store data.
  Future<List<Map<String, dynamic>>> getReviews({
    int? minRating,
    int? maxRating,
    String? storeId,
  }) async {
    var query = _client
        .from('reviews')
        .select('*, customer:users!customer_id(full_name, phone), store:stores!store_id(name), order:orders!order_id(id)');

    if (minRating != null) query = query.gte('rating', minRating);
    if (maxRating != null) query = query.lte('rating', maxRating);
    if (storeId != null) query = query.eq('store_id', storeId);

    final response = await query.order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Delete a review (admin moderation).
  Future<void> deleteReview(String reviewId) async {
    await _client.from('reviews').delete().eq('id', reviewId);
  }

  /// Fetch stores list for the filter dropdown.
  Future<List<Map<String, dynamic>>> getStoresList() async {
    final response = await _client
        .from('stores')
        .select('id, name')
        .order('name', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }
}
