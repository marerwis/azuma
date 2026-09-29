import * as admin from 'firebase-admin';
import * as fs from 'fs';
import * as path from 'path';

// ---------------------------------------------------------------------------
// Firebase Admin SDK — Singleton initialization.
//
// Strategy: Read service-account.json file directly from disk.
// This avoids the Windows multiline private key issue in .env files entirely.
//
// HOW TO SET UP:
//   1. Firebase Console → Project Settings → Service Accounts
//      → "Generate New Private Key" → downloads a JSON file
//   2. Rename it to `service-account.json`
//   3. Place it in: azooma-api/service-account.json
//   4. It is already in .gitignore — NEVER commit it to Git.
// ---------------------------------------------------------------------------

// New firebase-admin API uses getApps() instead of admin.apps
const existingApps = admin.getApps();

if (existingApps.length === 0) {
  const serviceAccountPath = path.resolve(process.cwd(), 'service-account.json');

  if (!fs.existsSync(serviceAccountPath)) {
    throw new Error(
      `❌ Firebase service account file not found!\n` +
      `Expected at: ${serviceAccountPath}\n\n` +
      `To fix:\n` +
      `  1. Open Firebase Console → Project Settings → Service Accounts\n` +
      `  2. Click "Generate New Private Key"\n` +
      `  3. Save the downloaded JSON as "service-account.json" inside azooma-api/`
    );
  }

  const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf-8'));

  admin.initializeApp({
    credential: admin.cert(serviceAccount),
  });

  console.log(`🔥 Firebase Admin SDK initialized (project: ${serviceAccount.project_id})`);
}

export const firebaseAdmin = admin;
