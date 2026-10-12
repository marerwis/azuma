import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/report_repository.dart';

final reportRepositoryProvider = Provider<ReportRepository>((ref) {
  return ReportRepository();
});

/// Holds the currently selected date range for the reports page.
class DateRangeState {
  final DateTime from;
  final DateTime to;
  const DateRangeState({required this.from, required this.to});
}

class DateRangeNotifier extends Notifier<DateRangeState> {
  @override
  DateRangeState build() {
    // Default: last 30 days
    final now = DateTime.now();
    return DateRangeState(
      from: DateTime(now.year, now.month - 1, now.day),
      to: now,
    );
  }

  void setRange(DateTime from, DateTime to) {
    state = DateRangeState(from: from, to: to);
  }
}

final dateRangeProvider =
    NotifierProvider<DateRangeNotifier, DateRangeState>(() {
  return DateRangeNotifier();
});

/// Reacts to the date range and re-fetches stats automatically.
final salesStatsProvider =
    FutureProvider<Map<String, dynamic>>((ref) async {
  final range = ref.watch(dateRangeProvider);
  return ref
      .read(reportRepositoryProvider)
      .getSalesStats(from: range.from, to: range.to);
});

/// Top stores in the selected range.
final topStoresProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final range = ref.watch(dateRangeProvider);
  return ref
      .read(reportRepositoryProvider)
      .getTopStores(from: range.from, to: range.to);
});
