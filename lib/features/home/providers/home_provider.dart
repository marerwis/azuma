import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/home_repository.dart';

final homeRepositoryProvider = Provider<HomeRepository>((ref) {
  return HomeRepository();
});

final bannersProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(homeRepositoryProvider);
  return repository.getBanners();
});

final categoriesProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(homeRepositoryProvider);
  return repository.getCategories();
});

final topStoresProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(homeRepositoryProvider);
  return repository.getTopStores();
});

final offersProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(homeRepositoryProvider);
  return repository.getOffers();
});
