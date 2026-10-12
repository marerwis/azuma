import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../stores/providers/product_provider.dart';

class CatalogScreen extends ConsumerWidget {
  const CatalogScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final productsAsync = ref.watch(allProductsProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFF3F4F6),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // ── Header ──────────────────────────────────────────────────────────
          _ScreenHeader(
            onAdd: () => _showProductDialog(context, ref, null),
            onRefresh: () => ref.invalidate(allProductsProvider),
          ),

          // ── Table ───────────────────────────────────────────────────────────
          Expanded(
            child: productsAsync.when(
              data: (products) => _ProductsTable(
                products: products,
                onEdit: (p) => _showProductDialog(context, ref, p),
                onDelete: (id) => _confirmDelete(context, ref, id),
                onToggle: (id, val) async {
                  await ref
                      .read(productRepositoryProvider)
                      .toggleActive(id, val);
                  ref.invalidate(allProductsProvider);
                },
              ),
              loading: () =>
                  const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.error_outline,
                        color: Colors.red, size: 48),
                    const SizedBox(height: 8),
                    Text('خطأ في تحميل المنتجات:\n$e',
                        textAlign: TextAlign.center),
                    const SizedBox(height: 12),
                    ElevatedButton.icon(
                      onPressed: () => ref.invalidate(allProductsProvider),
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

  // ── Product Add/Edit Dialog ──────────────────────────────────────────────────

  void _showProductDialog(BuildContext context, WidgetRef ref,
      Map<String, dynamic>? product) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => _ProductFormDialog(
        product: product,
        onSaved: () {
          ref.invalidate(allProductsProvider);
        },
      ),
    );
  }

  Future<void> _confirmDelete(
      BuildContext context, WidgetRef ref, String id) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('تأكيد الحذف'),
        content: const Text(
            'سيتم حذف المنتج نهائياً. هل أنت متأكد؟'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('إلغاء')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red,
                foregroundColor: Colors.white),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('حذف'),
          ),
        ],
      ),
    );
    if (ok == true) {
      try {
        await ref.read(productRepositoryProvider).deleteProduct(id);
        ref.invalidate(allProductsProvider);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
                content: Text('تم حذف المنتج'),
                backgroundColor: Colors.green),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
                content: Text('خطأ: $e'),
                backgroundColor: Colors.red),
          );
        }
      }
    }
  }
}

// ── Screen Header ─────────────────────────────────────────────────────────────

class _ScreenHeader extends StatelessWidget {
  const _ScreenHeader(
      {required this.onAdd, required this.onRefresh});
  final VoidCallback onAdd;
  final VoidCallback onRefresh;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: const Color(0xFFFF5722).withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(Icons.restaurant_menu,
                color: Color(0xFFFF5722), size: 24),
          ),
          const SizedBox(width: 12),
          const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('إدارة المنتجات',
                  style: TextStyle(
                      fontSize: 20, fontWeight: FontWeight.bold)),
              Text('عرض وإدارة جميع المنتجات المتاحة',
                  style: TextStyle(color: Colors.grey, fontSize: 13)),
            ],
          ),
          const Spacer(),
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'تحديث',
            onPressed: onRefresh,
          ),
          const SizedBox(width: 8),
          ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFFF5722),
              foregroundColor: Colors.white,
              padding:
                  const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
            ),
            onPressed: onAdd,
            icon: const Icon(Icons.add),
            label: const Text('إضافة منتج',
                style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}

// ── Products DataTable ────────────────────────────────────────────────────────

class _ProductsTable extends StatefulWidget {
  const _ProductsTable({
    required this.products,
    required this.onEdit,
    required this.onDelete,
    required this.onToggle,
  });
  final List<Map<String, dynamic>> products;
  final void Function(Map<String, dynamic>) onEdit;
  final void Function(String) onDelete;
  final void Function(String, bool) onToggle;

  @override
  State<_ProductsTable> createState() => _ProductsTableState();
}

class _ProductsTableState extends State<_ProductsTable> {
  String _search = '';
  String? _filterStore;

  List<Map<String, dynamic>> get _filtered {
    return widget.products.where((p) {
      final name = (p['name'] ?? '').toString().toLowerCase();
      final storeName =
          ((p['store'] as Map?)?['name'] ?? '').toString().toLowerCase();
      final matchSearch = _search.isEmpty ||
          name.contains(_search.toLowerCase()) ||
          storeName.contains(_search.toLowerCase());
      final matchStore = _filterStore == null ||
          p['store_id']?.toString() == _filterStore;
      return matchSearch && matchStore;
    }).toList();
  }

  Set<String> get _storeIds {
    final ids = <String>{};
    for (final p in widget.products) {
      if (p['store_id'] != null) ids.add(p['store_id'].toString());
    }
    return ids;
  }

  @override
  Widget build(BuildContext context) {
    final fmt = NumberFormat('#,##0.00');
    final filtered = _filtered;

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        elevation: 2,
        shape:
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        child: Column(
          children: [
            // Search & filter bar
            Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  // Stats chip
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color:
                          const Color(0xFFFF5722).withValues(alpha: 0.08),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      '${filtered.length} منتج',
                      style: const TextStyle(
                          color: Color(0xFFFF5722),
                          fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(width: 16),

                  // Search
                  SizedBox(
                    width: 260,
                    child: TextField(
                      onChanged: (v) => setState(() => _search = v),
                      decoration: InputDecoration(
                        hintText: 'بحث باسم المنتج أو المطعم...',
                        isDense: true,
                        prefixIcon:
                            const Icon(Icons.search, size: 18),
                        border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(
                                color: Colors.grey.shade300)),
                        enabledBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(8),
                            borderSide: BorderSide(
                                color: Colors.grey.shade300)),
                        contentPadding:
                            const EdgeInsets.symmetric(
                                vertical: 10, horizontal: 12),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),

                  // Store filter
                  DropdownButtonHideUnderline(
                    child: DropdownButton<String?>(
                      value: _filterStore,
                      hint: const Text('كل المطاعم',
                          style: TextStyle(fontSize: 13)),
                      items: [
                        const DropdownMenuItem(
                            value: null,
                            child: Text('كل المطاعم')),
                        ..._storeIds.map((sid) {
                          final store = widget.products
                              .firstWhere((p) =>
                                  p['store_id']?.toString() == sid,
                              orElse: () => {})['store'];
                          final name =
                              (store as Map?)?['name'] ?? sid;
                          return DropdownMenuItem(
                              value: sid, child: Text(name.toString()));
                        }),
                      ],
                      onChanged: (v) =>
                          setState(() => _filterStore = v),
                    ),
                  ),
                ],
              ),
            ),

            const Divider(height: 1),

            // Table
            Expanded(
              child: filtered.isEmpty
                  ? const Center(
                      child: Text('لا توجد منتجات تطابق البحث',
                          style: TextStyle(color: Colors.grey)))
                  : SingleChildScrollView(
                      child: SizedBox(
                        width: double.infinity,
                        child: DataTable(
                          headingRowColor: WidgetStateProperty.all(
                              Colors.grey.shade50),
                          columnSpacing: 16,
                          dataRowMinHeight: 56,
                          dataRowMaxHeight: 72,
                          columns: const [
                            DataColumn(label: Text('الصورة')),
                            DataColumn(label: Text('اسم المنتج')),
                            DataColumn(label: Text('المطعم')),
                            DataColumn(label: Text('القسم')),
                            DataColumn(label: Text('السعر')),
                            DataColumn(label: Text('المخزون')),
                            DataColumn(label: Text('نباتي')),
                            DataColumn(label: Text('الحالة')),
                            DataColumn(label: Text('إجراءات')),
                          ],
                          rows: filtered.map((p) {
                            final store = p['store'] as Map?;
                            final menuCat = p['menu_cat'] as Map?;
                            final isActive =
                                p['is_active'] as bool? ?? false;
                            final isVeg =
                                p['is_veg'] as bool? ?? false;
                            final price =
                                (p['base_price'] as num?)
                                    ?.toDouble() ??
                                    0;
                            final stock =
                                p['stock_quantity'] as int? ?? 0;

                            return DataRow(cells: [
                              // Image
                              DataCell(_ProductImage(
                                  p['image_url']?.toString())),

                              // Name
                              DataCell(SizedBox(
                                width: 160,
                                child: Text(
                                  p['name'] ?? '--',
                                  style: const TextStyle(
                                      fontWeight:
                                          FontWeight.w600),
                                  maxLines: 2,
                                  overflow:
                                      TextOverflow.ellipsis,
                                ),
                              )),

                              // Store
                              DataCell(Text(
                                  store?['name'] ?? '--',
                                  style: const TextStyle(
                                      fontSize: 13))),

                              // Menu Category
                              DataCell(Text(
                                  menuCat?['name'] ?? '--',
                                  style: const TextStyle(
                                      fontSize: 13,
                                      color: Colors.grey))),

                              // Price
                              DataCell(Text(
                                '${fmt.format(price)} د.ل',
                                style: const TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: Color(0xFFFF5722)),
                              )),

                              // Stock
                              DataCell(_StockBadge(stock)),

                              // Veg
                              DataCell(Icon(
                                isVeg
                                    ? Icons.eco
                                    : Icons.no_food,
                                color: isVeg
                                    ? Colors.green
                                    : Colors.grey,
                                size: 20,
                              )),

                              // Active toggle
                              DataCell(Switch.adaptive(
                                value: isActive,
                                activeColor:
                                    const Color(0xFFFF5722),
                                onChanged: (v) =>
                                    widget.onToggle(
                                        p['id'].toString(), v),
                              )),

                              // Actions
                              DataCell(Row(
                                mainAxisSize:
                                    MainAxisSize.min,
                                children: [
                                  IconButton(
                                    icon: const Icon(
                                        Icons.edit_outlined,
                                        size: 18),
                                    tooltip: 'تعديل',
                                    color: Colors.blue,
                                    onPressed: () =>
                                        widget.onEdit(p),
                                  ),
                                  IconButton(
                                    icon: const Icon(
                                        Icons.delete_outline,
                                        size: 18),
                                    tooltip: 'حذف',
                                    color: Colors.red,
                                    onPressed: () =>
                                        widget.onDelete(
                                            p['id'].toString()),
                                  ),
                                ],
                              )),
                            ]);
                          }).toList(),
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

// ── Product Form Dialog ───────────────────────────────────────────────────────

class _ProductFormDialog extends ConsumerStatefulWidget {
  const _ProductFormDialog(
      {required this.product, required this.onSaved});
  final Map<String, dynamic>? product;
  final VoidCallback onSaved;

  @override
  ConsumerState<_ProductFormDialog> createState() =>
      _ProductFormDialogState();
}

class _ProductFormDialogState
    extends ConsumerState<_ProductFormDialog> {
  final _formKey = GlobalKey<FormState>();
  bool _saving = false;

  // Controllers
  late final TextEditingController _name;
  late final TextEditingController _description;
  late final TextEditingController _price;
  late final TextEditingController _stock;
  late final TextEditingController _imageUrl;

  // Dropdown selections
  String? _storeId;
  String? _categoryId;        // app_categories
  String? _menuCategoryId;    // menu_categories (scoped to store)
  bool _isVeg = false;
  bool _isActive = true;

  bool get _isEdit => widget.product != null;

  @override
  void initState() {
    super.initState();
    final p = widget.product;
    _name = TextEditingController(text: p?['name'] ?? '');
    _description =
        TextEditingController(text: p?['description'] ?? '');
    _price = TextEditingController(
        text: (p?['base_price'] as num?)?.toString() ?? '');
    _stock = TextEditingController(
        text: (p?['stock_quantity'] as int?)?.toString() ?? '0');
    _imageUrl =
        TextEditingController(text: p?['image_url'] ?? '');
    _storeId = p?['store_id']?.toString();
    _categoryId = p?['category_id']?.toString();
    _menuCategoryId = p?['menu_category_id']?.toString();
    _isVeg = p?['is_veg'] as bool? ?? false;
    _isActive = p?['is_active'] as bool? ?? true;
  }

  @override
  void dispose() {
    _name.dispose();
    _description.dispose();
    _price.dispose();
    _stock.dispose();
    _imageUrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    if (_storeId == null) {
      _showError('يرجى اختيار المطعم');
      return;
    }
    setState(() => _saving = true);
    try {
      final repo = ref.read(productRepositoryProvider);
      final args = (
        storeId: _storeId!,
        categoryId: _categoryId,
        menuCategoryId: _menuCategoryId,
        name: _name.text.trim(),
        description: _description.text.trim().isEmpty
            ? null
            : _description.text.trim(),
        basePrice: double.parse(_price.text.trim()),
        stockQuantity: int.tryParse(_stock.text.trim()) ?? 0,
        isVeg: _isVeg,
        isActive: _isActive,
        imageUrl: _imageUrl.text.trim().isEmpty
            ? null
            : _imageUrl.text.trim(),
      );
      if (_isEdit) {
        await repo.updateProduct(
          widget.product!['id'].toString(),
          storeId: args.storeId,
          categoryId: args.categoryId,
          menuCategoryId: args.menuCategoryId,
          name: args.name,
          description: args.description,
          basePrice: args.basePrice,
          stockQuantity: args.stockQuantity,
          isVeg: args.isVeg,
          isActive: args.isActive,
          imageUrl: args.imageUrl,
        );
      } else {
        await repo.createProduct(
          storeId: args.storeId,
          categoryId: args.categoryId,
          menuCategoryId: args.menuCategoryId,
          name: args.name,
          description: args.description,
          basePrice: args.basePrice,
          stockQuantity: args.stockQuantity,
          isVeg: args.isVeg,
          isActive: args.isActive,
          imageUrl: args.imageUrl,
        );
      }
      widget.onSaved();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) _showError('خطأ: $e');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg), backgroundColor: Colors.red),
    );
  }

  @override
  Widget build(BuildContext context) {
    final storesAsync = ref.watch(storesListProvider);
    final appCatsAsync = ref.watch(appCategoriesProvider);
    final menuCatsAsync = _storeId != null
        ? ref.watch(menuCategoriesForStoreProvider(_storeId!))
        : const AsyncValue<List<Map<String, dynamic>>>.data([]);

    return Dialog(
      shape:
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      insetPadding:
          const EdgeInsets.symmetric(horizontal: 40, vertical: 24),
      child: SizedBox(
        width: 680,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Dialog header
            Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: 24, vertical: 16),
              decoration: const BoxDecoration(
                color: Color(0xFFFF5722),
                borderRadius:
                    BorderRadius.vertical(top: Radius.circular(16)),
              ),
              child: Row(
                children: [
                  Icon(
                    _isEdit
                        ? Icons.edit_note
                        : Icons.add_circle_outline,
                    color: Colors.white,
                  ),
                  const SizedBox(width: 10),
                  Text(
                    _isEdit ? 'تعديل المنتج' : 'إضافة منتج جديد',
                    style: const TextStyle(
                        color: Colors.white,
                        fontSize: 18,
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

            // Form body
            Flexible(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(24),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Row 1: Store + App Category
                      Row(
                        children: [
                          Expanded(
                            child: _buildDropdownField<String?>(
                              label: 'المطعم *',
                              value: _storeId,
                              asyncValue: storesAsync,
                              itemBuilder: (stores) => stores
                                  .map((s) => DropdownMenuItem(
                                        value: s['id'].toString(),
                                        child:
                                            Text(s['name'] ?? ''),
                                      ))
                                  .toList(),
                              onChanged: (v) => setState(() {
                                _storeId = v;
                                _menuCategoryId =
                                    null; // reset menu cat
                              }),
                              validator: (v) =>
                                  v == null ? 'مطلوب' : null,
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: _buildDropdownField<String?>(
                              label: 'تصنيف التطبيق (category_id)',
                              value: _categoryId,
                              asyncValue: appCatsAsync,
                              itemBuilder: (cats) => [
                                const DropdownMenuItem(
                                    value: null,
                                    child: Text('-- بدون --')),
                                ...cats.map((c) => DropdownMenuItem(
                                      value: c['id'].toString(),
                                      child:
                                          Text(c['name'] ?? ''),
                                    )),
                              ],
                              onChanged: (v) =>
                                  setState(() => _categoryId = v),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Row 2: Menu Category
                      _buildDropdownField<String?>(
                        label: 'قسم المطعم (menu_category_id)',
                        value: _menuCategoryId,
                        asyncValue: menuCatsAsync,
                        itemBuilder: (cats) => [
                          const DropdownMenuItem(
                              value: null,
                              child: Text('-- بدون --')),
                          ...cats.map((c) => DropdownMenuItem(
                                value: c['id'].toString(),
                                child: Text(c['name'] ?? ''),
                              )),
                        ],
                        onChanged: (v) =>
                            setState(() => _menuCategoryId = v),
                        hint: _storeId == null
                            ? 'اختر المطعم أولاً'
                            : null,
                      ),
                      const SizedBox(height: 16),

                      // Row 3: Name
                      _buildTextField(
                        controller: _name,
                        label: 'اسم المنتج (name) *',
                        validator: (v) => (v == null || v.trim().isEmpty)
                            ? 'مطلوب'
                            : null,
                      ),
                      const SizedBox(height: 16),

                      // Row 4: Description
                      _buildTextField(
                        controller: _description,
                        label: 'الوصف (description)',
                        maxLines: 3,
                      ),
                      const SizedBox(height: 16),

                      // Row 5: Price + Stock
                      Row(
                        children: [
                          Expanded(
                            child: _buildTextField(
                              controller: _price,
                              label: 'السعر الأساسي (base_price) *',
                              keyboardType:
                                  TextInputType.number,
                              prefix: 'د.ل',
                              validator: (v) {
                                if (v == null || v.trim().isEmpty)
                                  return 'مطلوب';
                                if (double.tryParse(v.trim()) ==
                                    null) {
                                  return 'رقم غير صالح';
                                }
                                return null;
                              },
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: _buildTextField(
                              controller: _stock,
                              label:
                                  'الكمية بالمخزون (stock_quantity)',
                              keyboardType:
                                  TextInputType.number,
                              validator: (v) {
                                if (v != null &&
                                    v.trim().isNotEmpty &&
                                    int.tryParse(v.trim()) ==
                                        null) {
                                  return 'عدد صحيح فقط';
                                }
                                return null;
                              },
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Row 6: Image URL
                      _buildTextField(
                        controller: _imageUrl,
                        label: 'رابط الصورة (image_url)',
                        hint: 'https://... أو المسار النسبي',
                      ),
                      const SizedBox(height: 16),

                      // Row 7: Switches
                      Row(
                        children: [
                          Expanded(
                            child: _SwitchTile(
                              label: 'منتج نباتي (is_veg)',
                              value: _isVeg,
                              icon: Icons.eco,
                              activeColor: Colors.green,
                              onChanged: (v) =>
                                  setState(() => _isVeg = v),
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: _SwitchTile(
                              label: 'متاح (is_active)',
                              value: _isActive,
                              icon: Icons.visibility,
                              activeColor:
                                  const Color(0xFFFF5722),
                              onChanged: (v) =>
                                  setState(() => _isActive = v),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),

            // Actions
            Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: 24, vertical: 16),
              decoration: BoxDecoration(
                color: Colors.grey.shade50,
                borderRadius: const BorderRadius.vertical(
                    bottom: Radius.circular(16)),
                border: Border(
                    top: BorderSide(color: Colors.grey.shade200)),
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
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFFF5722),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(
                          horizontal: 24, vertical: 14),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8)),
                    ),
                    onPressed: _saving ? null : _save,
                    icon: _saving
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(
                                color: Colors.white,
                                strokeWidth: 2))
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

  // ── Form Helpers ─────────────────────────────────────────────────────────────

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    String? hint,
    int maxLines = 1,
    TextInputType keyboardType = TextInputType.text,
    String? prefix,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: controller,
      maxLines: maxLines,
      keyboardType: keyboardType,
      decoration: InputDecoration(
        labelText: label,
        hintText: hint,
        prefixText: prefix != null ? '$prefix  ' : null,
        border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8)),
        isDense: true,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
      ),
      validator: validator,
    );
  }

  Widget _buildDropdownField<T>({
    required String label,
    required T value,
    required AsyncValue<List<Map<String, dynamic>>> asyncValue,
    required List<DropdownMenuItem<T>> Function(
            List<Map<String, dynamic>>)
        itemBuilder,
    required void Function(T?) onChanged,
    String? Function(T?)? validator,
    String? hint,
  }) {
    return asyncValue.when(
      data: (items) => DropdownButtonFormField<T>(
        value: value,
        decoration: InputDecoration(
          labelText: label,
          border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8)),
          isDense: true,
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        ),
        hint: hint != null
            ? Text(hint,
                style: const TextStyle(color: Colors.grey))
            : null,
        items: itemBuilder(items),
        onChanged: onChanged,
        validator: validator,
      ),
      loading: () => const LinearProgressIndicator(),
      error: (e, _) => Text('خطأ: $e',
          style: const TextStyle(color: Colors.red)),
    );
  }
}

// ── Small Widgets ─────────────────────────────────────────────────────────────

class _ProductImage extends StatelessWidget {
  const _ProductImage(this.url);
  final String? url;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 48,
      height: 48,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(8),
        color: Colors.grey.shade100,
      ),
      clipBehavior: Clip.antiAlias,
      child: url != null && url!.startsWith('http')
          ? Image.network(url!, fit: BoxFit.cover,
              errorBuilder: (_, __, ___) =>
                  const Icon(Icons.image_not_supported,
                      color: Colors.grey, size: 24))
          : const Icon(Icons.fastfood,
              color: Colors.grey, size: 24),
    );
  }
}

class _StockBadge extends StatelessWidget {
  const _StockBadge(this.stock);
  final int stock;

  @override
  Widget build(BuildContext context) {
    final color = stock == 0
        ? Colors.red
        : stock < 5
            ? Colors.orange
            : Colors.green;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        border: Border.all(color: color.withValues(alpha: 0.4)),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        stock == 0 ? 'نفد' : '$stock',
        style: TextStyle(
            color: color,
            fontSize: 12,
            fontWeight: FontWeight.bold),
      ),
    );
  }
}

class _SwitchTile extends StatelessWidget {
  const _SwitchTile({
    required this.label,
    required this.value,
    required this.icon,
    required this.activeColor,
    required this.onChanged,
  });
  final String label;
  final bool value;
  final IconData icon;
  final Color activeColor;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: value
            ? activeColor.withValues(alpha: 0.06)
            : Colors.grey.shade50,
        border: Border.all(
            color: value
                ? activeColor.withValues(alpha: 0.3)
                : Colors.grey.shade300),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(icon,
              color: value ? activeColor : Colors.grey,
              size: 18),
          const SizedBox(width: 8),
          Expanded(
            child: Text(label,
                style: TextStyle(
                    fontSize: 13,
                    color: value ? activeColor : Colors.grey[700])),
          ),
          Switch.adaptive(
            value: value,
            activeColor: activeColor,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}
