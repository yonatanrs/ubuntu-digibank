CREATE TABLE payment (
  id UUID PRIMARY KEY,
  idempotency_key VARCHAR(100) NOT NULL,
  request_fingerprint VARCHAR(64) NOT NULL,
  debtor_account_id VARCHAR(255) NOT NULL,
  creditor_account_id VARCHAR(255) NOT NULL,
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  currency VARCHAR(3) NOT NULL CHECK (currency = 'ZAR'),
  rail VARCHAR(32) NOT NULL,
  reference VARCHAR(140) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_payment_idempotency UNIQUE (idempotency_key)
);

CREATE TABLE outbox_event (
  id UUID PRIMARY KEY,
  aggregate_id VARCHAR(255) NOT NULL,
  event_type VARCHAR(255) NOT NULL,
  payload TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  published_at TIMESTAMPTZ NULL
);

CREATE INDEX ix_outbox_unpublished ON outbox_event(created_at) WHERE published_at IS NULL;

