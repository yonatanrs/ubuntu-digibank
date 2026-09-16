ALTER TABLE payment ADD COLUMN updated_at TIMESTAMPTZ;
UPDATE payment SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE payment ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE payment ADD COLUMN external_provider VARCHAR(100);
ALTER TABLE payment ADD COLUMN external_reference VARCHAR(255);
ALTER TABLE payment ADD COLUMN failure_code VARCHAR(100);
CREATE INDEX ix_payment_dispatch ON payment(status, created_at);

ALTER TABLE outbox_event ADD COLUMN claimed_by VARCHAR(100);
ALTER TABLE outbox_event ADD COLUMN claimed_at TIMESTAMPTZ;
CREATE INDEX ix_outbox_claim ON outbox_event(published_at, claimed_at, created_at);

