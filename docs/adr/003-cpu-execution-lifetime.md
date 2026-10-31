# ADR-003: Acknowledge actual CPU execution exit

## Status
Accepted

## Context
Architecture follow-up emustudio/emuStudio#406 and emuLib#32 concern CPU lifecycle ownership.
Cancelling an execution Future completes that Future immediately, even when an in-flight guest
instruction or device operation has not yet returned. Waiting only for that Future allowed pause,
stop and reset to report completion while execution still touched CPU or device state.

## Decision
AbstractCPU retains execution ownership. Its stop watcher waits for a barrier on the CPU executor
after completion, cancellation or failure before publishing a stopped state or allowing a lifecycle
request to reset state. The same barrier works when cancellation happens before execution starts.
Lifecycle requests wait for completion despite caller interruption, then restore the interrupt flag.
No public interface or plugin implementation change is required.

## Consequences
Stopped CPU notifications and completed pause, stop and reset calls now acknowledge actual execution
exit. Plugins must still respond to interruption; an operation that never returns prevents completion
rather than exposing concurrently mutable state as paused. Tests hold an in-flight operation with
latches to verify lifecycle ordering and interrupt preservation.
