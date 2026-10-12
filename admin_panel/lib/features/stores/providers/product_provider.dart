import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/product_repository.dart';

final productRepositoryProvider = Provider<ProductRepository>((ref) {
  return ProductRepository();
});

// ── Catalogue list (all products, admin view) ──────────────────────────────────

final allProductsProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(productRepositoryProvider).getAllProducts();
});

// ── Store-scoped products (used by store_menu_screen) ────────────────────────

final storeProductsProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>(
        (ref, storeId) async {
  return ref.read(productRepositoryProvider).getProductsByStore(storeId);
});

// ── Store-scoped menu categories (used by store_menu_screen) ─────────────────

final storeMenuCategoriesProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>(
        (ref, storeId) async {
  return ref
      .read(productRepositoryProvider)
      .getMenuCategoriesByStore(storeId);
});

// ── Form support providers ────────────────────────────────────────────────────

final storesListProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(productRepositoryProvider).getAllStores();
});

final appCategoriesProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(productRepositoryProvider).getAppCategories();
});

/// Menu categories for a specific store — used in the product form.
final menuCategoriesForStoreProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>((ref, storeId) async {
  if (storeId.isEmpty) return [];
  return ref.read(productRepositoryProvider).getMenuCategoriesByStore(storeId);
});
