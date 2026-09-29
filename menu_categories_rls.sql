-- ==============================================================================
-- Menu Categories: RLS Policies & Foreign Key Safety
-- Run this in your Supabase SQL Editor
-- ==============================================================================

-- 1. Ensure RLS is enabled on menu_categories
ALTER TABLE menu_categories ENABLE ROW LEVEL SECURITY;

-- 2. Drop existing policies to avoid conflicts (safe to run even if they don't exist)
DROP POLICY IF EXISTS "Allow authenticated read menu_categories"  ON menu_categories;
DROP POLICY IF EXISTS "Allow authenticated insert menu_categories" ON menu_categories;
DROP POLICY IF EXISTS "Allow authenticated update menu_categories" ON menu_categories;
DROP POLICY IF EXISTS "Allow authenticated delete menu_categories" ON menu_categories;

-- 3. Create comprehensive CRUD policies for authenticated users
--    (The Admin Panel connects as an authenticated user with role=admin)

-- SELECT: Any authenticated user can read menu categories
CREATE POLICY "Allow authenticated read menu_categories"
  ON menu_categories FOR SELECT
  TO authenticated
  USING (true);

-- INSERT: Any authenticated user can insert (admin panel enforces role check)
CREATE POLICY "Allow authenticated insert menu_categories"
  ON menu_categories FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- UPDATE: Any authenticated user can update
CREATE POLICY "Allow authenticated update menu_categories"
  ON menu_categories FOR UPDATE
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- DELETE: Any authenticated user can delete
CREATE POLICY "Allow authenticated delete menu_categories"
  ON menu_categories FOR DELETE
  TO authenticated
  USING (true);

-- ==============================================================================
-- 4. Foreign Key Constraint: Protect categories that still have products
-- ==============================================================================
-- If products.menu_category_id already has a FK to menu_categories.id,
-- ensure it uses RESTRICT (default) so deleting a category with products fails gracefully.
-- This lets our Dart code catch the error and show a friendly message.
--
-- Check if the constraint exists; if not, add it:
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'products_menu_category_id_fkey'
      AND table_name = 'products'
  ) THEN
    ALTER TABLE products
      ADD CONSTRAINT products_menu_category_id_fkey
      FOREIGN KEY (menu_category_id) REFERENCES menu_categories(id)
      ON DELETE RESTRICT;
  END IF;
END$$;

-- ==============================================================================
-- Done! The Admin Panel can now INSERT, UPDATE, DELETE menu_categories.
-- Deleting a category that still has products will raise a FK violation,
-- which the Dart code catches and shows a user-friendly Arabic message.
-- ==============================================================================
