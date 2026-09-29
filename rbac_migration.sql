-- ==============================================================================
-- RBAC Ecosystem Migration
-- Adds necessary roles and secure functions to manage auth.users and app_metadata
-- ==============================================================================

-- 1. Ensure pgcrypto is enabled for password hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. Ensure user_role enum exists (fallback if it was missing)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('customer', 'driver', 'vendor', 'admin');
    END IF;
END$$;

-- 3. Ensure role column exists in users table (fallback)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'role') THEN
        ALTER TABLE users ADD COLUMN role user_role NOT NULL DEFAULT 'customer';
    END IF;
END$$;

-- ==============================================================================
-- SECURE RPC FUNCTIONS (Must be run by a Superuser / postgres role)
-- ==============================================================================

-- 4. RPC to securely update a user's role (both in public.users and auth.users app_metadata)
CREATE OR REPLACE FUNCTION set_user_role(target_user_id UUID, new_role text)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER -- Runs with elevated privileges
SET search_path = public
AS $$
BEGIN
  -- Update public.users
  UPDATE public.users SET role = new_role::user_role WHERE id = target_user_id;

  -- Update auth.users app_metadata for RLS access
  UPDATE auth.users 
  SET raw_app_meta_data = coalesce(raw_app_meta_data, '{}'::jsonb) || jsonb_build_object('role', new_role)
  WHERE id = target_user_id;
END;
$$;

-- 5. RPC to securely create a new user directly from the Admin Panel
CREATE OR REPLACE FUNCTION admin_create_user(
  p_email text,
  p_password text,
  p_full_name text,
  p_phone text,
  p_role text
) RETURNS uuid
LANGUAGE plpgsql
SECURITY DEFINER -- Runs with elevated privileges
SET search_path = public
AS $$
DECLARE
  new_user_id uuid;
BEGIN
  -- Generate a new UUID
  new_user_id := gen_random_uuid();

  -- 1. Insert into Auth system securely
  INSERT INTO auth.users (
    id, instance_id, aud, role, email, encrypted_password, 
    email_confirmed_at, raw_app_meta_data, raw_user_meta_data, 
    created_at, updated_at
  ) VALUES (
    new_user_id, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', p_email, crypt(p_password, gen_salt('bf')),
    now(), jsonb_build_object('role', p_role), jsonb_build_object('full_name', p_full_name, 'phone', p_phone),
    now(), now()
  );

  -- 2. Insert into public.users profile
  INSERT INTO public.users (
    id, role, full_name, phone, email, is_active
  ) VALUES (
    new_user_id, p_role::user_role, p_full_name, p_phone, p_email, true
  );

  RETURN new_user_id;
END;
$$;
