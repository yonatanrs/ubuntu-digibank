CREATE TABLE payment_status_history (
  id UUID PRIMARY KEY,
  payment_id UUID NOT NULL REFERENCES payment(id),
  from_status VARCHAR(32),
  to_status VARCHAR(32) NOT NULL,
  reason VARCHAR(100) NOT NULL,
  provider_id VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_payment_history ON payment_status_history(payment_id, created_at);

