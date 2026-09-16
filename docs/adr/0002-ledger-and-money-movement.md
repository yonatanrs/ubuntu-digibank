# ADR-0002: Dedicated immutable double-entry ledger

- Status: Accepted
- Context: Payment workflows change frequently; accounting integrity must remain stable and independently controlled.
- Decision: Payment orchestration owns instruction state while the ledger owns journals, postings, holds and balances. Posted entries are immutable and corrections are compensating entries.
- Consequences: Stronger audit and reconciliation at the cost of an additional highly critical service dependency. Posting commands require idempotency and balanced-entry validation.

