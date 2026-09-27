import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/user_provider.dart';

class UsersScreen extends ConsumerStatefulWidget {
  const UsersScreen({super.key});

  @override
  ConsumerState<UsersScreen> createState() => _UsersScreenState();
}

class _UsersScreenState extends ConsumerState<UsersScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final usersAsync = ref.watch(usersProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('إدارة المستخدمين - Users'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add),
            tooltip: 'إضافة مستخدم جديد',
            onPressed: () {
              // Open add user dialog (currently a placeholder for Auth system)
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('لإضافة مستخدم بكلمة مرور، يرجى التسجيل عبر التطبيق أو إضافة المستخدم من لوحة Supabase Auth ثم ربطه هنا.')),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(usersProvider),
            tooltip: 'تحديث',
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          labelColor: const Color(0xFFFF5722),
          unselectedLabelColor: Colors.grey,
          indicatorColor: const Color(0xFFFF5722),
          tabs: const [
            Tab(text: 'المديرين (Admins)'),
            Tab(text: 'المطاعم (Vendors)'),
            Tab(text: 'المندوبين (Drivers)'),
            Tab(text: 'العملاء (Customers)'),
          ],
        ),
      ),
      body: Column(
        children: [
          // Search Bar
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'ابحث بالاسم، رقم الهاتف، أو البريد الإلكتروني...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchQuery.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          setState(() => _searchQuery = '');
                        },
                      )
                    : null,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
              onChanged: (val) {
                setState(() => _searchQuery = val.toLowerCase());
              },
            ),
          ),
          Expanded(
            child: usersAsync.when(
              data: (users) {
                // Filter by search query
                final filteredUsers = users.where((u) {
                  final name = (u['full_name'] ?? '').toString().toLowerCase();
                  final phone = (u['phone'] ?? '').toString().toLowerCase();
                  final email = (u['email'] ?? '').toString().toLowerCase();
                  return name.contains(_searchQuery) ||
                         phone.contains(_searchQuery) ||
                         email.contains(_searchQuery);
                }).toList();

                // Categorize by roles
                final admins = filteredUsers.where((u) => u['role'] == 'admin').toList();
                final vendors = filteredUsers.where((u) => u['role'] == 'vendor').toList();
                final drivers = filteredUsers.where((u) => u['role'] == 'driver').toList();
                final customers = filteredUsers.where((u) => u['role'] == 'customer').toList();

                return TabBarView(
                  controller: _tabController,
                  children: [
                    _buildUserTable(admins),
                    _buildUserTable(vendors),
                    _buildUserTable(drivers),
                    _buildUserTable(customers),
                  ],
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (err, stack) => Center(child: Text('Error: $err')),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildUserTable(List<Map<String, dynamic>> usersList) {
    if (usersList.isEmpty) {
      return const Center(child: Text('لا يوجد مستخدمين في هذا القسم.'));
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: SizedBox(
        width: double.infinity,
        child: Card(
          elevation: 2,
          child: DataTable(
            headingRowColor: WidgetStateProperty.all(Colors.grey[200]),
            columns: const [
              DataColumn(label: Text('الاسم الكامل')),
              DataColumn(label: Text('رقم الهاتف')),
              DataColumn(label: Text('البريد الإلكتروني')),
              DataColumn(label: Text('الدور (Role)')),
              DataColumn(label: Text('الإجراءات')),
            ],
            rows: usersList.map((user) {
              final role = user['role'] ?? 'customer';
              return DataRow(
                cells: [
                  DataCell(Text(user['full_name'] ?? 'مجهول', style: const TextStyle(fontWeight: FontWeight.bold))),
                  DataCell(Text(user['phone'] ?? '--')),
                  DataCell(Text(user['email'] ?? '--', style: const TextStyle(color: Colors.grey))),
                  DataCell(_buildRoleBadge(role)),
                  DataCell(
                    ElevatedButton.icon(
                      onPressed: () => _showManageUserDialog(context, ref, user),
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

  void _showManageUserDialog(BuildContext context, WidgetRef ref, Map<String, dynamic> user) {
    showDialog(
      context: context,
      builder: (context) => _ManageUserDialog(
        user: user,
        ref: ref,
      ),
    );
  }
}

class _ManageUserDialog extends StatefulWidget {
  final Map<String, dynamic> user;
  final WidgetRef ref;

  const _ManageUserDialog({
    required this.user,
    required this.ref,
  });

  @override
  State<_ManageUserDialog> createState() => _ManageUserDialogState();
}

class _ManageUserDialogState extends State<_ManageUserDialog> {
  final _formKey = GlobalKey<FormState>();
  
  late TextEditingController _nameController;
  late TextEditingController _phoneController;
  late TextEditingController _emailController;
  late String _selectedRole;
  
  bool _isLoading = false;

  final List<String> _roles = ['customer', 'driver', 'vendor', 'admin'];

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.user['full_name'] ?? '');
    _phoneController = TextEditingController(text: widget.user['phone'] ?? '');
    _emailController = TextEditingController(text: widget.user['email'] ?? '');
    _selectedRole = widget.user['role'] ?? 'customer';
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _emailController.dispose();
    super.dispose();
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
      title: const Text('تعديل بيانات المستخدم'),
      content: SizedBox(
        width: 400,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: 'الاسم الكامل',
                  border: OutlineInputBorder(),
                ),
                validator: (val) => (val == null || val.isEmpty) ? 'الاسم مطلوب' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _phoneController,
                decoration: const InputDecoration(
                  labelText: 'رقم الهاتف',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _emailController,
                readOnly: true, // Tied to Auth.users, should be read only in public.users
                decoration: InputDecoration(
                  labelText: 'البريد الإلكتروني',
                  border: const OutlineInputBorder(),
                  fillColor: Colors.grey[200],
                  filled: true,
                ),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _roles.contains(_selectedRole) ? _selectedRole : 'customer',
                decoration: const InputDecoration(
                  labelText: 'الدور (Role)',
                  border: OutlineInputBorder()
                ),
                items: _roles.map((role) {
                  return DropdownMenuItem(
                    value: role,
                    child: Text(_getReadableRole(role)),
                  );
                }).toList(),
                onChanged: (val) {
                  if (val != null) setState(() => _selectedRole = val);
                },
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('إلغاء')),
        ElevatedButton(
          onPressed: _isLoading ? null : _save,
          child: _isLoading ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator()) : const Text('حفظ التعديلات'),
        ),
      ],
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    
    setState(() => _isLoading = true);
    try {
      final updatedData = {
        'full_name': _nameController.text.trim(),
        'phone': _phoneController.text.trim(),
        'role': _selectedRole,
      };
      
      await widget.ref.read(userRepositoryProvider).updateUserData(widget.user['id'], updatedData);
      widget.ref.invalidate(usersProvider);
      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('تم تحديث بيانات المستخدم بنجاح!'), backgroundColor: Colors.green),
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
