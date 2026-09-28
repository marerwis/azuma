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
              label: Text(MediaQuery.of(context).size.width > 600 ? 'إضافة قسم' : 'قسم'),
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

  /// Opens a dialog to create a new [menu_categories] row scoped to this store.
  void _showAddMenuCategoryDialog(
    BuildContext context,
    WidgetRef ref,
    String storeId,
  ) {
    final nameController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => _AddMenuCategoryDialog(
        storeId: storeId,
        nameController: nameController,
        ref: ref,
        parentContext: context,
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Menu Category Dialog  (store-scoped → menu_categories table)
// ─────────────────────────────────────────────────────────────────────────────

class _AddMenuCategoryDialog extends StatefulWidget {
  final String storeId;
  final TextEditingController nameController;
  final WidgetRef ref;
  final BuildContext parentContext;

  const _AddMenuCategoryDialog({
    required this.storeId,
    required this.nameController,
    required this.ref,
    required this.parentContext,
  });

  @override
  State<_AddMenuCategoryDialog> createState() => _AddMenuCategoryDialogState();
}

class _AddMenuCategoryDialogState extends State<_AddMenuCategoryDialog> {
  bool _isSaving = false;

  Future<void> _save() async {
    final name = widget.nameController.text.trim();
    if (name.isEmpty) return;
    // Capture messenger BEFORE any await to satisfy use_build_context_synchronously.
    final messenger = ScaffoldMessenger.of(widget.parentContext);
    setState(() => _isSaving = true);
    try {
      await widget.ref
          .read(productRepositoryProvider)
          .createMenuCategory(widget.storeId, name);
      // Invalidate the scoped provider — the "Add Product" dropdown updates immediately.
      widget.ref.invalidate(storeMenuCategoriesProvider(widget.storeId));
      if (mounted) {
        Navigator.pop(context);
        messenger.showSnackBar(
          SnackBar(
            content: Text('تم إضافة القسم "$name" بنجاح!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      setState(() => _isSaving = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('فشل: $e'), backgroundColor: Colors.red),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('إضافة قسم منيو جديد'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Info banner — makes it clear this is store-scoped
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
            decoration: BoxDecoration(
              color: Colors.orange.shade50,
              border: Border.all(color: Colors.orange.shade200),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.info_outline, size: 16, color: Colors.orange.shade700),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    'هذا القسم خاص بهذا المطعم فقط.\nيُحفظ في جدول menu_categories.',
                    style: TextStyle(fontSize: 12, color: Colors.orange.shade800),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: widget.nameController,
            autofocus: true,
            decoration: const InputDecoration(
              labelText: 'اسم القسم (مثال: المشويات، المشروبات)',
              border: OutlineInputBorder(),
              prefixIcon: Icon(Icons.label_outline),
            ),
            onSubmitted: (_) => _save(),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('إلغاء'),
        ),
        ElevatedButton(
          onPressed: _isSaving ? null : _save,
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFFFF5722),
            foregroundColor: Colors.white,
          ),
          child: _isSaving
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                )
              : const Text('إضافة'),
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
    _selectedCategoryId = widget.product?['category_id'];
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
                                'لا توجد أقسام لهذا المطعم. أضف قسماً أولاً من زر "إضافة قسم".',
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
          'category_id': _selectedCategoryId,
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
