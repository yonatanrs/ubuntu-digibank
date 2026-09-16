# Production Readiness Review

No service may receive production traffic until every mandatory item has an accountable owner and evidence link.

## Architecture and ownership

- [ ] Business owner, service owner, technical owner and 24×7 support path named.
- [ ] C4 context/container/component views and ADRs reviewed.
- [ ] Upstream/downstream dependencies, data classes and failure modes recorded.
- [ ] Capacity model, quotas and cost envelope agreed.
- [ ] Deprecation, data migration and vendor exit paths documented.

## Financial correctness

- [ ] Idempotency tested under retry, timeout and concurrent requests.
- [ ] Payment state-machine illegal transitions rejected.
- [ ] Ledger postings balanced and immutable; compensating workflow approved.
- [ ] Provider, clearing, settlement and ledger reconciliation exercised.
- [ ] Indeterminate outcomes remain pending until inquiry/reconciliation resolves them.
- [ ] Duplicate event/payment simulation proves exactly-once business effect.

## Security, privacy and compliance

- [ ] OAuth issuer, audiences, scopes and machine identities verified.
- [ ] Threat model and abuse cases reviewed; penetration findings closed or risk accepted.
- [ ] Secrets use managed storage and rotation; no production defaults exist.
- [ ] POPIA purpose, minimisation, retention, operator and breach processes approved.
- [ ] FICA evidence, screening disposition and audit access demonstrated.
- [ ] PCI scope and card-data flows verified where applicable.
- [ ] SBOM, signed image, provenance and vulnerability policy pass.

## Reliability and operations

- [ ] SLI/SLO and error-budget policy have business approval.
- [ ] Dashboard covers technical and business outcomes without exposing PII.
- [ ] Alerts are actionable and linked to a tested runbook.
- [ ] Load, soak, spike, dependency-failure and regional-recovery tests pass.
- [ ] Backups restore successfully; RTO/RPO evidence meets capability tier.
- [ ] Graceful shutdown, pod disruption, autoscaling and zone spread tested.
- [ ] Reconciliation and customer/support procedures work during provider outages.

## Release governance

- [ ] Contract compatibility and database migration/rollback strategy verified.
- [ ] Progressive delivery, feature flags and automated rollback thresholds configured.
- [ ] Segregation of duties and four-eyes approval enforced for high-risk changes.
- [ ] Go-live, hypercare, incident command and executive/regulatory communication prepared.

