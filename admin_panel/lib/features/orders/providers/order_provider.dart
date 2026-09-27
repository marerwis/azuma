import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/order_repository.dart';

final orderRepositoryProvider = Provider<OrderRepository>((ref) {
  return OrderRepository();
});

final ordersProvider = FutureProvider<List<Map<String, dynamic>>>((ref) async {
  final repository = ref.read(orderRepositoryProvider);
  return repository.getOrders();
});
