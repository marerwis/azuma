import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../core/theme/admin_theme.dart';

// ── Repository ─────────────────────────────────────────────────────────────────

class SettingsRepository {
  final _db = Supabase.instance.client;

  /// Fetch all settings as a Map<key, value>.
  Future<Map<String, dynamic>> loadSettings() async {
    final rows = await _db
        .from('system_settings')
        .select('setting_key, setting_value');

    final result = <String, dynamic>{};
    for (final row in rows) {
      final key = row['setting_key']?.toString();
      if (key != null) result[key] = row['setting_value'];
    }
    return result;
  }

  /// Upsert a single setting by key.
  /// setting_value is JSONB — we wrap the primitive in a JSON object
  /// to keep it valid: { "v": value }
  Future<void> saveSetting(String key, dynamic value) async {
    await _db.from('system_settings').upsert(
      {
        'setting_key': key,
        'setting_value': {'v': value},
      },
      onConflict: 'setting_key',
    );
  }

  /// Upsert multiple settings at once.
  Future<void> saveAll(Map<String, dynamic> settings) async {
    final rows = settings.entries
        .map((e) => {
              'setting_key': e.key,
              'setting_value': {'v': e.value},
            })
        .toList();
    await _db.from('system_settings').upsert(rows,
        onConflict: 'setting_key');
  }
}

// ── Provider ──────────────────────────────────────────────────────────────────

final _settingsRepoProvider =
    Provider<SettingsRepository>((_) => SettingsRepository());

final settingsProvider =
    FutureProvider<Map<String, dynamic>>((ref) async {
  return ref.read(_settingsRepoProvider).loadSettings();
});

// ── Screen ─────────────────────────────────────────────────────────────────────

class SystemSettingsScreen extends ConsumerStatefulWidget {
  const SystemSettingsScreen({super.key});

  @override
  ConsumerState<SystemSettingsScreen> createState() =>
      _SystemSettingsScreenState();
}

class _SystemSettingsScreenState
    extends ConsumerState<SystemSettingsScreen> {
  final _formKey = GlobalKey<FormState>();
  bool _saving = false;
  bool _loaded = false;

  // ── General settings ──────────────────────────────────────────────────
  final _taxPct       = TextEditingController();
  final _minOrder     = TextEditingController();
  final _currency     = TextEditingController();
  final _currencySign = TextEditingController();
  final _appName      = TextEditingController();

  // ── Delivery ──────────────────────────────────────────────────────────
  final _deliveryFee  = TextEditingController();
  final _freeAbove    = TextEditingController();
  final _maxRadius    = TextEditingController();

  // ── API Keys ──────────────────────────────────────────────────────────
  final _oneSignalId   = TextEditingController();
  final _oneSignalKey  = TextEditingController();
  final _stripeKey     = TextEditingController();
  final _mapKey        = TextEditingController();

  @override
  void dispose() {
    for (final c in [
      _taxPct, _minOrder, _currency, _currencySign, _appName,
      _deliveryFee, _freeAbove, _maxRadius,
      _oneSignalId, _oneSignalKey, _stripeKey, _mapKey,
    ]) {
      c.dispose();
    }
    super.dispose();
  }

  void _populate(Map<String, dynamic> settings) {
    if (_loaded) return;
    _loaded = true;

    // Helper: extract value from JSONB wrapper {"v": value}
    String str(String key, [String def = '']) {
      final raw = settings[key];
      if (raw == null) return def;
      if (raw is Map) return (raw['v'] ?? def).toString();
      return raw.toString();
    }

    _taxPct.text       = str('tax_percentage', '0');
    _minOrder.text     = str('min_order_amount', '0');
    _currency.text     = str('currency', 'LYD');
    _currencySign.text = str('currency_sign', 'د.ل');
    _appName.text      = str('app_name', 'Azooma');
    _deliveryFee.text  = str('default_delivery_fee', '0');
    _freeAbove.text    = str('free_delivery_above', '0');
    _maxRadius.text    = str('max_delivery_radius_km', '10');
    _oneSignalId.text  = str('onesignal_app_id');
    _oneSignalKey.text = str('onesignal_api_key');
    _stripeKey.text    = str('stripe_public_key');
    _mapKey.text       = str('google_maps_api_key');
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);

    try {
      await ref.read(_settingsRepoProvider).saveAll({
        'tax_percentage':        double.tryParse(_taxPct.text.trim()) ?? 0,
        'min_order_amount':      double.tryParse(_minOrder.text.trim()) ?? 0,
        'currency':              _currency.text.trim(),
        'currency_sign':         _currencySign.text.trim(),
        'app_name':              _appName.text.trim(),
        'default_delivery_fee':  double.tryParse(_deliveryFee.text.trim()) ?? 0,
        'free_delivery_above':   double.tryParse(_freeAbove.text.trim()) ?? 0,
        'max_delivery_radius_km':double.tryParse(_maxRadius.text.trim()) ?? 10,
        'onesignal_app_id':      _oneSignalId.text.trim(),
        'onesignal_api_key':     _oneSignalKey.text.trim(),
        'stripe_public_key':     _stripeKey.text.trim(),
        'google_maps_api_key':   _mapKey.text.trim(),
      });

      ref.invalidate(settingsProvider);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Row(children: [
              Icon(Icons.check_circle, color: AzColor.success),
              SizedBox(width: 8),
              Text('تم حفظ الإعدادات بنجاح'),
            ]),
            backgroundColor: AzColor.bgElevated,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('خطأ في الحفظ: $e'),
            backgroundColor: AzColor.danger,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final settingsAsync = ref.watch(settingsProvider);

    return Scaffold(
      backgroundColor: AzColor.bgBase,
      body: Column(
        children: [
          AzScreenHeader(
            title: 'إعدادات النظام',
            subtitle: 'تكوين المتغيرات العامة وبيانات الاعتماد',
            icon: Icons.settings_outlined,
            onRefresh: () {
              _loaded = false;
              ref.invalidate(settingsProvider);
            },
          ),
          Expanded(
            child: settingsAsync.when(
              data: (settings) {
                _populate(settings);
                return _buildForm();
              },
              loading: () => const Center(
                  child: CircularProgressIndicator(
                      color: AzColor.primary)),
              error: (e, _) => Center(
                child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                  const Icon(Icons.error_outline,
                      color: AzColor.danger, size: 48),
                  const SizedBox(height: 12),
                  Text('$e',
                      style: const TextStyle(
                          color: AzColor.textSecondary)),
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: () =>
                        ref.invalidate(settingsProvider),
                    icon: const Icon(Icons.refresh),
                    label: const Text('إعادة المحاولة'),
                  ),
                ]),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildForm() {
    return Form(
      key: _formKey,
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // ── Row 1: General + Delivery ────────────────────────────────
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // General settings card
                Expanded(
                  child: _SettingsCard(
                    title: 'إعدادات عامة',
                    icon: Icons.tune_outlined,
                    color: AzColor.primary,
                    children: [
                      _SettingField(
                        ctrl: _appName,
                        label: 'اسم التطبيق',
                        hint: 'Azooma',
                        icon: Icons.apps,
                      ),
                      _SettingField(
                        ctrl: _taxPct,
                        label: 'نسبة الضريبة % (tax_percentage)',
                        hint: '0.00',
                        icon: Icons.percent,
                        isNumber: true,
                      ),
                      _SettingField(
                        ctrl: _minOrder,
                        label: 'الحد الأدنى للطلب (min_order_amount)',
                        hint: '0.00',
                        icon: Icons.shopping_cart_outlined,
                        isNumber: true,
                      ),
                      _SettingField(
                        ctrl: _currency,
                        label: 'رمز العملة (currency)',
                        hint: 'LYD',
                        icon: Icons.attach_money,
                      ),
                      _SettingField(
                        ctrl: _currencySign,
                        label: 'علامة العملة (currency_sign)',
                        hint: 'د.ل',
                        icon: Icons.currency_exchange,
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 16),

                // Delivery settings card
                Expanded(
                  child: _SettingsCard(
                    title: 'إعدادات التوصيل',
                    icon: Icons.delivery_dining_outlined,
                    color: AzColor.success,
                    children: [
                      _SettingField(
                        ctrl: _deliveryFee,
                        label: 'رسوم التوصيل الافتراضية',
                        hint: '0.00',
                        icon: Icons.local_shipping_outlined,
                        isNumber: true,
                      ),
                      _SettingField(
                        ctrl: _freeAbove,
                        label: 'توصيل مجاني عند (free_delivery_above)',
                        hint: '0.00 = غير مفعّل',
                        icon: Icons.card_giftcard,
                        isNumber: true,
                      ),
                      _SettingField(
                        ctrl: _maxRadius,
                        label: 'أقصى نطاق توصيل (كم)',
                        hint: '10',
                        icon: Icons.circle_outlined,
                        isNumber: true,
                      ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // ── Row 2: API Keys ────────────────────────────────────────────
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // OneSignal
                Expanded(
                  child: _SettingsCard(
                    title: 'OneSignal (الإشعارات)',
                    icon: Icons.notifications_active_outlined,
                    color: AzColor.warning,
                    children: [
                      _SettingField(
                        ctrl: _oneSignalId,
                        label: 'App ID',
                        hint: 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx',
                        icon: Icons.key_outlined,
                        isSecret: false,
                      ),
                      _SettingField(
                        ctrl: _oneSignalKey,
                        label: 'REST API Key',
                        hint: '••••••••••••',
                        icon: Icons.lock_outline,
                        isSecret: true,
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 16),

                // Payment + Maps
                Expanded(
                  child: _SettingsCard(
                    title: 'بوابة الدفع والخرائط',
                    icon: Icons.payment_outlined,
                    color: AzColor.secondary,
                    children: [
                      _SettingField(
                        ctrl: _stripeKey,
                        label: 'Stripe Public Key',
                        hint: 'pk_live_...',
                        icon: Icons.credit_card_outlined,
                        isSecret: true,
                      ),
                      _SettingField(
                        ctrl: _mapKey,
                        label: 'Google Maps API Key',
                        hint: 'AIza...',
                        icon: Icons.map_outlined,
                        isSecret: true,
                      ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 24),

            // ── Save button ────────────────────────────────────────────────
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AzColor.primary,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10)),
                ),
                onPressed: _saving ? null : _save,
                icon: _saving
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(
                            strokeWidth: 2, color: Colors.white))
                    : const Icon(Icons.save_outlined, size: 20),
                label: Text(
                  _saving ? 'جارٍ الحفظ...' : 'حفظ جميع الإعدادات',
                  style: const TextStyle(
                      fontSize: 15, fontWeight: FontWeight.bold),
                ),
              ),
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}

// ── Settings Card ──────────────────────────────────────────────────────────────

class _SettingsCard extends StatelessWidget {
  const _SettingsCard({
    required this.title,
    required this.icon,
    required this.color,
    required this.children,
  });
  final String title;
  final IconData icon;
  final Color color;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AzColor.bgSurface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AzColor.bgBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Card header
          Container(
            padding: const EdgeInsets.symmetric(
                horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.08),
              borderRadius: const BorderRadius.vertical(
                  top: Radius.circular(12)),
              border: Border(
                  bottom: BorderSide(
                      color: color.withValues(alpha: 0.2))),
            ),
            child: Row(children: [
              Icon(icon, color: color, size: 18),
              const SizedBox(width: 8),
              Text(title,
                  style: TextStyle(
                      color: color,
                      fontWeight: FontWeight.bold,
                      fontSize: 14)),
            ]),
          ),

          // Fields
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: children
                  .expand((w) => [w, const SizedBox(height: 12)])
                  .toList()
                ..removeLast(),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Setting Field ──────────────────────────────────────────────────────────────

class _SettingField extends StatefulWidget {
  const _SettingField({
    required this.ctrl,
    required this.label,
    required this.icon,
    this.hint,
    this.isNumber = false,
    this.isSecret = false,
  });
  final TextEditingController ctrl;
  final String label;
  final String? hint;
  final IconData icon;
  final bool isNumber;
  final bool isSecret;

  @override
  State<_SettingField> createState() => _SettingFieldState();
}

class _SettingFieldState extends State<_SettingField> {
  bool _obscure = true;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: widget.ctrl,
      obscureText: widget.isSecret && _obscure,
      keyboardType: widget.isNumber
          ? const TextInputType.numberWithOptions(decimal: true)
          : TextInputType.text,
      style: const TextStyle(color: AzColor.textPrimary, fontSize: 13),
      decoration: InputDecoration(
        labelText: widget.label,
        hintText: widget.hint,
        prefixIcon: Icon(widget.icon, size: 16, color: AzColor.primary),
        suffixIcon: widget.isSecret
            ? IconButton(
                icon: Icon(
                  _obscure ? Icons.visibility_outlined : Icons.visibility_off_outlined,
                  size: 16,
                  color: AzColor.textMuted,
                ),
                onPressed: () => setState(() => _obscure = !_obscure),
              )
            : null,
        isDense: true,
        contentPadding: const EdgeInsets.symmetric(
            horizontal: 12, vertical: 10),
      ),
      validator: widget.isNumber
          ? (v) {
              if (v != null &&
                  v.trim().isNotEmpty &&
                  double.tryParse(v.trim()) == null) {
                return 'أدخل رقماً صحيحاً';
              }
              return null;
            }
          : null,
    );
  }
}
