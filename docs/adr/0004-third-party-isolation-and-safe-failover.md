# ADR-0004: Third-party isolation and safe failover

- Status: Accepted
- Context: Critical bank capabilities depend on external providers, but vendor models and failure semantics must not leak into domains or create duplicate payments.
- Decision: All providers implement bank-owned ports through anti-corruption adapters. Routing distinguishes definitive pre-acceptance unavailability from indeterminate outcomes. Only the former permits failover; the latter requires status inquiry and reconciliation.
- Consequences: Providers are replaceable and failure handling is financially safe. Adapters, provider scorecards, simulators, contract tests and reconciliation add deliberate operational cost.

