import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

// ── Azooma Admin Dark Color Palette ──────────────────────────────────────────
class AzColor {
  AzColor._();

  // Backgrounds
  static const bgBase    = Color(0xFF0F172A); // Deep Navy (scaffold)
  static const bgSurface = Color(0xFF1E293B); // Slate (cards, sidebar)
  static const bgElevated= Color(0xFF273549); // Slightly lighter (hover, inputs)
  static const bgBorder  = Color(0xFF334155); // Dividers & borders

  // Primary / Accent
  static const primary   = Color(0xFF6366F1); // Indigo
  static const primaryHover = Color(0xFF818CF8);
  static const secondary = Color(0xFF8B5CF6); // Purple

  // Semantic colors
  static const success   = Color(0xFF10B981); // Green
  static const warning   = Color(0xFFF59E0B); // Amber
  static const danger    = Color(0xFFEF4444); // Red
  static const info      = Color(0xFF3B82F6); // Blue

  // Text
  static const textPrimary   = Color(0xFFF1F5F9); // Almost white
  static const textSecondary = Color(0xFF94A3B8); // Slate-400
  static const textMuted     = Color(0xFF64748B); // Slate-500
}

class AdminTheme {
  AdminTheme._();

  static ThemeData get darkTheme {
    final base = ThemeData.dark(useMaterial3: true);
    final cairoText = GoogleFonts.cairoTextTheme(base.textTheme).apply(
      bodyColor: AzColor.textPrimary,
      displayColor: AzColor.textPrimary,
    );

    return base.copyWith(
      colorScheme: const ColorScheme.dark(
        brightness: Brightness.dark,
        surface: AzColor.bgSurface,
        onSurface: AzColor.textPrimary,
        primary: AzColor.primary,
        onPrimary: Colors.white,
        secondary: AzColor.secondary,
        onSecondary: Colors.white,
        error: AzColor.danger,
        onError: Colors.white,
        outline: AzColor.bgBorder,
      ),
      scaffoldBackgroundColor: AzColor.bgBase,
      textTheme: cairoText,
      primaryTextTheme: cairoText,

      // ── AppBar ──────────────────────────────────────────────────────────
      appBarTheme: AppBarTheme(
        backgroundColor: AzColor.bgSurface,
        foregroundColor: AzColor.textPrimary,
        elevation: 0,
        surfaceTintColor: Colors.transparent,
        titleTextStyle: GoogleFonts.cairo(
            color: AzColor.textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.bold),
      ),

      // ── Cards ────────────────────────────────────────────────────────────
      cardTheme: CardThemeData(
        color: AzColor.bgSurface,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: AzColor.bgBorder, width: 1),
        ),
        margin: EdgeInsets.zero,
      ),

      // ── Input fields ─────────────────────────────────────────────────────
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AzColor.bgElevated,
        labelStyle: const TextStyle(color: AzColor.textSecondary),
        hintStyle: const TextStyle(color: AzColor.textMuted),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AzColor.bgBorder),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AzColor.bgBorder),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AzColor.primary, width: 1.5),
        ),
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      ),

      // ── Elevated Buttons ─────────────────────────────────────────────────
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AzColor.primary,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 13),
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8)),
          textStyle: GoogleFonts.cairo(
              fontWeight: FontWeight.bold, fontSize: 14),
        ),
      ),

      // ── Outlined Buttons ─────────────────────────────────────────────────
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AzColor.primary,
          side: const BorderSide(color: AzColor.primary),
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8)),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
        ),
      ),

      // ── DataTable ────────────────────────────────────────────────────────
      dataTableTheme: DataTableThemeData(
        headingRowColor:
            WidgetStateProperty.all(AzColor.bgElevated),
        dataRowColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.hovered)) {
            return AzColor.primary.withValues(alpha: 0.06);
          }
          return Colors.transparent;
        }),
        headingTextStyle: const TextStyle(
            color: AzColor.textSecondary,
            fontWeight: FontWeight.bold,
            fontSize: 12,
            letterSpacing: 0.5),
        dataTextStyle: const TextStyle(
            color: AzColor.textPrimary, fontSize: 13),
        dividerThickness: 1,
        decoration: BoxDecoration(
          border: Border.all(color: AzColor.bgBorder),
          borderRadius: BorderRadius.circular(12),
        ),
      ),

      // ── Dialog ───────────────────────────────────────────────────────────
      dialogTheme: DialogThemeData(
        backgroundColor: AzColor.bgSurface,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16)),
        titleTextStyle: GoogleFonts.cairo(
            color: AzColor.textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.bold),
      ),

      // ── Switch ───────────────────────────────────────────────────────────
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) =>
            states.contains(WidgetState.selected)
                ? AzColor.primary
                : AzColor.textMuted),
        trackColor: WidgetStateProperty.resolveWith((states) =>
            states.contains(WidgetState.selected)
                ? AzColor.primary.withValues(alpha: 0.4)
                : AzColor.bgBorder),
      ),

      // ── SnackBar ─────────────────────────────────────────────────────────
      snackBarTheme: SnackBarThemeData(
        backgroundColor: AzColor.bgElevated,
        contentTextStyle:
            const TextStyle(color: AzColor.textPrimary),
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8)),
        behavior: SnackBarBehavior.floating,
      ),

      // ── Divider ──────────────────────────────────────────────────────────
      dividerTheme: const DividerThemeData(
        color: AzColor.bgBorder,
        thickness: 1,
        space: 1,
      ),

      // ── Popup Menu ───────────────────────────────────────────────────────
      popupMenuTheme: PopupMenuThemeData(
        color: AzColor.bgElevated,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10),
          side: const BorderSide(color: AzColor.bgBorder),
        ),
        textStyle: const TextStyle(color: AzColor.textPrimary),
      ),

      // ── DropdownMenu ─────────────────────────────────────────────────────
      dropdownMenuTheme: DropdownMenuThemeData(
        menuStyle: MenuStyle(
          backgroundColor:
              WidgetStateProperty.all(AzColor.bgElevated),
          surfaceTintColor:
              WidgetStateProperty.all(Colors.transparent),
          shape: WidgetStateProperty.all(
            RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
              side: const BorderSide(color: AzColor.bgBorder),
            ),
          ),
        ),
      ),
    );
  }

  // Keep old reference for compatibility
  static ThemeData get lightTheme => darkTheme;
}

// ── Shared Widget Helpers (used across all screens) ─────────────────────────

/// Colored status pill — used in every DataTable.
class AzStatusBadge extends StatelessWidget {
  const AzStatusBadge({super.key, required this.isActive});
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    final color = isActive ? AzColor.success : AzColor.danger;
    final label = isActive ? 'نشط' : 'معطل';
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        border: Border.all(color: color.withValues(alpha: 0.5)),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
              width: 6,
              height: 6,
              decoration:
                  BoxDecoration(color: color, shape: BoxShape.circle)),
          const SizedBox(width: 5),
          Text(label,
              style: TextStyle(
                  color: color,
                  fontSize: 11,
                  fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}

/// Dark-styled screen header bar.
class AzScreenHeader extends StatelessWidget {
  const AzScreenHeader({
    super.key,
    required this.title,
    required this.subtitle,
    required this.icon,
    this.actionLabel,
    this.onAction,
    this.onRefresh,
  });
  final String title;
  final String subtitle;
  final IconData icon;
  final String? actionLabel;
  final VoidCallback? onAction;
  final VoidCallback? onRefresh;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: AzColor.bgSurface,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: AzColor.primary.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: AzColor.primary, size: 22),
          ),
          const SizedBox(width: 14),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title,
                  style: const TextStyle(
                      fontSize: 19,
                      fontWeight: FontWeight.bold,
                      color: AzColor.textPrimary)),
              Text(subtitle,
                  style: const TextStyle(
                      color: AzColor.textSecondary, fontSize: 12)),
            ],
          ),
          const Spacer(),
          if (onRefresh != null)
            IconButton(
              icon: const Icon(Icons.refresh,
                  color: AzColor.textSecondary),
              tooltip: 'تحديث',
              onPressed: onRefresh,
            ),
          if (actionLabel != null && onAction != null) ...[
            const SizedBox(width: 8),
            ElevatedButton.icon(
              onPressed: onAction,
              icon: const Icon(Icons.add, size: 18),
              label: Text(actionLabel!),
            ),
          ],
        ],
      ),
    );
  }
}

/// Dark card container.
class AzCard extends StatelessWidget {
  const AzCard({super.key, required this.child, this.padding});
  final Widget child;
  final EdgeInsetsGeometry? padding;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: AzColor.bgSurface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AzColor.bgBorder),
      ),
      child: child,
    );
  }
}

/// Search field styled for dark theme.
class AzSearchField extends StatelessWidget {
  const AzSearchField(
      {super.key, required this.onChanged, this.hint = 'بحث...'});
  final ValueChanged<String> onChanged;
  final String hint;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 280,
      child: TextField(
        onChanged: onChanged,
        style: const TextStyle(color: AzColor.textPrimary),
        decoration: InputDecoration(
          hintText: hint,
          isDense: true,
          prefixIcon: const Icon(Icons.search,
              size: 18, color: AzColor.textMuted),
        ),
      ),
    );
  }
}
