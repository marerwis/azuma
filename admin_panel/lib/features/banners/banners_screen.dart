import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:file_picker/file_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'providers/banner_provider.dart';

class BannersScreen extends ConsumerWidget {
  const BannersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bannersAsync = ref.watch(bannersProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('إدارة العروض والإعلانات'),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: ElevatedButton.icon(
              onPressed: () => _showAddEditBannerDialog(context, ref, null),
              icon: const Icon(Icons.add),
              label: const Text('إضافة إعلان'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF5722),
                foregroundColor: Colors.white,
              ),
            ),
          ),
        ],
      ),
      body: bannersAsync.when(
        data: (banners) {
          if (banners.isEmpty) return const Center(child: Text('لا توجد إعلانات'));
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: MaterialStateProperty.all(Colors.grey[200]),
                  columns: const [
                    DataColumn(label: Text('صورة الإعلان')),
                    DataColumn(label: Text('العنوان / الرابط')),
                    DataColumn(label: Text('الترتيب')),
                    DataColumn(label: Text('الحالة')),
                    DataColumn(label: Text('الإجراءات')),
                  ],
                  rows: banners.map((banner) {
                    final isActive = banner['is_active'] == true;
                    return DataRow(
                      cells: [
                        DataCell(
                          Container(
                            width: 100,
                            height: 50,
                            decoration: BoxDecoration(
                              image: DecorationImage(
                                image: NetworkImage(banner['image_url'] ?? ''),
                                fit: BoxFit.cover,
                              ),
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ),
                        ),
                        DataCell(Text(banner['action_url'] ?? '')),
                        DataCell(Text(banner['sort_order']?.toString() ?? '0')),
                        DataCell(
                          Icon(isActive ? Icons.check_circle : Icons.cancel, color: isActive ? Colors.green : Colors.red),
                        ),
                        DataCell(
                          Row(
                            children: [
                              IconButton(
                                icon: const Icon(Icons.edit, color: Colors.orange),
                                tooltip: 'تعديل',
                                onPressed: () => _showAddEditBannerDialog(context, ref, banner),
                              ),
                              IconButton(
                                icon: const Icon(Icons.delete, color: Colors.red),
                                tooltip: 'حذف',
                                onPressed: () async {
                                  final confirm = await showDialog<bool>(
                                    context: context,
                                    builder: (context) => AlertDialog(
                                      title: const Text('تأكيد الحذف'),
                                      content: const Text('هل أنت متأكد من حذف هذا الإعلان؟'),
                                      actions: [
                                        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('إلغاء')),
                                        TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('حذف', style: TextStyle(color: Colors.red))),
                                      ],
                                    ),
                                  );
                                  if (confirm == true) {
                                    await ref.read(bannerRepositoryProvider).deleteBanner(banner['id']);
                                    ref.invalidate(bannersProvider);
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

  void _showAddEditBannerDialog(BuildContext context, WidgetRef ref, Map<String, dynamic>? banner) {
    showDialog(
      context: context,
      builder: (context) => _AddEditBannerDialog(banner: banner, ref: ref),
    );
  }
}

class _AddEditBannerDialog extends StatefulWidget {
  final Map<String, dynamic>? banner;
  final WidgetRef ref;

  const _AddEditBannerDialog({this.banner, required this.ref});

  @override
  State<_AddEditBannerDialog> createState() => _AddEditBannerDialogState();
}

class _AddEditBannerDialogState extends State<_AddEditBannerDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _titleController;
  late TextEditingController _imageUrlController;
  late TextEditingController _sortController;
  bool _isActive = true;
  bool _isLoading = false;
  bool _isUploading = false;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.banner?['action_url'] ?? '');
    _imageUrlController = TextEditingController(text: widget.banner?['image_url'] ?? '');
    _sortController = TextEditingController(text: widget.banner?['sort_order']?.toString() ?? '0');
    _isActive = widget.banner?['is_active'] ?? true;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.banner == null ? 'إضافة إعلان جديد' : 'تعديل الإعلان'),
      content: SizedBox(
        width: 400,
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: _titleController,
                  decoration: const InputDecoration(labelText: 'العنوان أو الرابط (Action URL)', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _imageUrlController,
                        decoration: const InputDecoration(labelText: 'رابط الصورة (Image URL)', border: OutlineInputBorder()),
                        validator: (val) => val == null || val.isEmpty ? 'مطلوب' : null,
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
                  controller: _sortController,
                  decoration: const InputDecoration(labelText: 'الترتيب (Sort Order)', border: OutlineInputBorder()),
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
          'action_url': _titleController.text.isEmpty ? null : _titleController.text,
          'image_url': _imageUrlController.text,
          'sort_order': int.tryParse(_sortController.text) ?? 0,
          'is_active': _isActive,
        };

        if (widget.banner == null) {
          await widget.ref.read(bannerRepositoryProvider).createBanner(data);
        } else {
          await widget.ref.read(bannerRepositoryProvider).updateBanner(widget.banner!['id'], data);
        }

        widget.ref.invalidate(bannersProvider);
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
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.image,
        withData: true,
      );

      if (result != null && result.files.first.bytes != null) {
        setState(() => _isUploading = true);
        
        final fileBytes = result.files.first.bytes!;
        final fileName = result.files.first.name;
        final uniqueName = '${DateTime.now().millisecondsSinceEpoch}_$fileName';

        await Supabase.instance.client.storage.from('store_images').uploadBinary(
          uniqueName,
          fileBytes,
          fileOptions: const FileOptions(upsert: true),
        );

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
