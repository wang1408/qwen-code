# Managed Agent stream and creation simplification

[English](2026-10-31-managed-agent-stream-and-creation-simplification.md) | [简体中文](2026-10-31-managed-agent-stream-and-creation-simplification.zh-CN.md)

Status: implemented; focused local verification passed. Full CI acceptance is pending.

## Problem and scope

The Public and WebShell Session event streams duplicate the same replay, live delivery, heartbeat, grant recheck and termination loop. Workspace Session creation has nine repository overloads, ending in twenty positional arguments, with default-argument forwarding repeated in the contract and JDBC implementation.

Make these two changes separately within the existing Maven boundaries. Preserve observable behavior, HTTP/OpenAPI models, persisted bytes, SQL and admission order. Runtime provisioners, dispatch leases, recovery state machines and digest helpers are outside this change.

## Stream loop

Keep both public entry points and their synchronous readable-Session admission. Delegate to one private loop with event and resynchronization payload conversion functions. Event identifiers, types and deletion detection come from the original event record. Keep the existing Public seconds and WebShell milliseconds projections and their distinct resynchronization records.

Keep subscription-before-replay ordering, pagination, overflow reconciliation, heartbeat scheduling, permission-recheck timing, interruption handling and completion callbacks at their existing positions. The resynchronization frame must still have no SSE ID. Do not introduce a public streaming abstraction or change the event hub.

## Creation command

Introduce a contract-owned `WorkspaceSessionCommand` record carrying the twenty existing values. Keep one convenience constructor for the ten-field base form used throughout the fixtures; it supplies exactly the former defaults. The full constructor preserves nulls and collection references and performs no normalization or new validation.

Replace all nine `AgentStateStore` creation overloads with one command-taking method. Keep its explicit transactional override on `ManagedAgentStore`, using the existing dedicated transaction manager. Preserve the full transaction body and its check order. `ManagedAgentService` constructs one command, including the nullable worktree plan, instead of branching between nineteen- and twenty-argument calls. Migrate repository callers, fixtures and mock expectations together.

This changes the internal Java repository API: source callers must wrap the former arguments in the command, and compiled callers must rebuild. It does not change the public HTTP API, SDK request formats or database schema. Do not retain the old progressive overload chain as a compatibility layer.

## Validation and acceptance

Follow the module AGENTS.md shared ten-minute local Java verification budget, using JDK 21 and an isolated Maven repository with the matching SDK. Select stream permission/replay/resynchronization tests, creation/admission/skills/model/worktree consumers, and dedicated Spring transaction commit/rollback coverage. Preserve existing behavioral coverage while migrating call sites; replace the removed interface-default assertion with a real-store approval-negotiation check. Add targeted assertions for payload time units and command propagation where existing coverage is insufficient.

The creation API change must retain the real proxy's dedicated connection and rollback behavior. Any unavailable real MySQL check remains pending CI; H2 does not establish MySQL behavior. Run the API contract and surface gates to detect accidental wire changes. Shared-contract validation also includes the root build/typecheck requirements. Full Java, MySQL and Hosted acceptance stays in CI.

Acceptance requires one stream loop, one repository creation method, unchanged wire payloads and admission semantics, passing applicable focused checks, and two clean self-audit passes. Record failures, skips and pending CI work separately. No unresolved design questions remain.

## Local evidence

On macOS with JDK 21.0.8 and Maven 3.9.14, focused evidence covers 808 unit/H2 cases, including both API contract gates and transaction commit/rollback. The initial 753-case run had one failure from the removed interface-default assertion. After replacing that obsolete assertion, all 16 approval-selection cases passed; the other 738 passing cases remain applicable. Ten worktree store/service cases and forty-four Session row-lock ordering checks also passed in separate focused runs. Checkstyle passed across the reactor.

Five selected MySQL 8.4.11 cases passed: initial model/approval selection in three modes, case-sensitive Workspace actors and command keys, and cross-JVM replay of the original binding after default changes. The disposable server used `max_allowed_packet=134217728` and disabled binary logging; macOS native AIO settings differ from Linux CI. Its process and data directory were removed. An initial direct Failsafe invocation stopped at the reactor parent with no matching tests; the corrected lifecycle invocation and verification-module report check passed.

Root `npm run build` and `npm run typecheck` passed. These results and the 808-case/MySQL evidence were obtained before rebasing onto `1ccf0d43f3`. The rebase preserved every refactor source and test file unchanged. Because the new base adds configuration-metadata generation, reactor compilation, Checkstyle and 52 selected API-contract, streaming, creation/worktree and transaction-proxy cases were rerun and passed after the rebase; the broader checks were not repeated.

Full Java/SpotBugs, complete MySQL and Hosted/fault profiles remain pending CI. These local checks establish the selected paths only; they are not production or cross-platform acceptance.
