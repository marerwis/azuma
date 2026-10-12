import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/navigation_provider.dart';
import '../catalog/catalog_screen.dart';
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

// Navigation index map:
// 0  → Overview (Dashboard)
// 1  → Banners
// 2  → Categories & Stores
// 3  → Catalog (Products)    ← NEW
// 4  → Orders
// 5  → Wallets
// 6  → Users
// 7  → Notifications
// 8  → Reports
// 9  → Reviews

class DashboardLayout extends ConsumerWidget {
  const DashboardLayout({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedIndex = ref.watch(selectedIndexProvider);

    return Scaffold(
      body: Row(
        children: [
          // ── Fixed Left Sidebar ─────────────────────────────────────────
          _Sidebar(selectedIndex: selectedIndex, ref: ref),

          // ── Main Content Area (fills remaining width) ──────────────────
          Expanded(
            child: _buildPageContent(selectedIndex),
          ),
        ],
      ),
    );
  }

  Widget _buildPageContent(int index) {
    switch (index) {
      case 0:  return const OverviewScreen();
      case 1:  return const BannersScreen();
      case 2:  return const CategoriesScreen();
      case 3:  return const CatalogScreen();
      case 4:  return const OrdersScreen();
      case 5:  return const WalletsScreen();
      case 6:  return const UsersScreen();
      case 7:  return const SendNotificationScreen();
      case 8:  return const SalesReportsScreen();
      case 9:  return const OrderReviewsScreen();
      default: return const OverviewScreen();
    }
  }
}

// ── Fixed Sidebar ─────────────────────────────────────────────────────────────

class _Sidebar extends ConsumerWidget {
  const _Sidebar({required this.selectedIndex, required this.ref});
  final int selectedIndex;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context, WidgetRef _) {
    return Container(
      width: 252,
      color: Colors.white,
      child: Column(
        children: [
          // ── Logo / Brand ────────────────────────────────────────────────
          Container(
            padding: const EdgeInsets.symmetric(vertical: 22, horizontal: 16),
            decoration: BoxDecoration(
              border: Border(bottom: BorderSide(color: Colors.grey.shade200)),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFF5722),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(Icons.admin_panel_settings,
                      color: Colors.white, size: 22),
                ),
                const SizedBox(width: 10),
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Azooma',
                        style: TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.bold,
                            color: Color(0xFFFF5722))),
                    Text('Admin Panel',
                        style: TextStyle(fontSize: 10, color: Colors.grey)),
                  ],
                ),
              ],
            ),
          ),

          // ── Nav Items ───────────────────────────────────────────────────
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              children: [
                _SectionLabel('الرئيسية'),
                _NavItem(ref,
                    index: 0,
                    icon: Icons.dashboard_outlined,
                    activeIcon: Icons.dashboard,
                    title: 'لوحة البيانات'),

                _SectionLabel('الكتالوج'),
                _NavItem(ref,
                    index: 1,
                    icon: Icons.image_outlined,
                    activeIcon: Icons.image,
                    title: 'العروض والبانرات'),
                _NavItem(ref,
                    index: 2,
                    icon: Icons.category_outlined,
                    activeIcon: Icons.category,
                    title: 'الأقسام والمطاعم'),
                _NavItem(ref,
                    index: 3,
                    icon: Icons.restaurant_menu_outlined,
                    activeIcon: Icons.restaurant_menu,
                    title: 'المنتجات'),

                _SectionLabel('العمليات'),
                _NavItem(ref,
                    index: 4,
                    icon: Icons.receipt_long_outlined,
                    activeIcon: Icons.receipt_long,
                    title: 'الطلبات'),

                _SectionLabel('المستخدمون'),
                _NavItem(ref,
                    index: 5,
                    icon: Icons.account_balance_wallet_outlined,
                    activeIcon: Icons.account_balance_wallet,
                    title: 'المحافظ'),
                _NavItem(ref,
                    index: 6,
                    icon: Icons.people_outline,
                    activeIcon: Icons.people,
                    title: 'المستخدمين'),

                _SectionLabel('التسويق'),
                _NavItem(ref,
                    index: 7,
                    icon: Icons.notifications_outlined,
                    activeIcon: Icons.notifications_active,
                    title: 'الإشعارات'),

                _SectionLabel('التحليلات'),
                _NavItem(ref,
                    index: 8,
                    icon: Icons.bar_chart_outlined,
                    activeIcon: Icons.bar_chart,
                    title: 'التقارير والمبيعات'),
                _NavItem(ref,
                    index: 9,
                    icon: Icons.star_outline,
                    activeIcon: Icons.star_rate,
                    title: 'تقييمات العملاء'),
              ],
            ),
          ),

          // ── Logout ──────────────────────────────────────────────────────
          Container(
            decoration: BoxDecoration(
                border: Border(top: BorderSide(color: Colors.grey.shade200))),
            padding: const EdgeInsets.all(10),
            child: ListTile(
              leading: const Icon(Icons.logout_outlined, color: Colors.red),
              title: const Text('تسجيل الخروج',
                  style: TextStyle(color: Colors.red, fontSize: 14)),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8)),
              hoverColor: Colors.red.shade50,
              onTap: () => ref.read(authProvider.notifier).signOut(),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Sidebar Helper Widgets ────────────────────────────────────────────────────

class _SectionLabel extends StatelessWidget {
  const _SectionLabel(this.label);
  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 18, 16, 4),
      child: Text(
        label.toUpperCase(),
        style: const TextStyle(
          fontSize: 9,
          fontWeight: FontWeight.bold,
          color: Colors.grey,
          letterSpacing: 1.5,
        ),
      ),
    );
  }
}

class _NavItem extends ConsumerWidget {
  const _NavItem(this.ref, {
    required this.index,
    required this.icon,
    required this.activeIcon,
    required this.title,
  });

  final WidgetRef ref;
  final int index;
  final IconData icon;
  final IconData activeIcon;
  final String title;

  @override
  Widget build(BuildContext context, WidgetRef _) {
    final selectedIndex = ref.watch(selectedIndexProvider);
    final isSelected = selectedIndex == index;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 1),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        decoration: BoxDecoration(
          color: isSelected
              ? const Color(0xFFFF5722).withValues(alpha: 0.09)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(8),
        ),
        child: ListTile(
          dense: true,
          leading: Icon(
            isSelected ? activeIcon : icon,
            color: isSelected ? const Color(0xFFFF5722) : Colors.grey[600],
            size: 20,
          ),
          title: Text(
            title,
            style: TextStyle(
              fontSize: 13,
              color:
                  isSelected ? const Color(0xFFFF5722) : Colors.grey[700],
              fontWeight:
                  isSelected ? FontWeight.bold : FontWeight.normal,
            ),
          ),
          // Active indicator bar on left edge
          trailing: isSelected
              ? Container(
                  width: 3,
                  height: 28,
                  decoration: BoxDecoration(
                    color: const Color(0xFFFF5722),
                    borderRadius: BorderRadius.circular(3),
                  ),
                )
              : null,
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8)),
          onTap: () =>
              ref.read(selectedIndexProvider.notifier).setIndex(index),
        ),
      ),
    );
  }
}
