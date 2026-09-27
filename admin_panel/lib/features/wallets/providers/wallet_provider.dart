import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/wallet_repository.dart';

final walletRepositoryProvider = Provider<WalletRepository>((ref) {
  return WalletRepository();
});

final walletsProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(walletRepositoryProvider);
  return repository.getWallets();
});
