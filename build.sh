#!/bin/bash
set -e

# Run admin_panel/build.sh
cd "$(dirname "$0")/admin_panel"
sed -i 's/\r$//' build.sh 2>/dev/null || true
bash build.sh
