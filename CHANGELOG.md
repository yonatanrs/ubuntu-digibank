# Changelog

## V4 — End-to-end correctness and delivery hardening

- Fixed concurrent idempotency-key insert races with transaction isolation and winner reload.
- Removed Kafka network I/O from database transactions.
- Added per-event outbox retry, exponential backoff and dead-letter evidence.
- Corrected provider/persistence failure classification to protect payment state.
- Added correlation IDs and versioned event envelope metadata.
- Removed application dependency on API DTOs and added architecture fitness tests.
- Added non-root multi-stage container build and CI build/SBOM/vulnerability gates.
- Added a full trace-based technical audit with explicit residual production work.
- Updated the maintained Spring Boot 3.x line and current ArchUnit release after verification against official releases.

## V3 — Production-control hardening

- Connected bank-owned payment-rail ports to the executable payment lifecycle.
- Added atomic dispatch claims for safe horizontal scaling.
- Added explicit `DISPATCHING` and `PENDING_CONFIRMATION` states.
- Added immutable payment status history and lifecycle outbox events.
- Added multi-replica outbox claims with PostgreSQL `SKIP LOCKED`.
- Added GET payment-status API and aligned OpenAPI/AsyncAPI contracts.
- Added OAuth2/JWT production security with read/write scopes.
- Added production profile, zone spreading, graceful termination and HPA.
- Added reconciliation design and production-readiness review.

## V2 — Third-party safety

- Added bank-owned provider ports and local rail simulator.
- Added primary/secondary routing with safe failover semantics.
- Added vendor governance, provider-outage runbook and network policy.

## V1 — Architecture baseline

- Added South African digital-bank target architecture.
- Added Spring Boot payment vertical slice, Kafka outbox and React operations UI.
- Added OpenAPI, AsyncAPI, Kubernetes, SLOs, ADRs and interview playbook.
