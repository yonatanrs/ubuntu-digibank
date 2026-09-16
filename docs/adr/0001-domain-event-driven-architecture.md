# ADR-0001: Domain-aligned services with an event backbone

- Status: Accepted
- Context: The bank needs independent delivery, auditable boundaries and integration with multiple asynchronous rails.
- Decision: Use domain-aligned Spring Boot services, synchronous command/query APIs and Kafka for durable business events. Services own their databases.
- Consequences: Teams gain autonomy and replayable integration. The platform must govern schemas, lag, idempotency, observability and eventual consistency. Small domains may deploy together until scale requires separation.

