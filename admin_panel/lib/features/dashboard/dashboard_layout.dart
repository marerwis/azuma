import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/navigation_provider.dart';
import '../stores/stores_screen.dart';
import '../orders/orders_screen.dart';

class DashboardLayout extends ConsumerWidget {
  const DashboardLayout({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedIndex = ref.watch(selectedIndexProvider);

    return Scaffold(
      body: Row(
        children: [
          // Sidebar
          Container(
            width: 250,
            color: Colors.white,
            child: Column(
              children: [
                const SizedBox(height: 32),
                const Text(
                  'Azooma Admin',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFFFF5722),
                  ),
                ),
                const SizedBox(height: 32),
                _buildNavItem(ref, index: 0, icon: Icons.dashboard, title: 'الرئيسية'),
                _buildNavItem(ref, index: 1, icon: Icons.local_offer, title: 'العروض'),
                _buildNavItem(ref, index: 2, icon: Icons.storefront, title: 'المطاعم'),
                _buildNavItem(ref, index: 3, icon: Icons.category, title: 'الأقسام والمنتجات'),
                _buildNavItem(ref, index: 4, icon: Icons.shopping_bag, title: 'الطلبات'),
                _buildNavItem(ref, index: 5, icon: Icons.account_balance_wallet, title: 'المحافظ'),
                _buildNavItem(ref, index: 6, icon: Icons.people, title: 'المستخدمين'),
              ],
            ),
          ),
          // Main Content Area
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

  Widget _buildNavItem(WidgetRef ref, {required int index, required IconData icon, required String title}) {
    final selectedIndex = ref.watch(selectedIndexProvider);
    final isSelected = selectedIndex == index;

    return ListTile(
      leading: Icon(icon, color: isSelected ? const Color(0xFFFF5722) : Colors.grey),
      title: Text(
        title,
        style: TextStyle(
          color: isSelected ? const Color(0xFFFF5722) : Colors.grey,
          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        ),
      ),
      selected: isSelected,
      onTap: () {
        ref.read(selectedIndexProvider.notifier).setIndex(index);
      },
    );
  }

  Widget _buildPageContent(int index) {
    switch (index) {
      case 0:
        return const Center(child: Text('الرئيسية - Overview', style: TextStyle(fontSize: 24)));
      case 1:
        return const Center(child: Text('إدارة العروض - Banners & Offers (CRUD)', style: TextStyle(fontSize: 24)));
      case 2:
        return const StoresScreen();
      case 3:
        return const Center(child: Text('الأقسام والمنتجات - Categories & Products (CRUD)', style: TextStyle(fontSize: 24)));
      case 4:
        return const OrdersScreen();
      case 5:
        return const Center(child: Text('المحافظ - Wallets & Transactions (Read)', style: TextStyle(fontSize: 24)));
      case 6:
        return const Center(child: Text('المستخدمين - Users (CRUD)', style: TextStyle(fontSize: 24)));
      default:
        return const Center(child: Text('الرئيسية - Overview', style: TextStyle(fontSize: 24)));
    }
  }
}
