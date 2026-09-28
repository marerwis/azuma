import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'providers/category_provider.dart';
import 'category_children_screen.dart';

class CategoriesScreen extends ConsumerWidget {
  const CategoriesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final categoriesAsync = ref.watch(categoriesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('إدارة الأقسام العامة'),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: ElevatedButton.icon(
              onPressed: () => _showAddEditCategoryDialog(context, ref, null),
              icon: const Icon(Icons.add),
              label: const Text('إضافة قسم'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF5722),
                foregroundColor: Colors.white,
              ),
            ),
          ),
        ],
      ),
      body: categoriesAsync.when(
        data: (categories) {
          if (categories.isEmpty) {
            return const Center(child: Text('لا توجد أقسام'));
          }
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: WidgetStateProperty.all(Colors.grey[200]),
                  columns: const [
                    DataColumn(label: Text('صورة القسم')),
                    DataColumn(label: Text('اسم القسم')),
                    DataColumn(label: Text('الترتيب')),
                    DataColumn(label: Text('الحالة')),
                    DataColumn(label: Text('الإجراءات')),
                  ],
                  rows: categories.map((category) {
                    final isActive = category['is_active'] == true;
                    final imageVal = category['image_url'] as String?;
                    final hasUrl =
                        imageVal != null && imageVal.contains('.');
                    final imageUrl = hasUrl
                        ? (imageVal.startsWith('http')
                            ? imageVal
                            : 'https://arivoyaepcxaoupzvvbw.supabase.co/storage/v1/object/public/store_images/$imageVal')
                        : null;

                    return DataRow(
                      cells: [
                        // ── Image / Emoji ──────────────────────────────────
                        DataCell(
                          CircleAvatar(
                            backgroundImage: imageUrl != null
                                ? NetworkImage(imageUrl)
                                : null,
                            backgroundColor: const Color(0xFFFFECE8),
                            child: imageUrl == null
                                ? Text(imageVal ?? '📁',
                                    style: const TextStyle(fontSize: 20))
                                : null,
                          ),
                        ),

                        // ── Name ───────────────────────────────────────────
                        DataCell(Text(
                          category['name'] ?? '',
                          style: const TextStyle(fontWeight: FontWeight.w600),
                        )),

                        // ── Sort Order ─────────────────────────────────────
                        DataCell(Text(
                            category['sort_order']?.toString() ?? '0')),

                        // ── Status ─────────────────────────────────────────
                        DataCell(
                          Row(
                            children: [
                              Icon(
                                isActive
                                    ? Icons.check_circle
                                    : Icons.cancel,
                                color:
                                    isActive ? Colors.green : Colors.red,
                                size: 20,
                              ),
                              const SizedBox(width: 4),
                              Text(
                                isActive ? 'نشط' : 'معطل',
                                style: TextStyle(
                                    color: isActive
                                        ? Colors.green
                                        : Colors.red,
                                    fontSize: 12),
                              ),
                            ],
                          ),
                        ),

                        // ── Actions ────────────────────────────────────────
                        DataCell(
                          Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              // 1. Manage Children (Stores in this category)
                              Tooltip(
                                message:
                                    'إدارة المطاعم في هذا القسم',
                                child: IconButton(
                                  icon: const Icon(
                                    Icons.account_tree_outlined,
                                    color: Color(0xFF1565C0),
                                  ),
                                  onPressed: () {
                                    Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) =>
                                            CategoryChildrenScreen(
                                          appCategory: category,
                                        ),
                                      ),
                                    );
                                  },
                                ),
                              ),

                              // 2. Edit
                              Tooltip(
                                message: 'تعديل القسم',
                                child: IconButton(
                                  icon: const Icon(Icons.edit,
                                      color: Colors.orange),
                                  onPressed: () =>
                                      _showAddEditCategoryDialog(
                                          context, ref, category),
                                ),
                              ),

                              // 3. Delete
                              Tooltip(
                                message: 'حذف القسم',
                                child: IconButton(
                                  icon: const Icon(Icons.delete,
                                      color: Colors.red),
                                  onPressed: () async {
                                    final confirm =
                                        await showDialog<bool>(
                                      context: context,
                                      builder: (ctx) => AlertDialog(
                                        title: const Text('تأكيد الحذف'),
                                        content: Text(
                                            'هل أنت متأكد من حذف قسم "${category['name']}"؟'),
                                        actions: [
                                          TextButton(
                                            onPressed: () =>
                                                Navigator.pop(
                                                    ctx, false),
                                            child:
                                                const Text('إلغاء'),
                                          ),
                                          TextButton(
                                            onPressed: () =>
                                                Navigator.pop(
                                                    ctx, true),
                                            child: const Text(
                                              'حذف',
                                              style: TextStyle(
                                                  color: Colors.red),
                                            ),
                                          ),
                                        ],
                                      ),
                                    );
                                    if (confirm == true) {
                                      await ref
                                          .read(
                                              categoryRepositoryProvider)
                                          .deleteCategory(
                                              category['id']);
                                      ref.invalidate(
                                          categoriesProvider);
                                    }
                                  },
                                ),
                              ),
                            ],
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
        loading: () =>
            const Center(child: CircularProgressIndicator()),
        error: (err, stack) =>
            Center(child: Text('Error: $err')),
      ),
    );
  }

  void _showAddEditCategoryDialog(
      BuildContext context, WidgetRef ref, Map<String, dynamic>? category) {
    showDialog(
      context: context,
      builder: (context) =>
          _AddEditCategoryDialog(category: category, ref: ref),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add / Edit Category Dialog (unchanged logic, unchanged schema)
// ─────────────────────────────────────────────────────────────────────────────
class _AddEditCategoryDialog extends StatefulWidget {
  final Map<String, dynamic>? category;
  final WidgetRef ref;

  const _AddEditCategoryDialog({this.category, required this.ref});

  @override
  State<_AddEditCategoryDialog> createState() =>
      _AddEditCategoryDialogState();
}

class _AddEditCategoryDialogState
    extends State<_AddEditCategoryDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _imageUrlController;
  late TextEditingController _sortController;
  bool _isActive = true;
  bool _isLoading = false;
  bool _isUploading = false;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _nameController =
        TextEditingController(text: widget.category?['name'] ?? '');
    _imageUrlController =
        TextEditingController(text: widget.category?['image_url'] ?? '');
    _sortController = TextEditingController(
        text: widget.category?['sort_order']?.toString() ?? '0');
    _isActive = widget.category?['is_active'] ?? true;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _imageUrlController.dispose();
    _sortController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.category == null
          ? 'إضافة قسم جديد'
          : 'تعديل القسم'),
      content: SizedBox(
        width: 400,
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: _nameController,
                  decoration: const InputDecoration(
                      labelText: 'اسم القسم',
                      border: OutlineInputBorder()),
                  validator: (val) =>
                      val == null || val.isEmpty ? 'مطلوب' : null,
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _imageUrlController,
                        decoration: const InputDecoration(
                            labelText:
                                'الرمز التعبيري أو الرابط (Image URL/Emoji)',
                            border: OutlineInputBorder()),
                        validator: (val) =>
                            val == null || val.isEmpty ? 'مطلوب' : null,
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton.icon(
                      onPressed:
                          _isUploading ? null : _pickAndUploadImage,
                      icon: _isUploading
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(
                                  strokeWidth: 2))
                          : const Icon(Icons.upload_file),
                      label: const Text('رفع صورة'),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _sortController,
                  decoration: const InputDecoration(
                      labelText: 'الترتيب (Sort Order)',
                      border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 16),
                SwitchListTile(
                  title: const Text('مفعل (Active)'),
                  value: _isActive,
                  onChanged: (val) => setState(() => _isActive = val),
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
          child: _isLoading
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator())
              : const Text('حفظ'),
        ),
      ],
    );
  }

  Future<void> _save() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        final data = {
          'name': _nameController.text,
          'image_url': _imageUrlController.text,
          'sort_order': int.tryParse(_sortController.text) ?? 0,
          'is_active': _isActive,
          // NOTE: app_categories has no store_id column — intentionally omitted.
        };

        if (widget.category == null) {
          await widget.ref
              .read(categoryRepositoryProvider)
              .createCategory(data);
        } else {
          await widget.ref
              .read(categoryRepositoryProvider)
              .updateCategory(widget.category!['id'], data);
        }

        widget.ref.invalidate(categoriesProvider);
        if (mounted) Navigator.pop(context);
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Error: $e')));
        }
      } finally {
        if (mounted) setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _pickAndUploadImage() async {
    try {
      final XFile? image =
          await _picker.pickImage(source: ImageSource.gallery);

      if (image != null) {
        setState(() => _isUploading = true);

        final fileBytes = await image.readAsBytes();
        final fileName = image.name;
        final uniqueName =
            '${DateTime.now().millisecondsSinceEpoch}_$fileName';

        await Supabase.instance.client.storage
            .from('store_images')
            .uploadBinary(
              uniqueName,
              fileBytes,
              fileOptions: const FileOptions(upsert: true),
            );

        setState(() {
          _imageUrlController.text = uniqueName;
          _isUploading = false;
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
              content: Text('تم رفع الصورة بنجاح!'),
              backgroundColor: Colors.green));
        }
      }
    } catch (e) {
      setState(() => _isUploading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text('فشل رفع الصورة: $e'),
            backgroundColor: Colors.red));
      }
    }
  }
}
