import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'providers/review_provider.dart';

class OrderReviewsScreen extends ConsumerWidget {
  const OrderReviewsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reviewsAsync = ref.watch(reviewsProvider);
    final storesAsync = ref.watch(reviewStoresProvider);
    final filter = ref.watch(reviewFilterProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('تقييمات العملاء - Customer Reviews'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'تحديث',
            onPressed: () {
              ref.invalidate(reviewsProvider);
              ref.invalidate(reviewStoresProvider);
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // ── Filter Bar ────────────────────────────────────────────────
          Container(
            color: Colors.white,
            padding:
                const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            child: Row(
              children: [
                const Icon(Icons.filter_list,
                    color: Color(0xFFFF5722), size: 20),
                const SizedBox(width: 8),
                const Text('تصفية:',
                    style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(width: 16),

                // Star rating filter
                const Text('التقييم:',
                    style: TextStyle(color: Colors.grey, fontSize: 13)),
                const SizedBox(width: 8),
                _RatingFilterChips(
                  selectedMin: filter.minRating,
                  onChanged: (min) => ref
                      .read(reviewFilterProvider.notifier)
                      .update((s) => s.copyWith(minRating: min)),
                ),

                const SizedBox(width: 16),
                const VerticalDivider(width: 1),
                const SizedBox(width: 16),

                // Store filter
                const Text('المطعم:',
                    style: TextStyle(color: Colors.grey, fontSize: 13)),
                const SizedBox(width: 8),
                storesAsync.when(
                  data: (stores) => DropdownButton<String?>(
                    value: filter.storeId,
                    hint: const Text('الكل', style: TextStyle(fontSize: 13)),
                    underline: const SizedBox(),
                    items: [
                      const DropdownMenuItem(
                          value: null, child: Text('الكل')),
                      ...stores.map((s) => DropdownMenuItem(
                            value: s['id'].toString(),
                            child: Text(s['name'] ?? '--',
                                style: const TextStyle(fontSize: 13)),
                          )),
                    ],
                    onChanged: (val) => ref
                        .read(reviewFilterProvider.notifier)
                        .update((s) => s.copyWith(storeId: val)),
                  ),
                  loading: () => const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2)),
                  error: (_, _e) => const SizedBox.shrink(),
                ),

                const Spacer(),

                // Clear filters
                if (filter.minRating != null ||
                    filter.maxRating != null ||
                    filter.storeId != null)
                  TextButton.icon(
                    icon: const Icon(Icons.clear, size: 16),
                    label: const Text('مسح التصفية'),
                    onPressed: () =>
                        ref.read(reviewFilterProvider.notifier).clear(),
                  ),
              ],
            ),
          ),

          // ── Summary strip ─────────────────────────────────────────────
          reviewsAsync.when(
            data: (reviews) => _SummaryStrip(reviews: reviews),
            loading: () => const SizedBox.shrink(),
            error: (_, __) => const SizedBox.shrink(),
          ),

          // ── Reviews Table ─────────────────────────────────────────────
          Expanded(
            child: reviewsAsync.when(
              data: (reviews) {
                if (reviews.isEmpty) {
                  return const Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.rate_review_outlined,
                            size: 64, color: Colors.grey),
                        SizedBox(height: 12),
                        Text('لا توجد تقييمات تطابق المعايير المحددة',
                            style: TextStyle(color: Colors.grey)),
                      ],
                    ),
                  );
                }
                return SingleChildScrollView(
                  padding: const EdgeInsets.all(16),
                  child: SizedBox(
                    width: double.infinity,
                    child: Card(
                      elevation: 2,
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12)),
                      child: DataTable(
                        headingRowColor: WidgetStateProperty.all(
                            Colors.grey.shade100),
                        columnSpacing: 20,
                        columns: const [
                          DataColumn(label: Text('العميل')),
                          DataColumn(label: Text('المطعم')),
                          DataColumn(label: Text('رقم الطلب')),
                          DataColumn(label: Text('التقييم')),
                          DataColumn(label: Text('التعليق')),
                          DataColumn(label: Text('التاريخ')),
                          DataColumn(label: Text('إجراء')),
                        ],
                        rows: reviews.map((review) {
                          final customer =
                              review['customer'] as Map?;
                          final store = review['store'] as Map?;
                          final order = review['order'] as Map?;
                          final rating =
                              (review['rating'] as num?)?.toInt() ?? 0;
                          final date = DateTime.tryParse(
                              review['created_at']?.toString() ?? '');

                          return DataRow(cells: [
                            DataCell(
                              Column(
                                crossAxisAlignment:
                                    CrossAxisAlignment.start,
                                mainAxisAlignment:
                                    MainAxisAlignment.center,
                                children: [
                                  Text(
                                      customer?['full_name'] ?? 'مجهول',
                                      style: const TextStyle(
                                          fontWeight: FontWeight.bold)),
                                  Text(customer?['phone'] ?? '',
                                      style: const TextStyle(
                                          fontSize: 11,
                                          color: Colors.grey)),
                                ],
                              ),
                            ),
                            DataCell(
                                Text(store?['name'] ?? '--')),
                            DataCell(Text(
                              order?['id']
                                      ?.toString()
                                      .substring(0, 8)
                                      .toUpperCase() ??
                                  '--',
                              style: const TextStyle(
                                  fontFamily: 'monospace'),
                            )),
                            DataCell(_StarRating(rating)),
                            DataCell(
                              SizedBox(
                                width: 200,
                                child: Text(
                                  review['comment']?.toString() ??
                                      '(بدون تعليق)',
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                      color:
                                          review['comment'] == null
                                              ? Colors.grey
                                              : null,
                                      fontStyle:
                                          review['comment'] == null
                                              ? FontStyle.italic
                                              : null),
                                ),
                              ),
                            ),
                            DataCell(Text(date != null
                                ? DateFormat('yyyy-MM-dd').format(date)
                                : '--')),
                            DataCell(
                              IconButton(
                                icon: const Icon(Icons.delete_outline,
                                    color: Colors.red, size: 20),
                                tooltip: 'حذف التقييم',
                                onPressed: () => _confirmDelete(
                                    context,
                                    ref,
                                    review['id'].toString()),
                              ),
                            ),
                          ]);
                        }).toList(),
                      ),
                    ),
                  ),
                );
              },
              loading: () =>
                  const Center(child: CircularProgressIndicator()),
              error: (e, _) =>
                  Center(child: Text('خطأ في تحميل التقييمات: $e')),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _confirmDelete(
      BuildContext context, WidgetRef ref, String reviewId) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('تأكيد الحذف'),
        content:
            const Text('هل أنت متأكد من حذف هذا التقييم؟ لا يمكن التراجع.'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('إلغاء')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red,
                foregroundColor: Colors.white),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('حذف'),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      try {
        await ref
            .read(reviewRepositoryProvider)
            .deleteReview(reviewId);
        ref.invalidate(reviewsProvider);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
                content: Text('تم حذف التقييم'),
                backgroundColor: Colors.green),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
                content: Text('خطأ في الحذف: $e'),
                backgroundColor: Colors.red),
          );
        }
      }
    }
  }
}

// ── Helper Widgets ────────────────────────────────────────────────────────────

class _StarRating extends StatelessWidget {
  const _StarRating(this.rating);
  final int rating;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (i) {
        return Icon(
          i < rating ? Icons.star : Icons.star_border,
          color: Colors.amber,
          size: 16,
        );
      }),
    );
  }
}

class _RatingFilterChips extends StatelessWidget {
  const _RatingFilterChips(
      {required this.selectedMin, required this.onChanged});
  final int? selectedMin;
  final ValueChanged<int?> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [1, 2, 3, 4, 5].map((star) {
        final isSelected = selectedMin == star;
        return Padding(
          padding: const EdgeInsets.only(left: 4),
          child: FilterChip(
            label: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.star, size: 14,
                    color: isSelected ? Colors.white : Colors.amber),
                const SizedBox(width: 2),
                Text('$star+',
                    style: TextStyle(
                        fontSize: 12,
                        color: isSelected ? Colors.white : null)),
              ],
            ),
            selected: isSelected,
            onSelected: (_x) => onChanged(isSelected ? null : star),
            selectedColor: const Color(0xFFFF5722),
            checkmarkColor: Colors.white,
          ),
        );
      }).toList(),
    );
  }
}

class _SummaryStrip extends StatelessWidget {
  const _SummaryStrip({required this.reviews});
  final List<Map<String, dynamic>> reviews;

  @override
  Widget build(BuildContext context) {
    if (reviews.isEmpty) return const SizedBox.shrink();

    final ratings =
        reviews.map((r) => (r['rating'] as num?)?.toDouble() ?? 0).toList();
    final avg =
        ratings.isEmpty ? 0.0 : ratings.reduce((a, b) => a + b) / ratings.length;
    final fiveStars = ratings.where((r) => r == 5).length;
    final oneStar = ratings.where((r) => r == 1).length;

    return Container(
      color: const Color(0xFFFFF3E0),
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      child: Row(
        children: [
          _SummaryItem(
              label: 'إجمالي التقييمات', value: reviews.length.toString()),
          const VerticalDivider(width: 32),
          _SummaryItem(
              label: 'متوسط التقييم',
              value: '${avg.toStringAsFixed(1)} ⭐'),
          const VerticalDivider(width: 32),
          _SummaryItem(label: 'تقييمات 5 ⭐', value: fiveStars.toString()),
          const VerticalDivider(width: 32),
          _SummaryItem(label: 'تقييمات 1 ⭐', value: oneStar.toString()),
        ],
      ),
    );
  }
}

class _SummaryItem extends StatelessWidget {
  const _SummaryItem({required this.label, required this.value});
  final String label, value;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(value,
            style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 18,
                color: Color(0xFFFF5722))),
        const SizedBox(width: 6),
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 13)),
      ],
    );
  }
}
