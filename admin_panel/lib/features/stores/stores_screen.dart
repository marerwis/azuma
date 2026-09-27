import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'providers/store_provider.dart';
import 'store_menu_screen.dart';

class StoresScreen extends ConsumerWidget {
  const StoresScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final storesAsync = ref.watch(storesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('إدارة المطاعم - Stores'),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: ElevatedButton.icon(
              onPressed: () => _showAddEditStoreDialog(context, ref, null),
              icon: const Icon(Icons.add),
              label: const Text('إضافة مطعم'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF5722),
                foregroundColor: Colors.white,
              ),
            ),
          ),
        ],
      ),
      body: storesAsync.when(
        data: (stores) {
          if (stores.isEmpty) return const Center(child: Text('لا توجد مطاعم'));
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: MaterialStateProperty.all(Colors.grey[200]),
                  columns: const [
                    DataColumn(label: Text('المطعم')),
                    DataColumn(label: Text('العنوان')),
                    DataColumn(label: Text('العمولة (%)')),
                    DataColumn(label: Text('أوقات العمل')),
                    DataColumn(label: Text('الحالة')),
                    DataColumn(label: Text('الإجراءات')),
                  ],
                  rows: stores.map((store) {
                    final isOpen = store['is_open'] == true;
                    return DataRow(
                      cells: [
                        DataCell(
                          Row(
                            children: [
                              CircleAvatar(
                                backgroundImage: store['image_url'] != null ? NetworkImage(store['image_url']) : null,
                                child: store['image_url'] == null ? const Icon(Icons.store) : null,
                              ),
                              const SizedBox(width: 8),
                              Text(store['name'] ?? ''),
                            ],
                          ),
                        ),
                        DataCell(Text(store['address'] ?? '')),
                        DataCell(Text(store['commission_rate']?.toString() ?? '10.0')),
                        DataCell(Text('${store['opening_time'] ?? '--:--'} - ${store['closing_time'] ?? '--:--'}')),
                        DataCell(
                          Chip(
                            label: Text(isOpen ? 'مفتوح' : 'مغلق', style: const TextStyle(color: Colors.white)),
                            backgroundColor: isOpen ? Colors.green : Colors.red,
                          ),
                        ),
                        DataCell(
                          Row(
                            children: [
                              IconButton(
                                icon: const Icon(Icons.fastfood, color: Colors.blue),
                                tooltip: 'المنيو',
                                onPressed: () {
                                  Navigator.push(
                                    context,
                                    MaterialPageRoute(builder: (_) => StoreMenuScreen(store: store)),
                                  );
                                },
                              ),
                              IconButton(
                                icon: const Icon(Icons.edit, color: Colors.orange),
                                tooltip: 'تعديل',
                                onPressed: () => _showAddEditStoreDialog(context, ref, store),
                              ),
                              IconButton(
                                icon: const Icon(Icons.delete, color: Colors.red),
                                tooltip: 'حذف',
                                onPressed: () async {
                                  final confirm = await showDialog<bool>(
                                    context: context,
                                    builder: (context) => AlertDialog(
                                      title: const Text('تأكيد الحذف'),
                                      content: const Text('هل أنت متأكد من حذف هذا المطعم؟'),
                                      actions: [
                                        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('إلغاء')),
                                        TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('حذف', style: TextStyle(color: Colors.red))),
                                      ],
                                    ),
                                  );
                                  if (confirm == true) {
                                    await ref.read(storeRepositoryProvider).deleteStore(store['id']);
                                    ref.invalidate(storesProvider);
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

  void _showAddEditStoreDialog(BuildContext context, WidgetRef ref, Map<String, dynamic>? store) {
    showDialog(
      context: context,
      builder: (context) => _AddEditStoreDialog(store: store, ref: ref),
    );
  }
}

class _AddEditStoreDialog extends StatefulWidget {
  final Map<String, dynamic>? store;
  final WidgetRef ref;

  const _AddEditStoreDialog({this.store, required this.ref});

  @override
  State<_AddEditStoreDialog> createState() => _AddEditStoreDialogState();
}

class _AddEditStoreDialogState extends State<_AddEditStoreDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _addressController;
  late TextEditingController _radiusController;
  late TextEditingController _commissionController;
  late TextEditingController _openingTimeController;
  late TextEditingController _closingTimeController;
  late TextEditingController _imageUrlController;
  bool _isActive = true;
  bool _isOpen = true;
  bool _isLoading = false;
  bool _isUploading = false;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.store?['name'] ?? '');
    _addressController = TextEditingController(text: widget.store?['address'] ?? '');
    _radiusController = TextEditingController(text: widget.store?['delivery_radius_km']?.toString() ?? '5.0');
    _commissionController = TextEditingController(text: widget.store?['commission_rate']?.toString() ?? '10.0');
    _openingTimeController = TextEditingController(text: widget.store?['opening_time'] ?? '09:00:00');
    _closingTimeController = TextEditingController(text: widget.store?['closing_time'] ?? '23:00:00');
    _imageUrlController = TextEditingController(text: widget.store?['image_url'] ?? '');
    _isActive = widget.store?['is_active'] ?? true;
    _isOpen = widget.store?['is_open'] ?? true;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.store == null ? 'إضافة مطعم جديد' : 'تعديل المطعم'),
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
                  decoration: const InputDecoration(labelText: 'اسم المطعم', border: OutlineInputBorder()),
                  validator: (val) => val == null || val.isEmpty ? 'مطلوب' : null,
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _imageUrlController,
                        decoration: const InputDecoration(labelText: 'رابط الشعار (Image URL)', border: OutlineInputBorder()),
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton.icon(
                      onPressed: _isUploading ? null : _pickAndUploadImage,
                      icon: _isUploading ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)) : const Icon(Icons.upload_file),
                      label: const Text('رفع صورة'),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _addressController,
                  decoration: const InputDecoration(labelText: 'العنوان', border: OutlineInputBorder()),
                  validator: (val) => val == null || val.isEmpty ? 'مطلوب' : null,
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _radiusController,
                        decoration: const InputDecoration(labelText: 'نطاق التوصيل (كم)', border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: TextFormField(
                        controller: _commissionController,
                        decoration: const InputDecoration(labelText: 'العمولة (%)', border: OutlineInputBorder()),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _openingTimeController,
                        decoration: const InputDecoration(labelText: 'وقت الافتتاح (HH:MM:SS)', border: OutlineInputBorder()),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: TextFormField(
                        controller: _closingTimeController,
                        decoration: const InputDecoration(labelText: 'وقت الإغلاق (HH:MM:SS)', border: OutlineInputBorder()),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                SwitchListTile(
                  title: const Text('المطعم متاح (Active)'),
                  value: _isActive,
                  onChanged: (val) => setState(() => _isActive = val),
                ),
                SwitchListTile(
                  title: const Text('المطعم مفتوح للطلبات (Open)'),
                  value: _isOpen,
                  onChanged: (val) => setState(() => _isOpen = val),
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
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        final data = {
          'name': _nameController.text,
          'image_url': _imageUrlController.text.isEmpty ? null : _imageUrlController.text,
          'address': _addressController.text,
          'delivery_radius_km': double.tryParse(_radiusController.text) ?? 5.0,
          'commission_rate': double.tryParse(_commissionController.text) ?? 10.0,
          'opening_time': _openingTimeController.text,
          'closing_time': _closingTimeController.text,
          'is_active': _isActive,
          'is_open': _isOpen,
          // Defaults for latitude/longitude as admin usually sets address and a map picker sets coords
          'latitude': 32.115,
          'longitude': 20.082,
        };

        if (widget.store == null) {
          // You need to set a valid vendor_id here for real usage, we assume a hardcoded vendor or null if nullable
          // In actual app, admin selects the vendor from a dropdown
          data['vendor_id'] = '11111111-1111-1111-1111-111111111111';
          await widget.ref.read(storeRepositoryProvider).createStore(data);
        } else {
          await widget.ref.read(storeRepositoryProvider).updateStore(widget.store!['id'], data);
        }

        widget.ref.invalidate(storesProvider);
        if (mounted) Navigator.pop(context);
      } catch (e) {
        if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
      } finally {
        if (mounted) setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _pickAndUploadImage() async {
    try {
      final XFile? image = await _picker.pickImage(source: ImageSource.gallery);

      if (image != null) {
        setState(() => _isUploading = true);
        
        final fileBytes = await image.readAsBytes();
        final fileName = image.name;
        final uniqueName = '${DateTime.now().millisecondsSinceEpoch}_$fileName';

        // Upload to Supabase Storage
        await Supabase.instance.client.storage.from('store_images').uploadBinary(
          uniqueName,
          fileBytes,
          fileOptions: const FileOptions(upsert: true),
        );

        // Get public URL
        final String publicUrl = Supabase.instance.client.storage.from('store_images').getPublicUrl(uniqueName);
        
        setState(() {
          _imageUrlController.text = publicUrl;
          _isUploading = false;
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('تم رفع الصورة بنجاح!'), backgroundColor: Colors.green));
        }
      }
    } catch (e) {
      setState(() => _isUploading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('فشل رفع الصورة: $e'), backgroundColor: Colors.red));
      }
    }
  }
}
