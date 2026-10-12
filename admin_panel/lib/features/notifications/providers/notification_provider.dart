import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/notification_repository.dart';

final notificationRepositoryProvider = Provider<NotificationRepository>((ref) {
  return NotificationRepository();
});

/// History of all sent notifications.
final notificationsProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(notificationRepositoryProvider).getNotifications();
});

/// Customers list for the target selector.
final notifCustomersProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(notificationRepositoryProvider).getCustomers();
});

/// Drivers list for the target selector.
final notifDriversProvider =
    FutureProvider<List<Map<String, dynamic>>>((ref) async {
  return ref.read(notificationRepositoryProvider).getDrivers();
});

/// Tracks whether the send operation is in progress.
final notifSendingProvider = StateProvider<bool>((ref) => false);
