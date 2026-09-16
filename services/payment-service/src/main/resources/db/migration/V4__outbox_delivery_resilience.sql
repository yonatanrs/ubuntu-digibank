ALTER TABLE outbox_event ADD COLUMN publish_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE outbox_event ADD COLUMN next_attempt_at TIMESTAMPTZ;
UPDATE outbox_event SET next_attempt_at = created_at WHERE next_attempt_at IS NULL;
ALTER TABLE outbox_event ALTER COLUMN next_attempt_at SET NOT NULL;
ALTER TABLE outbox_event ADD COLUMN last_error VARCHAR(500);
ALTER TABLE outbox_event ADD COLUMN dead_lettered_at TIMESTAMPTZ;

DROP INDEX IF EXISTS ix_outbox_claim;
CREATE INDEX ix_outbox_claim
  ON outbox_event(published_at, dead_lettered_at, next_attempt_at, claimed_at, created_at);

