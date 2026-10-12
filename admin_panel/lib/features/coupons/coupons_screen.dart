import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../core/theme/admin_theme.dart';
import 'data/coupon_repository.dart';

// ── Providers ─────────────────────────────────────────────────────────────────

final _couponRepoProvider =
    Provider<CouponRepository>((_) => CouponRepository());

final couponsProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(_couponRepoProvider).getCoupons();
});

// ── Screen ─────────────────────────────────────────────────────────────────────

class CouponsScreen extends ConsumerStatefulWidget {
  const CouponsScreen({super.key});

  @override
  ConsumerState<CouponsScreen> createState() => _CouponsScreenState();
}

class _CouponsScreenState extends ConsumerState<CouponsScreen> {
  String _search = '';
  final _fmt = NumberFormat('#,##0.00');
  final _dateFmt = DateFormat('dd/MM/yyyy');

  @override
  Widget build(BuildContext context) {
    final couponsAsync = ref.watch(couponsProvider);

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: Column(
        children: [
          AzScreenHeader(
            title: 'كودات الخصم',
            subtitle: 'إدارة العروض والكوبونات التسويقية',
            icon: Icons.local_offer_outlined,
            actionLabel: 'إضافة كوبون',
            onAction: () => _showDialog(context, null),
            onRefresh: () => ref.invalidate(couponsProvider),
          ),
          Expanded(
            child: couponsAsync.when(
              data: (coupons) {
                final filtered = _applyFilter(coupons);
                return Padding(
                  padding: const EdgeInsets.all(16),
                  child: AzCard(
                    child: Column(
                      children: [
                        _buildToolbar(filtered.length),
                        const Divider(height: 1),
                        Expanded(child: _buildTable(filtered)),
                      ],
                    ),
                  ),
                );
              },
              loading: () => const Center(
                  child: CircularProgressIndicator(color: AzColor.primary)),
              error: (e, _) => Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.error_outline,
                        color: AzColor.danger, size: 48),
                    const SizedBox(height: 12),
                    Text('$e',
                        style: const TextStyle(
                            color: AzColor.textSecondary)),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: () => ref.invalidate(couponsProvider),
                      icon: const Icon(Icons.refresh),
                      label: const Text('إعادة المحاولة'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Toolbar ────────────────────────────────────────────────────────────────

  Widget _buildToolbar(int count) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: AzColor.primary.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: AzColor.primary.withValues(alpha: 0.3)),
            ),
            child: Text('$count كوبون',
                style: const TextStyle(
                    color: AzColor.primary, fontWeight: FontWeight.bold)),
          ),
          const Spacer(),
          AzSearchField(
            onChanged: (v) => setState(() => _search = v),
            hint: 'بحث بالكود أو العنوان...',
          ),
        ],
      ),
    );
  }

  // ── Table ──────────────────────────────────────────────────────────────────

  Widget _buildTable(List<Map<String, dynamic>> coupons) {
    if (coupons.isEmpty) {
      return const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.discount_outlined,
                size: 48, color: AzColor.textMuted),
            SizedBox(height: 12),
            Text('لا توجد كوبونات مطابقة',
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
          dataRowMaxHeight: 72,
          columns: const [
            DataColumn(label: Text('الكود')),
            DataColumn(label: Text('العنوان')),
            DataColumn(label: Text('نوع الخصم')),
            DataColumn(label: Text('قيمة الخصم')),
            DataColumn(label: Text('الحد الأدنى')),
            DataColumn(label: Text('صالح حتى')),
            DataColumn(label: Text('الاستخدام')),
            DataColumn(label: Text('الحالة')),
            DataColumn(label: Text('إجراءات')),
          ],
          rows: coupons.map((c) {
            final isActive = c['is_active'] as bool? ?? false;
            final discountType = c['discount_type']?.toString() ?? 'fixed';
            final isPercentage = discountType == 'percentage';
            final discountVal =
                (c['discount_value'] as num?)?.toDouble() ?? 0;
            final minOrder =
                (c['min_order_amount'] as num?)?.toDouble();
            final usageLimit = c['usage_limit'] as int?;
            final usedCount = c['used_count'] as int? ?? 0;
            final validUntil = c['valid_until'] != null
                ? _dateFmt
                    .format(DateTime.parse(c['valid_until']).toLocal())
                : '--';

            // Check if expired
            final isExpired = c['valid_until'] != null &&
                DateTime.parse(c['valid_until']).isBefore(DateTime.now());

            return DataRow(cells: [
              // Code
              DataCell(Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: AzColor.secondary.withValues(alpha: 0.12),
                  border: Border.all(
                      color: AzColor.secondary.withValues(alpha: 0.3)),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  c['code'] ?? '--',
                  style: const TextStyle(
                      fontFamily: 'monospace',
                      fontWeight: FontWeight.bold,
                      color: AzColor.secondary,
                      fontSize: 12,
                      letterSpacing: 1),
                ),
              )),

              // Title
              DataCell(SizedBox(
                width: 140,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(c['title'] ?? '--',
                        style: const TextStyle(
                            fontWeight: FontWeight.w600,
                            color: AzColor.textPrimary,
                            fontSize: 13),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis),
                    if (c['description'] != null &&
                        (c['description'] as String).isNotEmpty)
                      Text(c['description'],
                          style: const TextStyle(
                              color: AzColor.textMuted, fontSize: 11),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis),
                  ],
                ),
              )),

              // Discount type
              DataCell(_DiscountTypeBadge(isPercentage)),

              // Discount value
              DataCell(Text(
                isPercentage
                    ? '${discountVal.toStringAsFixed(0)}%'
                    : '${_fmt.format(discountVal)} د.ل',
                style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    color: AzColor.success),
              )),

              // Min order
              DataCell(Text(
                minOrder != null
                    ? '${_fmt.format(minOrder)} د.ل'
                    : '--',
                style: const TextStyle(
                    color: AzColor.textSecondary, fontSize: 12),
              )),

              // Valid until
              DataCell(_ExpiryBadge(
                  dateStr: validUntil, isExpired: isExpired)),

              // Usage
              DataCell(Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    '$usedCount',
                    style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        color: AzColor.textPrimary),
                  ),
                  Text(
                    usageLimit != null ? ' / $usageLimit' : ' / ∞',
                    style: const TextStyle(
                        color: AzColor.textMuted, fontSize: 12),
                  ),
                ],
              )),

              // Status
              DataCell(AzStatusBadge(isActive: isActive && !isExpired)),

              // Actions
              DataCell(Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Toggle active
                  Switch.adaptive(
                    value: isActive,
                    activeColor: AzColor.success,
                    inactiveThumbColor: AzColor.danger,
                    inactiveTrackColor:
                        AzColor.danger.withValues(alpha: 0.3),
                    onChanged: (v) => _toggle(c['id'].toString(), v),
                  ),
                  // Edit
                  IconButton(
                    icon: const Icon(Icons.edit_outlined, size: 17),
                    color: AzColor.info,
                    tooltip: 'تعديل',
                    onPressed: () => _showDialog(context, c),
                  ),
                  // Delete
                  IconButton(
                    icon: const Icon(Icons.delete_outline, size: 17),
                    color: AzColor.danger,
                    tooltip: 'حذف',
                    onPressed: () => _confirmDelete(c['id'].toString()),
                  ),
                ],
              )),
            ]);
          }).toList(),
        ),
      ),
    );
  }

  // ── Filter ─────────────────────────────────────────────────────────────────

  List<Map<String, dynamic>> _applyFilter(List<Map<String, dynamic>> list) {
    if (_search.isEmpty) return list;
    final q = _search.toLowerCase();
    return list.where((c) {
      final code = (c['code'] ?? '').toString().toLowerCase();
      final title = (c['title'] ?? '').toString().toLowerCase();
      return code.contains(q) || title.contains(q);
    }).toList();
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  Future<void> _toggle(String id, bool val) async {
    try {
      await ref.read(_couponRepoProvider).toggleActive(id, val);
      ref.invalidate(couponsProvider);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('خطأ: $e'),
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
        content: const Text('سيتم حذف الكوبون نهائياً.',
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
        await ref.read(_couponRepoProvider).deleteCoupon(id);
        ref.invalidate(couponsProvider);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('تم حذف الكوبون'),
              backgroundColor: AzColor.success,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('خطأ: $e'),
                backgroundColor: AzColor.danger),
          );
        }
      }
    }
  }

  void _showDialog(BuildContext context, Map<String, dynamic>? coupon) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => _CouponFormDialog(
        coupon: coupon,
        onSaved: () => ref.invalidate(couponsProvider),
        repo: ref.read(_couponRepoProvider),
      ),
    );
  }
}

// ── Coupon Form Dialog ────────────────────────────────────────────────────────

class _CouponFormDialog extends StatefulWidget {
  const _CouponFormDialog({
    required this.coupon,
    required this.onSaved,
    required this.repo,
  });
  final Map<String, dynamic>? coupon;
  final VoidCallback onSaved;
  final CouponRepository repo;

  @override
  State<_CouponFormDialog> createState() => _CouponFormDialogState();
}

class _CouponFormDialogState extends State<_CouponFormDialog> {
  final _key = GlobalKey<FormState>();
  bool _saving = false;

  late final TextEditingController _code;
  late final TextEditingController _title;
  late final TextEditingController _description;
  late final TextEditingController _discountValue;
  late final TextEditingController _minOrder;
  late final TextEditingController _maxDiscount;
  late final TextEditingController _usageLimit;

  String _discountType = 'percentage'; // 'percentage' | 'fixed'
  bool _isActive = true;
  DateTime? _validFrom;
  DateTime? _validUntil;

  bool get _isEdit => widget.coupon != null;

  @override
  void initState() {
    super.initState();
    final c = widget.coupon;
    _code = TextEditingController(text: c?['code'] ?? '');
    _title = TextEditingController(text: c?['title'] ?? '');
    _description =
        TextEditingController(text: c?['description'] ?? '');
    _discountValue = TextEditingController(
        text: (c?['discount_value'] as num?)?.toString() ?? '');
    _minOrder = TextEditingController(
        text: (c?['min_order_amount'] as num?)?.toString() ?? '');
    _maxDiscount = TextEditingController(
        text: (c?['max_discount_amount'] as num?)?.toString() ?? '');
    _usageLimit = TextEditingController(
        text: (c?['usage_limit'] as int?)?.toString() ?? '');
    _discountType = c?['discount_type'] ?? 'percentage';
    _isActive = c?['is_active'] as bool? ?? true;
    _validFrom =
        c?['valid_from'] != null ? DateTime.parse(c!['valid_from']) : null;
    _validUntil =
        c?['valid_until'] != null ? DateTime.parse(c!['valid_until']) : null;
  }

  @override
  void dispose() {
    for (final ctrl in [
      _code, _title, _description, _discountValue,
      _minOrder, _maxDiscount, _usageLimit
    ]) {
      ctrl.dispose();
    }
    super.dispose();
  }

  Future<void> _save() async {
    if (!_key.currentState!.validate()) return;
    setState(() => _saving = true);
    try {
      final args = (
        code: _code.text,
        title: _title.text,
        description: _description.text.isEmpty ? null : _description.text,
        discountType: _discountType,
        discountValue: double.parse(_discountValue.text.trim()),
        minOrderAmount: _minOrder.text.isEmpty
            ? null
            : double.tryParse(_minOrder.text.trim()),
        maxDiscountAmount: _maxDiscount.text.isEmpty
            ? null
            : double.tryParse(_maxDiscount.text.trim()),
        validFrom: _validFrom,
        validUntil: _validUntil,
        usageLimit: _usageLimit.text.isEmpty
            ? null
            : int.tryParse(_usageLimit.text.trim()),
        isActive: _isActive,
      );

      if (_isEdit) {
        await widget.repo.updateCoupon(
          widget.coupon!['id'].toString(),
          code: args.code,
          title: args.title,
          description: args.description,
          discountType: args.discountType,
          discountValue: args.discountValue,
          minOrderAmount: args.minOrderAmount,
          maxDiscountAmount: args.maxDiscountAmount,
          validFrom: args.validFrom,
          validUntil: args.validUntil,
          usageLimit: args.usageLimit,
          isActive: args.isActive,
        );
      } else {
        await widget.repo.createCoupon(
          code: args.code,
          title: args.title,
          description: args.description,
          discountType: args.discountType,
          discountValue: args.discountValue,
          minOrderAmount: args.minOrderAmount,
          maxDiscountAmount: args.maxDiscountAmount,
          validFrom: args.validFrom,
          validUntil: args.validUntil,
          usageLimit: args.usageLimit,
          isActive: args.isActive,
        );
      }
      widget.onSaved();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text('خطأ: $e'),
              backgroundColor: AzColor.danger),
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _pickDate(bool isFrom) async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: isFrom
          ? (_validFrom ?? now)
          : (_validUntil ?? now.add(const Duration(days: 30))),
      firstDate: now.subtract(const Duration(days: 365)),
      lastDate: now.add(const Duration(days: 365 * 3)),
      builder: (ctx, child) => Theme(
        data: Theme.of(ctx).copyWith(
          colorScheme: const ColorScheme.dark(
              primary: AzColor.primary, surface: AzColor.bgSurface),
        ),
        child: child!,
      ),
    );
    if (picked != null) {
      setState(() {
        if (isFrom) {
          _validFrom = picked;
        } else {
          _validUntil = picked;
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final dateFmt = DateFormat('dd/MM/yyyy');

    return Dialog(
      child: SizedBox(
        width: 640,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Header
            Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: 24, vertical: 16),
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [AzColor.primary, AzColor.secondary],
                ),
                borderRadius: BorderRadius.vertical(
                    top: Radius.circular(16)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.local_offer, color: Colors.white),
                  const SizedBox(width: 10),
                  Text(
                    _isEdit ? 'تعديل الكوبون' : 'إضافة كوبون جديد',
                    style: const TextStyle(
                        color: Colors.white,
                        fontSize: 17,
                        fontWeight: FontWeight.bold),
                  ),
                  const Spacer(),
                  IconButton(
                    icon: const Icon(Icons.close, color: Colors.white),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),

            // Body
            Flexible(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(24),
                child: Form(
                  key: _key,
                  child: Column(
                    children: [
                      // Row 1: Code + Title
                      Row(children: [
                        Expanded(child: _field(_code, 'الكود (code) *',
                            validator: (v) => v == null || v.trim().isEmpty
                                ? 'مطلوب'
                                : null)),
                        const SizedBox(width: 16),
                        Expanded(child: _field(_title, 'العنوان (title) *',
                            validator: (v) => v == null || v.trim().isEmpty
                                ? 'مطلوب'
                                : null)),
                      ]),
                      const SizedBox(height: 14),

                      // Description
                      _field(_description, 'الوصف (description)',
                          maxLines: 2),
                      const SizedBox(height: 14),

                      // Row 2: Discount type + value
                      Row(children: [
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            value: _discountType,
                            decoration: const InputDecoration(
                                labelText: 'نوع الخصم (discount_type)'),
                            dropdownColor: AzColor.bgElevated,
                            style: const TextStyle(
                                color: AzColor.textPrimary),
                            items: const [
                              DropdownMenuItem(
                                  value: 'percentage',
                                  child: Text('نسبة مئوية %')),
                              DropdownMenuItem(
                                  value: 'fixed',
                                  child: Text('مبلغ ثابت د.ل')),
                            ],
                            onChanged: (v) =>
                                setState(() => _discountType = v!),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: _field(_discountValue,
                              'قيمة الخصم (discount_value) *',
                              keyboardType: TextInputType.number,
                              validator: (v) {
                            if (v == null || v.trim().isEmpty)
                              return 'مطلوب';
                            if (double.tryParse(v.trim()) == null)
                              return 'رقم غير صالح';
                            return null;
                          }),
                        ),
                      ]),
                      const SizedBox(height: 14),

                      // Row 3: Min order + Max discount
                      Row(children: [
                        Expanded(
                            child: _field(
                                _minOrder,
                                'الحد الأدنى للطلب (min_order_amount)',
                                keyboardType: TextInputType.number)),
                        const SizedBox(width: 16),
                        Expanded(
                            child: _field(
                                _maxDiscount,
                                'أقصى خصم (max_discount_amount)',
                                keyboardType: TextInputType.number)),
                      ]),
                      const SizedBox(height: 14),

                      // Row 4: Dates
                      Row(children: [
                        Expanded(
                          child: _DatePicker(
                            label: 'صالح من (valid_from)',
                            value: _validFrom != null
                                ? dateFmt.format(_validFrom!)
                                : null,
                            onTap: () => _pickDate(true),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: _DatePicker(
                            label: 'صالح حتى (valid_until)',
                            value: _validUntil != null
                                ? dateFmt.format(_validUntil!)
                                : null,
                            onTap: () => _pickDate(false),
                          ),
                        ),
                      ]),
                      const SizedBox(height: 14),

                      // Row 5: Usage limit + Active
                      Row(children: [
                        Expanded(
                            child: _field(
                                _usageLimit,
                                'حد الاستخدام (usage_limit)',
                                keyboardType: TextInputType.number,
                                hint: 'اتركه فارغاً لاستخدام غير محدود')),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 12, vertical: 10),
                            decoration: BoxDecoration(
                              color: AzColor.bgElevated,
                              borderRadius: BorderRadius.circular(8),
                              border: Border.all(color: AzColor.bgBorder),
                            ),
                            child: Row(
                              children: [
                                const Expanded(
                                  child: Text('مفعّل (is_active)',
                                      style: TextStyle(
                                          color: AzColor.textSecondary,
                                          fontSize: 13)),
                                ),
                                Switch.adaptive(
                                  value: _isActive,
                                  activeColor: AzColor.success,
                                  onChanged: (v) =>
                                      setState(() => _isActive = v),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ]),
                    ],
                  ),
                ),
              ),
            ),

            // Footer
            Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: 24, vertical: 14),
              decoration: const BoxDecoration(
                color: AzColor.bgElevated,
                border: Border(top: BorderSide(color: AzColor.bgBorder)),
                borderRadius: BorderRadius.vertical(
                    bottom: Radius.circular(16)),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  OutlinedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('إلغاء'),
                  ),
                  const SizedBox(width: 12),
                  ElevatedButton.icon(
                    onPressed: _saving ? null : _save,
                    icon: _saving
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(
                                strokeWidth: 2, color: Colors.white))
                        : Icon(_isEdit ? Icons.save : Icons.add),
                    label: Text(_isEdit ? 'حفظ التعديلات' : 'إضافة',
                        style: const TextStyle(
                            fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _field(
    TextEditingController ctrl,
    String label, {
    int maxLines = 1,
    TextInputType keyboardType = TextInputType.text,
    String? hint,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: ctrl,
      maxLines: maxLines,
      keyboardType: keyboardType,
      style: const TextStyle(color: AzColor.textPrimary),
      decoration: InputDecoration(labelText: label, hintText: hint),
      validator: validator,
    );
  }
}

// ── Small reusable table widgets ─────────────────────────────────────────────

class _DiscountTypeBadge extends StatelessWidget {
  const _DiscountTypeBadge(this.isPercentage);
  final bool isPercentage;

  @override
  Widget build(BuildContext context) {
    final color = isPercentage ? AzColor.warning : AzColor.info;
    final label = isPercentage ? '%  نسبة' : 'مبلغ ثابت';
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        border: Border.all(color: color.withValues(alpha: 0.4)),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(label,
          style: TextStyle(
              color: color, fontSize: 11, fontWeight: FontWeight.bold)),
    );
  }
}

class _ExpiryBadge extends StatelessWidget {
  const _ExpiryBadge(
      {required this.dateStr, required this.isExpired});
  final String dateStr;
  final bool isExpired;

  @override
  Widget build(BuildContext context) {
    final color = isExpired ? AzColor.danger : AzColor.textSecondary;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (isExpired)
          const Icon(Icons.warning_amber, color: AzColor.danger, size: 13),
        if (isExpired) const SizedBox(width: 3),
        Text(dateStr,
            style: TextStyle(
                color: color,
                fontSize: 12,
                fontWeight:
                    isExpired ? FontWeight.bold : FontWeight.normal)),
      ],
    );
  }
}

class _DatePicker extends StatelessWidget {
  const _DatePicker(
      {required this.label, required this.value, required this.onTap});
  final String label;
  final String? value;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        decoration: BoxDecoration(
          color: AzColor.bgElevated,
          border: Border.all(color: AzColor.bgBorder),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          children: [
            const Icon(Icons.calendar_today,
                color: AzColor.primary, size: 16),
            const SizedBox(width: 8),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(label,
                      style: const TextStyle(
                          color: AzColor.textMuted, fontSize: 11)),
                  Text(
                    value ?? 'اختر تاريخاً',
                    style: TextStyle(
                        color: value != null
                            ? AzColor.textPrimary
                            : AzColor.textMuted,
                        fontSize: 13),
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
