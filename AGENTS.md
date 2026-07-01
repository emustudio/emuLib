# emuStudio Repo Routing

## Repository Map
- `/home/vbmacher/projects/emustudio/emuLib`: shared runtime library for emuStudio. Owns plugin API contracts, runtime services, settings/context APIs, Swing helpers, and reusable utilities.
- `/home/vbmacher/projects/emustudio/edigen`: generator for instruction decoders and disassemblers from `.eds` specifications. Owns the DSL, parser, and generated decoder/disassembler code shape.
- `/home/vbmacher/projects/emustudio/emuStudio`: desktop application, CLI launcher, bundled official plugins, bundled virtual computers, configs, and distribution packaging.
- `/home/vbmacher/projects/emustudio/emustudio.github.io`: website, user documentation, developer documentation, release-facing pages, and download-related content.
- `/home/vbmacher/projects/emustudio/edigen-gradle-plugin`: Gradle integration for Edigen. Owns Gradle tasks and DSL for source generation from `.eds`.
- `/home/vbmacher/projects/emustudio/cpu-testsuite`: shared CPU instruction test framework. Owns reusable CPU test builders, fixtures, and verification helpers.

## Task Routing
- Shared plugin API, common utility, runtime service, or reusable UI helper change: update `emuLib`; also check `emuStudio`, `edigen`, and `cpu-testsuite` for consumers.
- Desktop app behavior, plugin wiring, bundled configs, bundled computers, or packaging change: update `emuStudio`; check `emuLib` if the task needs shared API support.
- `.eds` syntax, decoder generation, disassembler generation, or generated code semantics change: update `edigen`; also check `edigen-gradle-plugin` and any affected bundled CPUs in `emuStudio`.
- Gradle build integration for generated decoders/disassemblers: update `edigen-gradle-plugin`; check `edigen` if the task depends on generator inputs or outputs.
- Shared CPU testing API or reusable instruction-test helper change: update `cpu-testsuite`; check `emuStudio` CPU plugin tests that consume it.
- User-facing docs, developer guides, website pages, release notes, or download links: update `emustudio.github.io`; also update the owning code repository when behavior changed.
- Cross-repository emulator feature work: start with the owning repository above, then update every listed consumer repository that depends on that contract or behavior.

## Tickets And Commits
- Every change must be tied to an existing GitHub ticket before edits are finalized.
- Every commit message must start with the ticket prefix in this format: `[#123] Short summary`.
- If one task spans multiple repositories, use the same ticket number in each related commit.
