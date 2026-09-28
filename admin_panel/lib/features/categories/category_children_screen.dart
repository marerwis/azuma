import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../stores/providers/store_provider.dart';
import '../stores/store_menu_screen.dart';

// ─────────────────────────────────────────────────────────────────────────────
// CategoryChildrenScreen
// Shows all Stores that belong to a given App Category (app_category_id).
// Allows the admin to Add / Edit / Delete stores inside the category,
// and navigate into a store's menu directly from here.
// ─────────────────────────────────────────────────────────────────────────────

class CategoryChildrenScreen extends ConsumerWidget {
  final Map<String, dynamic> appCategory;

  const CategoryChildrenScreen({super.key, required this.appCategory});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final categoryId = appCategory['id'] as String;
    final categoryName = appCategory['name'] as String? ?? '';
    final storesAsync = ref.watch(storesByCategoryProvider(categoryId));

    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 1,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Color(0xFF1A1A2E)),
          onPressed: () => Navigator.pop(context),
        ),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'المطاعم في: $categoryName',
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A2E),
              ),
            ),
            const Text(
              'إدارة المطاعم المرتبطة بهذا القسم',
              style: TextStyle(fontSize: 11, color: Colors.grey),
            ),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            child: ElevatedButton.icon(
              onPressed: () =>
                  _showAddEditStoreDialog(context, ref, categoryId, null),
              icon: const Icon(Icons.add, size: 18),
              label: const Text('إضافة مطعم'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF5722),
                foregroundColor: Colors.white,
                shape: RoundedCornerShape(10),
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              ),
            ),
          ),
        ],
      ),
      body: storesAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline, size: 48, color: Colors.red),
              const SizedBox(height: 12),
              Text('خطأ في التحميل:\n$e',
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.red)),
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: () =>
                    ref.invalidate(storesByCategoryProvider(categoryId)),
                child: const Text('إعادة المحاولة'),
              ),
            ],
          ),
        ),
        data: (stores) {
          if (stores.isEmpty) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.store_mall_directory_outlined,
                      size: 72, color: Colors.grey),
                  const SizedBox(height: 16),
                  Text(
                    'لا توجد مطاعم في قسم "$categoryName"',
                    style: const TextStyle(
                        fontSize: 16,
                        color: Colors.grey,
                        fontWeight: FontWeight.w500),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'اضغط على "إضافة مطعم" لإضافة أول مطعم في هذا القسم',
                    style: TextStyle(fontSize: 13, color: Colors.grey),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            );
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Stats header
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFFFF5722), Color(0xFFFF8A65)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.store, color: Colors.white, size: 32),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '${stores.length} مطعم',
                            style: const TextStyle(
                                color: Colors.white,
                                fontSize: 22,
                                fontWeight: FontWeight.bold),
                          ),
                          Text(
                            'مرتبط بـ "$categoryName"',
                            style: const TextStyle(
                                color: Colors.white70, fontSize: 13),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Store cards
                ...stores.map((store) => _StoreCard(
                      store: store,
                      categoryId: categoryId,
                      ref: ref,
                      onEdit: () => _showAddEditStoreDialog(
                          context, ref, categoryId, store),
                      onDelete: () =>
                          _confirmDelete(context, ref, store, categoryId),
                      onManageMenu: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => StoreMenuScreen(store: store),
                        ),
                      ),
                    )),
              ],
            ),
          );
        },
      ),
    );
  }

  void _showAddEditStoreDialog(BuildContext context, WidgetRef ref,
      String categoryId, Map<String, dynamic>? store) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => _AddEditStoreDialog(
        appCategoryId: categoryId,
        store: store,
        ref: ref,
      ),
    );
  }

  Future<void> _confirmDelete(BuildContext context, WidgetRef ref,
      Map<String, dynamic> store, String categoryId) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('تأكيد الحذف'),
        content: Text(
            'هل أنت متأكد من حذف المطعم "${store['name']}"؟\nسيتم حذف جميع المنيوهات والمنتجات المرتبطة به.'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('إلغاء')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('حذف', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );

    if (confirm == true) {
      try {
        await ref
            .read(storeRepositoryProvider)
            .deleteStore(store['id'] as String);
        ref.invalidate(storesByCategoryProvider(categoryId));
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
                content: Text('تم حذف المطعم بنجاح'),
                backgroundColor: Colors.green),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('خطأ: $e'), backgroundColor: Colors.red));
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Store Card Widget
// ─────────────────────────────────────────────────────────────────────────────
class _StoreCard extends StatelessWidget {
  final Map<String, dynamic> store;
  final String categoryId;
  final WidgetRef ref;
  final VoidCallback onEdit;
  final VoidCallback onDelete;
  final VoidCallback onManageMenu;

  const _StoreCard({
    required this.store,
    required this.categoryId,
    required this.ref,
    required this.onEdit,
    required this.onDelete,
    required this.onManageMenu,
  });

  @override
  Widget build(BuildContext context) {
    final isOpen = store['is_open'] == true;
    final isActive = store['is_active'] == true;
    final logoUrl = store['image_url'] as String?;
    final hasLogo = logoUrl != null && logoUrl.isNotEmpty;

    final logoImageUrl = hasLogo
        ? (logoUrl.startsWith('http')
            ? logoUrl
            : 'https://arivoyaepcxaoupzvvbw.supabase.co/storage/v1/object/public/store_images/$logoUrl')
        : null;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedCornerShape(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            // Logo
            CircleAvatar(
              radius: 30,
              backgroundImage:
                  logoImageUrl != null ? NetworkImage(logoImageUrl) : null,
              backgroundColor: const Color(0xFFFFECE8),
              child: logoImageUrl == null
                  ? Text(
                      (store['name'] as String? ?? 'S')
                          .substring(0, 1)
                          .toUpperCase(),
                      style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFFFF5722)),
                    )
                  : null,
            ),
            const SizedBox(width: 16),

            // Info
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          store['name'] ?? '',
                          style: const TextStyle(
                              fontWeight: FontWeight.bold, fontSize: 15),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      const SizedBox(width: 8),
                      _StatusChip(label: isOpen ? 'مفتوح' : 'مغلق', isGood: isOpen),
                      const SizedBox(width: 4),
                      _StatusChip(
                          label: isActive ? 'نشط' : 'معطل',
                          isGood: isActive),
                    ],
                  ),
                  const SizedBox(height: 4),
                  if (store['address'] != null)
                    Row(
                      children: [
                        const Icon(Icons.location_on_outlined,
                            size: 14, color: Colors.grey),
                        const SizedBox(width: 4),
                        Expanded(
                          child: Text(
                            store['address'],
                            style: const TextStyle(
                                fontSize: 12, color: Colors.grey),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      const Icon(Icons.percent, size: 14, color: Colors.grey),
                      const SizedBox(width: 4),
                      Text(
                        'عمولة: ${store['commission_rate'] ?? 10}%',
                        style:
                            const TextStyle(fontSize: 12, color: Colors.grey),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(width: 8),

            // Actions column
            Column(
              children: [
                // Manage Menu
                Tooltip(
                  message: 'إدارة المنيو',
                  child: InkWell(
                    onTap: onManageMenu,
                    borderRadius: BorderRadius.circular(8),
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: const Color(0xFFE8F5E9),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Icon(Icons.restaurant_menu,
                          color: Color(0xFF2E7D32), size: 20),
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                // Edit
                Tooltip(
                  message: 'تعديل',
                  child: InkWell(
                    onTap: onEdit,
                    borderRadius: BorderRadius.circular(8),
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFF3E0),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Icon(Icons.edit,
                          color: Color(0xFFE65100), size: 20),
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                // Delete
                Tooltip(
                  message: 'حذف',
                  child: InkWell(
                    onTap: onDelete,
                    borderRadius: BorderRadius.circular(8),
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFEBEE),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Icon(Icons.delete_outline,
                          color: Colors.red, size: 20),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  final String label;
  final bool isGood;
  const _StatusChip({required this.label, required this.isGood});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: isGood
            ? const Color(0xFFE8F5E9)
            : const Color(0xFFFFEBEE),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label,
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.bold,
          color: isGood ? const Color(0xFF2E7D32) : Colors.red,
        ),
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add / Edit Store Dialog
// Scoped to the appCategoryId of the parent category.
// ─────────────────────────────────────────────────────────────────────────────
class _AddEditStoreDialog extends StatefulWidget {
  final String appCategoryId;
  final Map<String, dynamic>? store;
  final WidgetRef ref;

  const _AddEditStoreDialog({
    required this.appCategoryId,
    this.store,
    required this.ref,
  });

  @override
  State<_AddEditStoreDialog> createState() => _AddEditStoreDialogState();
}

class _AddEditStoreDialogState extends State<_AddEditStoreDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _descController;
  late TextEditingController _addressController;
  late TextEditingController _logoController;
  late TextEditingController _commissionController;
  late TextEditingController _latController;
  late TextEditingController _lngController;
  bool _isOpen = true;
  bool _isActive = true;
  bool _isLoading = false;
  bool _isUploading = false;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    final s = widget.store;
    _nameController = TextEditingController(text: s?['name'] ?? '');
    _descController = TextEditingController(text: s?['description'] ?? '');
    _addressController = TextEditingController(text: s?['address'] ?? '');
    _logoController = TextEditingController(text: s?['image_url'] ?? '');
    _commissionController =
        TextEditingController(text: s?['commission_rate']?.toString() ?? '10');
    _latController =
        TextEditingController(text: s?['latitude']?.toString() ?? '32.1167');
    _lngController =
        TextEditingController(text: s?['longitude']?.toString() ?? '20.0667');
    _isOpen = s?['is_open'] ?? true;
    _isActive = s?['is_active'] ?? true;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descController.dispose();
    _addressController.dispose();
    _logoController.dispose();
    _commissionController.dispose();
    _latController.dispose();
    _lngController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isEdit = widget.store != null;
    return AlertDialog(
      title: Row(
        children: [
          Icon(isEdit ? Icons.edit_note : Icons.add_business,
              color: const Color(0xFFFF5722)),
          const SizedBox(width: 8),
          Text(isEdit ? 'تعديل المطعم' : 'إضافة مطعم جديد'),
        ],
      ),
      content: SizedBox(
        width: 520,
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Logo upload row
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _logoController,
                        decoration: const InputDecoration(
                          labelText: 'شعار المطعم (رابط أو ملف)',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.image_outlined),
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton.icon(
                      onPressed: _isUploading ? null : _pickAndUploadLogo,
                      icon: _isUploading
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child:
                                  CircularProgressIndicator(strokeWidth: 2))
                          : const Icon(Icons.upload_file),
                      label: const Text('رفع'),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                TextFormField(
                  controller: _nameController,
                  decoration: const InputDecoration(
                    labelText: 'اسم المطعم *',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.store),
                  ),
                  validator: (v) =>
                      v == null || v.isEmpty ? 'اسم المطعم مطلوب' : null,
                ),
                const SizedBox(height: 14),
                TextFormField(
                  controller: _descController,
                  maxLines: 2,
                  decoration: const InputDecoration(
                    labelText: 'الوصف',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.description_outlined),
                  ),
                ),
                const SizedBox(height: 14),
                TextFormField(
                  controller: _addressController,
                  decoration: const InputDecoration(
                    labelText: 'العنوان *',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.location_on_outlined),
                  ),
                  validator: (v) =>
                      v == null || v.isEmpty ? 'العنوان مطلوب' : null,
                ),
                const SizedBox(height: 14),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _latController,
                        decoration: const InputDecoration(
                            labelText: 'خط العرض (Lat)',
                            border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                        validator: (v) =>
                            v == null || v.isEmpty ? 'مطلوب' : null,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextFormField(
                        controller: _lngController,
                        decoration: const InputDecoration(
                            labelText: 'خط الطول (Lng)',
                            border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                        validator: (v) =>
                            v == null || v.isEmpty ? 'مطلوب' : null,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                TextFormField(
                  controller: _commissionController,
                  decoration: const InputDecoration(
                    labelText: 'نسبة العمولة (%)',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.percent),
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 8),
                SwitchListTile(
                  title: const Text('المطعم مفتوح الآن'),
                  subtitle: const Text('يظهر للمستخدمين عند التفعيل'),
                  value: _isOpen,
                  activeColor: const Color(0xFFFF5722),
                  onChanged: (v) => setState(() => _isOpen = v),
                ),
                SwitchListTile(
                  title: const Text('نشط في المنصة'),
                  subtitle: const Text('يظهر في قوائم التطبيق عند التفعيل'),
                  value: _isActive,
                  activeColor: const Color(0xFFFF5722),
                  onChanged: (v) => setState(() => _isActive = v),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('إلغاء')),
        ElevatedButton(
          onPressed: _isLoading ? null : _save,
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFFFF5722),
            foregroundColor: Colors.white,
          ),
          child: _isLoading
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                      strokeWidth: 2, color: Colors.white))
              : Text(widget.store == null ? 'إضافة' : 'حفظ'),
        ),
      ],
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isLoading = true);
    try {
      final data = {
        'name': _nameController.text.trim(),
        'description': _descController.text.trim(),
        'address': _addressController.text.trim(),
        'image_url': _logoController.text.trim().isEmpty
            ? null
            : _logoController.text.trim(),
        'latitude': double.tryParse(_latController.text) ?? 32.1167,
        'longitude': double.tryParse(_lngController.text) ?? 20.0667,
        'commission_rate':
            double.tryParse(_commissionController.text) ?? 10.0,
        'is_open': _isOpen,
        'is_active': _isActive,
        // ✅ Link this store to the parent App Category
        'app_category_id': widget.appCategoryId,
        // Required non-null columns with default vendor placeholder
        'vendor_id': Supabase.instance.client.auth.currentUser?.id ??
            '00000000-0000-0000-0000-000000000000',
      };

      if (widget.store == null) {
        await widget.ref.read(storeRepositoryProvider).createStore(data);
      } else {
        await widget.ref
            .read(storeRepositoryProvider)
            .updateStore(widget.store!['id'] as String, data);
      }

      widget.ref.invalidate(storesByCategoryProvider(widget.appCategoryId));
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('خطأ: $e'), backgroundColor: Colors.red));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _pickAndUploadLogo() async {
    try {
      final XFile? image =
          await _picker.pickImage(source: ImageSource.gallery);
      if (image == null) return;
      setState(() => _isUploading = true);
      final bytes = await image.readAsBytes();
      final name =
          '${DateTime.now().millisecondsSinceEpoch}_${image.name}';
      await Supabase.instance.client.storage
          .from('store_images')
          .uploadBinary(name, bytes,
              fileOptions: const FileOptions(upsert: true));
      setState(() {
        _logoController.text = name;
        _isUploading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('تم رفع الشعار بنجاح!'),
            backgroundColor: Colors.green));
      }
    } catch (e) {
      setState(() => _isUploading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text('فشل الرفع: $e'), backgroundColor: Colors.red));
      }
    }
  }
}

// Utility — copied locally to avoid import chain issues
class RoundedCornerShape extends RoundedRectangleBorder {
  RoundedCornerShape(double radius)
      : super(borderRadius: BorderRadius.circular(radius));
}
