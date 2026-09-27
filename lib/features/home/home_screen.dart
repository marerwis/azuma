import 'package:flutter/material.dart';
import '../../core/theme/app_theme.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildTopLocationBar(),
              const SizedBox(height: 16),
              _buildPromotionalCarousel(),
              const SizedBox(height: 24),
              _buildSectionHeader('الأقسام', null),
              _buildCategoriesGrid(),
              const SizedBox(height: 24),
              _buildSectionHeader('الأعلى تقييماً', null),
              _buildHorizontalStoreList(),
              const SizedBox(height: 24),
              _buildSectionHeader('العروض 🏷️', () {}),
              _buildHorizontalOffersList(),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTopLocationBar() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.dp, vertical: 8.dp),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              const Icon(Icons.location_on, color: AppTheme.primaryOrange),
              const SizedBox(width: 8),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('التوصيل إلى', style: TextStyle(fontSize: 12, color: AppTheme.textSecondary)),
                  const Text('بنغازي، ليبيا', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                ],
              ),
              const Icon(Icons.keyboard_arrow_down, size: 16),
            ],
          ),
          Row(
            children: [
              IconButton(onPressed: () {}, icon: const Icon(Icons.search)),
              IconButton(
                onPressed: () {},
                icon: const Badge(
                  child: Icon(Icons.notifications_none),
                ),
              ),
            ],
          )
        ],
      ),
    );
  }

  Widget _buildPromotionalCarousel() {
    return SizedBox(
      height: 165,
      child: PageView(
        controller: PageController(viewportFraction: 0.9),
        children: [
          _buildBannerCard('وجباتك المفضلة', 'خصم 20% على أول طلب', 'عرض الأسبوع', const [Color(0xFF0E2A47), Color(0xFFFF4800)]),
          _buildBannerCard('عروض الجمعة للمشاوي', 'أشهى المأكولات على الفحم', 'خصم 30% 🔥', const [Color(0xFF5D1003), Color(0xFFFF5722)]),
        ],
      ),
    );
  }

  Widget _buildBannerCard(String title, String subtitle, String badge, List<Color> colors) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: LinearGradient(colors: colors, begin: Alignment.centerRight, end: Alignment.centerLeft),
      ),
      child: Stack(
        children: [
          Positioned(
            top: 12,
            left: 12, // Since it's RTL, left is end
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(color: AppTheme.primaryOrange, borderRadius: BorderRadius.circular(12)),
              child: Text(badge, style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold)),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: const TextStyle(color: Colors.white, fontSize: 17, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 4),
                    Text(subtitle, style: const TextStyle(color: Colors.white70, fontSize: 12)),
                  ],
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                  decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(20)),
                  child: const Text('اطلب الآن', style: TextStyle(color: AppTheme.primaryOrange, fontSize: 12, fontWeight: FontWeight.bold)),
                )
              ],
            ),
          )
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title, VoidCallback? onSeeAll) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(title, style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold, color: AppTheme.textPrimary)),
          if (onSeeAll != null)
            GestureDetector(
              onTap: onSeeAll,
              child: const Text('عرض المزيد', style: TextStyle(color: AppTheme.primaryOrange, fontWeight: FontWeight.bold)),
            ),
        ],
      ),
    );
  }

  Widget _buildCategoriesGrid() {
    final categories = [
      {'name': 'برجر', 'icon': '🍔'},
      {'name': 'بيتزا', 'icon': '🍕'},
      {'name': 'شاورما', 'icon': '🌯'},
      {'name': 'مقهى', 'icon': '☕'},
      {'name': 'حلويات', 'icon': '🍰'},
      {'name': 'مشاوي', 'icon': '🥩'},
      {'name': 'صحي', 'icon': '🥗'},
      {'name': 'بقالة', 'icon': '🛍️'},
    ];

    return SizedBox(
      height: 205,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        children: [
          _buildCategoryColumn(categories.sublist(0, 2)),
          const SizedBox(width: 10),
          _buildCategoryColumn(categories.sublist(2, 4)),
          const SizedBox(width: 10),
          _buildCategoryColumn(categories.sublist(4, 6)),
          const SizedBox(width: 10),
          _buildCategoryColumn(categories.sublist(6, 8)),
          const SizedBox(width: 10),
          _buildCategoryColumn(categories.sublist(0, 2)), // Duplicated for scroll effect
        ],
      ),
    );
  }

  Widget _buildCategoryColumn(List<Map<String, String>> items) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        _buildCategoryItem(items[0]),
        _buildCategoryItem(items[1]),
      ],
    );
  }

  Widget _buildCategoryItem(Map<String, String> item) {
    return SizedBox(
      width: 80,
      child: Column(
        children: [
          Container(
            width: 68,
            height: 68,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: const Color(0xFFF0F1F5)),
              boxShadow: [
                BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 4, offset: const Offset(0, 2)),
              ],
            ),
            alignment: Alignment.center,
            child: Text(item['icon']!, style: const TextStyle(fontSize: 32)),
          ),
          const SizedBox(height: 6),
          Text(item['name']!, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: AppTheme.textPrimary)),
        ],
      ),
    );
  }

  Widget _buildHorizontalStoreList() {
    return SizedBox(
      height: 210,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: 4,
        separatorBuilder: (_, __) => const SizedBox(width: 14),
        itemBuilder: (context, index) {
          return _buildTopRatedCard();
        },
      ),
    );
  }

  Widget _buildTopRatedCard() {
    return Container(
      width: 220,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 5)],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 115,
            decoration: const BoxDecoration(
              borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
              image: DecorationImage(
                image: AssetImage('assets/images/food_hero_banner.jpg'),
                fit: BoxFit.cover,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('مطعم الرواق', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: AppTheme.textPrimary)),
                const SizedBox(height: 4),
                Row(
                  children: const [
                    Icon(Icons.star, color: AppTheme.yellow, size: 14),
                    SizedBox(width: 4),
                    Text('4.8 (120+)', style: TextStyle(fontSize: 11, color: AppTheme.textSecondary)),
                  ],
                ),
                const SizedBox(height: 4),
                const Text('30-40 دقيقة • 5 د.ل توصيل', style: TextStyle(fontSize: 11, color: AppTheme.textSecondary)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHorizontalOffersList() {
    return SizedBox(
      height: 220,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: 3,
        separatorBuilder: (_, __) => const SizedBox(width: 14),
        itemBuilder: (context, index) {
          return _buildOfferCard();
        },
      ),
    );
  }

  Widget _buildOfferCard() {
    return Container(
      width: 260,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 5)],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 120,
            decoration: const BoxDecoration(
              borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
              image: DecorationImage(
                image: AssetImage('assets/images/welcome_pizza_bg.jpg'),
                fit: BoxFit.cover,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('شاورما كينج', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: AppTheme.textPrimary)),
                const SizedBox(height: 4),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('توصيل 0 د.ل', style: TextStyle(color: AppTheme.primaryOrange, fontWeight: FontWeight.bold, fontSize: 12)),
                    Row(
                      children: const [
                        Icon(Icons.star, color: AppTheme.yellow, size: 14),
                        SizedBox(width: 4),
                        Text('4.5', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: AppTheme.textSecondary)),
                      ],
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

extension on num {
  double get dp => toDouble();
}
