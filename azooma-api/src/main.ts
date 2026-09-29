import './config/env'; // Must be first — validates all env vars on startup
import './config/firebase'; // Initialize Firebase Admin SDK singleton

import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import { env } from './config/env';

// ── Route modules ──────────────────────────────────────────────────────────
import { authRouter } from './modules/auth/auth.router';
import { categoriesRouter } from './modules/categories/categories.router';
import { storesRouter } from './modules/stores/stores.router';
import { usersRouter } from './modules/users/users.router';
import { ordersRouter } from './modules/orders/orders.router';

// ── App Bootstrap ──────────────────────────────────────────────────────────
const app = express();

// Global middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

// ── Health check ───────────────────────────────────────────────────────────
app.get('/health', (_req, res) => {
  res.status(200).json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ── API Routes ─────────────────────────────────────────────────────────────
app.use('/api/v1/auth', authRouter);
app.use('/api/v1/categories', categoriesRouter);
app.use('/api/v1/stores', storesRouter);
app.use('/api/v1/users', usersRouter);
app.use('/api/v1/orders', ordersRouter);

// ── Global Error Handler ───────────────────────────────────────────────────
app.use(
  (
    err: any,
    _req: express.Request,
    res: express.Response,
    _next: express.NextFunction
  ) => {
    console.error('Unhandled Error:', err);
    res.status(500).json({
      error: 'Internal Server Error',
      message: env.NODE_ENV === 'development' ? err.message : undefined,
    });
  }
);

// ── Start ──────────────────────────────────────────────────────────────────
app.listen(Number(env.PORT), () => {
  console.log(`🚀 Azooma API running → http://localhost:${env.PORT}`);
  console.log(`🌍 Environment: ${env.NODE_ENV}`);
});
