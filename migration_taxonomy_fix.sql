-- ═══════════════════════════════════════════════════════════════════════
-- AZOOMA: Strict Relational Taxonomy Fix
-- Run this in Supabase SQL Editor (Dashboard → SQL Editor → New Query)
-- ═══════════════════════════════════════════════════════════════════════

-- STEP 1: Add menu_category_id to products (the correct FK to menu_categories)
ALTER TABLE products
  ADD COLUMN IF NOT EXISTS menu_category_id UUID REFERENCES menu_categories(id) ON DELETE SET NULL;

-- STEP 2: Migrate any existing data where category_id was already pointing
--         at a menu_categories row (best-effort copy, safe to run)
UPDATE products p
SET menu_category_id = p.category_id
WHERE p.category_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM menu_categories mc WHERE mc.id = p.category_id);

-- STEP 3: Add app_category_id to stores (links a store to a top-level app category)
ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS app_category_id UUID REFERENCES app_categories(id) ON DELETE SET NULL;

-- STEP 4: Performance indexes
CREATE INDEX IF NOT EXISTS idx_products_menu_category_id ON products(menu_category_id);
CREATE INDEX IF NOT EXISTS idx_products_store_id         ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_menu_categories_store_id  ON menu_categories(store_id);

-- STEP 5: Verify structure — this should show menu_category_id in the column list
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'products'
ORDER BY ordinal_position;
