import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/review_repository.dart';

final reviewRepositoryProvider = Provider<ReviewRepository>((ref) {
  return ReviewRepository();
});

/// Filter state: min rating, max rating, store ID.
class ReviewFilter {
  final int? minRating;
  final int? maxRating;
  final String? storeId;

  const ReviewFilter({this.minRating, this.maxRating, this.storeId});

  ReviewFilter copyWith({
    Object? minRating = _sentinel,
    Object? maxRating = _sentinel,
    Object? storeId = _sentinel,
  }) {
    return ReviewFilter(
      minRating: minRating == _sentinel ? this.minRating : minRating as int?,
      maxRating: maxRating == _sentinel ? this.maxRating : maxRating as int?,
      storeId: storeId == _sentinel ? this.storeId : storeId as String?,
    );
  }
}

const _sentinel = Object();

final reviewFilterProvider =
    StateProvider<ReviewFilter>((ref) => const ReviewFilter());

/// Reviews list, reactive to filter changes.
final reviewsProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final filter = ref.watch(reviewFilterProvider);
  return ref.read(reviewRepositoryProvider).getReviews(
        minRating: filter.minRating,
        maxRating: filter.maxRating,
        storeId: filter.storeId,
      );
});

/// Stores list for filter dropdown.
final reviewStoresProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(reviewRepositoryProvider).getStoresList();
});
