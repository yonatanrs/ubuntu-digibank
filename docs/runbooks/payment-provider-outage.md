# Runbook: Payment Provider Outage

## Trigger

- Availability or latency SLO burn alert.
- Elevated submission errors or status inquiry failures.
- Reconciliation detects missing, duplicate or inconsistent outcomes.
- Provider or payment-scheme incident notification.

## Immediate actions

1. Declare incident severity from customer/settlement impact, not raw error count.
2. Freeze automatic failover for instructions with an indeterminate outcome.
3. Continue safe internal transfers and unaffected rails where policy permits.
4. Open provider incident channel and preserve correlation IDs, timestamps and external references.
5. Publish accurate in-app status; do not promise completion times not confirmed by the provider.

## Decision table

| Observation | Treatment |
|---|---|
| Connection refused before send | Eligible for controlled alternative-provider routing |
| HTTP/network timeout after send | `PENDING_CONFIRMATION`; status inquiry and reconciliation only |
| Explicit rejection | Final rejection or customer-correctable flow; no transparent failover |
| Provider accepted with reference | Track through the same provider/reference |
| Conflicting provider and ledger status | Stop settlement automation and escalate to Payments + Finance Control |

## Recovery

1. Perform status inquiry for every uncertain instruction.
2. Reconcile instruction, provider report, clearing/settlement position and ledger.
3. Apply compensating entries through approved four-eyes workflow where required.
4. Resume traffic gradually with synthetic payments and error-budget observation.
5. Notify affected customers and regulators according to approved legal/compliance decision.
6. Complete post-incident review, control evidence and vendor scorecard update.

