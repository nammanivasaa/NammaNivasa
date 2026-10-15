import 'dotenv/config';
import { z } from 'zod';

const envSchema = z.object({
  PORT: z.coerce.number().int().positive().default(4000),
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  DATABASE_URL: z.string().trim().optional(),
  TEST_DATABASE_URL: z.string().trim().optional(),
  FIREBASE_PROJECT_ID: z.string().trim().optional(),
  FIREBASE_CLIENT_EMAIL: z.string().trim().optional(),
  FIREBASE_PRIVATE_KEY: z.string().optional(),
  LOG_LEVEL: z.string().default('info'),
  AUTH_MODE: z.enum(['firebase', 'test']).default('firebase')
});

export const env = envSchema.parse(process.env);

if (env.NODE_ENV === 'production' && env.AUTH_MODE === 'test') {
  throw new Error('Production builds cannot enable test authentication. Set AUTH_MODE=firebase and configure Firebase credentials.');
}

export function isTestAuthAllowed(): boolean {
  return env.NODE_ENV !== 'production' && env.AUTH_MODE === 'test';
}

export function getDatabaseUrl(): string {
  const selectedUrl = env.NODE_ENV === 'test' ? env.TEST_DATABASE_URL ?? env.DATABASE_URL : env.DATABASE_URL;

  if (!selectedUrl) {
    throw new Error(
      `Database configuration is missing for ${env.NODE_ENV}. Set DATABASE_URL for local/prod or TEST_DATABASE_URL for isolated test runs.`
    );
  }

  return selectedUrl;
}

export function getDatabaseConfigForEnvironment(targetEnv: 'development' | 'test' | 'production' = env.NODE_ENV): string {
  const selectedUrl = targetEnv === 'test'
    ? process.env.TEST_DATABASE_URL ?? process.env.DATABASE_URL
    : process.env.DATABASE_URL;

  if (!selectedUrl) {
    throw new Error(
      `Database configuration is missing for ${targetEnv}. Set DATABASE_URL for local/prod or TEST_DATABASE_URL for isolated test runs.`
    );
  }

  return selectedUrl;
}
