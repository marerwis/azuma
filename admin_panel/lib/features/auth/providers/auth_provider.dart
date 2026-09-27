import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final authStateProvider = StreamProvider<AuthState>((ref) {
  return Supabase.instance.client.auth.onAuthStateChange;
});

class AuthNotifier extends StateNotifier<AsyncValue<User?>> {
  final SupabaseClient _client = Supabase.instance.client;

  AuthNotifier() : super(const AsyncValue.loading()) {
    _checkInitialSession();
  }

  Future<void> _checkInitialSession() async {
    final session = _client.auth.currentSession;
    if (session != null) {
      await _verifyAdminRole(session.user);
    } else {
      state = const AsyncValue.data(null);
    }
  }

  Future<void> _verifyAdminRole(User user) async {
    try {
      final response = await _client
          .from('users')
          .select('role')
          .eq('id', user.id)
          .maybeSingle();

      if (response != null && response['role'] == 'admin') {
        state = AsyncValue.data(user);
      } else {
        // Not an admin, sign out immediately
        await _client.auth.signOut();
        state = AsyncValue.error('عفواً، ليس لديك صلاحية للدخول إلى لوحة التحكم', StackTrace.current);
      }
    } catch (e) {
      await _client.auth.signOut();
      state = AsyncValue.error('خطأ في التحقق من الصلاحيات: $e', StackTrace.current);
    }
  }

  Future<void> signIn(String email, String password) async {
    state = const AsyncValue.loading();
    try {
      final response = await _client.auth.signInWithPassword(
        email: email,
        password: password,
      );
      if (response.user != null) {
        await _verifyAdminRole(response.user!);
      } else {
        state = const AsyncValue.data(null);
      }
    } catch (e) {
      state = AsyncValue.error(e, StackTrace.current);
    }
  }

  Future<void> signOut() async {
    await _client.auth.signOut();
    state = const AsyncValue.data(null);
  }
}

final authProvider = StateNotifierProvider<AuthNotifier, AsyncValue<User?>>((ref) {
  return AuthNotifier();
});
