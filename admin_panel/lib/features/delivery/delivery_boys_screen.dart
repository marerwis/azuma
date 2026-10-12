import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../core/theme/admin_theme.dart';

// ── Repository ────────────────────────────────────────────────────────────────

import 'package:supabase_flutter/supabase_flutter.dart';

class DeliveryRepository {
  final _db = Supabase.instance.client;

  Future<List<Map<String, dynamic>>> getDrivers() async {
    final r = await _db
        .from('users')
        .select('id, full_name, phone, email, is_active, created_at, profile_image_url')
        .eq('role', 'driver')
        .order('created_at', ascending: false);
    return List<Map<String, dynamic>>.from(r);
  }

  Future<Map<String, dynamic>?> getDriverLocation(String driverId) async {
    final r = await _db
        .from('driver_locations')
        .select('latitude, longitude, updated_at, is_online')
        .eq('driver_id', driverId)
        .maybeSingle();
    return r;
  }

  Future<void> toggleActive(String id, bool val) async {
    await _db.from('users').update({'is_active': val}).eq('id', id);
  }
}

// ── Providers ─────────────────────────────────────────────────────────────────

final _deliveryRepoProvider =
    Provider<DeliveryRepository>((_) => DeliveryRepository());

final driversProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(_deliveryRepoProvider).getDrivers();
});

final driverLocationProvider =
    FutureProvider.family<Map<String, dynamic>?, String>(
        (ref, driverId) async {
  return ref.read(_deliveryRepoProvider).getDriverLocation(driverId);
});

// ── Screen ─────────────────────────────────────────────────────────────────────

class DeliveryBoysScreen extends ConsumerStatefulWidget {
  const DeliveryBoysScreen({super.key});

  @override
  ConsumerState<DeliveryBoysScreen> createState() =>
      _DeliveryBoysScreenState();
}

class _DeliveryBoysScreenState extends ConsumerState<DeliveryBoysScreen> {
  String _search = '';
  String? _filterStatus;
  final _dateFmt = DateFormat('dd/MM/yyyy');

  @override
  Widget build(BuildContext context) {
    final driversAsync = ref.watch(driversProvider);

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: Column(
        children: [
          AzScreenHeader(
            title: 'سائقو التوصيل',
            subtitle: 'إدارة حسابات وتتبع سائقي التوصيل',
            icon: Icons.delivery_dining_outlined,
            onRefresh: () => ref.invalidate(driversProvider),
          ),
          Expanded(
            child: driversAsync.when(
              data: (drivers) {
                final filtered = _filter(drivers);
                return Padding(
                  padding: const EdgeInsets.all(16),
                  child: AzCard(
                    child: Column(
                      children: [
                        _toolbar(drivers, filtered.length),
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
              error: (e, _) => _error(e),
            ),
          ),
        ],
      ),
    );
  }

  Widget _toolbar(List<Map<String, dynamic>> all, int count) {
    final active =
        all.where((d) => d['is_active'] as bool? ?? false).length;
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          _Chip(label: 'الكل', count: all.length, color: AzColor.primary,
              selected: _filterStatus == null,
              onTap: () => setState(() => _filterStatus = null)),
          const SizedBox(width: 8),
          _Chip(label: 'متاح', count: active, color: AzColor.success,
              selected: _filterStatus == 'active',
              onTap: () => setState(() => _filterStatus = 'active')),
          const SizedBox(width: 8),
          _Chip(label: 'غير متاح', count: all.length - active,
              color: AzColor.danger,
              selected: _filterStatus == 'inactive',
              onTap: () => setState(() => _filterStatus = 'inactive')),
          const Spacer(),
          AzSearchField(
            onChanged: (v) => setState(() => _search = v),
            hint: 'بحث بالاسم أو الهاتف...',
          ),
        ],
      ),
    );
  }

  Widget _table(List<Map<String, dynamic>> drivers) {
    if (drivers.isEmpty) {
      return const Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.delivery_dining, size: 48, color: AzColor.textMuted),
          SizedBox(height: 12),
          Text('لا يوجد سائقون مطابقون',
              style: TextStyle(color: AzColor.textSecondary)),
        ]),
      );
    }
    return SingleChildScrollView(
      child: SizedBox(
        width: double.infinity,
        child: DataTable(
          columnSpacing: 20,
          dataRowMinHeight: 58,
          dataRowMaxHeight: 70,
          columns: const [
            DataColumn(label: Text('#')),
            DataColumn(label: Text('الصورة')),
            DataColumn(label: Text('الاسم')),
            DataColumn(label: Text('الهاتف')),
            DataColumn(label: Text('تاريخ الانضمام')),
            DataColumn(label: Text('الحالة')),
            DataColumn(label: Text('الموقع')),
            DataColumn(label: Text('تفعيل')),
          ],
          rows: drivers.asMap().entries.map((e) {
            final i = e.key + 1;
            final d = e.value;
            final active = d['is_active'] as bool? ?? false;
            final joinDate = d['created_at'] != null
                ? _dateFmt
                    .format(DateTime.parse(d['created_at']).toLocal())
                : '--';
            return DataRow(cells: [
              DataCell(Text('$i',
                  style: const TextStyle(
                      color: AzColor.textMuted, fontSize: 12))),
              DataCell(_DriverAvatar(
                url: d['profile_image_url']?.toString(),
                name: d['full_name']?.toString() ?? '?',
              )),
              DataCell(Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(d['full_name'] ?? '--',
                      style: const TextStyle(
                          fontWeight: FontWeight.w600,
                          color: AzColor.textPrimary,
                          fontSize: 13)),
                  Text(d['email'] ?? '',
                      style: const TextStyle(
                          color: AzColor.textMuted, fontSize: 11)),
                ],
              )),
              DataCell(Text(d['phone'] ?? '--',
                  style: const TextStyle(
                      color: AzColor.textSecondary, fontSize: 13))),
              DataCell(Text(joinDate,
                  style: const TextStyle(
                      color: AzColor.textMuted, fontSize: 12))),
              DataCell(AzStatusBadge(isActive: active)),
              // Location button
              DataCell(
                TextButton.icon(
                  style: TextButton.styleFrom(
                    foregroundColor: AzColor.info,
                    padding: const EdgeInsets.symmetric(
                        horizontal: 10, vertical: 6),
                  ),
                  onPressed: () => _showLocation(d),
                  icon: const Icon(Icons.location_on_outlined, size: 16),
                  label: const Text('تتبع', style: TextStyle(fontSize: 12)),
                ),
              ),
              DataCell(Switch.adaptive(
                value: active,
                activeColor: AzColor.success,
                inactiveThumbColor: AzColor.danger,
                inactiveTrackColor:
                    AzColor.danger.withValues(alpha: 0.3),
                onChanged: (v) async {
                  await ref
                      .read(_deliveryRepoProvider)
                      .toggleActive(d['id'].toString(), v);
                  ref.invalidate(driversProvider);
                },
              )),
            ]);
          }).toList(),
        ),
      ),
    );
  }

  List<Map<String, dynamic>> _filter(List<Map<String, dynamic>> list) {
    return list.where((d) {
      final name = (d['full_name'] ?? '').toString().toLowerCase();
      final phone = (d['phone'] ?? '').toString().toLowerCase();
      final q = _search.toLowerCase();
      final matchSearch =
          _search.isEmpty || name.contains(q) || phone.contains(q);
      final isActive = d['is_active'] as bool? ?? false;
      final matchStatus = _filterStatus == null ||
          (_filterStatus == 'active' && isActive) ||
          (_filterStatus == 'inactive' && !isActive);
      return matchSearch && matchStatus;
    }).toList();
  }

  void _showLocation(Map<String, dynamic> driver) {
    showDialog(
      context: context,
      builder: (ctx) => _LocationDialog(
          driver: driver, ref: ref),
    );
  }

  Widget _error(Object e) => Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const Icon(Icons.error_outline,
              color: AzColor.danger, size: 48),
          const SizedBox(height: 12),
          Text('$e',
              style: const TextStyle(color: AzColor.textSecondary)),
          const SizedBox(height: 16),
          ElevatedButton.icon(
            onPressed: () => ref.invalidate(driversProvider),
            icon: const Icon(Icons.refresh),
            label: const Text('إعادة المحاولة'),
          ),
        ]),
      );
}

// ── Location Dialog ───────────────────────────────────────────────────────────

class _LocationDialog extends ConsumerWidget {
  const _LocationDialog({required this.driver, required this.ref});
  final Map<String, dynamic> driver;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context, WidgetRef watchRef) {
    final locationAsync =
        watchRef.watch(driverLocationProvider(driver['id'].toString()));
    final timeFmt = DateFormat('dd/MM/yyyy – hh:mm a');

    return Dialog(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AzColor.info.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.location_on,
                    color: AzColor.info, size: 20),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                  Text(driver['full_name'] ?? '--',
                      style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AzColor.textPrimary)),
                  const Text('آخر موقع معروف',
                      style: TextStyle(
                          color: AzColor.textMuted, fontSize: 12)),
                ]),
              ),
              IconButton(
                icon: const Icon(Icons.close,
                    color: AzColor.textSecondary),
                onPressed: () => Navigator.pop(context),
              ),
            ]),
            const SizedBox(height: 20),
            const Divider(color: AzColor.bgBorder),
            const SizedBox(height: 16),

            // Location content
            locationAsync.when(
              data: (loc) {
                if (loc == null) {
                  return Container(
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: AzColor.bgElevated,
                      borderRadius: BorderRadius.circular(10),
                      border:
                          Border.all(color: AzColor.bgBorder),
                    ),
                    child: const Row(children: [
                      Icon(Icons.location_off,
                          color: AzColor.textMuted, size: 32),
                      SizedBox(width: 12),
                      Text('لا يوجد موقع مسجل لهذا السائق',
                          style: TextStyle(
                              color: AzColor.textSecondary)),
                    ]),
                  );
                }

                final isOnline = loc['is_online'] as bool? ?? false;
                final lat =
                    (loc['latitude'] as num?)?.toStringAsFixed(6) ??
                        '--';
                final lng =
                    (loc['longitude'] as num?)?.toStringAsFixed(6) ??
                        '--';
                final updatedAt = loc['updated_at'] != null
                    ? timeFmt.format(
                        DateTime.parse(loc['updated_at']).toLocal())
                    : '--';

                return Column(children: [
                  // Online status
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: isOnline
                          ? AzColor.success.withValues(alpha: 0.1)
                          : AzColor.danger.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                          color: isOnline
                              ? AzColor.success.withValues(alpha: 0.3)
                              : AzColor.danger.withValues(alpha: 0.3)),
                    ),
                    child: Row(children: [
                      Icon(
                          isOnline
                              ? Icons.circle
                              : Icons.circle_outlined,
                          color: isOnline
                              ? AzColor.success
                              : AzColor.danger,
                          size: 10),
                      const SizedBox(width: 8),
                      Text(
                          isOnline
                              ? 'متصل الآن'
                              : 'غير متصل',
                          style: TextStyle(
                              color: isOnline
                                  ? AzColor.success
                                  : AzColor.danger,
                              fontWeight: FontWeight.bold)),
                    ]),
                  ),
                  const SizedBox(height: 12),
                  // Coordinates
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: AzColor.bgElevated,
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: AzColor.bgBorder),
                    ),
                    child: Column(children: [
                      _LocRow(
                          icon: Icons.my_location,
                          label: 'خط العرض (latitude)',
                          value: lat),
                      const SizedBox(height: 10),
                      _LocRow(
                          icon: Icons.explore_outlined,
                          label: 'خط الطول (longitude)',
                          value: lng),
                      const SizedBox(height: 10),
                      _LocRow(
                          icon: Icons.access_time,
                          label: 'آخر تحديث',
                          value: updatedAt),
                    ]),
                  ),
                  const SizedBox(height: 12),
                  // Google Maps link
                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        // URL opens in a new tab (Web)
                        // ignore: avoid_print
                        print(
                            'https://maps.google.com/?q=$lat,$lng');
                      },
                      icon: const Icon(Icons.map_outlined, size: 16),
                      label: const Text('فتح في خرائط Google'),
                    ),
                  ),
                ]);
              },
              loading: () => const Padding(
                padding: EdgeInsets.all(24),
                child: Center(
                    child: CircularProgressIndicator(
                        color: AzColor.primary)),
              ),
              error: (e, _) => Text('خطأ: $e',
                  style: const TextStyle(color: AzColor.danger)),
            ),
          ],
        ),
      ),
    );
  }
}

class _LocRow extends StatelessWidget {
  const _LocRow(
      {required this.icon, required this.label, required this.value});
  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Icon(icon, color: AzColor.primary, size: 16),
      const SizedBox(width: 10),
      Text('$label: ',
          style: const TextStyle(
              color: AzColor.textMuted, fontSize: 12)),
      Expanded(
        child: Text(value,
            style: const TextStyle(
                color: AzColor.textPrimary,
                fontWeight: FontWeight.w600,
                fontSize: 13),
            textAlign: TextAlign.end),
      ),
    ]);
  }
}

// ── Tiny reusable widgets ─────────────────────────────────────────────────────

class _DriverAvatar extends StatelessWidget {
  const _DriverAvatar({this.url, required this.name});
  final String? url;
  final String name;

  @override
  Widget build(BuildContext context) {
    final initials = name.trim().isEmpty
        ? '?'
        : name.trim().split(' ').map((w) => w[0]).take(2).join();
    return Container(
      width: 36,
      height: 36,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: AzColor.info.withValues(alpha: 0.2),
        border: Border.all(color: AzColor.bgBorder),
      ),
      clipBehavior: Clip.antiAlias,
      child: url != null && url!.startsWith('http')
          ? Image.network(url!, fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Center(
                    child: Text(initials,
                        style: const TextStyle(
                            color: AzColor.info,
                            fontWeight: FontWeight.bold,
                            fontSize: 13)),
                  ))
          : Center(
              child: Text(initials,
                  style: const TextStyle(
                      color: AzColor.info,
                      fontWeight: FontWeight.bold,
                      fontSize: 13))),
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip({
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
