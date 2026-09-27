import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final authStateProvider = StreamProvider<AuthState>((ref) {
  return Supabase.instance.client.auth.onAuthStateChange;
});

class AuthNotifier extends AsyncNotifier<User?> {
  final SupabaseClient _client = Supabase.instance.client;

  @override
  Future<User?> build() async {
    final session = _client.auth.currentSession;
    if (session != null) {
      return await _verifyAdminRole(session.user);
    }
    return null;
  }

  Future<User?> _verifyAdminRole(User user) async {
    try {
      final response = await _client
          .from('users')
          .select('role')
          .eq('id', user.id)
          .maybeSingle();

      if (response != null && response['role'] == 'admin') {
        return user;
      } else {
        await _client.auth.signOut();
        throw 'عفواً، ليس لديك صلاحية للدخول إلى لوحة التحكم';
      }
    } catch (e) {
      await _client.auth.signOut();
      throw 'خطأ في التحقق من الصلاحيات: $e';
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
        final verifiedUser = await _verifyAdminRole(response.user!);
        state = AsyncValue.data(verifiedUser);
      } else {
        state = const AsyncValue.data(null);
      }
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> signOut() async {
    await _client.auth.signOut();
    state = const AsyncValue.data(null);
  }
}

final authProvider = AsyncNotifierProvider<AuthNotifier, User?>(() {
  return AuthNotifier();
});
