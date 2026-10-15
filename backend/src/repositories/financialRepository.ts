import { query, withTransaction } from '../config/db.js';

export type BillRecord = {
  id: string;
  community_id: string;
  flat_id: string | null;
  bill_type: string;
  amount: string;
  currency: string;
  period_start: string;
  period_end: string;
  due_at: string;
  status: 'OPEN' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'VOID';
  created_at: string;
};

export type PaymentRecord = {
  id: string;
  community_id: string;
  bill_id: string | null;
  payer_user_id: string;
  flat_id: string | null;
  amount: string;
  currency: string;
  payment_method: string | null;
  reference: string | null;
  status: 'PENDING' | 'PAID' | 'REJECTED' | 'PARTIALLY_PAID' | 'REVERSED';
  visibility: 'PRIVATE_TO_OWNER' | 'AUTHORIZED_HOUSEHOLD_ONLY' | 'COMMUNITY_SUMMARY_ONLY' | 'RESIDENT_VISIBLE' | 'ADMIN_ONLY';
  evidence_ref: string | null;
  created_at: string;
  updated_at: string;
  verified_by_user_id: string | null;
  verified_at: string | null;
  rejected_by_user_id: string | null;
  rejected_at: string | null;
  reversal_reason: string | null;
};

export async function createBill(input: {
  communityId: string;
  billType: string;
  amount: string;
  currency?: string;
  dueAt: string;
  periodStart: string;
  periodEnd: string;
  flatId?: string | null;
  status?: BillRecord['status'];
}): Promise<BillRecord> {
  const result = await query<BillRecord>(
    `INSERT INTO bills (community_id, flat_id, bill_type, amount, currency, period_start, period_end, due_at, status)
     VALUES ($1, $2, $3, $4::numeric, $5, $6, $7, $8, $9)
     RETURNING id, community_id, flat_id, bill_type, amount, currency, period_start, period_end, due_at, status, created_at`,
    [
      input.communityId,
      input.flatId ?? null,
      input.billType,
      input.amount,
      input.currency ?? 'INR',
      input.periodStart,
      input.periodEnd,
      input.dueAt,
      input.status ?? 'OPEN'
    ]
  );

  return result.rows[0];
}

export async function listBillsForCommunity(communityId: string): Promise<BillRecord[]> {
  const result = await query<BillRecord>(
    `SELECT id, community_id, flat_id, bill_type, amount, currency, period_start, period_end, due_at, status, created_at
     FROM bills
     WHERE community_id = $1
     ORDER BY due_at DESC, id DESC`,
    [communityId]
  );

  return result.rows;
}

export async function getBillById(communityId: string, billId: string): Promise<BillRecord | null> {
  const result = await query<BillRecord>(
    `SELECT id, community_id, flat_id, bill_type, amount, currency, period_start, period_end, due_at, status, created_at
     FROM bills
     WHERE community_id = $1 AND id = $2
     LIMIT 1`,
    [communityId, billId]
  );

  return result.rows[0] ?? null;
}

export async function updateBillStatus(
  communityId: string,
  billId: string,
  status: BillRecord['status'],
  dueAt?: string | null
): Promise<BillRecord | null> {
  const result = await query<BillRecord>(
    `UPDATE bills
     SET status = $3,
         due_at = COALESCE($4, due_at),
         updated_at = NOW()
     WHERE community_id = $1 AND id = $2
     RETURNING id, community_id, flat_id, bill_type, amount, currency, period_start, period_end, due_at, status, created_at`,
    [communityId, billId, status, dueAt ?? null]
  );

  return result.rows[0] ?? null;
}

export async function listPaymentsForCommunity(communityId: string): Promise<PaymentRecord[]> {
  const result = await query<PaymentRecord>(
    `SELECT id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason
     FROM payment_records
     WHERE community_id = $1
     ORDER BY created_at DESC, id DESC`,
    [communityId]
  );

  return result.rows;
}

export async function getOutstandingBalanceForBill(billId: string): Promise<{ billId: string; totalAmount: string; paidAmount: string; outstandingAmount: string }> {
  const result = await query<{
    bill_id: string;
    total_amount: string;
    paid_amount: string;
    outstanding_amount: string;
  }>(
    `SELECT
       b.id AS bill_id,
       b.amount AS total_amount,
       COALESCE(SUM(pr.amount) FILTER (WHERE pr.status = 'PAID' OR pr.status = 'PARTIALLY_PAID'), '0')::numeric(18,2) AS paid_amount,
       (b.amount - COALESCE(SUM(pr.amount) FILTER (WHERE pr.status = 'PAID' OR pr.status = 'PARTIALLY_PAID'), '0'))::numeric(18,2) AS outstanding_amount
     FROM bills b
     LEFT JOIN payment_records pr
       ON pr.bill_id = b.id
     WHERE b.id = $1
     GROUP BY b.id, b.amount`,
    [billId]
  );

  const row = result.rows[0];
  if (!row) {
    throw new Error('Bill not found.');
  }

  return {
    billId: row.bill_id,
    totalAmount: row.total_amount,
    paidAmount: row.paid_amount,
    outstandingAmount: row.outstanding_amount
  };
}

export async function createPayment(input: {
  communityId: string;
  billId: string | null;
  payerUserId: string;
  flatId?: string | null;
  amount: string;
  currency?: string;
  paymentMethod?: string | null;
  reference?: string | null;
  evidenceRef?: string | null;
  visibility?: PaymentRecord['visibility'];
}): Promise<PaymentRecord> {
  const amount = Number(input.amount);
  if (!Number.isFinite(amount) || amount <= 0) {
    throw new Error('Payment amount must be greater than zero.');
  }

  const result = await query<PaymentRecord>(
    `INSERT INTO payment_records (
       community_id,
       bill_id,
       payer_user_id,
       flat_id,
       amount,
       currency,
       payment_method,
       reference,
       status,
       visibility,
       evidence_ref
     ) VALUES ($1, $2, $3, $4, $5::numeric, $6, $7, $8, 'PENDING', $9, $10)
     RETURNING id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason`,
    [
      input.communityId,
      input.billId ?? null,
      input.payerUserId,
      input.flatId ?? null,
      input.amount,
      input.currency ?? 'INR',
      input.paymentMethod ?? null,
      input.reference ?? null,
      input.visibility ?? 'PRIVATE_TO_OWNER',
      input.evidenceRef ?? null
    ]
  );

  return result.rows[0];
}

export async function listPaymentsForUser(userId: string, communityId: string): Promise<PaymentRecord[]> {
  const result = await query<PaymentRecord>(
    `SELECT id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason
     FROM payment_records
     WHERE payer_user_id = $1 AND community_id = $2
     ORDER BY created_at DESC, id DESC`,
    [userId, communityId]
  );

  return result.rows;
}

export async function getPaymentById(communityId: string, paymentId: string): Promise<PaymentRecord | null> {
  const result = await query<PaymentRecord>(
    `SELECT id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason
     FROM payment_records
     WHERE community_id = $1 AND id = $2
     LIMIT 1`,
    [communityId, paymentId]
  );

  return result.rows[0] ?? null;
}

export async function verifyPayment(input: {
  communityId: string;
  paymentId: string;
  verifiedByUserId: string;
  reason?: string | null;
}): Promise<PaymentRecord | null> {
  return withTransaction(async (client) => {
    const paymentQuery = await client.query<PaymentRecord>(
      `SELECT id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason
       FROM payment_records
       WHERE community_id = $1 AND id = $2
       FOR UPDATE`,
      [input.communityId, input.paymentId]
    );

    const payment = paymentQuery.rows[0];
    if (!payment) {
      return null;
    }

    if (payment.status !== 'PENDING') {
      throw new Error('Payment cannot be verified because it is not pending.');
    }

    const updated = await client.query<PaymentRecord>(
      `UPDATE payment_records
       SET status = 'PAID',
           verified_by_user_id = $3,
           verified_at = NOW(),
           rejected_by_user_id = NULL,
           rejected_at = NULL,
           updated_at = NOW()
       WHERE id = $2 AND community_id = $1
       RETURNING id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason`,
      [input.communityId, payment.id, input.verifiedByUserId]
    );

    await client.query(
      `INSERT INTO payment_verification_events (community_id, payment_id, action, actor_user_id, reason, correlation_id)
       VALUES ($1, $2, 'VERIFIED', $3, $4, gen_random_uuid())`,
      [input.communityId, payment.id, input.verifiedByUserId, input.reason ?? null]
    );

    if (payment.bill_id) {
      const totalPaid = await client.query<{ total: string }>(
        `SELECT COALESCE(SUM(amount) FILTER (WHERE status IN ('PAID', 'PARTIALLY_PAID')), '0')::numeric(18,2) AS total
         FROM payment_records
         WHERE bill_id = $1`,
        [payment.bill_id]
      );

      const billAmount = await client.query<{ amount: string }>(
        `SELECT amount FROM bills WHERE id = $1 FOR UPDATE`,
        [payment.bill_id]
      );

      if (billAmount.rows[0]) {
        const nextStatus = Number(totalPaid.rows[0].total) >= Number(billAmount.rows[0].amount)
          ? 'PAID'
          : 'PARTIALLY_PAID';

        await client.query(
          `UPDATE bills SET status = $1 WHERE id = $2`,
          [nextStatus, payment.bill_id]
        );
      }
    }

    return updated.rows[0] ?? null;
  });
}

export async function rejectPayment(input: {
  communityId: string;
  paymentId: string;
  rejectedByUserId: string;
  reason?: string | null;
}): Promise<PaymentRecord | null> {
  return withTransaction(async (client) => {
    const paymentQuery = await client.query<PaymentRecord>(
      `SELECT id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason
       FROM payment_records
       WHERE community_id = $1 AND id = $2
       FOR UPDATE`,
      [input.communityId, input.paymentId]
    );

    const payment = paymentQuery.rows[0];
    if (!payment) {
      return null;
    }

    if (payment.status === 'PAID' || payment.status === 'REVERSED') {
      throw new Error('Only pending payments may be rejected.');
    }

    const updated = await client.query<PaymentRecord>(
      `UPDATE payment_records
       SET status = 'REJECTED',
           rejected_by_user_id = $3,
           rejected_at = NOW(),
           updated_at = NOW(),
           verified_by_user_id = NULL,
           verified_at = NULL
       WHERE id = $2 AND community_id = $1
       RETURNING id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason`,
      [input.communityId, payment.id, input.rejectedByUserId]
    );

    await client.query(
      `INSERT INTO payment_verification_events (community_id, payment_id, action, actor_user_id, reason, correlation_id)
       VALUES ($1, $2, 'REJECTED', $3, $4, gen_random_uuid())`,
      [input.communityId, payment.id, input.rejectedByUserId, input.reason ?? null]
    );

    return updated.rows[0] ?? null;
  });
}

export async function reversePayment(input: {
  communityId: string;
  paymentId: string;
  reversedByUserId: string;
  reason?: string | null;
}): Promise<PaymentRecord | null> {
  return withTransaction(async (client) => {
    const paymentQuery = await client.query<PaymentRecord>(
      `SELECT id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason
       FROM payment_records
       WHERE community_id = $1 AND id = $2
       FOR UPDATE`,
      [input.communityId, input.paymentId]
    );

    const payment = paymentQuery.rows[0];
    if (!payment) {
      return null;
    }

    if (payment.status !== 'PAID') {
      throw new Error('Only verified payments may be reversed.');
    }

    const updated = await client.query<PaymentRecord>(
      `UPDATE payment_records
       SET status = 'REVERSED',
           reversal_reason = $3,
           updated_at = NOW()
       WHERE id = $2 AND community_id = $1
       RETURNING id, community_id, bill_id, payer_user_id, flat_id, amount, currency, payment_method, reference, status, visibility, evidence_ref, created_at, updated_at, verified_by_user_id, verified_at, rejected_by_user_id, rejected_at, reversal_reason`,
      [input.communityId, payment.id, input.reason ?? null]
    );

    await client.query(
      `INSERT INTO payment_verification_events (community_id, payment_id, action, actor_user_id, reason, correlation_id)
       VALUES ($1, $2, 'REVERSED', $3, $4, gen_random_uuid())`,
      [input.communityId, payment.id, input.reversedByUserId, input.reason ?? null]
    );

    return updated.rows[0] ?? null;
  });
}
