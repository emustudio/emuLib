# ADR-001: Shared video and audio recording

## Status
Accepted

## Context
Space Invaders and ZX Spectrum use the same MP4 encoding and PCM muxing operations, with different frame and audio
sources. These operations belong beside emuLib's reusable runtime utilities rather than in individual device plugins.

## Decision
Provide `net.emustudio.emulib.runtime.recording.RecordingSession`, accepting copied video frames and signed 16-bit
little-endian stereo PCM, separately or as a pair. Producers configure dimensions, rational video timing and audio
sample rate. A bounded worker encodes H.264 during recording. Stop saves MP4 or discards the session, drains or drops
pending captures accordingly, and releases temporary files. JCodec 0.2.3 is an implementation dependency hidden from
the public API. Plugins retain capture, clocks, sound synthesis/mixing and UI responsibilities.

## Consequences
Device plugins share one engine and its tests. emuLib consumers receive JCodec runtime dependencies and must update
emuLib before adopting this API. Encoding stays off producer/UI threads; slow encoding fails capture explicitly.
Temporary video is already encoded, PCM is compressed losslessly, and saving only finalizes and muxes the streams.
