#!/bin/bash
set -e  # Exit on error

echo "======================================"
echo "  Azooma Admin Panel - Vercel Build   "
echo "======================================"

# Ensure we are in the admin_panel directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

FLUTTER_VERSION="3.47.7"
FLUTTER_DIR="$HOME/flutter"
FLUTTER_BIN="$FLUTTER_DIR/bin/flutter"

# ── Step 1: Fix Git ownership (root user on Vercel) ────────────────────────
echo "[1/5] Configuring git safe.directory..."
git config --global --add safe.directory '*' 2>/dev/null || true
git config --global --add safe.directory "$FLUTTER_DIR" 2>/dev/null || true
git config --global --add safe.directory "$FLUTTER_DIR/.git" 2>/dev/null || true

# ── Step 2: Install Flutter SDK ───────────────────────────────────────────
# Check if cached Flutter version matches, invalidate cache if older version exists
if [ ! -f "$FLUTTER_BIN" ] || ! "$FLUTTER_BIN" --version 2>/dev/null | grep -q "$FLUTTER_VERSION"; then
  echo "[2/5] Downloading Flutter $FLUTTER_VERSION..."
  rm -rf "$FLUTTER_DIR"
  curl -fsSL \
    "https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_${FLUTTER_VERSION}-stable.tar.xz" \
    | tar -xJ -C "$HOME"
  echo "Flutter successfully downloaded to $FLUTTER_DIR"
else
  echo "[2/5] Flutter $FLUTTER_VERSION already cached at $FLUTTER_DIR"
fi

# ── Step 3: Configure PATH ─────────────────────────────────────────────────
export PATH="$FLUTTER_DIR/bin:$PATH"
echo "[3/5] Flutter version:"
flutter --version --no-color 2>/dev/null || true

# ── Step 4: Configure Flutter for Web ──────────────────────────────────────
echo "[4/5] Configuring Flutter settings..."
flutter config --no-analytics --no-color 2>/dev/null || true

# ── Step 5: Build Flutter Web ──────────────────────────────────────────────
echo "[5/5] Building Flutter Web for release..."
flutter pub get --no-color
flutter build web --release --no-pub --no-color

echo ""
echo "✅ Build complete! Verifying build/web contents:"
ls -la build/web/
