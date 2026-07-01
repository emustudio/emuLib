# emuStudio Repo Routing

## Current Repository
- `emuLib` owns the shared plugin API, runtime services, settings/context APIs, Swing helpers, and reusable utilities used across emuStudio repositories.

## Sibling Repositories
The emuStudio project spans several repositories, listed below with their GitHub locations:
- `emuLib` (https://github.com/vbmacher/emuLib): shared plugin API, runtime services, shared UI helpers, and reusable utilities.
- `edigen` (https://github.com/emustudio/edigen): decoder/disassembler generator from `.eds` specifications.
- `emuStudio` (https://github.com/emustudio/emuStudio): desktop application, bundled plugins, virtual computers, configs, and packaging.
- `emustudio.github.io` (https://github.com/emustudio/emustudio.github.io): website, user documentation, developer documentation, and release-facing pages.
- `edigen-gradle-plugin` (https://github.com/emustudio/edigen-gradle-plugin): Gradle task and DSL integration for Edigen source generation.
- `cpu-testsuite` (https://github.com/emustudio/cpu-testsuite): shared CPU instruction test framework and reusable verification helpers.

When a change may affect a sibling repository, first check whether that repository is checked out locally (typically as a sibling directory next to this one). If it is present, inspect and update it as needed. If it is not available locally, do not attempt to modify it; instead report which repository is missing and what changes it would require, so it can be handled separately.

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
