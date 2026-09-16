# Delivery and Governance Roadmap

## Delivery model

Organise teams around business capabilities: Customer/KYC, Accounts/Ledger, Payments, Cards, Lending, Financial Crime, Data/Reporting and Platform/SRE. A small Architecture Enablement group maintains principles, reference patterns and fitness functions without becoming a delivery bottleneck.

| Phase | Duration | Outcome | Exit gate |
|---|---:|---|---|
| 0. Discovery and licence alignment | 6–8 weeks | Business case, capability map, regulatory plan, threat model, NFRs | Sponsor, Risk, Compliance and Architecture approval |
| 1. Foundation | 8–12 weeks | Landing zone, CI/CD, CIAM, secrets, observability, Kafka, data platform baseline | Security tests and DR foundation proven |
| 2. Minimum viable bank | 4–6 months | Onboarding, account, ledger, internal transfer, operations | Balanced ledger, reconciliation, support and audit evidence |
| 3. External payments | 3–5 months | PayShap, EFT/RTC adapters, beneficiaries, notifications | Scheme certification and operational readiness |
| 4. Cards and lending | 4–6 months | Card issuing/processing integration, savings and credit | PCI and credit-risk gates |
| 5. Scale and optimise | Continuous | Ecosystem APIs, advanced fraud, cost/latency optimisation | SLO and business KPI improvements |

## Quality gates

- Architecture: ADR, threat model, data classification, API/event review and failure-mode analysis.
- Engineering: unit, component, contract, integration, performance, security and resilience tests.
- Release: signed artifact, SBOM, migration rehearsal, rollback/roll-forward, feature flag and support handover.
- Financial: ledger invariant tests, scheme reconciliation, settlement control and finance sign-off.
- Operational: SLO/dashboard, alert, runbook, capacity, backup restore and game-day evidence.

## Delivery risks

| Risk | Mitigation |
|---|---|
| Scheme/regulatory lead time | Engage in discovery; certification plan and simulators early |
| Over-fragmented microservices | Domain maturity gate; deploy modular components together initially |
| Vendor lock-in | Ports/adapters, canonical models, data export, contractual exit and regular recovery tests |
| Fraud friction harms inclusion | Tiered limits, step-up rather than blanket denial, human review and bias monitoring |
| Event/data sprawl | Schema registry, ownership, compatibility policy, catalogue, retention and PII linting |

