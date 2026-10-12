import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:intl/intl.dart';
import '../../core/theme/admin_theme.dart';
import 'providers/navigation_provider.dart';

// ── Stats Provider ─────────────────────────────────────────────────────────────

final _db = Supabase.instance.client;

final dashboardStatsProvider =
    FutureProvider<Map<String, dynamic>>((ref) async {
  final results = await Future.wait([
    _db.from('orders').select('id, status, total_amount, created_at').order('created_at', ascending: false).limit(200),
    _db.from('users').select('id, role, created_at').order('created_at', ascending: false),
    _db.from('stores').select('id, name, is_active, created_at').order('created_at', ascending: false),
    _db.from('products').select('id, is_active').limit(1000),
    _db.from('coupons').select('id, is_active').limit(100),
    _db.from('reviews').select('id, rating').limit(500),
  ]);

  final orders    = List<Map<String, dynamic>>.from(results[0]);
  final users     = List<Map<String, dynamic>>.from(results[1]);
  final stores    = List<Map<String, dynamic>>.from(results[2]);
  final products  = List<Map<String, dynamic>>.from(results[3]);
  final coupons   = List<Map<String, dynamic>>.from(results[4]);
  final reviews   = List<Map<String, dynamic>>.from(results[5]);

  final customers = users.where((u) => u['role'] == 'customer').length;
  final drivers   = users.where((u) => u['role'] == 'driver').length;
  final pending   = orders.where((o) => o['status'] == 'pending' || o['status'] == 'preparing').length;
  final completed = orders.where((o) => o['status'] == 'completed' || o['status'] == 'delivered').length;
  final cancelled = orders.where((o) => o['status'] == 'cancelled').length;
  final revenue   = orders
      .where((o) => o['status'] == 'completed' || o['status'] == 'delivered')
      .fold<double>(0, (s, o) => s + (((o['total_amount'] ?? o['total_price']) as num?)?.toDouble() ?? 0));
  final avgRating = reviews.isEmpty
      ? 0.0
      : reviews.fold<double>(0, (s, r) => s + ((r['rating'] as num?)?.toDouble() ?? 0)) /
            reviews.length;

  // Last 7 days orders (for sparkline)
  final now = DateTime.now();
  final Map<String, int> dailyOrders = {};
  for (int i = 6; i >= 0; i--) {
    final d = now.subtract(Duration(days: i));
    final key = DateFormat('E').format(d); // Mon, Tue...
    dailyOrders[key] = 0;
  }
  for (final o in orders) {
    if (o['created_at'] != null) {
      final d = DateTime.parse(o['created_at']).toLocal();
      if (now.difference(d).inDays <= 6) {
        final key = DateFormat('E').format(d);
        dailyOrders[key] = (dailyOrders[key] ?? 0) + 1;
      }
    }
  }

  return {
    'orders_total'   : orders.length,
    'orders_pending' : pending,
    'orders_completed': completed,
    'orders_cancelled': cancelled,
    'revenue'        : revenue,
    'customers'      : customers,
    'drivers'        : drivers,
    'stores_total'   : stores.length,
    'stores_active'  : stores.where((s) => s['is_active'] as bool? ?? false).length,
    'products'       : products.length,
    'coupons_active' : coupons.where((c) => c['is_active'] as bool? ?? false).length,
    'reviews'        : reviews.length,
    'avg_rating'     : avgRating,
    'daily_orders'   : dailyOrders,
    'recent_orders'  : orders.take(6).toList(),
  };
});

// ── Screen ─────────────────────────────────────────────────────────────────────

class OverviewScreen extends ConsumerWidget {
  const OverviewScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statsAsync = ref.watch(dashboardStatsProvider);
    final fmt = NumberFormat('#,##0.00');

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: statsAsync.when(
        loading: () => const Center(
            child: CircularProgressIndicator(color: AzColor.primary)),
        error: (e, _) => Center(
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            const Icon(Icons.error_outline, color: AzColor.danger, size: 48),
            const SizedBox(height: 12),
            Text('$e', style: const TextStyle(color: AzColor.textSecondary)),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () => ref.invalidate(dashboardStatsProvider),
              icon: const Icon(Icons.refresh), label: const Text('إعادة التحميل'),
            ),
          ]),
        ),
        data: (s) => SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // ── Top header bar ────────────────────────────────────────────
              _TopBar(ref: ref),
              const SizedBox(height: 24),

              // ── KPI Cards Row 1 ───────────────────────────────────────────
              _sectionLabel('📊 الإحصائيات الرئيسية'),
              const SizedBox(height: 12),
              LayoutBuilder(builder: (ctx, constraints) {
                final w = constraints.maxWidth;
                final cols = w > 1100 ? 4 : w > 700 ? 3 : 2;
                return _CardGrid(cols: cols, children: [
                  _KpiCard(
                    label: 'إجمالي الطلبات',
                    value: '${s['orders_total']}',
                    icon: Icons.receipt_long,
                    colors: [const Color(0xFF6366F1), const Color(0xFF8B5CF6)],
                    sub: '${s['orders_pending']} معلّق',
                    subColor: AzColor.warning,
                    onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(4),
                  ),
                  _KpiCard(
                    label: 'الإيرادات المكتملة',
                    value: '${fmt.format(s['revenue'])} د.ل',
                    icon: Icons.attach_money,
                    colors: [const Color(0xFF059669), const Color(0xFF10B981)],
                    sub: '${s['orders_completed']} طلب مكتمل',
                    subColor: AzColor.success,
                  ),
                  _KpiCard(
                    label: 'العملاء',
                    value: '${s['customers']}',
                    icon: Icons.people,
                    colors: [const Color(0xFFDB2777), const Color(0xFFF472B6)],
                    sub: 'مسجّل في التطبيق',
                    subColor: AzColor.textMuted,
                    onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(5),
                  ),
                  _KpiCard(
                    label: 'المطاعم النشطة',
                    value: '${s['stores_active']}',
                    icon: Icons.storefront,
                    colors: [const Color(0xFFD97706), const Color(0xFFF59E0B)],
                    sub: 'من ${s['stores_total']} مسجّل',
                    subColor: AzColor.textMuted,
                    onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(2),
                  ),
                ]);
              }),
              const SizedBox(height: 14),

              // ── KPI Cards Row 2 ───────────────────────────────────────────
              LayoutBuilder(builder: (ctx, constraints) {
                final w = constraints.maxWidth;
                final cols = w > 1100 ? 4 : w > 700 ? 3 : 2;
                return _CardGrid(cols: cols, children: [
                  _KpiCard(
                    label: 'طلبات ملغية',
                    value: '${s['orders_cancelled']}',
                    icon: Icons.cancel_outlined,
                    colors: [const Color(0xFFDC2626), const Color(0xFFEF4444)],
                    sub: 'من إجمالي الطلبات',
                    subColor: AzColor.danger,
                  ),
                  _KpiCard(
                    label: 'سائقو التوصيل',
                    value: '${s['drivers']}',
                    icon: Icons.delivery_dining,
                    colors: [const Color(0xFF0891B2), const Color(0xFF06B6D4)],
                    sub: 'سائق مسجّل',
                    subColor: AzColor.info,
                    onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(6),
                  ),
                  _KpiCard(
                    label: 'المنتجات',
                    value: '${s['products']}',
                    icon: Icons.restaurant_menu,
                    colors: [const Color(0xFF7C3AED), const Color(0xFFA78BFA)],
                    sub: 'في الكتالوج',
                    subColor: AzColor.textMuted,
                    onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(3),
                  ),
                  _KpiCard(
                    label: 'التقييمات',
                    value: '${s['reviews']}',
                    icon: Icons.star,
                    colors: [const Color(0xFFB45309), const Color(0xFFF59E0B)],
                    sub: '★ ${((s['avg_rating'] as num?)?.toDouble() ?? 0.0).toStringAsFixed(1)} متوسط',
                    subColor: AzColor.warning,
                    onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(12),
                  ),
                ]);
              }),

              const SizedBox(height: 28),

              // ── Bottom section: sparkline + recent orders ─────────────────
              LayoutBuilder(builder: (ctx, constraints) {
                final wide = constraints.maxWidth > 900;
                if (wide) {
                  return Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        flex: 2,
                        child: _RecentOrdersCard(
                            orders: List<Map<String, dynamic>>.from(s['recent_orders'])),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        flex: 1,
                        child: Column(children: [
                          _WeeklySparkCard(data: Map<String, int>.from(s['daily_orders'])),
                          const SizedBox(height: 16),
                          _QuickLinksCard(ref: ref),
                        ]),
                      ),
                    ],
                  );
                } else {
                  return Column(children: [
                    _RecentOrdersCard(
                        orders: List<Map<String, dynamic>>.from(s['recent_orders'])),
                    const SizedBox(height: 16),
                    _WeeklySparkCard(data: Map<String, int>.from(s['daily_orders'])),
                    const SizedBox(height: 16),
                    _QuickLinksCard(ref: ref),
                  ]);
                }
              }),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Top bar ────────────────────────────────────────────────────────────────────

class _TopBar extends StatelessWidget {
  const _TopBar({required this.ref});
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    String now;
    try {
      now = DateFormat('EEEE, d MMMM yyyy', 'ar').format(DateTime.now());
    } catch (_) {
      now = DateFormat('yyyy-MM-dd').format(DateTime.now());
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF1E293B), Color(0xFF273549)],
          begin: Alignment.topLeft, end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AzColor.bgBorder),
      ),
      child: Row(
        children: [
          // Left: greeting
          Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            const Text('مرحباً، Super Admin 👋',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: AzColor.textPrimary)),
            const SizedBox(height: 2),
            Text(now, style: const TextStyle(color: AzColor.textMuted, fontSize: 12)),
          ]),
          const Spacer(),
          // Refresh
          IconButton(
            icon: const Icon(Icons.refresh, color: AzColor.textSecondary),
            tooltip: 'تحديث',
            onPressed: () => ref.invalidate(dashboardStatsProvider),
          ),
          const SizedBox(width: 8),
          // Status indicator
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: AzColor.success.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: AzColor.success.withValues(alpha: 0.4)),
            ),
            child: const Row(mainAxisSize: MainAxisSize.min, children: [
              Icon(Icons.circle, color: AzColor.success, size: 8),
              SizedBox(width: 6),
              Text('النظام يعمل', style: TextStyle(color: AzColor.success, fontSize: 12, fontWeight: FontWeight.bold)),
            ]),
          ),
        ],
      ),
    );
  }
}

// ── KPI Card ──────────────────────────────────────────────────────────────────

class _KpiCard extends StatefulWidget {
  const _KpiCard({
    required this.label,
    required this.value,
    required this.icon,
    required this.colors,
    required this.sub,
    required this.subColor,
    this.onTap,
  });
  final String label, value, sub;
  final IconData icon;
  final List<Color> colors;
  final Color subColor;
  final VoidCallback? onTap;

  @override
  State<_KpiCard> createState() => _KpiCardState();
}

class _KpiCardState extends State<_KpiCard>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ctrl;
  late final Animation<double> _scale;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 120));
    _scale = Tween<double>(begin: 1.0, end: 0.97).animate(
        CurvedAnimation(parent: _ctrl, curve: Curves.easeInOut));
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) { if (widget.onTap != null) _ctrl.forward(); },
      onTapUp: (_) { _ctrl.reverse(); widget.onTap?.call(); },
      onTapCancel: () => _ctrl.reverse(),
      child: AnimatedBuilder(
        animation: _scale,
        builder: (_, child) =>
            Transform.scale(scale: _scale.value, child: child),
        child: Container(
          height: 120,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            gradient: LinearGradient(
              colors: [
                AzColor.bgSurface,
                AzColor.bgElevated,
              ],
              begin: Alignment.topLeft, end: Alignment.bottomRight,
            ),
            border: Border.all(color: AzColor.bgBorder),
            boxShadow: [
              BoxShadow(
                color: widget.colors[0].withValues(alpha: 0.12),
                blurRadius: 12, offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Stack(
            children: [
              // Coloured left accent bar
              Positioned(
                top: 0, bottom: 0, left: 0,
                child: Container(
                  width: 4,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: widget.colors,
                      begin: Alignment.topCenter, end: Alignment.bottomCenter,
                    ),
                    borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(14),
                      bottomLeft: Radius.circular(14),
                    ),
                  ),
                ),
              ),

              // Background glow
              Positioned(
                top: -20, right: -20,
                child: Container(
                  width: 90, height: 90,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: widget.colors[0].withValues(alpha: 0.06),
                  ),
                ),
              ),

              // Content
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 14, 14, 14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(children: [
                      Container(
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          gradient: LinearGradient(colors: widget.colors),
                          borderRadius: BorderRadius.circular(10),
                          boxShadow: [
                            BoxShadow(
                              color: widget.colors[0].withValues(alpha: 0.35),
                              blurRadius: 8, offset: const Offset(0, 3),
                            ),
                          ],
                        ),
                        child: Icon(widget.icon, color: Colors.white, size: 18),
                      ),
                      const Spacer(),
                      if (widget.onTap != null)
                        Icon(Icons.arrow_forward_ios,
                            color: widget.colors[0], size: 12),
                    ]),
                    const SizedBox(height: 10),
                    Text(widget.value,
                        style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: AzColor.textPrimary,
                            letterSpacing: -0.5)),
                    const SizedBox(height: 2),
                    Text(widget.label,
                        style: const TextStyle(
                            fontSize: 12, color: AzColor.textSecondary)),
                    const Spacer(),
                    Text(widget.sub,
                        style: TextStyle(
                            fontSize: 10,
                            color: widget.subColor,
                            fontWeight: FontWeight.w500)),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Card Grid ────────────────────────────────────────────────────────────────

class _CardGrid extends StatelessWidget {
  const _CardGrid({required this.cols, required this.children});
  final int cols;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    final rows = <Widget>[];
    for (int i = 0; i < children.length; i += cols) {
      final slice = children.skip(i).take(cols).toList();
      rows.add(Row(children: [
        for (int j = 0; j < slice.length; j++) ...[
          Expanded(child: slice[j]),
          if (j < slice.length - 1) const SizedBox(width: 14),
        ],
        // Fill empty slots
        for (int k = slice.length; k < cols; k++) ...[
          const SizedBox(width: 14),
          const Expanded(child: SizedBox()),
        ],
      ]));
      if (i + cols < children.length) rows.add(const SizedBox(height: 14));
    }
    return Column(children: rows);
  }
}

// ── Weekly Spark Card ─────────────────────────────────────────────────────────

class _WeeklySparkCard extends StatelessWidget {
  const _WeeklySparkCard({required this.data});
  final Map<String, int> data;

  @override
  Widget build(BuildContext context) {
    final maxVal = data.values.fold(0, (a, b) => a > b ? a : b);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AzColor.bgSurface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AzColor.bgBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(children: [
            Icon(Icons.bar_chart, color: AzColor.primary, size: 18),
            SizedBox(width: 8),
            Text('طلبات آخر 7 أيام',
                style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: AzColor.textPrimary,
                    fontSize: 13)),
          ]),
          const SizedBox(height: 16),
          SizedBox(
            height: 80,
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: data.entries.map((e) {
                final ratio = maxVal == 0 ? 0.1 : e.value / maxVal;
                return Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 3),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        Text('${e.value}',
                            style: const TextStyle(
                                color: AzColor.textMuted, fontSize: 9)),
                        const SizedBox(height: 2),
                        AnimatedContainer(
                          duration: const Duration(milliseconds: 600),
                          curve: Curves.easeOut,
                          height: (60 * ratio).clamp(4, 60).toDouble(),
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              colors: [AzColor.primary, AzColor.secondary],
                              begin: Alignment.bottomCenter,
                              end: Alignment.topCenter,
                            ),
                            borderRadius: BorderRadius.circular(4),
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(e.key,
                            style: const TextStyle(
                                color: AzColor.textMuted, fontSize: 9)),
                      ],
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Recent Orders Card ────────────────────────────────────────────────────────

class _RecentOrdersCard extends StatelessWidget {
  const _RecentOrdersCard({required this.orders});
  final List<Map<String, dynamic>> orders;

  @override
  Widget build(BuildContext context) {
    final timeFmt = DateFormat('dd/MM hh:mm a');

    return Container(
      decoration: BoxDecoration(
        color: AzColor.bgSurface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AzColor.bgBorder),
      ),
      child: Column(
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(children: [
              Container(
                padding: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                      colors: [AzColor.primary, AzColor.secondary]),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.receipt_long, color: Colors.white, size: 16),
              ),
              const SizedBox(width: 10),
              const Text('أحدث الطلبات',
                  style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: AzColor.textPrimary,
                      fontSize: 14)),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: AzColor.primary.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Text('آخر ${orders.length} طلبات',
                    style: const TextStyle(
                        color: AzColor.primary, fontSize: 11)),
              ),
            ]),
          ),
          const Divider(height: 1, color: AzColor.bgBorder),

          if (orders.isEmpty)
            const Padding(
              padding: EdgeInsets.all(24),
              child: Text('لا توجد طلبات بعد',
                  style: TextStyle(color: AzColor.textMuted)),
            )
          else
            ...orders.asMap().entries.map((e) {
              final i = e.key;
              final o = e.value;
              final status = o['status']?.toString() ?? 'pending';
              final price = (((o['total_amount'] ?? o['total_price']) as num?)?.toDouble() ?? 0);
              final date = o['created_at'] != null
                  ? timeFmt.format(DateTime.parse(o['created_at']).toLocal())
                  : '--';
              final statusInfo = _statusInfo(status);

              return Container(
                decoration: BoxDecoration(
                  color: i.isOdd
                      ? AzColor.bgElevated.withValues(alpha: 0.5)
                      : Colors.transparent,
                ),
                padding: const EdgeInsets.symmetric(
                    horizontal: 16, vertical: 10),
                child: Row(children: [
                  // Order number
                  Container(
                    width: 32, height: 32,
                    decoration: BoxDecoration(
                      color: AzColor.primary.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Center(
                      child: Text('#${i + 1}',
                          style: const TextStyle(
                              color: AzColor.primary,
                              fontSize: 11,
                              fontWeight: FontWeight.bold)),
                    ),
                  ),
                  const SizedBox(width: 12),

                  // ID
                  Expanded(
                    flex: 3,
                    child: Text(
                      o['id']?.toString().substring(0, 8).toUpperCase() ?? '--',
                      style: const TextStyle(
                          fontFamily: 'monospace',
                          fontSize: 12,
                          color: AzColor.textSecondary),
                    ),
                  ),

                  // Status pill
                  Expanded(
                    flex: 2,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: statusInfo.$2.withValues(alpha: 0.12),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                            color: statusInfo.$2.withValues(alpha: 0.3)),
                      ),
                      child: Text(statusInfo.$1,
                          style: TextStyle(
                              color: statusInfo.$2,
                              fontSize: 10,
                              fontWeight: FontWeight.bold),
                          textAlign: TextAlign.center),
                    ),
                  ),

                  // Price
                  Expanded(
                    flex: 2,
                    child: Text(
                      '${price.toStringAsFixed(2)} د.ل',
                      style: const TextStyle(
                          color: AzColor.success,
                          fontWeight: FontWeight.bold,
                          fontSize: 12),
                      textAlign: TextAlign.center,
                    ),
                  ),

                  // Date
                  Expanded(
                    flex: 2,
                    child: Text(date,
                        style: const TextStyle(
                            color: AzColor.textMuted, fontSize: 11),
                        textAlign: TextAlign.end),
                  ),
                ]),
              );
            }),
        ],
      ),
    );
  }

  (String, Color) _statusInfo(String status) {
    switch (status) {
      case 'pending':    return ('معلّق',    AzColor.warning);
      case 'preparing':  return ('يُحضَّر',  AzColor.info);
      case 'on_the_way': return ('في الطريق', const Color(0xFF8B5CF6));
      case 'completed':  return ('مكتمل',   AzColor.success);
      case 'cancelled':  return ('ملغى',    AzColor.danger);
      default:           return (status,    AzColor.textSecondary);
    }
  }
}

// ── Quick Links Card ──────────────────────────────────────────────────────────

class _QuickLinksCard extends StatelessWidget {
  const _QuickLinksCard({required this.ref});
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    final links = [
      (Icons.add_box_outlined,      'إضافة منتج',    3, AzColor.primary),
      (Icons.receipt_long_outlined, 'الطلبات',       4, AzColor.warning),
      (Icons.local_offer_outlined,  'كوبون جديد',   8, AzColor.success),
      (Icons.notifications_outlined,'إشعار جديد',   9, AzColor.info),
      (Icons.star_outline,          'التقييمات',    12, AzColor.warning),
      (Icons.settings_outlined,     'الإعدادات',   13, AzColor.textSecondary),
    ];

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AzColor.bgSurface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AzColor.bgBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(children: [
            Icon(Icons.flash_on, color: AzColor.warning, size: 18),
            SizedBox(width: 8),
            Text('وصول سريع',
                style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: AzColor.textPrimary,
                    fontSize: 13)),
          ]),
          const SizedBox(height: 12),
          ...links.map((l) => Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: InkWell(
                  onTap: () =>
                      ref.read(selectedIndexProvider.notifier).setIndex(l.$3),
                  borderRadius: BorderRadius.circular(8),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 10, vertical: 8),
                    decoration: BoxDecoration(
                      color: l.$4.withValues(alpha: 0.06),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                          color: l.$4.withValues(alpha: 0.2)),
                    ),
                    child: Row(children: [
                      Icon(l.$1, color: l.$4, size: 16),
                      const SizedBox(width: 10),
                      Text(l.$2,
                          style: TextStyle(
                              color: l.$4,
                              fontSize: 12,
                              fontWeight: FontWeight.w500)),
                      const Spacer(),
                      Icon(Icons.arrow_forward_ios,
                          color: l.$4.withValues(alpha: 0.5), size: 11),
                    ]),
                  ),
                ),
              )),
        ],
      ),
    );
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

Widget _sectionLabel(String text) => Text(text,
    style: const TextStyle(
        fontSize: 13,
        fontWeight: FontWeight.bold,
        color: AzColor.textSecondary,
        letterSpacing: 0.5));
