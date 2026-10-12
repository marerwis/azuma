import 'package:supabase_flutter/supabase_flutter.dart';

class ReportRepository {
  final SupabaseClient _client = Supabase.instance.client;

  /// Fetch aggregated sales stats for the given date range.
  /// Returns a list of rows: { date, total_orders, completed, cancelled, revenue }
  Future<Map<String, dynamic>> getSalesStats({
    required DateTime from,
    required DateTime to,
  }) async {
    final fromStr = from.toIso8601String();
    final toStr = to.toIso8601String();

    // Total orders in range
    final allOrders = await _client
        .from('orders')
        .select('id, status, total_amount, created_at')
        .gte('created_at', fromStr)
        .lte('created_at', toStr);

    final orders = List<Map<String, dynamic>>.from(allOrders);

    final completed =
        orders.where((o) => o['status'] == 'delivered').toList();
    final cancelled = orders
        .where((o) =>
            o['status'] == 'cancelled' || o['status'] == 'rejected_by_vendor')
        .toList();
    final pending = orders
        .where((o) =>
            o['status'] == 'pending' || o['status'] == 'preparing')
        .toList();

    final totalRevenue = completed.fold<double>(
        0, (sum, o) => sum + ((o['total_amount'] as num?)?.toDouble() ?? 0));

    return {
      'total': orders.length,
      'completed': completed.length,
      'cancelled': cancelled.length,
      'pending': pending.length,
      'revenue': totalRevenue,
      'orders': orders,
    };
  }

  /// Fetch top-performing stores by completed order count in range.
  Future<List<Map<String, dynamic>>> getTopStores({
    required DateTime from,
    required DateTime to,
  }) async {
    final fromStr = from.toIso8601String();
    final toStr = to.toIso8601String();

    final response = await _client
        .from('orders')
        .select('store_id, store:stores(name), total_amount, status')
        .eq('status', 'delivered')
        .gte('created_at', fromStr)
        .lte('created_at', toStr);

    final rows = List<Map<String, dynamic>>.from(response);

    // Group by store_id client-side (Supabase FutureProvider doesn't support GROUP BY)
    final Map<String, Map<String, dynamic>> grouped = {};
    for (final row in rows) {
      final storeId = row['store_id']?.toString() ?? '';
      final storeName =
          (row['store'] as Map?)?['name'] ?? 'مجهول';
      final amount = (row['total_amount'] as num?)?.toDouble() ?? 0;
      if (grouped.containsKey(storeId)) {
        grouped[storeId]!['orders'] = (grouped[storeId]!['orders'] as int) + 1;
        grouped[storeId]!['revenue'] =
            (grouped[storeId]!['revenue'] as double) + amount;
      } else {
        grouped[storeId] = {
          'store_id': storeId,
          'name': storeName,
          'orders': 1,
          'revenue': amount,
        };
      }
    }

    final result = grouped.values.toList()
      ..sort((a, b) =>
          (b['orders'] as int).compareTo(a['orders'] as int));
    return result.take(10).toList();
  }
}
