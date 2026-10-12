-- ==============================================================================
-- Azooma Supabase Migration (FIXED v2)
-- Purpose: Add `notifications` and `reviews` tables for Admin Panel features
-- Run in: Supabase Dashboard → SQL Editor
--
-- FIX v2: Cast order_status enum to TEXT in RLS policy (ERROR 22P02 fix)
-- Also: role comparison uses ::text cast to handle user_role enum safely
-- ==============================================================================


-- ── 1. NOTIFICATIONS TABLE ────────────────────────────────────────────────────
-- Stores a persistent history of every push notification sent from the admin panel.
-- FCM delivery itself is handled by a Cloud Function / Edge Function (not stored here).

CREATE TABLE IF NOT EXISTS public.notifications (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         TEXT        NOT NULL,
    body          TEXT        NOT NULL,
    -- 'all' = broadcast | 'customer' = single customer | 'driver' = single driver
    target_type   TEXT        NOT NULL CHECK (target_type IN ('all', 'customer', 'driver')),
    -- NULL when target_type = 'all'
    target_user_id UUID       REFERENCES public.users(id) ON DELETE SET NULL,
    -- The admin who triggered the send
    sent_by       UUID        REFERENCES public.users(id) ON DELETE SET NULL,
    -- 'sent' | 'failed' | 'pending'
    status        TEXT        NOT NULL DEFAULT 'sent' CHECK (status IN ('sent', 'failed', 'pending')),
    -- Optional deep-link data payload for the mobile app
    data          JSONB,
    sent_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for fast history queries (newest first)
CREATE INDEX IF NOT EXISTS idx_notifications_sent_at
    ON public.notifications (sent_at DESC);

-- Index for querying by target user
CREATE INDEX IF NOT EXISTS idx_notifications_target_user
    ON public.notifications (target_user_id)
    WHERE target_user_id IS NOT NULL;

COMMENT ON TABLE public.notifications IS
    'Audit log of all push notifications sent via the admin panel.';

-- RLS: Only admins can read/insert.
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- ⚠️ FIX: Cast role enum to TEXT for safe comparison
CREATE POLICY "Admins can view all notifications"
    ON public.notifications FOR SELECT
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.users
            WHERE id = auth.uid() AND role::text = 'admin'
        )
    );

CREATE POLICY "Admins can insert notifications"
    ON public.notifications FOR INSERT
    TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.users
            WHERE id = auth.uid() AND role::text = 'admin'
        )
    );


-- ── 2. REVIEWS TABLE ──────────────────────────────────────────────────────────
-- Stores customer ratings and comments for completed orders.
-- One review per order (enforced by UNIQUE constraint).

CREATE TABLE IF NOT EXISTS public.reviews (
    id          UUID  PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID  NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    customer_id UUID  NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    store_id    UUID  NOT NULL REFERENCES public.stores(id) ON DELETE CASCADE,
    -- Rating: 1–5 stars
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    -- Optional flag set by admin to hide abusive reviews
    is_hidden   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- Prevent duplicate reviews for the same order
    CONSTRAINT uq_reviews_order UNIQUE (order_id)
);

-- Indexes for fast filtering
CREATE INDEX IF NOT EXISTS idx_reviews_store_id
    ON public.reviews (store_id);
CREATE INDEX IF NOT EXISTS idx_reviews_customer_id
    ON public.reviews (customer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating
    ON public.reviews (rating);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at
    ON public.reviews (created_at DESC);

COMMENT ON TABLE public.reviews IS
    'Customer ratings (1–5 stars) and comments for completed orders.';

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_reviews_updated_at ON public.reviews;
CREATE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON public.reviews
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- RLS Policies
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;

-- Customers can read non-hidden reviews for any store
CREATE POLICY "Anyone can read visible reviews"
    ON public.reviews FOR SELECT
    USING (is_hidden = FALSE);

-- ⚠️ FIX: Cast order_status enum → TEXT to avoid ERROR 22P02
-- The orders.status column is an enum type (order_status), not TEXT.
-- Casting to ::text makes the comparison safe across all Postgres versions.
CREATE POLICY "Customers can submit reviews for their orders"
    ON public.reviews FOR INSERT
    TO authenticated
    WITH CHECK (
        customer_id = auth.uid()
        AND EXISTS (
            SELECT 1 FROM public.orders o
            WHERE o.id = order_id
              AND o.customer_id = auth.uid()
              AND o.status::text = 'delivered'   -- ← FIXED: enum cast to text
        )
    );

-- Admins can read ALL reviews (including hidden)
-- ⚠️ FIX: Cast user_role enum → TEXT
CREATE POLICY "Admins can read all reviews"
    ON public.reviews FOR SELECT
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.users
            WHERE id = auth.uid() AND role::text = 'admin'  -- ← FIXED
        )
    );

-- Admins can delete reviews (moderation)
CREATE POLICY "Admins can delete reviews"
    ON public.reviews FOR DELETE
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.users
            WHERE id = auth.uid() AND role::text = 'admin'  -- ← FIXED
        )
    );

-- Admins can hide/unhide reviews
CREATE POLICY "Admins can update reviews (hide/unhide)"
    ON public.reviews FOR UPDATE
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.users
            WHERE id = auth.uid() AND role::text = 'admin'  -- ← FIXED
        )
    );


-- ── 3. OPTIONAL: Average Rating View ─────────────────────────────────────────
-- Handy view for quickly getting per-store average ratings.

CREATE OR REPLACE VIEW public.store_ratings AS
SELECT
    s.id          AS store_id,
    s.name        AS store_name,
    COUNT(r.id)   AS total_reviews,
    ROUND(AVG(r.rating)::NUMERIC, 2) AS avg_rating
FROM public.stores s
LEFT JOIN public.reviews r ON r.store_id = s.id AND r.is_hidden = FALSE
GROUP BY s.id, s.name;

COMMENT ON VIEW public.store_ratings IS
    'Aggregated per-store review counts and average ratings (excluding hidden reviews).';

-- ==============================================================================
-- Migration complete. Tables created:
--   ✅ public.notifications
--   ✅ public.reviews
--   ✅ View: public.store_ratings
-- ==============================================================================
