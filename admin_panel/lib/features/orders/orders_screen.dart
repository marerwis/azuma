import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'providers/order_provider.dart';

// ── Valid order_status enum values (matches PostgreSQL enum) ─────────────────
const _orderStatuses = [
  'pending',
  'confirmed',
  'preparing',
  'ready_for_pickup',
  'out_for_delivery',
  'delivered',
  'cancelled',
];

// ── Status display config ────────────────────────────────────────────────────
const _statusConfig = {
  'pending':           {'ar': 'قيد الانتظار', 'color': 0xFFF59E0B, 'icon': Icons.hourglass_top},
  'confirmed':         {'ar': 'مؤكد',           'color': 0xFF3B82F6, 'icon': Icons.check_circle_outline},
  'preparing':         {'ar': 'قيد التحضير',   'color': 0xFF8B5CF6, 'icon': Icons.restaurant},
  'ready_for_pickup':  {'ar': 'جاهز',           'color': 0xFF0EA5E9, 'icon': Icons.shopping_bag_outlined},
  'out_for_delivery':  {'ar': 'في الطريق',      'color': 0xFFFF5722, 'icon': Icons.delivery_dining},
  'delivered':         {'ar': 'تم التوصيل',    'color': 0xFF10B981, 'icon': Icons.check_circle},
  'cancelled':         {'ar': 'ملغي',           'color': 0xFFEF4444, 'icon': Icons.cancel},
};

Color _statusColor(String? s) =>
    Color((_statusConfig[s]?['color'] as int?) ?? 0xFF9CA3AF);

String _statusAr(String? s) =>
    (_statusConfig[s]?['ar'] as String?) ?? (s ?? '--');

IconData _statusIcon(String? s) =>
    (_statusConfig[s]?['icon'] as IconData?) ?? Icons.circle;

class OrdersScreen extends ConsumerStatefulWidget {
  const OrdersScreen({super.key});

  @override
  ConsumerState<OrdersScreen> createState() => _OrdersScreenState();
}

class _OrdersScreenState extends ConsumerState<OrdersScreen> {
  String _search = '';
  String? _filterStatus;
  final _fmt = NumberFormat('#,##0.00');
  final _dateFmt = DateFormat('dd/MM/yyyy\nhh:mm a');

  @override
  Widget build(BuildContext context) {
    final ordersAsync = ref.watch(ordersProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFF3F4F6),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildHeader(ordersAsync),
          _buildStatusFilterBar(),
          Expanded(
            child: ordersAsync.when(
              data: (orders) {
                final filtered = _applyFilter(orders);
                return Padding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                  child: Card(
                    elevation: 2,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                    child: Column(
                      children: [
                        _buildTableToolbar(filtered.length),
                        const Divider(height: 1),
                        Expanded(child: _buildTable(filtered)),
                      ],
                    ),
                  ),
                );
              },
              loading: () =>
                  const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.error_outline,
                        color: Colors.red, size: 48),
                    const SizedBox(height: 8),
                    Text('$e', textAlign: TextAlign.center),
                    const SizedBox(height: 12),
                    ElevatedButton.icon(
                      onPressed: () => ref.invalidate(ordersProvider),
                      icon: const Icon(Icons.refresh),
                      label: const Text('إعادة المحاولة'),
                    )
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Header ────────────────────────────────────────────────────────────────

  Widget _buildHeader(AsyncValue<List<Map<String, dynamic>>> ordersAsync) {
    final total = ordersAsync.value?.length ?? 0;
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: const Color(0xFFFF5722).withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(Icons.receipt_long,
                color: Color(0xFFFF5722), size: 24),
          ),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('إدارة الطلبات',
                  style: TextStyle(
                      fontSize: 20, fontWeight: FontWeight.bold)),
              Text('إجمالي $total طلب',
                  style: const TextStyle(
                      color: Colors.grey, fontSize: 13)),
            ],
          ),
          const Spacer(),
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'تحديث',
            onPressed: () => ref.invalidate(ordersProvider),
          ),
        ],
      ),
    );
  }

  // ── Status Filter Chips ────────────────────────────────────────────────────

  Widget _buildStatusFilterBar() {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: Row(
          children: [
            _StatusChip(
              label: 'الكل',
              selected: _filterStatus == null,
              color: Colors.grey,
              onTap: () => setState(() => _filterStatus = null),
            ),
            ..._orderStatuses.map((s) => _StatusChip(
                  label: _statusAr(s),
                  selected: _filterStatus == s,
                  color: _statusColor(s),
                  onTap: () =>
                      setState(() => _filterStatus = s),
                )),
          ],
        ),
      ),
    );
  }

  // ── Table Toolbar (search) ─────────────────────────────────────────────────

  Widget _buildTableToolbar(int count) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(
                horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: const Color(0xFFFF5722).withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text('$count طلب',
                style: const TextStyle(
                    color: Color(0xFFFF5722),
                    fontWeight: FontWeight.bold)),
          ),
          const SizedBox(width: 16),
          SizedBox(
            width: 280,
            child: TextField(
              onChanged: (v) => setState(() => _search = v),
              decoration: InputDecoration(
                hintText: 'بحث برقم الطلب أو اسم العميل...',
                isDense: true,
                prefixIcon: const Icon(Icons.search, size: 18),
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                    borderSide:
                        BorderSide(color: Colors.grey.shade300)),
                enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                    borderSide:
                        BorderSide(color: Colors.grey.shade300)),
                contentPadding: const EdgeInsets.symmetric(
                    vertical: 10, horizontal: 12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Main Data Table ────────────────────────────────────────────────────────

  Widget _buildTable(List<Map<String, dynamic>> orders) {
    if (orders.isEmpty) {
      return const Center(
        child: Text('لا توجد طلبات مطابقة',
            style: TextStyle(color: Colors.grey, fontSize: 15)),
      );
    }
    return SingleChildScrollView(
      child: SizedBox(
        width: double.infinity,
        child: DataTable(
          headingRowColor:
              WidgetStateProperty.all(Colors.grey.shade50),
          columnSpacing: 16,
          dataRowMinHeight: 60,
          dataRowMaxHeight: 80,
          columns: const [
            DataColumn(label: Text('رقم الطلب')),
            DataColumn(label: Text('العميل')),
            DataColumn(label: Text('المطعم')),
            DataColumn(label: Text('المبلغ')),
            DataColumn(label: Text('الحالة')),
            DataColumn(label: Text('الدفع')),
            DataColumn(label: Text('التاريخ')),
            DataColumn(label: Text('إجراءات')),
          ],
          rows: orders.map((o) => _buildRow(o)).toList(),
        ),
      ),
    );
  }

  DataRow _buildRow(Map<String, dynamic> o) {
    final customer = o['customer'] as Map?;
    final store = o['store'] as Map?;
    final status = o['status']?.toString();
    final total = (o['total_amount'] as num?)?.toDouble() ?? 0;
    final isPaid = o['is_paid'] as bool? ?? false;
    final createdAt = o['created_at'] != null
        ? _dateFmt.format(DateTime.parse(o['created_at']).toLocal())
        : '--';

    return DataRow(
      cells: [
        // Order ID (short)
        DataCell(
          Text(
            '#${o['id'].toString().substring(0, 8).toUpperCase()}',
            style: const TextStyle(
                fontFamily: 'monospace',
                fontWeight: FontWeight.bold,
                fontSize: 12),
          ),
        ),

        // Customer
        DataCell(Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(customer?['full_name'] ?? '--',
                style: const TextStyle(fontWeight: FontWeight.w600,
                    fontSize: 13)),
            Text(customer?['phone'] ?? '',
                style: const TextStyle(
                    color: Colors.grey, fontSize: 11)),
          ],
        )),

        // Store
        DataCell(Text(store?['name'] ?? '--',
            style: const TextStyle(fontSize: 13))),

        // Total amount
        DataCell(Text(
          '${_fmt.format(total)} د.ل',
          style: const TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFFFF5722)),
        )),

        // Status badge
        DataCell(_StatusBadge(status)),

        // Payment
        DataCell(Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              isPaid ? Icons.check_circle : Icons.pending,
              color: isPaid ? Colors.green : Colors.orange,
              size: 18,
            ),
            Text(
              o['payment_method']?.toString() ?? '--',
              style: const TextStyle(fontSize: 10, color: Colors.grey),
            ),
          ],
        )),

        // Date
        DataCell(Text(createdAt,
            style: const TextStyle(fontSize: 11, color: Colors.grey))),

        // Actions
        DataCell(Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            IconButton(
              icon: const Icon(Icons.visibility_outlined, size: 18),
              tooltip: 'عرض التفاصيل',
              color: Colors.blue,
              onPressed: () => _showOrderDetails(o),
            ),
            _StatusDropdown(
              currentStatus: o['status']?.toString() ?? 'pending',
              onChanged: (newStatus) =>
                  _updateStatus(o['id'].toString(), newStatus),
            ),
          ],
        )),
      ],
    );
  }

  // ── Filters ───────────────────────────────────────────────────────────────

  List<Map<String, dynamic>> _applyFilter(
      List<Map<String, dynamic>> orders) {
    return orders.where((o) {
      final customer = o['customer'] as Map?;
      final name =
          (customer?['full_name'] ?? '').toString().toLowerCase();
      final id = o['id'].toString().toLowerCase();
      final matchSearch = _search.isEmpty ||
          id.contains(_search.toLowerCase()) ||
          name.contains(_search.toLowerCase());
      final matchStatus =
          _filterStatus == null || o['status'] == _filterStatus;
      return matchSearch && matchStatus;
    }).toList();
  }

  // ── Status Update ─────────────────────────────────────────────────────────

  Future<void> _updateStatus(String orderId, String newStatus) async {
    try {
      await ref
          .read(orderRepositoryProvider)
          .updateOrderStatus(orderId, newStatus);
      ref.invalidate(ordersProvider);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content:
              Text('تم تحديث الحالة إلى: ${_statusAr(newStatus)}'),
          backgroundColor: _statusColor(newStatus),
        ));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text('خطأ: $e'),
          backgroundColor: Colors.red,
        ));
      }
    }
  }

  // ── Order Details Dialog ──────────────────────────────────────────────────

  void _showOrderDetails(Map<String, dynamic> order) {
    showDialog(
      context: context,
      builder: (ctx) => _OrderDetailsDialog(order: order),
    );
  }
}

// ── Order Details Dialog ───────────────────────────────────────────────────────

class _OrderDetailsDialog extends ConsumerWidget {
  const _OrderDetailsDialog({required this.order});
  final Map<String, dynamic> order;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final orderId = order['id'].toString();
    final itemsAsync = ref.watch(orderItemsProvider(orderId));
    final historyAsync = ref.watch(orderHistoryProvider(orderId));
    final customer = order['customer'] as Map?;
    final store = order['store'] as Map?;
    final driver = order['driver'] as Map?;
    final fmt = NumberFormat('#,##0.00');

    return Dialog(
      shape:
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      insetPadding:
          const EdgeInsets.symmetric(horizontal: 32, vertical: 24),
      child: SizedBox(
        width: 780,
        height: 640,
        child: Column(
          children: [
            // Header
            Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: 24, vertical: 16),
              decoration: BoxDecoration(
                color: _statusColor(order['status']?.toString()),
                borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(16)),
              ),
              child: Row(
                children: [
                  Icon(
                      _statusIcon(order['status']?.toString()),
                      color: Colors.white),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '#${orderId.substring(0, 8).toUpperCase()}',
                          style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 16),
                        ),
                        Text(
                          _statusAr(order['status']?.toString()),
                          style: TextStyle(
                              color:
                                  Colors.white.withValues(alpha: 0.85),
                              fontSize: 13),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close, color: Colors.white),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),

            // Content
            Expanded(
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Left: Info panels
                  Expanded(
                    flex: 5,
                    child: SingleChildScrollView(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment:
                            CrossAxisAlignment.start,
                        children: [
                          // Parties
                          Row(
                            crossAxisAlignment:
                                CrossAxisAlignment.start,
                            children: [
                              Expanded(
                                  child: _InfoCard(
                                      title: 'العميل',
                                      icon: Icons.person_outline,
                                      children: [
                                    _InfoRow('الاسم',
                                        customer?['full_name']),
                                    _InfoRow('الهاتف',
                                        customer?['phone']),
                                    _InfoRow('البريد',
                                        customer?['email']),
                                  ])),
                              const SizedBox(width: 12),
                              Expanded(
                                  child: _InfoCard(
                                      title: 'المطعم',
                                      icon: Icons.store_outlined,
                                      children: [
                                    _InfoRow('الاسم',
                                        store?['name']),
                                    if (driver != null)
                                      _InfoRow('السائق',
                                          driver['full_name']),
                                    if (driver != null)
                                      _InfoRow('هاتف السائق',
                                          driver['phone']),
                                  ])),
                            ],
                          ),
                          const SizedBox(height: 12),

                          // Delivery Info
                          _InfoCard(
                            title: 'معلومات التوصيل',
                            icon: Icons.location_on_outlined,
                            children: [
                              _InfoRow('العنوان',
                                  order['delivery_address']),
                              _InfoRow('ملاحظات',
                                  order['special_instructions']),
                              _InfoRow('نوع الطلب',
                                  order['order_type']),
                              _InfoRow('وقت التحضير',
                                  order['prep_time_minutes'] != null
                                      ? '${order['prep_time_minutes']} دقيقة'
                                      : null),
                              _InfoRow('سبب الإلغاء',
                                  order['cancellation_reason']),
                            ],
                          ),
                          const SizedBox(height: 12),

                          // Financial Summary
                          _InfoCard(
                            title: 'الملخص المالي',
                            icon: Icons.receipt_outlined,
                            children: [
                              _InfoRow('المجموع الجزئي',
                                  '${fmt.format((order['subtotal'] as num?)?.toDouble() ?? 0)} د.ل'),
                              _InfoRow('رسوم التوصيل',
                                  '${fmt.format((order['delivery_fee'] as num?)?.toDouble() ?? 0)} د.ل'),
                              _InfoRow('الخصم',
                                  '${fmt.format((order['discount_amount'] as num?)?.toDouble() ?? 0)} د.ل'),
                              _InfoRow('الضريبة',
                                  '${fmt.format((order['tax_amount'] as num?)?.toDouble() ?? 0)} د.ل'),
                              _InfoRow('الإجمالي',
                                  '${fmt.format((order['total_amount'] as num?)?.toDouble() ?? 0)} د.ل',
                                  bold: true),
                              _InfoRow('طريقة الدفع',
                                  order['payment_method']),
                              _InfoRow('حالة الدفع',
                                  (order['is_paid'] as bool? ?? false)
                                      ? '✅ مدفوع'
                                      : '⏳ غير مدفوع'),
                            ],
                          ),

                          // Order Items
                          const SizedBox(height: 12),
                          _SectionTitle('تفاصيل المنتجات'),
                          itemsAsync.when(
                            data: (items) => items.isEmpty
                                ? const Text('لا توجد منتجات',
                                    style: TextStyle(
                                        color: Colors.grey))
                                : Column(
                                    children: items
                                        .map((item) =>
                                            _OrderItemTile(
                                                item: item,
                                                fmt: fmt))
                                        .toList()),
                            loading: () => const Padding(
                              padding: EdgeInsets.all(16),
                              child:
                                  CircularProgressIndicator(),
                            ),
                            error: (e, _) => Text('خطأ: $e',
                                style: const TextStyle(
                                    color: Colors.red)),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // Divider
                  VerticalDivider(
                      width: 1, color: Colors.grey.shade200),

                  // Right: Status History
                  SizedBox(
                    width: 200,
                    child: Column(
                      crossAxisAlignment:
                          CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.all(16),
                          child: _SectionTitle('سجل الحالات'),
                        ),
                        Expanded(
                          child: historyAsync.when(
                            data: (history) => history.isEmpty
                                ? const Center(
                                    child: Text('لا يوجد سجل',
                                        style: TextStyle(
                                            color: Colors.grey)))
                                : ListView.builder(
                                    padding:
                                        const EdgeInsets.symmetric(
                                            horizontal: 12),
                                    itemCount: history.length,
                                    itemBuilder: (_, i) {
                                      final h = history[i];
                                      final isLast =
                                          i == history.length - 1;
                                      return _HistoryItem(
                                        status: h['status']
                                            ?.toString(),
                                        notes: h['notes']
                                            ?.toString(),
                                        createdAt:
                                            h['created_at']
                                                ?.toString(),
                                        isLast: isLast,
                                      );
                                    }),
                            loading: () => const Center(
                                child:
                                    CircularProgressIndicator()),
                            error: (e, _) => Text('خطأ: $e'),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Status Dropdown ────────────────────────────────────────────────────────────

class _StatusDropdown extends StatelessWidget {
  const _StatusDropdown(
      {required this.currentStatus, required this.onChanged});
  final String currentStatus;
  final void Function(String) onChanged;

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<String>(
      tooltip: 'تغيير الحالة',
      icon: const Icon(Icons.swap_horiz, size: 18, color: Colors.grey),
      itemBuilder: (_) => _orderStatuses
          .where((s) => s != currentStatus)
          .map((s) => PopupMenuItem(
                value: s,
                child: Row(
                  children: [
                    Icon(_statusIcon(s),
                        color: _statusColor(s), size: 16),
                    const SizedBox(width: 8),
                    Text(_statusAr(s),
                        style: TextStyle(color: _statusColor(s))),
                  ],
                ),
              ))
          .toList(),
      onSelected: onChanged,
    );
  }
}

// ── Small Reusable Widgets ────────────────────────────────────────────────────

class _StatusBadge extends StatelessWidget {
  const _StatusBadge(this.status);
  final String? status;

  @override
  Widget build(BuildContext context) {
    final color = _statusColor(status);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        border: Border.all(color: color.withValues(alpha: 0.4)),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(_statusIcon(status), color: color, size: 12),
          const SizedBox(width: 4),
          Text(_statusAr(status),
              style: TextStyle(
                  color: color,
                  fontSize: 11,
                  fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({
    required this.label,
    required this.selected,
    required this.color,
    required this.onTap,
  });
  final String label;
  final bool selected;
  final Color color;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(
              horizontal: 14, vertical: 6),
          decoration: BoxDecoration(
            color: selected ? color : Colors.transparent,
            border: Border.all(
                color: selected
                    ? color
                    : Colors.grey.shade300),
            borderRadius: BorderRadius.circular(20),
          ),
          child: Text(
            label,
            style: TextStyle(
              color: selected ? Colors.white : Colors.grey[600],
              fontWeight:
                  selected ? FontWeight.bold : FontWeight.normal,
              fontSize: 12,
            ),
          ),
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  const _InfoCard(
      {required this.title,
      required this.icon,
      required this.children});
  final String title;
  final IconData icon;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey.shade50,
        border: Border.all(color: Colors.grey.shade200),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon,
                  size: 15, color: const Color(0xFFFF5722)),
              const SizedBox(width: 6),
              Text(title,
                  style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 13,
                      color: Color(0xFFFF5722))),
            ],
          ),
          const SizedBox(height: 8),
          ...children.where((w) => w is! SizedBox || true),
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow(this.label, this.value, {this.bold = false});
  final String label;
  final String? value;
  final bool bold;

  @override
  Widget build(BuildContext context) {
    if (value == null || value!.isEmpty) return const SizedBox.shrink();
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 90,
            child: Text('$label:',
                style: const TextStyle(
                    color: Colors.grey, fontSize: 12)),
          ),
          Expanded(
            child: Text(value!,
                style: TextStyle(
                    fontSize: 12,
                    fontWeight: bold
                        ? FontWeight.bold
                        : FontWeight.normal)),
          ),
        ],
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.title);
  final String title;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(title,
          style: const TextStyle(
              fontWeight: FontWeight.bold, fontSize: 14)),
    );
  }
}

class _OrderItemTile extends StatelessWidget {
  const _OrderItemTile(
      {required this.item, required this.fmt});
  final Map<String, dynamic> item;
  final NumberFormat fmt;

  @override
  Widget build(BuildContext context) {
    final product = item['product'] as Map?;
    final addons = item['addons'] as List? ?? [];
    final qty = item['quantity'] as int? ?? 1;
    final unitPrice =
        (item['unit_price'] as num?)?.toDouble() ?? 0;
    final totalPrice =
        (item['total_price'] as num?)?.toDouble() ?? 0;

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.grey.shade200),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              // Product thumbnail
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(6),
                    color: Colors.grey.shade100),
                clipBehavior: Clip.antiAlias,
                child: product?['image_url'] != null &&
                        (product?['image_url'] as String)
                            .startsWith('http')
                    ? Image.network(product!['image_url'],
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) =>
                            const Icon(Icons.fastfood,
                                color: Colors.grey))
                    : const Icon(Icons.fastfood,
                        color: Colors.grey, size: 20),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment:
                      CrossAxisAlignment.start,
                  children: [
                    Text(product?['name'] ?? '--',
                        style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 13)),
                    Text(
                      '$qty × ${fmt.format(unitPrice)} د.ل',
                      style: const TextStyle(
                          color: Colors.grey, fontSize: 11),
                    ),
                  ],
                ),
              ),
              Text(
                '${fmt.format(totalPrice)} د.ل',
                style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    color: Color(0xFFFF5722)),
              ),
            ],
          ),
          if (addons.isNotEmpty) ...[
            const SizedBox(height: 6),
            Wrap(
              spacing: 6,
              runSpacing: 4,
              children: addons.map((a) {
                final addon = a['addon'] as Map?;
                final addonPrice =
                    (a['price'] as num?)?.toDouble() ?? 0;
                return Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: Colors.orange.shade50,
                    border:
                        Border.all(color: Colors.orange.shade200),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    '+ ${addon?['name'] ?? '--'} (${fmt.format(addonPrice)})',
                    style: const TextStyle(
                        fontSize: 10, color: Colors.orange),
                  ),
                );
              }).toList(),
            ),
          ],
        ],
      ),
    );
  }
}

class _HistoryItem extends StatelessWidget {
  const _HistoryItem({
    required this.status,
    required this.notes,
    required this.createdAt,
    required this.isLast,
  });
  final String? status;
  final String? notes;
  final String? createdAt;
  final bool isLast;

  @override
  Widget build(BuildContext context) {
    final color = _statusColor(status);
    final dateStr = createdAt != null
        ? DateFormat('dd/MM\nhh:mm a')
            .format(DateTime.parse(createdAt!).toLocal())
        : '--';

    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Timeline
          Column(
            children: [
              Container(
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                    color: color,
                    shape: BoxShape.circle),
              ),
              if (!isLast)
                Expanded(
                  child: Container(
                      width: 2,
                      color: Colors.grey.shade200),
                ),
            ],
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: Column(
                crossAxisAlignment:
                    CrossAxisAlignment.start,
                children: [
                  Text(_statusAr(status),
                      style: TextStyle(
                          color: color,
                          fontWeight: FontWeight.bold,
                          fontSize: 12)),
                  if (notes != null && notes!.isNotEmpty)
                    Text(notes!,
                        style: const TextStyle(
                            fontSize: 10, color: Colors.grey)),
                  Text(dateStr,
                      style: const TextStyle(
                          fontSize: 10, color: Colors.grey)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
