#!/bin/bash
set -e  # Exit on any error

echo "======================================"
echo "  Azooma Admin Panel - Vercel Build   "
echo "======================================"

FLUTTER_VERSION="3.32.0"
FLUTTER_DIR="$HOME/flutter"
FLUTTER_BIN="$FLUTTER_DIR/bin/flutter"

# ── Step 1: Fix Git ownership (root user on Vercel) ────────────────────────
echo "[1/5] Fixing git safe.directory..."
git config --global --add safe.directory '*' 2>/dev/null || true
git config --global --add safe.directory "$FLUTTER_DIR" 2>/dev/null || true
git config --global --add safe.directory "$FLUTTER_DIR/.git" 2>/dev/null || true

# ── Step 2: Install Flutter ────────────────────────────────────────────────
if [ ! -f "$FLUTTER_BIN" ]; then
  echo "[2/5] Downloading Flutter $FLUTTER_VERSION..."
  curl -fsSL \
    "https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_${FLUTTER_VERSION}-stable.tar.xz" \
    | tar -xJ -C "$HOME"
  echo "Flutter downloaded to $FLUTTER_DIR"
else
  echo "[2/5] Flutter already cached at $FLUTTER_DIR"
fi

# ── Step 3: Configure PATH ─────────────────────────────────────────────────
export PATH="$FLUTTER_BIN/../:$FLUTTER_DIR/bin:$PATH"
echo "[3/5] Flutter version:"
flutter --version --no-color 2>/dev/null || true

# ── Step 4: Configure Flutter for web ─────────────────────────────────────
echo "[4/5] Configuring Flutter..."
flutter config --no-analytics --no-color 2>/dev/null || true

# ── Step 5: Build Flutter Web ──────────────────────────────────────────────
echo "[5/5] Building Flutter Web..."
cd admin_panel
flutter pub get --no-color
flutter build web \
  --release \
  --no-pub \
  --no-color

echo ""
echo "✅ Build complete! Output: admin_panel/build/web"
ls -la build/web/
