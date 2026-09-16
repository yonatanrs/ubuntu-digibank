# Architecture Traceability Matrix

| Requirement / risk | Design control | Implementation evidence | Verification evidence |
|---|---|---|---|
| Duplicate client retry | Idempotency key plus canonical fingerprint | `PaymentService`, unique `idempotency_key` | Concurrent PostgreSQL integration test |
| Concurrent payment dispatch | Compare-and-set claim | `PaymentRepository.claimForDispatch` | State-machine and architecture tests |
| Unknown provider outcome | No blind failover; pending confirmation | `PaymentRailPort`, `PaymentDispatchWorker` | Router tests and provider-outage runbook |
| Database/Kafka dual write | Transactional outbox | `JpaPaymentEventOutbox`, `outbox_event` | Migration validation and event contract |
| Multi-pod event publishing | Short leased claim using `SKIP LOCKED` | `OutboxClaimStore` | ADR-0005 and PostgreSQL query review |
| Poison event | Per-event backoff and dead-letter evidence | `OutboxDeliveryStore`, V4 migration | `OutboxEventTest` |
| Vendor lock-in | Bank-owned ports and canonical model | `PaymentRailPort`, `PaymentEventPort` | ADR-0004 and architecture fitness test |
| Unauthorized access | JWT scopes and stateless API | `SecurityConfiguration` | CI tests; production identity integration required |
| Cross-service investigation | Correlation/event envelope metadata | `CorrelationIdFilter`, event payload | OpenAPI/AsyncAPI contract review |
| Unauthorized state mutation | Explicit transition methods and optimistic version | `Payment` aggregate | `PaymentDomainTest` |
| Financial exception handling | Reconciliation and compensating entries | Reconciliation design and audit history | Operational exercise required |
| Supply-chain compromise | Reproducible build, SBOM and image scanning | Dockerfile and quality-gates workflow | CI pipeline evidence |
| Pod/node/zone loss | Replicas, PDB, topology spread and probes | Kubernetes manifests | Game day and DR evidence required |
| POPIA/FICA evidence | Minimisation, audit, KYC/AML boundaries and retention | Architecture/regulatory mapping | Compliance approval required |

The matrix separates implemented controls from controls that require a licensed-bank environment. This avoids presenting design intent as verified production evidence.

