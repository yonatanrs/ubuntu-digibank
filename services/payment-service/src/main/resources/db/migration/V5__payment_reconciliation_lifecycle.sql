ALTER TABLE payment ADD COLUMN inquiry_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE payment ADD COLUMN next_inquiry_at TIMESTAMPTZ;

CREATE INDEX ix_payment_reconciliation
  ON payment(status, next_inquiry_at, updated_at)
  WHERE status = 'PENDING_CONFIRMATION';

