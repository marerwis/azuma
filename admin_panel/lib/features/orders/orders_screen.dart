import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'providers/order_provider.dart';

class OrdersScreen extends ConsumerWidget {
  const OrdersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ordersAsync = ref.watch(ordersProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('الطلبات - Orders'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(ordersProvider),
            tooltip: 'تحديث',
          ),
        ],
      ),
      body: ordersAsync.when(
        data: (orders) {
          if (orders.isEmpty) return const Center(child: Text('لا توجد طلبات'));
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: Card(
                elevation: 2,
                child: DataTable(
                  headingRowColor: MaterialStateProperty.all(Colors.grey[200]),
                  columns: const [
                    DataColumn(label: Text('رقم الطلب')),
                    DataColumn(label: Text('التاريخ')),
                    DataColumn(label: Text('العميل')),
                    DataColumn(label: Text('المطعم')),
                    DataColumn(label: Text('الإجمالي')),
                    DataColumn(label: Text('الحالة')),
                    DataColumn(label: Text('تحديث الحالة')),
                  ],
                  rows: orders.map((order) {
                    final customer = order['customer'];
                    final store = order['store'];
                    final status = order['status'] ?? 'pending';
                    final date = DateTime.tryParse(order['created_at'] ?? '');
                    
                    return DataRow(
                      cells: [
                        DataCell(Text(order['id'].toString().substring(0, 8).toUpperCase())),
                        DataCell(Text(date != null ? DateFormat('yyyy-MM-dd HH:mm').format(date) : '--')),
                        DataCell(Text(customer?['full_name'] ?? 'مجهول')),
                        DataCell(Text(store?['name'] ?? 'مجهول')),
                        DataCell(Text('${order['total_amount']} د.ل')),
                        DataCell(_buildStatusBadge(status)),
                        DataCell(_buildStatusDropdown(context, ref, order['id'], status)),
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

  Widget _buildStatusBadge(String status) {
    Color bgColor;
    String text;

    switch (status) {
      case 'pending':
        bgColor = Colors.orange;
        text = 'قيد الانتظار';
        break;
      case 'accepted_by_vendor':
        bgColor = Colors.blue;
        text = 'مقبول من المطعم';
        break;
      case 'preparing':
        bgColor = Colors.blue;
        text = 'قيد التجهيز';
        break;
      case 'ready_for_pickup':
        bgColor = Colors.teal;
        text = 'جاهز للاستلام';
        break;
      case 'delivered':
        bgColor = Colors.green;
        text = 'تم التوصيل';
        break;
      case 'rejected_by_vendor':
      case 'cancelled':
        bgColor = Colors.red;
        text = status == 'cancelled' ? 'ملغي' : 'مرفوض من المطعم';
        break;
      default:
        bgColor = Colors.grey;
        text = status;
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

  Widget _buildStatusDropdown(BuildContext context, WidgetRef ref, String orderId, String currentStatus) {
    final statuses = [
      'pending',
      'accepted_by_vendor',
      'rejected_by_vendor',
      'preparing',
      'ready_for_pickup',
      'delivered',
      'cancelled'
    ];

    // Map technical status to readable Arabic
    String _getReadableStatus(String st) {
      switch (st) {
        case 'pending': return 'قيد الانتظار';
        case 'accepted_by_vendor': return 'مقبول';
        case 'preparing': return 'تجهيز';
        case 'ready_for_pickup': return 'جاهز';
        case 'delivered': return 'مكتمل';
        case 'rejected_by_vendor': return 'مرفوض';
        case 'cancelled': return 'ملغي';
        default: return st;
      }
    }

    return DropdownButtonHideUnderline(
      child: DropdownButton<String>(
        value: statuses.contains(currentStatus) ? currentStatus : null,
        hint: const Text('تغيير'),
        icon: const Icon(Icons.arrow_drop_down),
        onChanged: (newStatus) async {
          if (newStatus != null && newStatus != currentStatus) {
            try {
              await ref.read(orderRepositoryProvider).updateOrderStatus(orderId, newStatus);
              ref.invalidate(ordersProvider); // Refresh optimistically
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('تم تحديث حالة الطلب بنجاح')),
                );
              }
            } catch (e) {
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('خطأ في التحديث: $e')),
                );
              }
            }
          }
        },
        items: statuses.map((st) {
          return DropdownMenuItem<String>(
            value: st,
            child: Text(_getReadableStatus(st), style: const TextStyle(fontSize: 13)),
          );
        }).toList(),
      ),
    );
  }
}
