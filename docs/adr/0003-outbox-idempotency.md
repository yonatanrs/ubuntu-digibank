# ADR-0003: Transactional outbox and idempotent consumers

- Status: Accepted
- Context: A database update and Kafka publish cannot safely be treated as one distributed transaction.
- Decision: Persist aggregate state and an outbox message in one local transaction. A relay publishes events. Producers and consumers use stable event IDs, unique constraints and processed-message records.
- Consequences: At-least-once transport with exactly-once business effect. Monitoring and retention of outbox/consumer records are mandatory.

