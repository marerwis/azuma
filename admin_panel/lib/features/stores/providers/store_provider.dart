import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/store_repository.dart';

final storeRepositoryProvider = Provider<StoreRepository>((ref) {
  return StoreRepository();
});

final storesProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(storeRepositoryProvider);
  return repository.getStores();
});
