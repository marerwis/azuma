import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/navigation_provider.dart';
import '../orders/orders_screen.dart';
import '../wallets/wallets_screen.dart';
import '../banners/banners_screen.dart';
import '../categories/categories_screen.dart';
import '../users/users_screen.dart';
import '../notifications/send_notification_screen.dart';
import '../reports/sales_reports_screen.dart';
import '../reviews/order_reviews_screen.dart';
import '../auth/providers/auth_provider.dart';
import 'overview_screen.dart';

class DashboardLayout extends ConsumerWidget {
  const DashboardLayout({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedIndex = ref.watch(selectedIndexProvider);

    return Scaffold(
      body: Row(
        children: [
          // ── Sidebar ────────────────────────────────────────────────
          Container(
            width: 250,
            color: Colors.white,
            child: Column(
              children: [
                // Logo
                Container(
                  padding: const EdgeInsets.symmetric(
                      vertical: 24, horizontal: 16),
                  decoration: BoxDecoration(
                    border: Border(
                        bottom: BorderSide(color: Colors.grey.shade200)),
                  ),
                  child: const Row(
                    children: [
                      Icon(Icons.admin_panel_settings,
                          color: Color(0xFFFF5722), size: 28),
                      SizedBox(width: 10),
                      Text(
                        'Azooma Admin',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFFFF5722),
                        ),
                      ),
                    ],
                  ),
                ),

                // Nav items
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    children: [
                      _SectionLabel('الرئيسية'),
                      _NavItem(ref, index: 0, icon: Icons.dashboard, title: 'لوحة البيانات'),

                      _SectionLabel('العمليات'),
                      _NavItem(ref, index: 1, icon: Icons.local_offer, title: 'العروض والبانرات'),
                      _NavItem(ref, index: 2, icon: Icons.category, title: 'الأقسام والمطاعم'),
                      _NavItem(ref, index: 3, icon: Icons.shopping_bag, title: 'الطلبات'),

                      _SectionLabel('المستخدمون'),
                      _NavItem(ref, index: 4, icon: Icons.account_balance_wallet, title: 'المحافظ'),
                      _NavItem(ref, index: 5, icon: Icons.people, title: 'المستخدمين'),

                      _SectionLabel('التسويق والتواصل'),
                      _NavItem(ref, index: 6, icon: Icons.notifications_active, title: 'الإشعارات'),

                      _SectionLabel('التحليلات'),
                      _NavItem(ref, index: 7, icon: Icons.bar_chart, title: 'التقارير والمبيعات'),
                      _NavItem(ref, index: 8, icon: Icons.star_rate, title: 'تقييمات العملاء'),
                    ],
                  ),
                ),

                // Logout button
                Container(
                  decoration: BoxDecoration(
                    border: Border(
                        top: BorderSide(color: Colors.grey.shade200))),
                  padding: const EdgeInsets.all(12),
                  child: ListTile(
                    leading: const Icon(Icons.logout, color: Colors.red),
                    title: const Text('تسجيل الخروج',
                        style: TextStyle(color: Colors.red)),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8)),
                    onTap: () => ref.read(authProvider.notifier).signOut(),
                  ),
                ),
              ],
            ),
          ),

          // ── Main Content ───────────────────────────────────────────
          Expanded(
            child: Container(
              color: const Color(0xFFF3F4F6),
              child: _buildPageContent(selectedIndex),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPageContent(int index) {
    switch (index) {
      case 0: return const OverviewScreen();
      case 1: return const BannersScreen();
      case 2: return const CategoriesScreen();
      case 3: return const OrdersScreen();
      case 4: return const WalletsScreen();
      case 5: return const UsersScreen();
      case 6: return const SendNotificationScreen();
      case 7: return const SalesReportsScreen();
      case 8: return const OrderReviewsScreen();
      default: return const OverviewScreen();
    }
  }
}

// ── Helper widgets ────────────────────────────────────────────────────────────

class _SectionLabel extends StatelessWidget {
  const _SectionLabel(this.label);
  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
      child: Text(
        label.toUpperCase(),
        style: const TextStyle(
          fontSize: 10,
          fontWeight: FontWeight.bold,
          color: Colors.grey,
          letterSpacing: 1.2,
        ),
      ),
    );
  }
}

class _NavItem extends ConsumerWidget {
  const _NavItem(this.ref,
      {required this.index,
      required this.icon,
      required this.title});

  final WidgetRef ref;
  final int index;
  final IconData icon;
  final String title;

  @override
  Widget build(BuildContext context, WidgetRef _) {
    final selectedIndex = ref.watch(selectedIndexProvider);
    final isSelected = selectedIndex == index;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      child: ListTile(
        leading: Icon(icon,
            color: isSelected ? const Color(0xFFFF5722) : Colors.grey[600],
            size: 20),
        title: Text(
          title,
          style: TextStyle(
            fontSize: 14,
            color: isSelected ? const Color(0xFFFF5722) : Colors.grey[700],
            fontWeight:
                isSelected ? FontWeight.bold : FontWeight.normal,
          ),
        ),
        selected: isSelected,
        selectedTileColor: const Color(0xFFFF5722).withOpacity(0.08),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        onTap: () => ref.read(selectedIndexProvider.notifier).setIndex(index),
      ),
    );
  }
}
