import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/wallet_provider.dart';

class WalletsScreen extends ConsumerWidget {
  const WalletsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final walletsAsync = ref.watch(walletsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('المحافظ المالية - Wallets'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(walletsProvider),
            tooltip: 'تحديث',
          ),
        ],
      ),
      body: walletsAsync.when(
        data: (wallets) {
          if (wallets.isEmpty) return const Center(child: Text('لا توجد محافظ نشطة'));
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: MaterialStateProperty.all(Colors.grey[200]),
                  columns: const [
                    DataColumn(label: Text('المستخدم')),
                    DataColumn(label: Text('معلومات التواصل')),
                    DataColumn(label: Text('الدور')),
                    DataColumn(label: Text('الرصيد الحالي')),
                    DataColumn(label: Text('الإجراءات')),
                  ],
                  rows: wallets.map((wallet) {
                    final user = wallet['user'];
                    final double balance = double.tryParse(wallet['balance'].toString()) ?? 0.0;

                    return DataRow(
                      cells: [
                        DataCell(Text(user?['full_name'] ?? 'مجهول', style: const TextStyle(fontWeight: FontWeight.bold))),
                        DataCell(
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              if (user?['phone'] != null) Text(user!['phone'], style: const TextStyle(fontSize: 12)),
                              if (user?['email'] != null) Text(user!['email'], style: const TextStyle(fontSize: 12, color: Colors.grey)),
                            ],
                          ),
                        ),
                        DataCell(Text(_translateRole(user?['role'] ?? 'customer'))),
                        DataCell(
                          Text(
                            '${balance.toStringAsFixed(2)} د.ل',
                            style: TextStyle(
                              color: balance < 0 ? Colors.red : (balance > 0 ? Colors.green : Colors.black),
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        DataCell(
                          ElevatedButton.icon(
                            onPressed: () => _showManageBalanceDialog(context, ref, wallet['id'], balance, user?['full_name'] ?? 'مجهول'),
                            icon: const Icon(Icons.account_balance_wallet, size: 16),
                            label: const Text('إدارة الرصيد'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFFFF5722),
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

  String _translateRole(String role) {
    switch (role) {
      case 'customer': return 'عميل';
      case 'vendor': return 'مطعم';
      case 'driver': return 'مندوب';
      case 'admin': return 'مدير';
      default: return role;
    }
  }

  void _showManageBalanceDialog(BuildContext context, WidgetRef ref, String walletId, double currentBalance, String userName) {
    showDialog(
      context: context,
      builder: (context) => _ManageBalanceDialog(
        walletId: walletId,
        currentBalance: currentBalance,
        userName: userName,
        ref: ref,
      ),
    );
  }
}

class _ManageBalanceDialog extends StatefulWidget {
  final String walletId;
  final double currentBalance;
  final String userName;
  final WidgetRef ref;

  const _ManageBalanceDialog({
    required this.walletId,
    required this.currentBalance,
    required this.userName,
    required this.ref,
  });

  @override
  State<_ManageBalanceDialog> createState() => _ManageBalanceDialogState();
}

class _ManageBalanceDialogState extends State<_ManageBalanceDialog> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _noteController = TextEditingController();
  bool _isAddition = true; // true = Add Funds, false = Deduct
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('إدارة رصيد: ${widget.userName}'),
      content: SizedBox(
        width: 400,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('الرصيد الحالي: ${widget.currentBalance.toStringAsFixed(2)} د.ل', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: RadioListTile<bool>(
                      title: const Text('إيداع (شحن)'),
                      value: true,
                      groupValue: _isAddition,
                      activeColor: Colors.green,
                      onChanged: (val) => setState(() => _isAddition = val!),
                    ),
                  ),
                  Expanded(
                    child: RadioListTile<bool>(
                      title: const Text('خصم'),
                      value: false,
                      groupValue: _isAddition,
                      activeColor: Colors.red,
                      onChanged: (val) => setState(() => _isAddition = val!),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _amountController,
                decoration: const InputDecoration(labelText: 'المبلغ (د.ل)', border: OutlineInputBorder()),
                keyboardType: TextInputType.number,
                validator: (val) {
                  if (val == null || val.isEmpty) return 'يرجى إدخال المبلغ';
                  if (double.tryParse(val) == null || double.parse(val) <= 0) return 'مبلغ غير صالح';
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _noteController,
                decoration: const InputDecoration(labelText: 'ملاحظة / السبب (اختياري)', border: OutlineInputBorder()),
                maxLines: 2,
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('إلغاء')),
        ElevatedButton(
          style: ElevatedButton.styleFrom(backgroundColor: _isAddition ? Colors.green : Colors.red),
          onPressed: _isLoading ? null : _submit,
          child: _isLoading 
            ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(color: Colors.white))
            : Text(_isAddition ? 'إضافة الرصيد' : 'خصم الرصيد', style: const TextStyle(color: Colors.white)),
        ),
      ],
    );
  }

  Future<void> _submit() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        final amount = double.parse(_amountController.text);
        final desc = _noteController.text.trim().isEmpty 
            ? (_isAddition ? 'شحن إداري' : 'خصم إداري') 
            : _noteController.text.trim();

        await widget.ref.read(walletRepositoryProvider).adjustBalance(
          walletId: widget.walletId,
          currentBalance: widget.currentBalance,
          amount: amount,
          isAddition: _isAddition,
          description: desc,
        );

        widget.ref.invalidate(walletsProvider);
        if (mounted) {
          Navigator.pop(context);
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('تم تحديث الرصيد بنجاح!'), backgroundColor: Colors.green),
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
}
