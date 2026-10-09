# ADR-001: Consumer-neutral file loading

## Status
Accepted

## Context
[Ticket #119](https://github.com/emustudio/emuLib/issues/119).
emuStudio now has two TAP/TZX consumers with duplicated parsing, while byte-mem separately adapts IntelHEX and BIN.
Tape playback and memory writes require distinct information and policies.

## Decision
Expose one `net.emustudio.emulib.runtime.io.FileLoader` contract for BIN, HEX, TAP and TZX.
Synchronous `FileLoader.Listener` consumers receive immutable `FileLoader.Block` values.
BIN emits unaddressed bytes; HEX preserves embedded addresses. Tape blocks preserve raw bytes,
timing and IDs, with shared packet and Spectrum header decoding available on the block.
`TapeListener` extends the generic listener and decodes callbacks through its default `onBlock` method.
The loader accepts no memory destination or placement address. byte-mem selects BIN placement and
applies strict Spectrum CODE pairing, checksum and length policy to shared decoded blocks.
FileLoader.Options distinguishes file order from tape control-flow traversal and supports cancellation.
Keep existing IntelHEX generation/loading APIs and I8HEX parsing scope, including its checksum policy.
IntelHEX stores bytes in a TreeMap and exposes ascending address iteration through the existing Map API.
Generation streams records; line-based parsing reports malformed records as IOException with line numbers.
Unknown extensions have no shared fallback; callers explicitly select BIN when desired.

## Consequences
Both emuStudio plugins depend only on emuLib for format loading. No new dependencies are required.
Bytes, tape timing and non-memory blocks remain available without forcing playback into an address-only model.
Stateful listeners must be separate for concurrent loads. Cancellation/errors omit the completion callback.
CSW/generalized blocks are preserved and reported; this extraction does not implement their missing playback support.
