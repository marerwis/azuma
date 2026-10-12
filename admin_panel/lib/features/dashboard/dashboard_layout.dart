import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/admin_theme.dart';
import 'providers/navigation_provider.dart';
import '../catalog/catalog_screen.dart';
import '../orders/orders_screen.dart';
import '../wallets/wallets_screen.dart';
import '../banners/banners_screen.dart';
import '../categories/categories_screen.dart';
import '../users/users_screen.dart';
import '../customers/customers_screen.dart';
import '../coupons/coupons_screen.dart';
import '../notifications/send_notification_screen.dart';
import '../reports/sales_reports_screen.dart';
import '../reviews/order_reviews_screen.dart';
import '../auth/providers/auth_provider.dart';
import 'overview_screen.dart';

// Navigation index:
// 0  → Overview
// 1  → Banners
// 2  → Categories & Stores
// 3  → Catalog (Products)
// 4  → Orders
// 5  → Customers
// 6  → Users (Delivery Boys / All)
// 7  → Coupons
// 8  → Wallets
// 9  → Notifications
// 10 → Reports
// 11 → Reviews

class DashboardLayout extends ConsumerWidget {
  const DashboardLayout({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedIndex = ref.watch(selectedIndexProvider);

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: Row(
        children: [
          _DarkSidebar(selectedIndex: selectedIndex, ref: ref),
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
      case 5:  return const CustomersScreen();
      case 6:  return const UsersScreen();
      case 7:  return const CouponsScreen();
      case 8:  return const WalletsScreen();
      case 9:  return const SendNotificationScreen();
      case 10: return const SalesReportsScreen();
      case 11: return const OrderReviewsScreen();
      default: return const OverviewScreen();
    }
  }
}

// ── Dark Sidebar ──────────────────────────────────────────────────────────────

class _DarkSidebar extends ConsumerWidget {
  const _DarkSidebar({required this.selectedIndex, required this.ref});
  final int selectedIndex;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context, WidgetRef _) {
    return Container(
      width: 256,
      color: AzColor.bgSurface,
      child: Column(
        children: [
          // ── Brand ──────────────────────────────────────────────────────────
          Container(
            padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 18),
            decoration: const BoxDecoration(
              border: Border(
                  bottom: BorderSide(color: AzColor.bgBorder, width: 1)),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [AzColor.primary, AzColor.secondary],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(Icons.electric_bolt,
                      color: Colors.white, size: 20),
                ),
                const SizedBox(width: 10),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('عزومة',
                        style: TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.bold,
                            color: AzColor.textPrimary,
                            fontFamily: 'Cairo')),
                    const Text('لوحة التحكم',
                        style: TextStyle(
                            fontSize: 10,
                            color: AzColor.textMuted)),
                  ],
                ),
              ],
            ),
          ),

          // ── Nav Items ─────────────────────────────────────────────────────
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              children: [
                _NavSection('الرئيسية'),
                _NavItem(ref, index: 0, icon: Icons.dashboard_outlined, activeIcon: Icons.dashboard, title: 'لوحة القيادة'),

                _NavSection('الكتالوج'),
                _NavItem(ref, index: 1, icon: Icons.image_outlined, activeIcon: Icons.image, title: 'البنرات الإعلانية'),
                _NavItem(ref, index: 2, icon: Icons.category_outlined, activeIcon: Icons.category, title: 'الأقسام والمطاعم'),
                _NavItem(ref, index: 3, icon: Icons.restaurant_menu_outlined, activeIcon: Icons.restaurant_menu, title: 'المنتجات'),

                _NavSection('العمليات'),
                _NavItem(ref, index: 4, icon: Icons.receipt_long_outlined, activeIcon: Icons.receipt_long, title: 'الطلبات'),

                _NavSection('المستخدمون'),
                _NavItem(ref, index: 5, icon: Icons.people_outline, activeIcon: Icons.people, title: 'العملاء'),
                _NavItem(ref, index: 6, icon: Icons.delivery_dining_outlined, activeIcon: Icons.delivery_dining, title: 'سائقي التوصيل'),

                _NavSection('التسويق'),
                _NavItem(ref, index: 7, icon: Icons.local_offer_outlined, activeIcon: Icons.local_offer, title: 'كودات الخصم'),
                _NavItem(ref, index: 9, icon: Icons.notifications_outlined, activeIcon: Icons.notifications_active, title: 'الإشعارات'),

                _NavSection('التحليلات'),
                _NavItem(ref, index: 8, icon: Icons.account_balance_wallet_outlined, activeIcon: Icons.account_balance_wallet, title: 'المحافظ'),
                _NavItem(ref, index: 10, icon: Icons.bar_chart_outlined, activeIcon: Icons.bar_chart, title: 'التقارير'),
                _NavItem(ref, index: 11, icon: Icons.star_outline, activeIcon: Icons.star, title: 'التقييمات'),
              ],
            ),
          ),

          // ── Logout ─────────────────────────────────────────────────────────
          Container(
            decoration: const BoxDecoration(
                border: Border(
                    top: BorderSide(color: AzColor.bgBorder, width: 1))),
            padding: const EdgeInsets.all(10),
            child: ListTile(
              dense: true,
              leading: const Icon(Icons.logout_outlined,
                  color: AzColor.danger, size: 20),
              title: const Text('تسجيل الخروج',
                  style: TextStyle(
                      color: AzColor.danger, fontSize: 13)),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8)),
              hoverColor: AzColor.danger.withValues(alpha: 0.08),
              onTap: () => ref.read(authProvider.notifier).signOut(),
            ),
          ),
        ],
      ),
    );
  }
}

class _NavSection extends StatelessWidget {
  const _NavSection(this.label);
  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 4),
      child: Text(
        label.toUpperCase(),
        style: const TextStyle(
          fontSize: 9,
          fontWeight: FontWeight.bold,
          color: AzColor.textMuted,
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
    final selected = ref.watch(selectedIndexProvider) == index;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 1),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        decoration: BoxDecoration(
          color: selected
              ? AzColor.primary.withValues(alpha: 0.15)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(8),
        ),
        child: ListTile(
          dense: true,
          leading: Icon(
            selected ? activeIcon : icon,
            color: selected ? AzColor.primary : AzColor.textSecondary,
            size: 19,
          ),
          title: Text(
            title,
            style: TextStyle(
              fontSize: 13,
              color: selected ? AzColor.primary : AzColor.textSecondary,
              fontWeight:
                  selected ? FontWeight.bold : FontWeight.normal,
            ),
          ),
          trailing: selected
              ? Container(
                  width: 3,
                  height: 26,
                  decoration: BoxDecoration(
                    color: AzColor.primary,
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
