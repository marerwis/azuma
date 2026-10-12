import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/order_repository.dart';

final orderRepositoryProvider = Provider<OrderRepository>((ref) {
  return OrderRepository();
});

/// All orders — main list for the orders screen.
final ordersProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(orderRepositoryProvider).getOrders();
});

/// Order items (with addons) for a specific order — used in details dialog.
final orderItemsProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>(
        (ref, orderId) async {
  return ref.read(orderRepositoryProvider).getOrderItems(orderId);
});

/// Status history for a specific order — used in details dialog timeline.
final orderHistoryProvider =
    FutureProvider.family<List<Map<String, dynamic>>, String>(
        (ref, orderId) async {
  return ref.read(orderRepositoryProvider).getOrderHistory(orderId);
});
