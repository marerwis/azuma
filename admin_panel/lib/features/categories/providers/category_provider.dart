import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/category_repository.dart';

final categoryRepositoryProvider = Provider<CategoryRepository>((ref) {
  return CategoryRepository();
});

final categoriesProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(categoryRepositoryProvider);
  return repository.getGlobalCategories();
});
