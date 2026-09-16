# ADR-0005: Atomic work claims and at-least-once events

- Status: Accepted
- Context: Multiple service replicas may concurrently discover the same payment or outbox row. External payment and Kafka delivery cannot be made globally atomic with the service database.
- Decision: Claim payment dispatch with a compare-and-set database update. Claim outbox batches using PostgreSQL `FOR UPDATE SKIP LOCKED`. Use Kafka idempotent production and require consumers to deduplicate by `eventId`. Treat crash-after-send as possible duplicate delivery and reconcile external payment outcomes.
- Consequences: Horizontal replicas cannot intentionally process the same current work item. At-least-once delivery remains explicit; downstream idempotency and reconciliation are mandatory. Stale claim leases and `DISPATCHING` payments require operational monitoring.

