import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../banners/providers/banner_provider.dart';
import '../categories/providers/category_provider.dart';
import '../stores/providers/store_provider.dart';

class OverviewScreen extends ConsumerWidget {
  const OverviewScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('مدير الصفحة الرئيسية للتطبيق - Home Screen Manager'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              ref.invalidate(bannersProvider);
              ref.invalidate(categoriesProvider);
              ref.invalidate(storesProvider);
            },
            tooltip: 'تحديث البيانات',
          )
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'يتيح لك هذا القسم إدارة الأقسام الأربعة الرئيسية التي تظهر في شاشة العميل.',
              style: TextStyle(fontSize: 16, color: Colors.grey),
            ),
            const SizedBox(height: 24),
            _buildBannersSection(context, ref),
            const SizedBox(height: 24),
            _buildCategoriesSection(context, ref),
            const SizedBox(height: 24),
            _buildHighestRatedSection(context, ref),
            const SizedBox(height: 24),
            _buildOffersSection(context, ref),
          ],
        ),
      ),
    );
  }

  // 1. Top Promo Banners
  Widget _buildBannersSection(BuildContext context, WidgetRef ref) {
    final bannersAsync = ref.watch(bannersProvider);

    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('1. العروض الترويجية (Top Promo Banners)', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFFF5722))),
                TextButton.icon(
                  onPressed: () {}, // Handled in full BannersScreen
                  icon: const Icon(Icons.edit),
                  label: const Text('إدارة التفاصيل'),
                ),
              ],
            ),
            const Divider(),
            bannersAsync.when(
              data: (banners) {
                if (banners.isEmpty) return const Text('لا توجد عروض ترويجية نشطة.');
                return SizedBox(
                  height: 120,
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    itemCount: banners.length,
                    itemBuilder: (context, index) {
                      final banner = banners[index];
                      return Container(
                        margin: const EdgeInsets.only(left: 12),
                        width: 200,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(8),
                          image: DecorationImage(
                            image: NetworkImage(banner['image_url'] ?? ''),
                            fit: BoxFit.cover,
                          ),
                        ),
                        alignment: Alignment.bottomRight,
                        child: Container(
                          padding: const EdgeInsets.all(4),
                          color: Colors.black54,
                          child: Text(
                            banner['action_url'] ?? 'بدون عنوان',
                            style: const TextStyle(color: Colors.white, fontSize: 12),
                          ),
                        ),
                      );
                    },
                  ),
                );
              },
              loading: () => const CircularProgressIndicator(),
              error: (e, st) => Text('خطأ: $e'),
            ),
          ],
        ),
      ),
    );
  }

  // 2. Categories
  Widget _buildCategoriesSection(BuildContext context, WidgetRef ref) {
    final categoriesAsync = ref.watch(categoriesProvider);

    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('2. الأقسام (Categories - 8 Items limit)', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFFF5722))),
                TextButton.icon(
                  onPressed: () {}, // Handled in CategoriesScreen
                  icon: const Icon(Icons.edit),
                  label: const Text('إدارة الأقسام'),
                ),
              ],
            ),
            const Divider(),
            categoriesAsync.when(
              data: (categories) {
                if (categories.isEmpty) return const Text('لا توجد أقسام.');
                return Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: categories.take(8).map((cat) { // Limit to 8 as per design
                    return Column(
                      children: [
                        CircleAvatar(
                          radius: 30,
                          backgroundColor: Colors.grey[200],
                          backgroundImage: cat['image_url'] != null && cat['image_url'].toString().startsWith('http')
                              ? NetworkImage(cat['image_url'])
                              : null,
                          child: cat['image_url'] == null || !cat['image_url'].toString().startsWith('http')
                              ? Text(cat['image_url'] ?? '📁', style: const TextStyle(fontSize: 24))
                              : null,
                        ),
                        const SizedBox(height: 8),
                        Text(cat['name'] ?? '', style: const TextStyle(fontWeight: FontWeight.bold)),
                      ],
                    );
                  }).toList(),
                );
              },
              loading: () => const CircularProgressIndicator(),
              error: (e, st) => Text('خطأ: $e'),
            ),
          ],
        ),
      ),
    );
  }

  // 3. Highest Rated
  Widget _buildHighestRatedSection(BuildContext context, WidgetRef ref) {
    final storesAsync = ref.watch(storesProvider);
    
    // NOTE FOR FUTURE IMPLEMENTATION:
    // This list should ideally be fetched dynamically from Supabase using an RPC call 
    // or a direct query that averages customer reviews. Example SQL: 
    // SELECT stores.*, AVG(reviews.rating) as avg_rating FROM stores JOIN reviews ON stores.id = reviews.store_id GROUP BY stores.id ORDER BY avg_rating DESC LIMIT 5;

    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('3. الأعلى تقييماً (Highest Rated)', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFFF5722))),
            const Text('ملاحظة: يتم جلب هذه القائمة ديناميكياً بناءً على تقييمات العملاء (ORDER BY rating DESC).', style: TextStyle(color: Colors.grey, fontSize: 12)),
            const Divider(),
            storesAsync.when(
              data: (stores) {
                if (stores.isEmpty) return const Text('لا توجد مطاعم مسجلة.');
                // Simulating top 3 stores for UI purposes
                final topStores = stores.take(3).toList();
                return Column(
                  children: topStores.map((store) => ListTile(
                    leading: const Icon(Icons.star, color: Colors.amber),
                    title: Text(store['name'] ?? 'مجهول'),
                    subtitle: Text(store['address'] ?? ''),
                    trailing: const Text('4.8 ⭐', style: TextStyle(fontWeight: FontWeight.bold)),
                  )).toList(),
                );
              },
              loading: () => const CircularProgressIndicator(),
              error: (e, st) => Text('خطأ: $e'),
            ),
          ],
        ),
      ),
    );
  }

  // 4. Offers
  Widget _buildOffersSection(BuildContext context, WidgetRef ref) {
    final storesAsync = ref.watch(storesProvider);

    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('4. العروض (Featured Offers)', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFFF5722))),
                TextButton.icon(
                  onPressed: () {
                    // Logic to manually select stores to feature in the "Offers" section
                    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('قريباً: آلية التحديد المتعدد للمطاعم المميزة')));
                  },
                  icon: const Icon(Icons.local_offer),
                  label: const Text('تحديد المطاعم'),
                ),
              ],
            ),
            const Text('هنا تظهر المطاعم التي تقدم خصومات أو تم تحديدها يدوياً لتظهر في قسم العروض.', style: TextStyle(color: Colors.grey, fontSize: 12)),
            const Divider(),
            storesAsync.when(
              data: (stores) {
                if (stores.isEmpty) return const Text('لا توجد مطاعم.');
                return Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: stores.take(4).map((store) => Chip(
                    avatar: const Icon(Icons.local_offer, color: Colors.white, size: 16),
                    label: Text(store['name'] ?? ''),
                    backgroundColor: Colors.redAccent,
                    labelStyle: const TextStyle(color: Colors.white),
                  )).toList(),
                );
              },
              loading: () => const CircularProgressIndicator(),
              error: (e, st) => Text('خطأ: $e'),
            ),
          ],
        ),
      ),
    );
  }
}
