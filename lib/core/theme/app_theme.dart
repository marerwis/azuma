import 'package:flutter/material.dart';

class AppTheme {
  // Azooma Brand Colors
  static const Color primaryOrange = Color(0xFFFF5722);
  static const Color secondaryOrange = Color(0xFFFF4800);
  static const Color background = Color(0xFFF8F9FB);
  static const Color textPrimary = Color(0xFF1E293B);
  static const Color textSecondary = Color(0xFF64748B);
  static const Color yellow = Color(0xFFFFC107);

  static ThemeData get lightTheme {
    return ThemeData(
      primaryColor: primaryOrange,
      scaffoldBackgroundColor: background,
      fontFamily: 'Cairo',
      colorScheme: ColorScheme.fromSeed(
        seedColor: primaryOrange,
        primary: primaryOrange,
        secondary: secondaryOrange,
        background: background,
      ),
      textTheme: const TextTheme(
        titleLarge: TextStyle(color: textPrimary, fontWeight: FontWeight.bold),
        titleMedium: TextStyle(color: textPrimary, fontWeight: FontWeight.bold),
        titleSmall: TextStyle(color: textPrimary, fontWeight: FontWeight.bold),
        bodyLarge: TextStyle(color: textPrimary),
        bodyMedium: TextStyle(color: textPrimary),
        bodySmall: TextStyle(color: textSecondary),
        labelLarge: TextStyle(color: primaryOrange, fontWeight: FontWeight.bold),
        labelMedium: TextStyle(color: primaryOrange, fontWeight: FontWeight.bold),
        labelSmall: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
      ),
      useMaterial3: true,
    );
  }
}
