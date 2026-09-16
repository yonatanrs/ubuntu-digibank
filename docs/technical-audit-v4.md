# End-to-End Technical Audit — V4

## Executive result

V4 closes the highest-impact correctness and operability gaps found while tracing API request → database commit → dispatch → provider → state transition → outbox → Kafka. The project remains a portfolio reference vertical slice; account validation, fraud authorization, ledger reservation and scheme-certified adapters remain target-state integration work.

## Findings and remediation

| Severity | Finding | V4 remediation | Residual consideration |
|---|---|---|---|
| Critical | Concurrent requests using one idempotency key could race and expose a database constraint error | Creation runs in an isolated transaction; loser reloads winner and verifies request fingerprint | Load-test against production PostgreSQL settings |
| High | Outbox held a database transaction while waiting for Kafka | Short claim transaction followed by network I/O and per-event completion transaction | Crash after Kafka send can redeliver; consumer deduplication remains mandatory |
| High | One poison event could roll back/block the whole batch | Per-event retry, exponential backoff, attempt count and database dead-letter state | Add operator replay API with maker-checker authorization |
| High | Broad runtime exception could incorrectly mark an accepted payment as rail unavailable | Provider errors classified separately; persistence failure leaves `DISPATCHING` for reconciliation | Alert on aged `DISPATCHING` state |
| Medium | API/event traces lacked a stable cross-service identifier | Sanitised `X-Correlation-ID`, MDC propagation and event envelope metadata | Configure log encoder and tracing backend in deployment environment |
| Medium | Application layer depended on API DTO | Added application command and architecture fitness test | Extend rules as modules grow |
| Medium | No repeatable hardened build pipeline | Multi-stage non-root image and CI build/test/SBOM/vulnerability gates | Pin actions by commit SHA in regulated production repositories |

## Traced flows

### Create payment

1. Edge authenticates caller and validates `payments.write` scope in production.
2. Correlation filter accepts only a safe identifier or generates a UUID.
3. Request validation rejects missing IDs, non-ZAR currency, invalid scale or excessive references.
4. Service computes request fingerprint and resolves an existing idempotent result.
5. New payment, audit history and outbox event commit atomically.
6. Concurrent insert loser reloads the committed winner; mismatched payload returns HTTP 409.

### Dispatch payment

1. Worker lists `RECEIVED` candidates.
2. Compare-and-set atomically claims one payment as `DISPATCHING`.
3. Router chooses eligible providers by priority.
4. Pre-send unavailability permits controlled failover.
5. Unknown acceptance becomes `PENDING_CONFIRMATION`; no duplicate submission occurs.
6. Accepted/rejected outcome, audit history and status event commit together.

### Publish event

1. Worker leases due outbox rows using `FOR UPDATE SKIP LOCKED` in a short transaction.
2. Kafka publish happens after database transaction completion.
3. Success is marked in its own transaction.
4. Failure increments attempts and schedules exponential backoff.
5. After the configured maximum, the row is retained as dead-letter evidence.

## Open production work

These items intentionally require a real bank/provider environment:

- Account ownership, balance/limit validation and ledger hold before external dispatch.
- Real-time fraud/AML authorization with latency and degraded-mode policy.
- Scheme-certified PayShap/EFT/RTC/SAMOS adapters and status inquiry.
- Consumer deduplication store and contract tests across every downstream service.
- Reconciliation file/message ingestion with maker-checker exception resolution.
- Performance, chaos, penetration, DR and regulatory operational-readiness evidence.

