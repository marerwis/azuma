import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'providers/product_provider.dart';

class StoreMenuScreen extends ConsumerWidget {
  final Map<String, dynamic> store;

  const StoreMenuScreen({super.key, required this.store});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final productsAsync = ref.watch(storeProductsProvider(store['id']));

    return Scaffold(
      appBar: AppBar(
        title: Text('المنيو: ${store['name']}'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          // ── Add Menu Category button ───────────────────────────────────
          Padding(
            padding: const EdgeInsets.only(right: 8.0, top: 10, bottom: 10),
            child: ElevatedButton.icon(
              onPressed: () => _showAddMenuCategoryDialog(context, ref, store['id']),
              icon: const Icon(Icons.category, size: 18),
              label: Text(MediaQuery.of(context).size.width > 600 ? 'إدارة الأقسام' : 'أقسام'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
          ),
          // ── Add Product button ─────────────────────────────────────────
          Padding(
            padding: const EdgeInsets.only(right: 8.0, left: 16.0, top: 10, bottom: 10),
            child: ElevatedButton.icon(
              onPressed: () => _showAddEditProductDialog(context, ref, store['id'], null),
              icon: const Icon(Icons.add, size: 18),
              label: Text(MediaQuery.of(context).size.width > 600 ? 'إضافة منتج' : 'منتج'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF5722),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
          ),
        ],
      ),
      body: productsAsync.when(
        data: (products) {
          if (products.isEmpty) {
            return const Center(child: Text('لا توجد منتجات في هذا المطعم'));
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
                    DataColumn(label: Text('المنتج')),
                    DataColumn(label: Text('القسم')),
                    DataColumn(label: Text('السعر')),
                    DataColumn(label: Text('المخزون')),
                    DataColumn(label: Text('متاح')),
                    DataColumn(label: Text('الإجراءات')),
                  ],
                  rows: products.map((product) {
                    final isActive = product['is_active'] == true;
                    return DataRow(
                      cells: [
                        DataCell(
                          Row(
                            children: [
                              CircleAvatar(
                                backgroundImage: product['image_url'] != null
                                    ? NetworkImage(
                                        product['image_url'].toString().startsWith('http')
                                            ? product['image_url']
                                            : 'https://arivoyaepcxaoupzvvbw.supabase.co/storage/v1/object/public/store_images/${product['image_url']}',
                                      )
                                    : null,
                                child: product['image_url'] == null
                                    ? const Icon(Icons.fastfood)
                                    : null,
                              ),
                              const SizedBox(width: 8),
                              Text(product['name'] ?? ''),
                            ],
                          ),
                        ),
                        // category name comes from the joined menu_categories row
                        DataCell(Text(product['menu_categories']?['name'] ?? product['categories']?['name'] ?? 'بدون قسم')),
                        DataCell(Text('${product['base_price']} د.ل')),
                        DataCell(Text('${product['stock_quantity'] ?? 0}')),
                        DataCell(
                          Icon(
                            isActive ? Icons.check_circle : Icons.cancel,
                            color: isActive ? Colors.green : Colors.red,
                          ),
                        ),
                        DataCell(
                          Row(
                            children: [
                              IconButton(
                                icon: const Icon(Icons.edit, color: Colors.orange),
                                tooltip: 'تعديل',
                                onPressed: () =>
                                    _showAddEditProductDialog(context, ref, store['id'], product),
                              ),
                              IconButton(
                                icon: const Icon(Icons.delete, color: Colors.red),
                                tooltip: 'حذف',
                                onPressed: () async {
                                  final confirm = await showDialog<bool>(
                                    context: context,
                                    builder: (ctx) => AlertDialog(
                                      title: const Text('تأكيد الحذف'),
                                      content: const Text('هل أنت متأكد من حذف هذا المنتج؟'),
                                      actions: [
                                        TextButton(
                                          onPressed: () => Navigator.pop(ctx, false),
                                          child: const Text('إلغاء'),
                                        ),
                                        TextButton(
                                          onPressed: () => Navigator.pop(ctx, true),
                                          child: const Text('حذف',
                                              style: TextStyle(color: Colors.red)),
                                        ),
                                      ],
                                    ),
                                  );
                                  if (confirm == true) {
                                    await ref
                                        .read(productRepositoryProvider)
                                        .deleteProduct(product['id']);
                                    ref.invalidate(storeProductsProvider(store['id']));
                                  }
                                },
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
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(child: Text('Error: $err')),
      ),
    );
  }

  void _showAddEditProductDialog(
    BuildContext context,
    WidgetRef ref,
    String storeId,
    Map<String, dynamic>? product,
  ) {
    showDialog(
      context: context,
      builder: (ctx) =>
          _AddEditProductDialog(storeId: storeId, product: product, ref: ref),
    );
  }

  /// Opens the full management dialog for menu categories scoped to this store.
  void _showAddMenuCategoryDialog(
    BuildContext context,
    WidgetRef ref,
    String storeId,
  ) {
    showDialog(
      context: context,
      builder: (ctx) => _ManageMenuCategoriesDialog(
        storeId: storeId,
        ref: ref,
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Manage Menu Categories Dialog  (store-scoped → menu_categories table)
// Full CRUD: Add new categories + view/delete existing ones
// ─────────────────────────────────────────────────────────────────────────────

class _ManageMenuCategoriesDialog extends StatefulWidget {
  final String storeId;
  final WidgetRef ref;

  const _ManageMenuCategoriesDialog({
    required this.storeId,
    required this.ref,
  });

  @override
  State<_ManageMenuCategoriesDialog> createState() =>
      _ManageMenuCategoriesDialogState();
}

class _ManageMenuCategoriesDialogState
    extends State<_ManageMenuCategoriesDialog> {
  final TextEditingController _nameController = TextEditingController();
  bool _isAdding = false;
  bool _isDeleting = false;

  List<Map<String, dynamic>> _categories = [];
  bool _isLoadingList = true;
  String? _loadError;

  @override
  void initState() {
    super.initState();
    _loadCategories();
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  Future<void> _loadCategories() async {
    setState(() {
      _isLoadingList = true;
      _loadError = null;
    });
    try {
      final cats = await widget.ref
          .read(productRepositoryProvider)
          .getMenuCategoriesByStore(widget.storeId);
      if (mounted) {
        setState(() {
          _categories = cats;
          _isLoadingList = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _loadError = e.toString();
          _isLoadingList = false;
        });
      }
    }
  }

  Future<void> _addCategory() async {
    final name = _nameController.text.trim();
    if (name.isEmpty) return;

    setState(() => _isAdding = true);
    try {
      await widget.ref
          .read(productRepositoryProvider)
          .createMenuCategory(widget.storeId, name);
      _nameController.clear();
      // Refresh both the local list AND the external provider
      widget.ref.invalidate(storeMenuCategoriesProvider(widget.storeId));
      await _loadCategories();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('تم إضافة القسم "$name" بنجاح!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('فشل الإضافة: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isAdding = false);
    }
  }

  Future<void> _deleteCategory(Map<String, dynamic> category) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('تأكيد الحذف'),
        content: Text('هل أنت متأكد من حذف قسم "${category['name']}"؟'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('إلغاء'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('حذف', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirm != true || !mounted) return;

    setState(() => _isDeleting = true);
    try {
      await widget.ref
          .read(productRepositoryProvider)
          .deleteMenuCategory(category['id']);
      widget.ref.invalidate(storeMenuCategoriesProvider(widget.storeId));
      await _loadCategories();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('تم حذف القسم بنجاح!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        final errorMsg = e.toString().toLowerCase();
        final isFkError = errorMsg.contains('violates foreign key') ||
            errorMsg.contains('foreign key constraint') ||
            errorMsg.contains('referenced from') ||
            errorMsg.contains('23503');
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              isFkError
                  ? 'لا يمكن حذف قسم يحتوي على منتجات. احذف المنتجات أولاً.'
                  : 'فشل الحذف: $e',
            ),
            backgroundColor: isFkError ? Colors.orange : Colors.red,
            duration: const Duration(seconds: 4),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isDeleting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Row(
        children: [
          const Icon(Icons.category, color: Colors.blue),
          const SizedBox(width: 8),
          const Expanded(child: Text('إدارة أقسام المنيو')),
          IconButton(
            icon: const Icon(Icons.refresh, size: 20),
            onPressed: _loadCategories,
            tooltip: 'تحديث',
          ),
        ],
      ),
      content: SizedBox(
        width: 450,
        height: 420,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ── Info Banner ──────────────────────────────────────────────
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                border: Border.all(color: Colors.blue.shade200),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, size: 16, color: Colors.blue.shade700),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      'هذه الأقسام خاصة بهذا المطعم فقط (جدول menu_categories).',
                      style: TextStyle(fontSize: 12, color: Colors.blue.shade800),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // ── Add New Category Row ─────────────────────────────────────
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _nameController,
                    autofocus: true,
                    decoration: const InputDecoration(
                      labelText: 'اسم القسم الجديد',
                      hintText: 'مثال: المشويات، المشروبات...',
                      border: OutlineInputBorder(),
                      prefixIcon: Icon(Icons.label_outline),
                      isDense: true,
                    ),
                    onSubmitted: (_) => _addCategory(),
                  ),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _isAdding ? null : _addCategory,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                  ),
                  child: _isAdding
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(
                              strokeWidth: 2, color: Colors.white),
                        )
                      : const Text('إضافة'),
                ),
              ],
            ),

            const SizedBox(height: 16),
            const Divider(),
            const SizedBox(height: 8),

            // ── Header for existing list ─────────────────────────────────
            Text(
              'الأقسام الحالية (${_isLoadingList ? '...' : _categories.length})',
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
            ),
            const SizedBox(height: 8),

            // ── Categories List ──────────────────────────────────────────
            Expanded(
              child: _isLoadingList
                  ? const Center(child: CircularProgressIndicator())
                  : _loadError != null
                      ? Center(
                          child: Text('خطأ: $_loadError',
                              style: const TextStyle(color: Colors.red)),
                        )
                      : _categories.isEmpty
                          ? const Center(
                              child: Text(
                                'لا توجد أقسام بعد. أضف أول قسم أعلاه!',
                                style: TextStyle(color: Colors.grey),
                              ),
                            )
                          : ListView.separated(
                              itemCount: _categories.length,
                              separatorBuilder: (_, __) =>
                                  const Divider(height: 1),
                              itemBuilder: (context, index) {
                                final cat = _categories[index];
                                return ListTile(
                                  leading: CircleAvatar(
                                    backgroundColor: Colors.blue.shade100,
                                    child: Text(
                                      '${index + 1}',
                                      style: TextStyle(
                                          color: Colors.blue.shade800,
                                          fontWeight: FontWeight.bold),
                                    ),
                                  ),
                                  title: Text(
                                    cat['name'] ?? '',
                                    style: const TextStyle(
                                        fontWeight: FontWeight.w600),
                                  ),
                                  subtitle: Text(
                                    cat['is_active'] == true ? 'فعّال' : 'معطّل',
                                    style: TextStyle(
                                      fontSize: 11,
                                      color: cat['is_active'] == true
                                          ? Colors.green
                                          : Colors.red,
                                    ),
                                  ),
                                  trailing: IconButton(
                                    icon: const Icon(Icons.delete_outline,
                                        color: Colors.red),
                                    tooltip: 'حذف القسم',
                                    onPressed:
                                        _isDeleting ? null : () => _deleteCategory(cat),
                                  ),
                                );
                              },
                            ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () {
            // Invalidate the provider on close so the product dropdown refreshes
            widget.ref.invalidate(storeMenuCategoriesProvider(widget.storeId));
            Navigator.pop(context);
          },
          child: const Text('إغلاق'),
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add / Edit Product Dialog
// ─────────────────────────────────────────────────────────────────────────────

class _AddEditProductDialog extends StatefulWidget {
  final String storeId;
  final Map<String, dynamic>? product;
  final WidgetRef ref;

  const _AddEditProductDialog({
    required this.storeId,
    this.product,
    required this.ref,
  });

  @override
  State<_AddEditProductDialog> createState() => _AddEditProductDialogState();
}

class _AddEditProductDialogState extends State<_AddEditProductDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _descriptionController;
  late TextEditingController _priceController;
  late TextEditingController _stockController;
  late TextEditingController _imageUrlController;
  bool _isActive = true;
  String? _selectedCategoryId;
  bool _isLoading = false;
  bool _isUploading = false;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.product?['name'] ?? '');
    _descriptionController =
        TextEditingController(text: widget.product?['description'] ?? '');
    _priceController =
        TextEditingController(text: widget.product?['base_price']?.toString() ?? '');
    _stockController =
        TextEditingController(text: widget.product?['stock_quantity']?.toString() ?? '0');
    _imageUrlController =
        TextEditingController(text: widget.product?['image_url'] ?? '');
    _isActive = widget.product?['is_active'] ?? true;
    // Read from the correct FK column; fall back to category_id for legacy rows
    _selectedCategoryId = widget.product?['menu_category_id'] ??
        widget.product?['category_id'];
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    _priceController.dispose();
    _stockController.dispose();
    _imageUrlController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Watches the store-scoped menu_categories provider — never touches app_categories.
    final categoriesAsync =
        widget.ref.watch(storeMenuCategoriesProvider(widget.storeId));

    return AlertDialog(
      title: Text(widget.product == null ? 'إضافة منتج جديد' : 'تعديل المنتج'),
      content: SizedBox(
        width: 500,
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: _nameController,
                  decoration: const InputDecoration(
                      labelText: 'اسم المنتج', border: OutlineInputBorder()),
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
                            labelText: 'رابط الصورة', border: OutlineInputBorder()),
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton.icon(
                      onPressed: _isUploading ? null : _pickAndUploadImage,
                      icon: _isUploading
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(strokeWidth: 2))
                          : const Icon(Icons.upload_file),
                      label: const Text('رفع صورة'),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _descriptionController,
                  decoration: const InputDecoration(
                      labelText: 'الوصف', border: OutlineInputBorder()),
                  maxLines: 3,
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _priceController,
                        decoration: const InputDecoration(
                            labelText: 'السعر (د.ل)', border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                        validator: (val) =>
                            val == null || val.isEmpty ? 'مطلوب' : null,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: TextFormField(
                        controller: _stockController,
                        decoration: const InputDecoration(
                            labelText: 'المخزون', border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                // Category dropdown — strictly from menu_categories for this store
                categoriesAsync.when(
                  data: (categories) {
                    if (categories.isEmpty) {
                      return Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.amber.shade50,
                          border: Border.all(color: Colors.amber.shade300),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Row(
                          children: [
                            Icon(Icons.warning_amber_outlined,
                                color: Colors.amber.shade700, size: 18),
                            const SizedBox(width: 8),
                            const Expanded(
                              child: Text(
                                'لا توجد أقسام منيو لهذا المطعم. يرجى إضافة قسم أولاً.',
                                style: TextStyle(fontSize: 12),
                              ),
                            ),
                          ],
                        ),
                      );
                    }
                    return DropdownButtonFormField<String>(
                      decoration: const InputDecoration(
                          labelText: 'قسم المنيو', border: OutlineInputBorder()),
                      initialValue: _selectedCategoryId,
                      items: categories
                          .map((c) => DropdownMenuItem<String>(
                                value: c['id'],
                                child: Text(c['name']),
                              ))
                          .toList(),
                      onChanged: (val) =>
                          setState(() => _selectedCategoryId = val),
                      validator: (val) =>
                          val == null ? 'يرجى اختيار القسم' : null,
                    );
                  },
                  loading: () => const Padding(
                    padding: EdgeInsets.all(8.0),
                    child: CircularProgressIndicator(),
                  ),
                  error: (e, _) =>
                      Text('خطأ في تحميل الأقسام: $e'),
                ),
                const SizedBox(height: 16),
                SwitchListTile(
                  title: const Text('متاح للطلب (Available)'),
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
          child: const Text('إلغاء'),
        ),
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
    if (_formKey.currentState!.validate() && _selectedCategoryId != null) {
      setState(() => _isLoading = true);
      try {
        final data = {
          'store_id': widget.storeId,
          // ✅ Strict FK: references menu_categories.id (NOT app_categories)
          'menu_category_id': _selectedCategoryId,
          'name': _nameController.text,
          'description': _descriptionController.text,
          'base_price': double.tryParse(_priceController.text) ?? 0.0,
          'stock_quantity': int.tryParse(_stockController.text) ?? 0,
          'image_url': _imageUrlController.text.isEmpty
              ? null
              : _imageUrlController.text,
          'is_active': _isActive,
        };

        if (widget.product == null) {
          await widget.ref.read(productRepositoryProvider).createProduct(data);
        } else {
          await widget.ref
              .read(productRepositoryProvider)
              .updateProduct(widget.product!['id'], data);
        }

        widget.ref.invalidate(storeProductsProvider(widget.storeId));
        if (mounted) Navigator.pop(context);
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context)
              .showSnackBar(SnackBar(content: Text('Error: $e')));
        }
      } finally {
        if (mounted) setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _pickAndUploadImage() async {
    try {
      final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
      if (image == null) return;

      setState(() => _isUploading = true);

      final fileBytes = await image.readAsBytes();
      final uniqueName = '${DateTime.now().millisecondsSinceEpoch}_${image.name}';

      await Supabase.instance.client.storage
          .from('store_images')
          .uploadBinary(uniqueName, fileBytes,
              fileOptions: const FileOptions(upsert: true));

      setState(() {
        _imageUrlController.text = uniqueName; // relative path only
        _isUploading = false;
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
              content: Text('تم رفع الصورة بنجاح!'),
              backgroundColor: Colors.green),
        );
      }
    } catch (e) {
      setState(() => _isUploading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text('فشل رفع الصورة: $e'),
              backgroundColor: Colors.red),
        );
      }
    }
  }
}
