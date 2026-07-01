# emuStudio Repo Routing

## Current Repository
- `emuLib` owns the shared plugin API, runtime services, settings/context APIs, Swing helpers, and reusable utilities used across emuStudio repositories.

## Sibling Repositories
- `/home/vbmacher/projects/emustudio/emuLib`: shared plugin API, runtime services, shared UI helpers, and reusable utilities.
- `/home/vbmacher/projects/emustudio/edigen`: decoder/disassembler generator from `.eds` specifications.
- `/home/vbmacher/projects/emustudio/emuStudio`: desktop application, bundled plugins, virtual computers, configs, and packaging.
- `/home/vbmacher/projects/emustudio/emustudio.github.io`: website, user documentation, developer documentation, and release-facing pages.
- `/home/vbmacher/projects/emustudio/edigen-gradle-plugin`: Gradle task and DSL integration for Edigen source generation.
- `/home/vbmacher/projects/emustudio/cpu-testsuite`: shared CPU instruction test framework and reusable verification helpers.

## When To Update Which Repository
- Shared plugin API, runtime service, settings/context contract, Swing helper, or reusable utility change: update `emuLib`.
- If an `emuLib` contract change affects generator output, check `edigen` and `edigen-gradle-plugin`.
- If an `emuLib` contract change affects bundled plugins, application wiring, or virtual computers, update `emuStudio`.
- If an `emuLib` contract change affects reusable CPU tests, update `cpu-testsuite`.
- If public behavior or developer-facing usage changed, update `emustudio.github.io`.

## Tickets And Commits
- Every change must have an existing GitHub ticket.
- Every commit subject must start with the ticket prefix: `[#123] Short summary`.
- If one task touches multiple emuStudio repositories, use the same ticket prefix in each related commit.
