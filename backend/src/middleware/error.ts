import type { ErrorRequestHandler, NextFunction, Request, Response } from 'express';

export class HttpError extends Error {
  constructor(
    public statusCode: number,
    public code: string,
    message: string
  ) {
    super(message);
  }
}

export function notFoundHandler(_req: Request, res: Response): void {
  res.status(404).json({
    error: { code: 'NOT_FOUND', message: 'Resource not found.' }
  });
}

export const errorHandler: ErrorRequestHandler = (error, _req, res, _next: NextFunction) => {
  const statusCode = error instanceof HttpError ? error.statusCode : 500;
  const code = error instanceof HttpError ? error.code : 'SERVER_ERROR';
  const message = error instanceof Error ? error.message : 'Unexpected server error.';

  res.status(statusCode).json({
    error: { code, message },
    meta: { timestamp: new Date().toISOString() }
  });
};
