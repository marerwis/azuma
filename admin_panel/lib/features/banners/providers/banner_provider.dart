import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/banner_repository.dart';

final bannerRepositoryProvider = Provider<BannerRepository>((ref) {
  return BannerRepository();
});

final bannersProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(bannerRepositoryProvider);
  return repository.getBanners();
});
