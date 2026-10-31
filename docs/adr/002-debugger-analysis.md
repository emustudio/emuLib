# ADR-002: Optional debugging ports and host analysis workspace

## Status
Accepted

## Context
Issue #454 adds memory analysis, annotations, portable watches/watchpoints, flow navigation and execution history.
Existing plugins expose instruction text and UI refresh notifications, which cannot reliably observe guest accesses.

## Decision
emuLib owns optional DebugCPU, immutable instruction/access events, register descriptors, and structured InstructionFlow.
CPU.getDebugger(), Disassembler.getFlow(), CPU.getBreakpoints() and memory-bank inspection have defaults for old plugins.
DebugProbe provides a reusable CPU-local memory adapter. It observes guest accesses, including fetches and attempted writes,
without extra reads or dependence on memory-change notifications. Debug inspection retains the original memory context.
8080/Z80 engines keep that original memory reference while debugging is disabled. Listener changes and live inspection
require paused execution. Register edits notify the owning CPU so existing status views refresh. Listener callbacks must not call CPU lifecycle methods.

CPU plugins own opcode semantics. A small 8080/Z80 flow adapter adds direct targets, conditional fall-through, calls,
returns and unresolved indirect jumps without parsing mnemonic strings. Edigen still owns generated disassembly.
No generated source is edited; see the separate generator assessment before extending the DSL.

emuStudio owns the analysis workspace, captures, searches, user annotations, rules, watches, history and scripts.
Debugger rules/watches and ordinary breakpoints use an atomic versioned properties companion to the computer configuration.
User annotations use a separate companion bound to the full capture SHA-256, layout and physical bank/address. Compiler
source-position annotations remain independent. Common-region addresses normalize to bank zero. Source rules resolve only
current source annotations; invalidated mappings become ineffective rather than using stale addresses.

Ghidra interoperability uses raw bank binaries and versioned metadata files plus an external Java script. No Ghidra runtime
or scripting-language dependency is added to emuStudio. History is bounded and read-only; Move PC Back remains PC movement.
True reverse execution requires a later coordinated checkpoint/input-replay contract (see assessment).

## Consequences
Rebuild emuLib and participating plugins together to use new features. Existing plugins retain their basic debugger through
default methods. CPU instrumentation allocates no events/register snapshots while disabled. Enabled recording captures
registers and guest accesses at instruction boundaries and may reduce emulation throughput.

Banked memory providers must implement readBank without selecting a bank or introducing guest timing effects. Spectrum bus
inspection delegates directly to underlying memory. Generic defaults cannot promise side-effect-free inspection of arbitrary
memory-mapped devices; such providers must supply appropriate inspection semantics.

Instruction graphs and reference searches are bounded static analyses; indirect targets and undecided code/data remain
incomplete. Persistent user metadata applies only to its captured image. New captures do not silently reuse old annotations.
No full-machine rewind, embedded decompiler, dynamic scripting engine or processor-semantic IR is introduced.
