import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/store_repository.dart';

final storeRepositoryProvider = Provider<StoreRepository>((ref) {
  return StoreRepository();
});

/// All stores (global list — used by StoresScreen).
final storesProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(storeRepositoryProvider);
  return repository.getStores();
});

/// Stores filtered by a specific app_category_id.
/// Used by CategoryChildrenScreen to show only the restaurants that belong
/// to the tapped App Category row.
final storesByCategoryProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>(
        (ref, appCategoryId) async {
  final repository = ref.read(storeRepositoryProvider);
  return repository.getStoresByCategory(appCategoryId);
});
