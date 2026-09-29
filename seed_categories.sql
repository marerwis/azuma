-- 1. Delete all existing placeholder categories (this deletes both main and store-specific categories)
-- NOTE: If you only want to delete main app categories, use: DELETE FROM public.categories WHERE store_id IS NULL;
DELETE FROM public.categories;

-- 2. Insert the correct Main App Categories (Level 1)
INSERT INTO public.categories (name, image_url, store_id, is_active, sort_order)
VALUES 
  ('المطاعم', '🍔', NULL, TRUE, 1),
  ('الصيدليات', '💊', NULL, TRUE, 2),
  ('المتاجر', '🛍️', NULL, TRUE, 3),
  ('الغذائية', '🛒', NULL, TRUE, 4),
  ('خضروات', '🥬', NULL, TRUE, 5);
