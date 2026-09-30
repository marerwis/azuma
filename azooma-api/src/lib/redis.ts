import Redis from 'ioredis';

const redisUrl = process.env.REDIS_URL;

if (!redisUrl) {
  console.warn("⚠️ REDIS_URL is not set. Caching will be disabled or might fail.");
}

export const redis = new Redis(redisUrl || 'redis://localhost:6379');

export async function withCache<T>(
  key: string,
  ttlSeconds: number,
  fetcher: () => Promise<T>
): Promise<T> {
  try {
    const cached = await redis.get(key);
    if (cached) {
      return JSON.parse(cached) as T;
    }
  } catch (error) {
    console.error(`Redis get error for key ${key}:`, error);
    // If Redis fails, gracefully fall back to fetching data directly
  }

  const data = await fetcher();

  try {
    await redis.setex(key, ttlSeconds, JSON.stringify(data));
  } catch (error) {
    console.error(`Redis setex error for key ${key}:`, error);
  }

  return data;
}
