import 'package:supabase_flutter/supabase_flutter.dart';

class CouponRepository {
  final SupabaseClient _client = Supabase.instance.client;

  // ── READ ─────────────────────────────────────────────────────────────────────

  Future<List<Map<String, dynamic>>> getCoupons() async {
    final r = await _client
        .from('coupons')
        .select('''
          id, code, title, description,
          discount_type, discount_value,
          min_order_amount, max_discount_amount,
          valid_from, valid_until,
          usage_limit, used_count,
          is_active, created_at
        ''')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(r);
  }

  // ── WRITE ─────────────────────────────────────────────────────────────────────

  Future<void> createCoupon({
    required String code,
    required String title,
    String? description,
    required String discountType,   // 'percentage' | 'fixed'
    required double discountValue,
    double? minOrderAmount,
    double? maxDiscountAmount,
    DateTime? validFrom,
    DateTime? validUntil,
    int? usageLimit,
    required bool isActive,
  }) async {
    await _client.from('coupons').insert({
      'code': code.toUpperCase().trim(),
      'title': title.trim(),
      'description': description?.trim(),
      'discount_type': discountType,
      'discount_value': discountValue,
      'min_order_amount': minOrderAmount,
      'max_discount_amount': maxDiscountAmount,
      'valid_from': validFrom?.toIso8601String(),
      'valid_until': validUntil?.toIso8601String(),
      'usage_limit': usageLimit,
      'is_active': isActive,
    });
  }

  Future<void> updateCoupon(String id, {
    required String code,
    required String title,
    String? description,
    required String discountType,
    required double discountValue,
    double? minOrderAmount,
    double? maxDiscountAmount,
    DateTime? validFrom,
    DateTime? validUntil,
    int? usageLimit,
    required bool isActive,
  }) async {
    await _client.from('coupons').update({
      'code': code.toUpperCase().trim(),
      'title': title.trim(),
      'description': description?.trim(),
      'discount_type': discountType,
      'discount_value': discountValue,
      'min_order_amount': minOrderAmount,
      'max_discount_amount': maxDiscountAmount,
      'valid_from': validFrom?.toIso8601String(),
      'valid_until': validUntil?.toIso8601String(),
      'usage_limit': usageLimit,
      'is_active': isActive,
    }).eq('id', id);
  }

  Future<void> toggleActive(String id, bool isActive) async {
    await _client.from('coupons').update({'is_active': isActive}).eq('id', id);
  }

  Future<void> deleteCoupon(String id) async {
    await _client.from('coupons').delete().eq('id', id);
  }
}
