import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'providers/notification_provider.dart';

class SendNotificationScreen extends ConsumerStatefulWidget {
  const SendNotificationScreen({super.key});

  @override
  ConsumerState<SendNotificationScreen> createState() =>
      _SendNotificationScreenState();
}

class _SendNotificationScreenState
    extends ConsumerState<SendNotificationScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _bodyController = TextEditingController();

  // Target type: 'all' | 'customer' | 'driver'
  String _targetType = 'all';
  Map<String, dynamic>? _selectedUser;

  @override
  void dispose() {
    _titleController.dispose();
    _bodyController.dispose();
    super.dispose();
  }

  Future<void> _sendNotification() async {
    if (!_formKey.currentState!.validate()) return;
    if (_targetType != 'all' && _selectedUser == null) {
      _showSnack('يرجى اختيار المستخدم المستهدف', isError: true);
      return;
    }

    ref.read(notifSendingProvider.notifier).setSending(true);
    try {
      final currentUser = Supabase.instance.client.auth.currentUser;
      await ref.read(notificationRepositoryProvider).saveNotification(
            title: _titleController.text.trim(),
            body: _bodyController.text.trim(),
            targetType: _targetType,
            targetUserId: _targetType != 'all' ? _selectedUser!['id'] : null,
            sentBy: currentUser?.id ?? '',
          );

      // TODO: Call your FCM Cloud Function / Edge Function here.
      // Example: await supabase.functions.invoke('send-push-notification', body: {...})

      if (mounted) {
        _showSnack('✅ تم حفظ الإشعار وإرساله بنجاح');
        _formKey.currentState!.reset();
        _titleController.clear();
        _bodyController.clear();
        setState(() {
          _targetType = 'all';
          _selectedUser = null;
        });
        ref.invalidate(notificationsProvider);
      }
    } catch (e) {
      if (mounted) _showSnack('خطأ: $e', isError: true);
    } finally {
      ref.read(notifSendingProvider.notifier).setSending(false);
    }
  }

  void _showSnack(String msg, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg),
        backgroundColor: isError ? Colors.red[700] : Colors.green[700],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isSending = ref.watch(notifSendingProvider);
    final historyAsync = ref.watch(notificationsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('إرسال الإشعارات - Push Notifications'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'تحديث السجل',
            onPressed: () => ref.invalidate(notificationsProvider),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ── LEFT: Send Form ──────────────────────────────────────────
            Expanded(
              flex: 2,
              child: Card(
                elevation: 3,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12)),
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        // Header
                        Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: const Color(0xFFFF5722).withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: const Icon(Icons.notifications_active,
                                  color: Color(0xFFFF5722)),
                            ),
                            const SizedBox(width: 12),
                            const Text(
                              'إنشاء إشعار جديد',
                              style: TextStyle(
                                  fontSize: 20, fontWeight: FontWeight.bold),
                            ),
                          ],
                        ),
                        const Divider(height: 32),

                        // Title
                        TextFormField(
                          controller: _titleController,
                          decoration: const InputDecoration(
                            labelText: 'عنوان الإشعار *',
                            hintText: 'مثال: عرض خاص اليوم فقط!',
                            border: OutlineInputBorder(),
                            prefixIcon: Icon(Icons.title),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty)
                              ? 'العنوان مطلوب'
                              : null,
                        ),
                        const SizedBox(height: 16),

                        // Body
                        TextFormField(
                          controller: _bodyController,
                          maxLines: 4,
                          decoration: const InputDecoration(
                            labelText: 'نص الإشعار *',
                            hintText: 'اكتب محتوى الإشعار هنا...',
                            border: OutlineInputBorder(),
                            prefixIcon: Icon(Icons.message),
                            alignLabelWithHint: true,
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty)
                              ? 'نص الإشعار مطلوب'
                              : null,
                        ),
                        const SizedBox(height: 20),

                        // Target type selector
                        const Text('الجمهور المستهدف',
                            style: TextStyle(
                                fontWeight: FontWeight.bold, fontSize: 15)),
                        const SizedBox(height: 8),
                        _TargetTypeSelector(
                          selectedType: _targetType,
                          onChanged: (val) => setState(() {
                            _targetType = val;
                            _selectedUser = null;
                          }),
                        ),
                        const SizedBox(height: 16),

                        // User picker (visible only for customer / driver)
                        if (_targetType == 'customer')
                          _UserDropdown(
                            label: 'اختر العميل',
                            provider: notifCustomersProvider,
                            selectedUser: _selectedUser,
                            onChanged: (u) =>
                                setState(() => _selectedUser = u),
                          ),
                        if (_targetType == 'driver')
                          _UserDropdown(
                            label: 'اختر السائق',
                            provider: notifDriversProvider,
                            selectedUser: _selectedUser,
                            onChanged: (u) =>
                                setState(() => _selectedUser = u),
                          ),

                        const SizedBox(height: 24),

                        // Preview chip
                        if (_targetType == 'all')
                          _PreviewChip(
                            icon: Icons.broadcast_on_personal,
                            label: 'إشعار جماعي لجميع المستخدمين',
                            color: Colors.blue,
                          )
                        else if (_selectedUser != null)
                          _PreviewChip(
                            icon: _targetType == 'customer'
                                ? Icons.person
                                : Icons.delivery_dining,
                            label:
                                'إلى: ${_selectedUser!['full_name'] ?? _selectedUser!['phone'] ?? '--'}',
                            color: const Color(0xFFFF5722),
                          ),

                        const SizedBox(height: 24),

                        // Send button
                        SizedBox(
                          width: double.infinity,
                          height: 50,
                          child: ElevatedButton.icon(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFFFF5722),
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10)),
                            ),
                            onPressed: isSending ? null : _sendNotification,
                            icon: isSending
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                        color: Colors.white, strokeWidth: 2))
                                : const Icon(Icons.send),
                            label: Text(
                              isSending ? 'جاري الإرسال...' : 'إرسال الإشعار',
                              style: const TextStyle(
                                  fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),

            const SizedBox(width: 24),

            // ── RIGHT: History ───────────────────────────────────────────
            Expanded(
              flex: 3,
              child: Card(
                elevation: 3,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12)),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Row(
                        children: [
                          Icon(Icons.history, color: Color(0xFFFF5722)),
                          SizedBox(width: 8),
                          Text('سجل الإشعارات المرسلة',
                              style: TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.bold)),
                        ],
                      ),
                      const Divider(height: 24),
                      historyAsync.when(
                        data: (items) {
                          if (items.isEmpty) {
                            return const Padding(
                              padding: EdgeInsets.symmetric(vertical: 40),
                              child: Center(
                                  child: Text('لا توجد إشعارات مرسلة بعد')),
                            );
                          }
                          return SizedBox(
                            width: double.infinity,
                            child: DataTable(
                              headingRowColor: WidgetStateProperty.all(
                                  Colors.grey.shade100),
                              columnSpacing: 20,
                              columns: const [
                                DataColumn(label: Text('العنوان')),
                                DataColumn(label: Text('المستهدف')),
                                DataColumn(label: Text('الحالة')),
                                DataColumn(label: Text('التاريخ')),
                              ],
                              rows: items.map((n) {
                                final date = DateTime.tryParse(
                                    n['sent_at']?.toString() ?? '');
                                final targetLabel = switch (n['target_type']) {
                                  'all' => '🌐 الجميع',
                                  'customer' => '👤 عميل',
                                  'driver' => '🚴 سائق',
                                  _ => n['target_type'] ?? '--',
                                };
                                return DataRow(cells: [
                                  DataCell(Text(n['title'] ?? '--',
                                      style: const TextStyle(
                                          fontWeight: FontWeight.w600))),
                                  DataCell(Text(targetLabel)),
                                  DataCell(_StatusBadge(
                                      n['status'] ?? 'sent')),
                                  DataCell(Text(date != null
                                      ? DateFormat('yyyy-MM-dd HH:mm')
                                          .format(date)
                                      : '--')),
                                ]);
                              }).toList(),
                            ),
                          );
                        },
                        loading: () =>
                            const Center(child: CircularProgressIndicator()),
                        error: (e, _) =>
                            Center(child: Text('خطأ في تحميل السجل: $e')),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

class _TargetTypeSelector extends StatelessWidget {
  const _TargetTypeSelector(
      {required this.selectedType, required this.onChanged});
  final String selectedType;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    final options = [
      ('all', Icons.public, 'الجميع'),
      ('customer', Icons.person, 'عميل محدد'),
      ('driver', Icons.delivery_dining, 'سائق محدد'),
    ];
    return Row(
      children: options.map((opt) {
        final (value, icon, label) = opt;
        final isSelected = selectedType == value;
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.only(left: 8),
            child: InkWell(
              onTap: () => onChanged(value),
              borderRadius: BorderRadius.circular(8),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                padding: const EdgeInsets.symmetric(vertical: 12),
                decoration: BoxDecoration(
                  color: isSelected
                      ? const Color(0xFFFF5722).withValues(alpha: 0.12)
                      : Colors.grey.shade100,
                  border: Border.all(
                      color: isSelected
                          ? const Color(0xFFFF5722)
                          : Colors.grey.shade300),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Column(
                  children: [
                    Icon(icon,
                        color: isSelected
                            ? const Color(0xFFFF5722)
                            : Colors.grey),
                    const SizedBox(height: 4),
                    Text(label,
                        style: TextStyle(
                            fontSize: 12,
                            fontWeight: isSelected
                                ? FontWeight.bold
                                : FontWeight.normal,
                            color: isSelected
                                ? const Color(0xFFFF5722)
                                : Colors.grey[700])),
                  ],
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}

class _UserDropdown extends ConsumerWidget {
  const _UserDropdown({
    required this.label,
    required this.provider,
    required this.selectedUser,
    required this.onChanged,
  });

  final String label;
  final FutureProvider<List<Map<String, dynamic>>> provider;
  final Map<String, dynamic>? selectedUser;
  final ValueChanged<Map<String, dynamic>?> onChanged;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final usersAsync = ref.watch(provider);
    return usersAsync.when(
      data: (users) => DropdownButtonFormField<Map<String, dynamic>>(
        value: selectedUser,
        hint: Text(label),
        decoration: InputDecoration(
          border: const OutlineInputBorder(),
          labelText: label,
          prefixIcon: const Icon(Icons.search),
        ),
        items: users
            .map((u) => DropdownMenuItem(
                  value: u,
                  child: Text(
                      '${u['full_name'] ?? '--'} (${u['phone'] ?? u['email'] ?? ''})'),
                ))
            .toList(),
        onChanged: onChanged,
      ),
      loading: () => const LinearProgressIndicator(),
      error: (e, _) => Text('خطأ: $e'),
    );
  }
}

class _PreviewChip extends StatelessWidget {
  const _PreviewChip(
      {required this.icon, required this.label, required this.color});
  final IconData icon;
  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.08),
        border: Border.all(color: color.withValues(alpha: 0.3)),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: color),
          const SizedBox(width: 8),
          Text(label, style: TextStyle(color: color, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge(this.status);
  final String status;

  @override
  Widget build(BuildContext context) {
    final (label, color) = switch (status) {
      'sent' => ('مرسل', Colors.green),
      'failed' => ('فشل', Colors.red),
      'pending' => ('معلق', Colors.orange),
      _ => (status, Colors.grey),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        border: Border.all(color: color.withValues(alpha: 0.4)),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(label,
          style: TextStyle(
              color: color, fontSize: 11, fontWeight: FontWeight.bold)),
    );
  }
}
