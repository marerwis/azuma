import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
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
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: ElevatedButton.icon(
              onPressed: () => _showAddEditProductDialog(context, ref, store['id'], null),
              icon: const Icon(Icons.add),
              label: const Text('إضافة منتج'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF5722),
                foregroundColor: Colors.white,
              ),
            ),
          ),
        ],
      ),
      body: productsAsync.when(
        data: (products) {
          if (products.isEmpty) return const Center(child: Text('لا توجد منتجات في هذا المطعم'));
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: MaterialStateProperty.all(Colors.grey[200]),
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
                                backgroundImage: product['image_url'] != null ? NetworkImage(product['image_url']) : null,
                                child: product['image_url'] == null ? const Icon(Icons.fastfood) : null,
                              ),
                              const SizedBox(width: 8),
                              Text(product['name'] ?? ''),
                            ],
                          ),
                        ),
                        DataCell(Text(product['categories']?['name'] ?? 'بدون قسم')),
                        DataCell(Text('${product['base_price']} د.ل')),
                        DataCell(Text('${product['stock_quantity'] ?? 0}')),
                        DataCell(
                          Icon(isActive ? Icons.check_circle : Icons.cancel, color: isActive ? Colors.green : Colors.red),
                        ),
                        DataCell(
                          Row(
                            children: [
                              IconButton(
                                icon: const Icon(Icons.edit, color: Colors.orange),
                                tooltip: 'تعديل',
                                onPressed: () => _showAddEditProductDialog(context, ref, store['id'], product),
                              ),
                              IconButton(
                                icon: const Icon(Icons.delete, color: Colors.red),
                                tooltip: 'حذف',
                                onPressed: () async {
                                  final confirm = await showDialog<bool>(
                                    context: context,
                                    builder: (context) => AlertDialog(
                                      title: const Text('تأكيد الحذف'),
                                      content: const Text('هل أنت متأكد من حذف هذا المنتج؟'),
                                      actions: [
                                        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('إلغاء')),
                                        TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('حذف', style: TextStyle(color: Colors.red))),
                                      ],
                                    ),
                                  );
                                  if (confirm == true) {
                                    await ref.read(productRepositoryProvider).deleteProduct(product['id']);
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

  void _showAddEditProductDialog(BuildContext context, WidgetRef ref, String storeId, Map<String, dynamic>? product) {
    showDialog(
      context: context,
      builder: (context) => _AddEditProductDialog(storeId: storeId, product: product, ref: ref),
    );
  }
}

class _AddEditProductDialog extends StatefulWidget {
  final String storeId;
  final Map<String, dynamic>? product;
  final WidgetRef ref;

  const _AddEditProductDialog({required this.storeId, this.product, required this.ref});

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

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.product?['name'] ?? '');
    _descriptionController = TextEditingController(text: widget.product?['description'] ?? '');
    _priceController = TextEditingController(text: widget.product?['base_price']?.toString() ?? '');
    _stockController = TextEditingController(text: widget.product?['stock_quantity']?.toString() ?? '0');
    _imageUrlController = TextEditingController(text: widget.product?['image_url'] ?? '');
    _isActive = widget.product?['is_active'] ?? true;
    _selectedCategoryId = widget.product?['category_id'];
  }

  @override
  Widget build(BuildContext context) {
    final categoriesAsync = widget.ref.watch(allCategoriesProvider);

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
                  decoration: const InputDecoration(labelText: 'اسم المنتج', border: OutlineInputBorder()),
                  validator: (val) => val == null || val.isEmpty ? 'مطلوب' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _imageUrlController,
                  decoration: const InputDecoration(labelText: 'رابط الصورة (Image URL)', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _descriptionController,
                  decoration: const InputDecoration(labelText: 'الوصف', border: OutlineInputBorder()),
                  maxLines: 3,
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _priceController,
                        decoration: const InputDecoration(labelText: 'السعر (د.ل)', border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                        validator: (val) => val == null || val.isEmpty ? 'مطلوب' : null,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: TextFormField(
                        controller: _stockController,
                        decoration: const InputDecoration(labelText: 'كمية المخزون (Stock)', border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                categoriesAsync.when(
                  data: (categories) => DropdownButtonFormField<String>(
                    decoration: const InputDecoration(labelText: 'القسم', border: OutlineInputBorder()),
                    value: _selectedCategoryId,
                    items: categories.map((c) => DropdownMenuItem<String>(
                      value: c['id'],
                      child: Text(c['name']),
                    )).toList(),
                    onChanged: (val) => setState(() => _selectedCategoryId = val),
                    validator: (val) => val == null ? 'يرجى اختيار القسم' : null,
                  ),
                  loading: () => const CircularProgressIndicator(),
                  error: (_, __) => const Text('خطأ في تحميل الأقسام'),
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
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('إلغاء')),
        ElevatedButton(
          onPressed: _isLoading ? null : _save,
          child: _isLoading ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator()) : const Text('حفظ'),
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
          'image_url': _imageUrlController.text.isEmpty ? null : _imageUrlController.text,
          'is_active': _isActive,
        };

        if (widget.product == null) {
          await widget.ref.read(productRepositoryProvider).createProduct(data);
        } else {
          await widget.ref.read(productRepositoryProvider).updateProduct(widget.product!['id'], data);
        }

        widget.ref.invalidate(storeProductsProvider(widget.storeId));
        if (mounted) Navigator.pop(context);
      } catch (e) {
        if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
      } finally {
        if (mounted) setState(() => _isLoading = false);
      }
    }
  }
}
