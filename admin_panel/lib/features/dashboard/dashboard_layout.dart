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
import '../delivery/delivery_boys_screen.dart';
import '../coupons/coupons_screen.dart';
import '../notifications/send_notification_screen.dart';
import '../reports/sales_reports_screen.dart';
import '../reviews/reviews_admin_screen.dart';
import '../settings/system_settings_screen.dart';
import '../auth/providers/auth_provider.dart';
import 'overview_screen.dart';

// Navigation index map:
// 0  → Overview
// 1  → Banners
// 2  → Categories & Stores
// 3  → Catalog (Products)
// 4  → Orders
// 5  → Customers
// 6  → Delivery Boys (drivers)
// 7  → Users (all/admins)
// 8  → Coupons
// 9  → Notifications
// 10 → Wallets
// 11 → Reports
// 12 → Reviews
// 13 → Settings

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
          Expanded(child: _buildPage(selectedIndex)),
        ],
      ),
    );
  }

  Widget _buildPage(int index) {
    switch (index) {
      case 0:  return const OverviewScreen();
      case 1:  return const BannersScreen();
      case 2:  return const CategoriesScreen();
      case 3:  return const CatalogScreen();
      case 4:  return const OrdersScreen();
      case 5:  return const CustomersScreen();
      case 6:  return const DeliveryBoysScreen();
      case 7:  return const UsersScreen();
      case 8:  return const CouponsScreen();
      case 9:  return const SendNotificationScreen();
      case 10: return const WalletsScreen();
      case 11: return const SalesReportsScreen();
      case 12: return const ReviewsAdminScreen();
      case 13: return const SystemSettingsScreen();
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
          // ── Brand ──────────────────────────────────────────────────────
          Container(
            padding:
                const EdgeInsets.symmetric(vertical: 20, horizontal: 18),
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
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('عزومة',
                        style: TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.bold,
                            color: AzColor.textPrimary)),
                    Text('لوحة التحكم',
                        style: TextStyle(
                            fontSize: 10,
                            color: AzColor.textMuted)),
                  ],
                ),
              ],
            ),
          ),

          // ── Nav ────────────────────────────────────────────────────────
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              children: [
                _S('الرئيسية'),
                _N(ref, i: 0,  icon: Icons.dashboard_outlined,           active: Icons.dashboard,                title: 'لوحة القيادة'),

                _S('الكتالوج'),
                _N(ref, i: 1,  icon: Icons.image_outlined,               active: Icons.image,                    title: 'البنرات الإعلانية'),
                _N(ref, i: 2,  icon: Icons.category_outlined,            active: Icons.category,                 title: 'الأقسام والمطاعم'),
                _N(ref, i: 3,  icon: Icons.restaurant_menu_outlined,     active: Icons.restaurant_menu,          title: 'المنتجات'),

                _S('العمليات'),
                _N(ref, i: 4,  icon: Icons.receipt_long_outlined,        active: Icons.receipt_long,             title: 'الطلبات'),

                _S('المستخدمون'),
                _N(ref, i: 5,  icon: Icons.people_outline,               active: Icons.people,                   title: 'العملاء'),
                _N(ref, i: 6,  icon: Icons.delivery_dining_outlined,     active: Icons.delivery_dining,          title: 'سائقو التوصيل'),
                _N(ref, i: 7,  icon: Icons.manage_accounts_outlined,     active: Icons.manage_accounts,          title: 'إدارة المستخدمين'),

                _S('التسويق'),
                _N(ref, i: 8,  icon: Icons.local_offer_outlined,         active: Icons.local_offer,              title: 'كودات الخصم'),
                _N(ref, i: 9,  icon: Icons.notifications_outlined,       active: Icons.notifications_active,     title: 'الإشعارات'),

                _S('التحليلات'),
                _N(ref, i: 10, icon: Icons.account_balance_wallet_outlined, active: Icons.account_balance_wallet, title: 'المحافظ'),
                _N(ref, i: 11, icon: Icons.bar_chart_outlined,           active: Icons.bar_chart,                title: 'التقارير'),
                _N(ref, i: 12, icon: Icons.star_outline,                 active: Icons.star,                     title: 'التقييمات'),

                _S('النظام'),
                _N(ref, i: 13, icon: Icons.settings_outlined,            active: Icons.settings,                 title: 'إعدادات النظام'),
              ],
            ),
          ),

          // ── Logout ─────────────────────────────────────────────────────
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
                  style: TextStyle(color: AzColor.danger, fontSize: 13)),
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

// ── Sidebar Helpers ───────────────────────────────────────────────────────────

class _S extends StatelessWidget {
  const _S(this.label);
  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(18, 16, 18, 4),
      child: Text(label.toUpperCase(),
          style: const TextStyle(
              fontSize: 9,
              fontWeight: FontWeight.bold,
              color: AzColor.textMuted,
              letterSpacing: 1.5)),
    );
  }
}

class _N extends ConsumerWidget {
  const _N(this.ref, {
    required this.i,
    required this.icon,
    required this.active,
    required this.title,
  });
  final WidgetRef ref;
  final int i;
  final IconData icon;
  final IconData active;
  final String title;

  @override
  Widget build(BuildContext context, WidgetRef _) {
    final sel = ref.watch(selectedIndexProvider) == i;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 1),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        decoration: BoxDecoration(
          color: sel
              ? AzColor.primary.withValues(alpha: 0.15)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(8),
        ),
        child: ListTile(
          dense: true,
          leading: Icon(sel ? active : icon,
              color: sel ? AzColor.primary : AzColor.textSecondary,
              size: 19),
          title: Text(title,
              style: TextStyle(
                  fontSize: 13,
                  color: sel ? AzColor.primary : AzColor.textSecondary,
                  fontWeight:
                      sel ? FontWeight.bold : FontWeight.normal)),
          trailing: sel
              ? Container(
                  width: 3,
                  height: 26,
                  decoration: BoxDecoration(
                      color: AzColor.primary,
                      borderRadius: BorderRadius.circular(3)))
              : null,
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8)),
          onTap: () =>
              ref.read(selectedIndexProvider.notifier).setIndex(i),
        ),
      ),
    );
  }
}
