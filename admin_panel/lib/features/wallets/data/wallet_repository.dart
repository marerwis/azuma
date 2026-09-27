import 'package:supabase_flutter/supabase_flutter.dart';

class WalletRepository {
  final SupabaseClient _client = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getWallets() async {
    // Fetch wallets and join user details
    final response = await _client
        .from('wallets')
        .select('*, user:users(full_name, phone, email, role)')
        .order('updated_at', ascending: false);
    return List<Map<String, dynamic>>.from(response);
  }

  Future<void> adjustBalance({
    required String walletId,
    required double currentBalance,
    required double amount,
    required bool isAddition,
    required String description,
  }) async {
    final double newBalance = isAddition ? (currentBalance + amount) : (currentBalance - amount);
    
    // Update the wallet balance
    await _client.from('wallets').update({
      'balance': newBalance,
      'updated_at': DateTime.now().toIso8601String(),
    }).eq('id', walletId);

    // Insert a transaction record
    await _client.from('transactions').insert({
      'wallet_id': walletId,
      'type': isAddition ? 'recharge' : 'withdrawal',
      'amount': amount,
      'description': description,
    });
  }
}
