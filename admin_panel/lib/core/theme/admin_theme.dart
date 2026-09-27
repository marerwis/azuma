import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AdminTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFFF5722)),
      useMaterial3: true,
      textTheme: GoogleFonts.cairoTextTheme(),
      scaffoldBackgroundColor: const Color(0xFFF3F4F6),
    );
  }
}
