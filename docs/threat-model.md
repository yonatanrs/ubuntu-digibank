# Threat Model

## Assets and trust boundaries

Critical assets are customer identity, authentication factors, payment instructions, ledger balances, cryptographic keys, card data, screening evidence and audit records. Trust boundaries exist at public channels, API edge, workforce access, service-to-service traffic, data stores, Kafka, third-party egress and administrative planes.

## Priority abuse cases

| Abuse case | Prevent | Detect | Respond |
|---|---|---|---|
| Account takeover and beneficiary change | Passkey/MFA, device binding, step-up, cooling period | Device/behaviour anomaly and change alerts | Session/token revocation and payment hold |
| Payment replay or duplicate submission | Idempotency, signed command, timestamp/nonce and state claim | Duplicate-key and repeated-device telemetry | Return original result; investigate abuse pattern |
| Provider timeout creates double payment | Safe failure classification and no blind retry | Aged unknown-state and reconciliation alerts | Inquiry, hold, maker-checker resolution |
| Privileged insider reads customer data | Purpose-based access, masking, JIT/PAM and segregation | Immutable access audit and unusual-query detection | Revoke, preserve evidence and breach workflow |
| Kafka event spoofing/tampering | mTLS/SASL, ACL, schema governance and network segmentation | Producer identity and schema rejection metrics | Quarantine producer and replay trusted events |
| Dependency/container compromise | Locked dependencies, SBOM, signing and admission policy | SCA/image/runtime detection | Block release, revoke artifact and rebuild |
| Data exfiltration through logs/events | PII linting, allowlisted fields and secure logging | DLP and unusual egress detection | Containment, key/token rotation and POPIA process |
| DR split brain corrupts balances | Fenced single writer and quorum-based promotion | Dual-writer/position divergence alarm | Stop financial writes and reconcile before reopen |

## Security validation

- Threat-model review for every material flow or provider change.
- SAST, SCA, secret, IaC and container scanning in CI.
- API authorization and object-level access tests.
- Penetration and abuse testing before launch and after significant change.
- Tabletop exercises for account takeover, provider compromise, data breach and settlement mismatch.

