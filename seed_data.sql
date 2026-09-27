-- Azooma Delivery Ecosystem: Seed Data
-- CAUTION: Ensure your Supabase schema is deployed before running this.

-- 1. Insert a dummy Vendor User
INSERT INTO auth.users (id, instance_id, aud, role, email, encrypted_password, email_confirmed_at, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  '00000000-0000-0000-0000-000000000000',
  'authenticated',
  'authenticated',
  'vendor@azooma.com',
  'dummy_hash',
  NOW(),
  '{"provider":"email","providers":["email"]}',
  '{}',
  NOW(),
  NOW()
) ON CONFLICT (id) DO NOTHING;

INSERT INTO public.users (id, role, full_name, phone, email, is_active)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'vendor',
  'أحمد صاحب المطعم',
  '0912345678',
  'vendor@azooma.com',
  TRUE
) ON CONFLICT (id) DO NOTHING;

-- 2. Insert a Store
INSERT INTO public.stores (id, vendor_id, name, description, address, latitude, longitude, delivery_radius_km, commission_rate, is_active)
VALUES (
  '22222222-2222-2222-2222-222222222222',
  '11111111-1111-1111-1111-111111111111',
  'شاورما كينج',
  'أفضل شاورما في بنغازي، طعم لا يقاوم.',
  'شارع دبي، بنغازي',
  32.115,
  20.082,
  5.0,
  10.0,
  TRUE
) ON CONFLICT (id) DO NOTHING;

-- 3. Insert Categories
INSERT INTO public.categories (id, name, image_url, is_active, sort_order) VALUES
('33333333-3333-3333-3333-333333333301', 'وجبات سريعة', '🍔', TRUE, 1),
('33333333-3333-3333-3333-333333333302', 'مشروبات', '🥤', TRUE, 2),
('33333333-3333-3333-3333-333333333303', 'حلويات', '🍰', TRUE, 3),
('33333333-3333-3333-3333-333333333304', 'بيتزا', '🍕', TRUE, 4),
('33333333-3333-3333-3333-333333333305', 'مشاوي', '🥩', TRUE, 5),
('33333333-3333-3333-3333-333333333306', 'صحي', '🥗', TRUE, 6),
('33333333-3333-3333-3333-333333333307', 'بقالة', '🛒', TRUE, 7),
('33333333-3333-3333-3333-333333333308', 'مخبوزات', '🥖', TRUE, 8)
ON CONFLICT (id) DO NOTHING;

-- 4. Insert Products
INSERT INTO public.products (id, store_id, category_id, name, description, base_price, is_active) VALUES
(
  '44444444-4444-4444-4444-444444444401',
  '22222222-2222-2222-2222-222222222222',
  '33333333-3333-3333-3333-333333333301',
  'شاورما دجاج صاروخ',
  'شاورما دجاج طازجة مع البطاطس والمثومة',
  12.00,
  TRUE
),
(
  '44444444-4444-4444-4444-444444444402',
  '22222222-2222-2222-2222-222222222222',
  '33333333-3333-3333-3333-333333333301',
  'شاورما لحم عربي',
  'شاورما لحم مقطعة مع الطحينة والسلطة',
  15.00,
  TRUE
)
ON CONFLICT (id) DO NOTHING;

-- 5. Insert Banners
INSERT INTO public.banners (id, store_id, image_url, action_url, is_active, sort_order) VALUES
(
  '55555555-5555-5555-5555-555555555501',
  '22222222-2222-2222-2222-222222222222',
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&auto=format&fit=crop&q=80',
  '#',
  TRUE,
  1
),
(
  '55555555-5555-5555-5555-555555555502',
  '22222222-2222-2222-2222-222222222222',
  'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800&auto=format&fit=crop&q=80',
  '#',
  TRUE,
  2
)
ON CONFLICT (id) DO NOTHING;
