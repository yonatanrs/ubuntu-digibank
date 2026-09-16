# Solution Architect Interview Playbook

## 30-second opening

“I designed Ubuntu Digital Bank as a South African, event-driven banking platform. The design starts from financial invariants and local payment rails rather than from technology. Spring Boot services own clear domains, Kafka carries durable business events, PostgreSQL provides transactional integrity, and React powers operations. The architecture separates payment orchestration from the immutable double-entry ledger, applies zero trust and POPIA/FICA controls by design, and defines capability-specific SLO and recovery objectives.”

## 15-minute walkthrough

1. **Business and constraints (2 min):** retail/SME inclusion, instant low-cost payments, regulatory evidence and trusted operations.
2. **Context and domains (3 min):** channels → edge → domain services → ledger → South African rails; explain ownership boundaries.
3. **Critical payment journey (3 min):** idempotency, fraud decision, reserve, rail submission, settlement, outbox event and reconciliation.
4. **Security/compliance (2 min):** identity, device trust, tokenisation, least privilege, purpose limitation and evidence.
5. **Reliability (2 min):** bulkheads, degraded modes, SLOs, DR fencing, backups and reconciliation.
6. **Delivery (2 min):** thin vertical slice, platform paved road, contract tests, progressive delivery and regulatory gates.
7. **Trade-off close (1 min):** identify what you would validate through discovery and load/resilience testing.

## Likely questions

**Why microservices?** Domain and regulatory boundaries need independent scaling and ownership, but I would not create one service per noun. Early delivery can deploy several modules together until team and operational maturity justify separation.

**Why Kafka and REST together?** REST is appropriate for an immediate command outcome such as authorization. Kafka is appropriate for durable propagation, asynchronous work and replay. Choosing only one would distort business semantics.

**How do you guarantee exactly-once payment?** I do not claim global exactly-once delivery. I achieve exactly-once business effect using idempotency keys, unique constraints, state-transition guards, transactional outbox, idempotent consumers and reconciliation.

**How does this scale across multiple pods?** A database compare-and-set changes `RECEIVED` to `DISPATCHING`, so only one pod wins a payment. Outbox workers claim rows with `FOR UPDATE SKIP LOCKED`. Kafka delivery remains deliberately at-least-once, so consumers deduplicate by `eventId`.

**What if a pod crashes during external submission?** `DISPATCHING` is treated as potentially uncertain rather than automatically retried. Recovery performs provider inquiry and reconciliation. This sacrifices some automatic availability to protect financial correctness.

**What happens when Kafka is down?** The payment transaction commits with an outbox row. Relay resumes publishing later. The synchronous financial decision remains authoritative, while lag thresholds trigger operational controls.

**Does the outbox transaction wait for Kafka?** No. A short PostgreSQL transaction leases due rows with `SKIP LOCKED`, then commits. Kafka I/O occurs outside that transaction. Each event is acknowledged in its own transaction or receives backoff/dead-letter metadata, so a poison event does not roll back the entire batch.

**What if two identical requests arrive at precisely the same time?** A unique idempotency key remains the final arbiter. Creation executes in an isolated transaction; the losing insert reloads the committed winner and returns it only when the request fingerprint matches. Otherwise it returns a conflict.

**What happens when PayShap is unavailable?** New submissions are blocked or routed only when policy, consent and scheme rules allow an alternative. Pending instructions are not blindly duplicated. Status inquiry and reconciliation establish the authoritative outcome.

**Do you need third parties?** Yes, for scheme connectivity, identity/liveness, screening data, credit bureaux, card processing, communications and selected security services. I isolate them behind bank-owned ports. The bank retains payment state, ledger, consent, risk policy, cases, reconciliation and audit evidence.

**Why not always fail over to a second vendor?** Failover is safe only when the first provider definitely did not accept the instruction. A timeout after sending is an unknown outcome; automatic resubmission could duplicate money movement. I hold it for inquiry and reconciliation.

**How do you avoid ledger split brain in DR?** Only one fenced writer has authority per account partition. Promotion requires loss-of-quorum evidence, fencing token rotation and reconciliation before channels reopen.

**What would you build first?** Onboarding, account, ledger and internal transfer as one thin vertical slice; then PayShap, operations and reconciliation. This validates identity, money movement and operating model early.

## Honest boundaries

- Scheme interface details and certification depend on the sponsoring/participant bank and providers.
- Regulatory interpretations require South African legal/compliance approval.
- Capacity figures require measured workloads; do not invent TPS.
- The demo vertical slice demonstrates patterns, not every target-state service.
