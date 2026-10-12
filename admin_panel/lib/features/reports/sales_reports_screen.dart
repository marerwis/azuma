import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'providers/report_provider.dart';

class SalesReportsScreen extends ConsumerWidget {
  const SalesReportsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final range = ref.watch(dateRangeProvider);
    final statsAsync = ref.watch(salesStatsProvider);
    final topStoresAsync = ref.watch(topStoresProvider);
    final fmt = DateFormat('yyyy-MM-dd');

    return Scaffold(
      appBar: AppBar(
        title: const Text('التقارير والإحصائيات - Sales Reports'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'تحديث',
            onPressed: () {
              ref.invalidate(salesStatsProvider);
              ref.invalidate(topStoresProvider);
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ── Date Range Picker ──────────────────────────────────────
            Card(
              elevation: 2,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12)),
              child: Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                child: Row(
                  children: [
                    const Icon(Icons.date_range, color: Color(0xFFFF5722)),
                    const SizedBox(width: 12),
                    Text(
                      'الفترة: ${fmt.format(range.from)} — ${fmt.format(range.to)}',
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 15),
                    ),
                    const Spacer(),
                    _DateRangeButton(
                        label: 'آخر 7 أيام',
                        onTap: () => _setDays(ref, 7)),
                    const SizedBox(width: 8),
                    _DateRangeButton(
                        label: 'آخر 30 يوم',
                        onTap: () => _setDays(ref, 30)),
                    const SizedBox(width: 8),
                    _DateRangeButton(
                        label: 'آخر 90 يوم',
                        onTap: () => _setDays(ref, 90)),
                    const SizedBox(width: 8),
                    OutlinedButton.icon(
                      icon: const Icon(Icons.calendar_today, size: 16),
                      label: const Text('نطاق مخصص'),
                      onPressed: () => _pickCustomRange(context, ref, range),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // ── KPI Cards ─────────────────────────────────────────────
            statsAsync.when(
              data: (stats) => Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      _KpiCard(
                        label: 'إجمالي الطلبات',
                        value: stats['total'].toString(),
                        icon: Icons.shopping_bag,
                        color: Colors.blue,
                      ),
                      const SizedBox(width: 16),
                      _KpiCard(
                        label: 'طلبات مكتملة',
                        value: stats['completed'].toString(),
                        icon: Icons.check_circle,
                        color: Colors.green,
                      ),
                      const SizedBox(width: 16),
                      _KpiCard(
                        label: 'طلبات ملغاة / مرفوضة',
                        value: stats['cancelled'].toString(),
                        icon: Icons.cancel,
                        color: Colors.red,
                      ),
                      const SizedBox(width: 16),
                      _KpiCard(
                        label: 'إجمالي الإيرادات',
                        value:
                            '${_fmt(stats['revenue'])} د.ل',
                        icon: Icons.attach_money,
                        color: const Color(0xFFFF5722),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),

                  // Completion rate bar
                  _CompletionRateBar(stats: stats),

                  const SizedBox(height: 24),

                  // Orders breakdown table
                  _OrdersBreakdownTable(orders: stats['orders'] as List),
                ],
              ),
              loading: () =>
                  const Center(child: CircularProgressIndicator()),
              error: (e, _) =>
                  Center(child: Text('خطأ في تحميل البيانات: $e')),
            ),

            const SizedBox(height: 24),

            // ── Top Stores ────────────────────────────────────────────
            topStoresAsync.when(
              data: (stores) => _TopStoresTable(stores: stores),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Text('خطأ: $e'),
            ),
          ],
        ),
      ),
    );
  }

  void _setDays(WidgetRef ref, int days) {
    final now = DateTime.now();
    ref
        .read(dateRangeProvider.notifier)
        .setRange(now.subtract(Duration(days: days)), now);
  }

  Future<void> _pickCustomRange(
      BuildContext context, WidgetRef ref, DateRangeState current) async {
    final picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2024),
      lastDate: DateTime.now(),
      initialDateRange:
          DateTimeRange(start: current.from, end: current.to),
      locale: const Locale('ar'),
    );
    if (picked != null) {
      ref
          .read(dateRangeProvider.notifier)
          .setRange(picked.start, picked.end);
    }
  }

  String _fmt(dynamic val) =>
      NumberFormat('#,##0.00').format((val as num?)?.toDouble() ?? 0);
}

// ── Widgets ──────────────────────────────────────────────────────────────────

class _DateRangeButton extends StatelessWidget {
  const _DateRangeButton({required this.label, required this.onTap});
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => TextButton(
        onPressed: onTap,
        child: Text(label, style: const TextStyle(fontSize: 13)),
      );
}

class _KpiCard extends StatelessWidget {
  const _KpiCard(
      {required this.label,
      required this.value,
      required this.icon,
      required this.color});
  final String label, value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Card(
        elevation: 3,
        shape:
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: color, size: 28),
              ),
              const SizedBox(width: 16),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(value,
                      style: TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                          color: color)),
                  const SizedBox(height: 4),
                  Text(label,
                      style: const TextStyle(
                          color: Colors.grey, fontSize: 13)),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CompletionRateBar extends StatelessWidget {
  const _CompletionRateBar({required this.stats});
  final Map<String, dynamic> stats;

  @override
  Widget build(BuildContext context) {
    final total = (stats['total'] as int?) ?? 0;
    final completed = (stats['completed'] as int?) ?? 0;
    final cancelled = (stats['cancelled'] as int?) ?? 0;
    final rate = total > 0 ? completed / total : 0.0;

    return Card(
      elevation: 2,
      shape:
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('معدل إتمام الطلبات',
                style: TextStyle(
                    fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: LinearProgressIndicator(
                value: rate,
                minHeight: 20,
                backgroundColor: Colors.red.withOpacity(0.15),
                valueColor:
                    const AlwaysStoppedAnimation<Color>(Colors.green),
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                    '✅ مكتملة: $completed (${(rate * 100).toStringAsFixed(1)}%)',
                    style: const TextStyle(color: Colors.green)),
                Text(
                    '❌ ملغاة/مرفوضة: $cancelled (${total > 0 ? (cancelled / total * 100).toStringAsFixed(1) : 0}%)',
                    style: const TextStyle(color: Colors.red)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _OrdersBreakdownTable extends StatelessWidget {
  const _OrdersBreakdownTable({required this.orders});
  final List orders;

  @override
  Widget build(BuildContext context) {
    final fmt = DateFormat('yyyy-MM-dd HH:mm');
    return Card(
      elevation: 2,
      shape:
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('تفاصيل الطلبات في الفترة المحددة',
                style: TextStyle(
                    fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: DataTable(
                headingRowColor:
                    MaterialStateProperty.all(Colors.grey.shade100),
                columns: const [
                  DataColumn(label: Text('رقم الطلب')),
                  DataColumn(label: Text('التاريخ')),
                  DataColumn(label: Text('الإجمالي')),
                  DataColumn(label: Text('الحالة')),
                ],
                rows: orders.take(50).map<DataRow>((o) {
                  final date =
                      DateTime.tryParse(o['created_at']?.toString() ?? '');
                  final status = o['status']?.toString() ?? '--';
                  return DataRow(cells: [
                    DataCell(Text(
                        o['id'].toString().substring(0, 8).toUpperCase())),
                    DataCell(
                        Text(date != null ? fmt.format(date) : '--')),
                    DataCell(Text(
                        '${o['total_amount'] ?? 0} د.ل',
                        style: const TextStyle(
                            fontWeight: FontWeight.bold))),
                    DataCell(_StatusChip(status)),
                  ]);
                }).toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _TopStoresTable extends StatelessWidget {
  const _TopStoresTable({required this.stores});
  final List<Map<String, dynamic>> stores;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape:
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.emoji_events, color: Color(0xFFFF5722)),
                SizedBox(width: 8),
                Text('أفضل المطاعم أداءً',
                    style: TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 16)),
              ],
            ),
            const SizedBox(height: 12),
            if (stores.isEmpty)
              const Text('لا توجد بيانات في هذه الفترة')
            else
              SizedBox(
                width: double.infinity,
                child: DataTable(
                  headingRowColor:
                      MaterialStateProperty.all(Colors.grey.shade100),
                  columns: const [
                    DataColumn(label: Text('#')),
                    DataColumn(label: Text('المطعم')),
                    DataColumn(label: Text('الطلبات المكتملة')),
                    DataColumn(label: Text('الإيرادات')),
                  ],
                  rows: stores.asMap().entries.map((e) {
                    final i = e.key + 1;
                    final s = e.value;
                    return DataRow(cells: [
                      DataCell(Text('$i',
                          style: TextStyle(
                              color: i <= 3
                                  ? const Color(0xFFFF5722)
                                  : null,
                              fontWeight: i <= 3
                                  ? FontWeight.bold
                                  : null))),
                      DataCell(Text(s['name'] ?? '--')),
                      DataCell(Text(s['orders'].toString(),
                          style: const TextStyle(
                              fontWeight: FontWeight.bold))),
                      DataCell(Text(
                          '${NumberFormat('#,##0.00').format(s['revenue'])} د.ل')),
                    ]);
                  }).toList(),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip(this.status);
  final String status;

  @override
  Widget build(BuildContext context) {
    final (label, color) = switch (status) {
      'delivered' => ('مكتمل', Colors.green),
      'cancelled' => ('ملغي', Colors.red),
      'rejected_by_vendor' => ('مرفوض', Colors.red),
      'pending' => ('انتظار', Colors.orange),
      'preparing' => ('تجهيز', Colors.blue),
      'ready_for_pickup' => ('جاهز', Colors.teal),
      _ => (status, Colors.grey),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          border: Border.all(color: color.withOpacity(0.4)),
          borderRadius: BorderRadius.circular(20)),
      child: Text(label,
          style: TextStyle(
              color: color,
              fontSize: 11,
              fontWeight: FontWeight.bold)),
    );
  }
}
