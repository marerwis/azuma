import 'package:supabase/supabase.dart';

void main() async {
  final client = SupabaseClient(
    'https://arivoyaepcxaoupzvvbw.supabase.co',
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFyaXZveWFlcGN4YW91cHp2dmJ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA1MzAwODQsImV4cCI6MjEwNjEwNjA4NH0.ktk8TTu6QZB5PZuYnQl-Qy9Edx3RN8ZPmaR2AGigwok',
  );

  try {
    print('Attempting login...');
    final response = await client.auth.signInWithPassword(
      email: 'marerwis@gmail.com',
      password: '19840marE',
    );
    print('Login successful! User ID: ${response.user?.id}');
    
    print('Checking public.users table...');
    final dbResponse = await client
        .from('users')
        .select('role')
        .eq('id', response.user!.id)
        .maybeSingle();
        
    print('DB Response: $dbResponse');
  } catch (e) {
    print('Error caught: $e');
  }
}
