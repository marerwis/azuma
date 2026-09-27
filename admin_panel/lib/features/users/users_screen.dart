import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/user_provider.dart';

class UsersScreen extends ConsumerWidget {
  const UsersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final usersAsync = ref.watch(usersProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('إدارة المستخدمين - Users'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(usersProvider),
            tooltip: 'تحديث',
          ),
        ],
      ),
      body: usersAsync.when(
        data: (users) {
          if (users.isEmpty) return const Center(child: Text('لا يوجد مستخدمين'));
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: MaterialStateProperty.all(Colors.grey[200]),
                  columns: const [
                    DataColumn(label: Text('الاسم الكامل')),
                    DataColumn(label: Text('رقم الهاتف')),
                    DataColumn(label: Text('البريد الإلكتروني')),
                    DataColumn(label: Text('الدور (Role)')),
                    DataColumn(label: Text('الإجراءات')),
                  ],
                  rows: users.map((user) {
                    final role = user['role'] ?? 'customer';
                    return DataRow(
                      cells: [
                        DataCell(Text(user['full_name'] ?? 'مجهول', style: const TextStyle(fontWeight: FontWeight.bold))),
                        DataCell(Text(user['phone'] ?? '--')),
                        DataCell(Text(user['email'] ?? '--', style: const TextStyle(color: Colors.grey))),
                        DataCell(_buildRoleBadge(role)),
                        DataCell(
                          ElevatedButton.icon(
                            onPressed: () => _showManageUserDialog(context, ref, user['id'], user['full_name'] ?? '', role),
                            icon: const Icon(Icons.manage_accounts, size: 16),
                            label: const Text('إدارة'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.blueGrey,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            ),
                          ),
                        ),
                      ],
                    );
                  }).toList(),
                ),
              ),
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(child: Text('Error: $err')),
      ),
    );
  }

  Widget _buildRoleBadge(String role) {
    Color bgColor;
    String text;

    switch (role) {
      case 'admin':
        bgColor = Colors.purple;
        text = 'مدير (Admin)';
        break;
      case 'vendor':
        bgColor = Colors.orange;
        text = 'مطعم (Vendor)';
        break;
      case 'customer':
        bgColor = Colors.blue;
        text = 'عميل (Customer)';
        break;
      case 'driver':
        bgColor = Colors.green;
        text = 'مندوب (Driver)';
        break;
      default:
        bgColor = Colors.grey;
        text = role;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: bgColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: bgColor),
      ),
      child: Text(
        text,
        style: TextStyle(color: bgColor, fontWeight: FontWeight.bold, fontSize: 12),
      ),
    );
  }

  void _showManageUserDialog(BuildContext context, WidgetRef ref, String userId, String userName, String currentRole) {
    showDialog(
      context: context,
      builder: (context) => _ManageUserDialog(
        userId: userId,
        userName: userName,
        currentRole: currentRole,
        ref: ref,
      ),
    );
  }
}

class _ManageUserDialog extends StatefulWidget {
  final String userId;
  final String userName;
  final String currentRole;
  final WidgetRef ref;

  const _ManageUserDialog({
    required this.userId,
    required this.userName,
    required this.currentRole,
    required this.ref,
  });

  @override
  State<_ManageUserDialog> createState() => _ManageUserDialogState();
}

class _ManageUserDialogState extends State<_ManageUserDialog> {
  late String _selectedRole;
  bool _isLoading = false;

  final List<String> _roles = ['customer', 'driver', 'vendor', 'admin'];

  @override
  void initState() {
    super.initState();
    _selectedRole = widget.currentRole;
  }

  String _getReadableRole(String role) {
    switch (role) {
      case 'admin': return 'مدير (Admin)';
      case 'vendor': return 'مطعم (Vendor)';
      case 'customer': return 'عميل (Customer)';
      case 'driver': return 'مندوب (Driver)';
      default: return role;
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('إدارة المستخدم: ${widget.userName}'),
      content: SizedBox(
        width: 300,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('تغيير دور المستخدم (Role):'),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              value: _roles.contains(_selectedRole) ? _selectedRole : 'customer',
              decoration: const InputDecoration(border: OutlineInputBorder()),
              items: _roles.map((role) {
                return DropdownMenuItem(
                  value: role,
                  child: Text(_getReadableRole(role)),
                );
              }).toList(),
              onChanged: (val) {
                if (val != null) {
                  setState(() => _selectedRole = val);
                }
              },
            ),
          ],
        ),
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('إلغاء')),
        ElevatedButton(
          onPressed: _isLoading ? null : _save,
          child: _isLoading ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator()) : const Text('حفظ والتحديث'),
        ),
      ],
    );
  }

  Future<void> _save() async {
    if (_selectedRole == widget.currentRole) {
      Navigator.pop(context);
      return;
    }
    
    setState(() => _isLoading = true);
    try {
      await widget.ref.read(userRepositoryProvider).updateUserRole(widget.userId, _selectedRole);
      widget.ref.invalidate(usersProvider);
      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('تم تحديث دور المستخدم بنجاح!'), backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('خطأ: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }
}
