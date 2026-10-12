import 'package:supabase_flutter/supabase_flutter.dart';

class OrderRepository {
  final SupabaseClient _client = Supabase.instance.client;

  // ── ORDERS LIST ──────────────────────────────────────────────────────────────

  Future<List<Map<String, dynamic>>> getOrders() async {
    final response = await _client
        .from('orders')
        .select('''
          id, status, total_amount, subtotal, delivery_fee,
          discount_amount, tax_amount, created_at, updated_at,
          delivery_address, special_instructions,
          payment_method, is_paid, prep_time_minutes,
          cancellation_reason, delivery_eta, order_type,
          customer_id, store_id, driver_id, coupon_id,
          customer:users!orders_customer_id_fkey(id, full_name, phone, email),
          store:stores!orders_store_id_fkey(id, name),
          driver:users!orders_driver_id_fkey(id, full_name, phone)
        ''')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  // ── ORDER DETAILS ────────────────────────────────────────────────────────────

  /// Fetch order_items + addons for a specific order.
  /// Does NOT fetch pickup_otp (feature cancelled).
  Future<List<Map<String, dynamic>>> getOrderItems(String orderId) async {
    final response = await _client
        .from('order_items')
        .select('''
          id, quantity, unit_price, total_price,
          product:products!order_items_product_id_fkey(id, name, image_url),
          addons:order_item_addons(
            id, price,
            addon:add_ons!order_item_addons_addon_id_fkey(id, name)
          )
        ''')
        .eq('order_id', orderId);
    return List<Map<String, dynamic>>.from(response);
  }

  /// Fetch order_status_history for a specific order.
  Future<List<Map<String, dynamic>>> getOrderHistory(String orderId) async {
    final response = await _client
        .from('order_status_history')
        .select('id, status, notes, created_at')
        .eq('order_id', orderId)
        .order('created_at', ascending: true);
    return List<Map<String, dynamic>>.from(response);
  }

  // ── STATUS UPDATE ────────────────────────────────────────────────────────────

  /// Updates the order status. The status value is cast to the
  /// order_status enum by Supabase automatically via the typed column.
  Future<void> updateOrderStatus(String orderId, String newStatus) async {
    await _client
        .from('orders')
        .update({'status': newStatus}).eq('id', orderId);
  }
}
