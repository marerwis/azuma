import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../core/theme/admin_theme.dart';
import 'data/customer_repository.dart';

// ── Providers ─────────────────────────────────────────────────────────────────

final _customerRepoProvider = Provider<CustomerRepository>(
    (_) => CustomerRepository());

final customersProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(_customerRepoProvider).getCustomers();
});

// ── Screen ─────────────────────────────────────────────────────────────────────

class CustomersScreen extends ConsumerStatefulWidget {
  const CustomersScreen({super.key});

  @override
  ConsumerState<CustomersScreen> createState() => _CustomersScreenState();
}

class _CustomersScreenState extends ConsumerState<CustomersScreen> {
  String _search = '';
  String? _filterStatus; // null = all, 'active', 'inactive'
  final _dateFmt = DateFormat('dd/MM/yyyy');

  @override
  Widget build(BuildContext context) {
    final customersAsync = ref.watch(customersProvider);

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: Column(
        children: [
          // Header
          AzScreenHeader(
            title: 'إدارة العملاء',
            subtitle: 'عرض وإدارة حسابات العملاء المسجلين',
            icon: Icons.people_alt_outlined,
            onRefresh: () => ref.invalidate(customersProvider),
          ),

          // Content
          Expanded(
            child: customersAsync.when(
              data: (customers) {
                final filtered = _applyFilter(customers);
                return Padding(
                  padding: const EdgeInsets.all(16),
                  child: AzCard(
                    child: Column(
                      children: [
                        _buildToolbar(customers, filtered.length),
                        const Divider(height: 1),
                        Expanded(child: _buildTable(filtered)),
                      ],
                    ),
                  ),
                );
              },
              loading: () => const Center(
                  child: CircularProgressIndicator(color: AzColor.primary)),
              error: (e, _) => _buildError(e),
            ),
          ),
        ],
      ),
    );
  }

  // ── Toolbar ────────────────────────────────────────────────────────────────

  Widget _buildToolbar(List<Map<String, dynamic>> all, int filteredCount) {
    final active = all.where((u) => u['is_active'] as bool? ?? false).length;
    final inactive = all.length - active;

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          // Stats
          _StatChip(label: 'الكل', count: all.length,
              color: AzColor.primary,
              selected: _filterStatus == null,
              onTap: () => setState(() => _filterStatus = null)),
          const SizedBox(width: 8),
          _StatChip(label: 'نشط', count: active,
              color: AzColor.success,
              selected: _filterStatus == 'active',
              onTap: () => setState(() => _filterStatus = 'active')),
          const SizedBox(width: 8),
          _StatChip(label: 'معطل', count: inactive,
              color: AzColor.danger,
              selected: _filterStatus == 'inactive',
              onTap: () => setState(() => _filterStatus = 'inactive')),

          const Spacer(),

          // Search
          AzSearchField(
            onChanged: (v) => setState(() => _search = v),
            hint: 'بحث بالاسم أو البريد أو الهاتف...',
          ),
        ],
      ),
    );
  }

  // ── Table ──────────────────────────────────────────────────────────────────

  Widget _buildTable(List<Map<String, dynamic>> customers) {
    if (customers.isEmpty) {
      return const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.person_search, size: 48, color: AzColor.textMuted),
            SizedBox(height: 12),
            Text('لا يوجد عملاء مطابقون',
                style: TextStyle(color: AzColor.textSecondary)),
          ],
        ),
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
            DataColumn(label: Text('الاسم الكامل')),
            DataColumn(label: Text('البريد الإلكتروني')),
            DataColumn(label: Text('رقم الهاتف')),
            DataColumn(label: Text('تاريخ التسجيل')),
            DataColumn(label: Text('الحالة')),
            DataColumn(label: Text('إجراءات')),
          ],
          rows: customers.asMap().entries.map((entry) {
            final i = entry.key + 1;
            final u = entry.value;
            final isActive = u['is_active'] as bool? ?? false;
            final joinDate = u['created_at'] != null
                ? _dateFmt.format(DateTime.parse(u['created_at']).toLocal())
                : '--';

            return DataRow(cells: [
              // #
              DataCell(Text('$i',
                  style: const TextStyle(
                      color: AzColor.textMuted, fontSize: 12))),

              // Avatar
              DataCell(_UserAvatar(
                imageUrl: u['profile_image_url']?.toString(),
                name: u['full_name']?.toString() ?? '?',
              )),

              // Name
              DataCell(Text(
                u['full_name'] ?? '--',
                style: const TextStyle(
                    fontWeight: FontWeight.w600,
                    color: AzColor.textPrimary),
              )),

              // Email
              DataCell(Text(
                u['email'] ?? '--',
                style: const TextStyle(
                    color: AzColor.textSecondary, fontSize: 12),
              )),

              // Phone
              DataCell(Text(
                u['phone'] ?? '--',
                style: const TextStyle(
                    color: AzColor.textSecondary, fontSize: 12),
              )),

              // Join date
              DataCell(Text(joinDate,
                  style: const TextStyle(
                      color: AzColor.textMuted, fontSize: 12))),

              // Status pill
              DataCell(AzStatusBadge(isActive: isActive)),

              // Toggle action
              DataCell(
                Switch.adaptive(
                  value: isActive,
                  activeColor: AzColor.success,
                  inactiveThumbColor: AzColor.danger,
                  inactiveTrackColor: AzColor.danger.withValues(alpha: 0.3),
                  onChanged: (v) async {
                    try {
                      await ref
                          .read(_customerRepoProvider)
                          .toggleActive(u['id'].toString(), v);
                      ref.invalidate(customersProvider);
                    } catch (e) {
                      if (mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text('خطأ: $e'),
                            backgroundColor: AzColor.danger,
                          ),
                        );
                      }
                    }
                  },
                ),
              ),
            ]);
          }).toList(),
        ),
      ),
    );
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  List<Map<String, dynamic>> _applyFilter(List<Map<String, dynamic>> list) {
    return list.where((u) {
      final name = (u['full_name'] ?? '').toString().toLowerCase();
      final email = (u['email'] ?? '').toString().toLowerCase();
      final phone = (u['phone'] ?? '').toString().toLowerCase();
      final q = _search.toLowerCase();

      final matchSearch = _search.isEmpty ||
          name.contains(q) ||
          email.contains(q) ||
          phone.contains(q);

      final isActive = u['is_active'] as bool? ?? false;
      final matchStatus = _filterStatus == null ||
          (_filterStatus == 'active' && isActive) ||
          (_filterStatus == 'inactive' && !isActive);

      return matchSearch && matchStatus;
    }).toList();
  }

  Widget _buildError(Object e) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.error_outline, color: AzColor.danger, size: 48),
          const SizedBox(height: 12),
          Text('$e',
              textAlign: TextAlign.center,
              style: const TextStyle(color: AzColor.textSecondary)),
          const SizedBox(height: 16),
          ElevatedButton.icon(
            onPressed: () => ref.invalidate(customersProvider),
            icon: const Icon(Icons.refresh),
            label: const Text('إعادة المحاولة'),
          ),
        ],
      ),
    );
  }
}

// ── Small reusable widgets ────────────────────────────────────────────────────

class _UserAvatar extends StatelessWidget {
  const _UserAvatar({this.imageUrl, required this.name});
  final String? imageUrl;
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
        color: AzColor.primary.withValues(alpha: 0.2),
        border: Border.all(color: AzColor.bgBorder),
      ),
      clipBehavior: Clip.antiAlias,
      child: imageUrl != null && imageUrl!.startsWith('http')
          ? Image.network(imageUrl!, fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => _InitialsText(initials))
          : _InitialsText(initials),
    );
  }
}

class _InitialsText extends StatelessWidget {
  const _InitialsText(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(text,
          style: const TextStyle(
              color: AzColor.primary,
              fontWeight: FontWeight.bold,
              fontSize: 13)),
    );
  }
}

class _StatChip extends StatelessWidget {
  const _StatChip({
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
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: selected
              ? color.withValues(alpha: 0.15)
              : AzColor.bgElevated,
          border: Border.all(
              color: selected ? color : AzColor.bgBorder),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(label,
                style: TextStyle(
                    color: selected ? color : AzColor.textSecondary,
                    fontSize: 12)),
            const SizedBox(width: 6),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
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
          ],
        ),
      ),
    );
  }
}
