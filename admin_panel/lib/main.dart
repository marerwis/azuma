import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'core/theme/admin_theme.dart';
import 'features/dashboard/dashboard_layout.dart';
import 'features/auth/login_screen.dart';
import 'features/auth/providers/auth_provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  await Supabase.initialize(
    url: 'https://arivoyaepcxaoupzvvbw.supabase.co',
    anonKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFyaXZveWFlcGN4YW91cHp2dmJ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA1MzAwODQsImV4cCI6MjEwNjEwNjA4NH0.ktk8TTu6QZB5PZuYnQl-Qy9Edx3RN8ZPmaR2AGigwok',
  );

  runApp(const ProviderScope(child: AdminPanelApp()));
}

class AdminPanelApp extends ConsumerWidget {
  const AdminPanelApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);

    return MaterialApp(
      title: 'Azooma Admin Panel',
      debugShowCheckedModeBanner: false,
      theme: AdminTheme.lightTheme,
      builder: (context, child) {
        return Directionality(
          textDirection: TextDirection.rtl,
          child: child!,
        );
      },
      home: authState.when(
        data: (user) {
          if (user != null) {
            return const DashboardLayout();
          } else {
            return const LoginScreen();
          }
        },
        loading: () => const Scaffold(body: Center(child: CircularProgressIndicator())),
        error: (e, st) => const LoginScreen(),
      ),
    );
  }
}
