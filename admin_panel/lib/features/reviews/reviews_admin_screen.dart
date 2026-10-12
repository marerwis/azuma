import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../core/theme/admin_theme.dart';

// ── Repository ─────────────────────────────────────────────────────────────────

class ReviewRepository {
  final _db = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getReviews() async {
    final r = await _db
        .from('reviews')
        .select('''
          id, rating, comment, is_hidden, created_at,
          customer:users!reviews_customer_id_fkey(id, full_name, phone),
          store:stores!reviews_store_id_fkey(id, name)
        ''')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(r);
  }

  Future<void> toggleHidden(String id, bool isHidden) async {
    await _db.from('reviews').update({'is_hidden': isHidden}).eq('id', id);
  }

  Future<void> deleteReview(String id) async {
    await _db.from('reviews').delete().eq('id', id);
  }
}

// ── Providers ─────────────────────────────────────────────────────────────────

final _reviewRepoProvider =
    Provider<ReviewRepository>((_) => ReviewRepository());

final reviewsAdminProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(_reviewRepoProvider).getReviews();
});

// ── Screen ─────────────────────────────────────────────────────────────────────

class ReviewsAdminScreen extends ConsumerStatefulWidget {
  const ReviewsAdminScreen({super.key});

  @override
  ConsumerState<ReviewsAdminScreen> createState() =>
      _ReviewsAdminScreenState();
}

class _ReviewsAdminScreenState extends ConsumerState<ReviewsAdminScreen> {
  String _search = '';
  String? _filterVisibility; // null | 'visible' | 'hidden'
  int? _filterRating;
  final _dateFmt = DateFormat('dd/MM/yyyy');

  @override
  Widget build(BuildContext context) {
    final reviewsAsync = ref.watch(reviewsAdminProvider);

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: Column(
        children: [
          AzScreenHeader(
            title: 'التقييمات والشكاوى',
            subtitle: 'مراجعة وإدارة تقييمات العملاء',
            icon: Icons.star_outline,
            onRefresh: () => ref.invalidate(reviewsAdminProvider),
          ),
          Expanded(
            child: reviewsAsync.when(
              data: (reviews) {
                final filtered = _filter(reviews);
                return Padding(
                  padding: const EdgeInsets.all(16),
                  child: AzCard(
                    child: Column(
                      children: [
                        _toolbar(reviews, filtered.length),
                        const Divider(height: 1),
                        Expanded(child: _table(filtered)),
                      ],
                    ),
                  ),
                );
              },
              loading: () => const Center(
                  child:
                      CircularProgressIndicator(color: AzColor.primary)),
              error: (e, _) => Center(
                child: Column(mainAxisSize: MainAxisSize.min, children: [
                  const Icon(Icons.error_outline,
                      color: AzColor.danger, size: 48),
                  const SizedBox(height: 12),
                  Text('$e',
                      style:
                          const TextStyle(color: AzColor.textSecondary)),
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: () =>
                        ref.invalidate(reviewsAdminProvider),
                    icon: const Icon(Icons.refresh),
                    label: const Text('إعادة المحاولة'),
                  ),
                ]),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Toolbar ────────────────────────────────────────────────────────────────

  Widget _toolbar(List<Map<String, dynamic>> all, int count) {
    final hidden =
        all.where((r) => r['is_hidden'] as bool? ?? false).length;
    final visible = all.length - hidden;

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          // Visibility filter
          _FilterChip2(
              label: 'الكل',
              count: all.length,
              color: AzColor.primary,
              selected: _filterVisibility == null,
              onTap: () => setState(() => _filterVisibility = null)),
          const SizedBox(width: 8),
          _FilterChip2(
              label: 'ظاهر',
              count: visible,
              color: AzColor.success,
              selected: _filterVisibility == 'visible',
              onTap: () =>
                  setState(() => _filterVisibility = 'visible')),
          const SizedBox(width: 8),
          _FilterChip2(
              label: 'مخفي',
              count: hidden,
              color: AzColor.danger,
              selected: _filterVisibility == 'hidden',
              onTap: () =>
                  setState(() => _filterVisibility = 'hidden')),

          const SizedBox(width: 16),
          // Star filter
          _RatingFilter(
            selected: _filterRating,
            onChanged: (v) => setState(() => _filterRating = v),
          ),

          const Spacer(),
          // Search
          AzSearchField(
            onChanged: (v) => setState(() => _search = v),
            hint: 'بحث بالعميل أو المطعم أو التعليق...',
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(
                horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: AzColor.primary.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text('$count تقييم',
                style: const TextStyle(
                    color: AzColor.primary,
                    fontWeight: FontWeight.bold,
                    fontSize: 12)),
          ),
        ],
      ),
    );
  }

  // ── Table ──────────────────────────────────────────────────────────────────

  Widget _table(List<Map<String, dynamic>> reviews) {
    if (reviews.isEmpty) {
      return const Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.rate_review_outlined,
              size: 48, color: AzColor.textMuted),
          SizedBox(height: 12),
          Text('لا توجد تقييمات مطابقة',
              style: TextStyle(color: AzColor.textSecondary)),
        ]),
      );
    }

    return SingleChildScrollView(
      child: SizedBox(
        width: double.infinity,
        child: DataTable(
          columnSpacing: 16,
          dataRowMinHeight: 62,
          dataRowMaxHeight: 80,
          columns: const [
            DataColumn(label: Text('العميل')),
            DataColumn(label: Text('المطعم')),
            DataColumn(label: Text('التقييم')),
            DataColumn(label: Text('التعليق')),
            DataColumn(label: Text('التاريخ')),
            DataColumn(label: Text('الحالة')),
            DataColumn(label: Text('إجراءات')),
          ],
          rows: reviews.map((r) {
            final customer = r['customer'] as Map?;
            final store = r['store'] as Map?;
            final rating = (r['rating'] as num?)?.toInt() ?? 0;
            final isHidden = r['is_hidden'] as bool? ?? false;
            final date = r['created_at'] != null
                ? _dateFmt.format(
                    DateTime.parse(r['created_at']).toLocal())
                : '--';

            return DataRow(cells: [
              // Customer
              DataCell(Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(customer?['full_name'] ?? '--',
                      style: const TextStyle(
                          fontWeight: FontWeight.w600,
                          color: AzColor.textPrimary,
                          fontSize: 13)),
                  Text(customer?['phone'] ?? '',
                      style: const TextStyle(
                          color: AzColor.textMuted, fontSize: 11)),
                ],
              )),

              // Store
              DataCell(Text(store?['name'] ?? '--',
                  style: const TextStyle(
                      color: AzColor.textSecondary, fontSize: 13))),

              // Rating stars
              DataCell(_StarRating(rating)),

              // Comment
              DataCell(SizedBox(
                width: 220,
                child: Text(
                  r['comment'] ?? '--',
                  style: TextStyle(
                    color: isHidden
                        ? AzColor.textMuted
                        : AzColor.textSecondary,
                    fontSize: 12,
                    fontStyle: isHidden
                        ? FontStyle.italic
                        : FontStyle.normal,
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              )),

              // Date
              DataCell(Text(date,
                  style: const TextStyle(
                      color: AzColor.textMuted, fontSize: 12))),

              // Visibility status
              DataCell(_VisibilityBadge(isHidden: isHidden)),

              // Actions
              DataCell(Row(mainAxisSize: MainAxisSize.min, children: [
                // Toggle visibility
                Tooltip(
                  message: isHidden ? 'إظهار التقييم' : 'إخفاء التقييم',
                  child: IconButton(
                    icon: Icon(
                      isHidden
                          ? Icons.visibility_outlined
                          : Icons.visibility_off_outlined,
                      size: 17,
                      color: isHidden
                          ? AzColor.success
                          : AzColor.warning,
                    ),
                    onPressed: () => _toggleHide(
                        r['id'].toString(), !isHidden),
                  ),
                ),
                // Delete
                IconButton(
                  icon: const Icon(Icons.delete_outline,
                      size: 17, color: AzColor.danger),
                  tooltip: 'حذف التقييم',
                  onPressed: () =>
                      _confirmDelete(r['id'].toString()),
                ),
              ])),
            ]);
          }).toList(),
        ),
      ),
    );
  }

  // ── Filter ─────────────────────────────────────────────────────────────────

  List<Map<String, dynamic>> _filter(List<Map<String, dynamic>> list) {
    return list.where((r) {
      final customer = r['customer'] as Map?;
      final store = r['store'] as Map?;
      final name =
          (customer?['full_name'] ?? '').toString().toLowerCase();
      final storeName = (store?['name'] ?? '').toString().toLowerCase();
      final comment = (r['comment'] ?? '').toString().toLowerCase();
      final q = _search.toLowerCase();
      final matchSearch = _search.isEmpty ||
          name.contains(q) ||
          storeName.contains(q) ||
          comment.contains(q);

      final isHidden = r['is_hidden'] as bool? ?? false;
      final matchVis = _filterVisibility == null ||
          (_filterVisibility == 'visible' && !isHidden) ||
          (_filterVisibility == 'hidden' && isHidden);

      final rating = (r['rating'] as num?)?.toInt() ?? 0;
      final matchRating =
          _filterRating == null || rating == _filterRating;

      return matchSearch && matchVis && matchRating;
    }).toList();
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  Future<void> _toggleHide(String id, bool hide) async {
    try {
      await ref.read(_reviewRepoProvider).toggleHidden(id, hide);
      ref.invalidate(reviewsAdminProvider);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(hide ? 'تم إخفاء التقييم' : 'تم إظهار التقييم'),
          backgroundColor:
              hide ? AzColor.warning : AzColor.success,
        ));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text('خطأ: $e'),
              backgroundColor: AzColor.danger),
        );
      }
    }
  }

  Future<void> _confirmDelete(String id) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AzColor.bgSurface,
        title: const Text('تأكيد الحذف',
            style: TextStyle(color: AzColor.textPrimary)),
        content: const Text('سيتم حذف التقييم نهائياً.',
            style: TextStyle(color: AzColor.textSecondary)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('إلغاء',
                style: TextStyle(color: AzColor.textSecondary)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
                backgroundColor: AzColor.danger),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('حذف'),
          ),
        ],
      ),
    );
    if (ok == true) {
      try {
        await ref.read(_reviewRepoProvider).deleteReview(id);
        ref.invalidate(reviewsAdminProvider);
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
                content: Text('خطأ: $e'),
                backgroundColor: AzColor.danger),
          );
        }
      }
    }
  }
}

// ── Small Widgets ──────────────────────────────────────────────────────────────

class _StarRating extends StatelessWidget {
  const _StarRating(this.rating);
  final int rating;

  @override
  Widget build(BuildContext context) {
    final color = rating >= 4
        ? AzColor.warning
        : rating >= 2
            ? Colors.orange
            : AzColor.danger;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(
        5,
        (i) => Icon(
          i < rating ? Icons.star : Icons.star_outline,
          color: i < rating ? color : AzColor.textMuted,
          size: 14,
        ),
      ),
    );
  }
}

class _VisibilityBadge extends StatelessWidget {
  const _VisibilityBadge({required this.isHidden});
  final bool isHidden;

  @override
  Widget build(BuildContext context) {
    final color = isHidden ? AzColor.danger : AzColor.success;
    final label = isHidden ? 'مخفي' : 'ظاهر';
    final icon =
        isHidden ? Icons.visibility_off : Icons.visibility;
    return Container(
      padding:
          const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        border: Border.all(color: color.withValues(alpha: 0.4)),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        Icon(icon, color: color, size: 11),
        const SizedBox(width: 4),
        Text(label,
            style: TextStyle(
                color: color,
                fontSize: 11,
                fontWeight: FontWeight.bold)),
      ]),
    );
  }
}

class _FilterChip2 extends StatelessWidget {
  const _FilterChip2({
    required this.label,
    required this.count,
    required this.color,
    required this.selected,
    required this.onTap,
  });
  final String label;
  final int count;
  final Color color;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
        decoration: BoxDecoration(
          color: selected
              ? color.withValues(alpha: 0.15)
              : AzColor.bgElevated,
          border: Border.all(
              color: selected ? color : AzColor.bgBorder),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Text(label,
              style: TextStyle(
                  color: selected ? color : AzColor.textSecondary,
                  fontSize: 12)),
          const SizedBox(width: 6),
          Container(
            padding:
                const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Text('$count',
                style: TextStyle(
                    color: color,
                    fontWeight: FontWeight.bold,
                    fontSize: 11)),
          ),
        ]),
      ),
    );
  }
}

class _RatingFilter extends StatelessWidget {
  const _RatingFilter({required this.selected, required this.onChanged});
  final int? selected;
  final ValueChanged<int?> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Text('نجوم: ',
            style: TextStyle(color: AzColor.textMuted, fontSize: 12)),
        ...[null, 5, 4, 3, 2, 1].map((v) {
          final isSelected = selected == v;
          return Padding(
            padding: const EdgeInsets.only(left: 4),
            child: InkWell(
              onTap: () => onChanged(v),
              borderRadius: BorderRadius.circular(6),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 120),
                padding: const EdgeInsets.symmetric(
                    horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: isSelected
                      ? AzColor.warning.withValues(alpha: 0.2)
                      : Colors.transparent,
                  border: Border.all(
                      color: isSelected
                          ? AzColor.warning
                          : AzColor.bgBorder),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  v == null ? 'الكل' : '★ $v',
                  style: TextStyle(
                      color: isSelected
                          ? AzColor.warning
                          : AzColor.textMuted,
                      fontSize: 11,
                      fontWeight: isSelected
                          ? FontWeight.bold
                          : FontWeight.normal),
                ),
              ),
            ),
          );
        }),
      ],
    );
  }
}
