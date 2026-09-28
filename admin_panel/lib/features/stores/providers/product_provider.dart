import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/product_repository.dart';

final productRepositoryProvider = Provider<ProductRepository>((ref) {
  return ProductRepository();
});

/// Provider for products belonging to a specific store.
final storeProductsProvider = FutureProvider.family<List<Map<String, dynamic>>, String>((ref, storeId) async {
  final repository = ref.read(productRepositoryProvider);
  return repository.getProductsByStore(storeId);
});

/// Store-scoped menu categories provider.
/// Strictly fetches from [menu_categories] where store_id = [storeId].
/// Completely isolated from [app_categories] (Home Screen categories).
final storeMenuCategoriesProvider = FutureProvider.family<List<Map<String, dynamic>>, String>((ref, storeId) async {
  final repository = ref.read(productRepositoryProvider);
  return repository.getMenuCategoriesByStore(storeId);
});

