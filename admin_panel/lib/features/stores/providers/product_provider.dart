import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/product_repository.dart';

final productRepositoryProvider = Provider<ProductRepository>((ref) {
  return ProductRepository();
});

final storeProductsProvider = FutureProvider.family<List<Map<String, dynamic>>, String>((ref, storeId) async {
  final repository = ref.read(productRepositoryProvider);
  return repository.getProductsByStore(storeId);
});

final allCategoriesProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(productRepositoryProvider);
  return repository.getCategories();
});
