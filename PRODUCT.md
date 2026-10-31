# emuLib Product

<!-- impeccable:product-schema 1 -->

## Platform

desktop

Java library for the emuStudio desktop application and its plugins, with reusable
Swing UI helpers. The user confirmed `desktop`, matching the sibling emuStudio
product record; Impeccable's current platform vocabulary does not cover this SDK.

## Users

Plugin authors and emuStudio maintainers are the primary users. They implement
compiler, CPU, memory, and device plugins, integrate them with the host application,
and reuse runtime services and UI helpers.

Their work supports the broader emuStudio audiences of students, teachers,
retrocomputing users, and emulator developers described in `../emuStudio/PRODUCT.md`.

## Product Purpose

Provide the shared contracts and reusable building blocks that let emuStudio and
its plugins work together. Success means developers can implement plugins,
communicate through explicit contexts, access host services and settings, and
reuse common functionality without depending on application internals.

## Positioning

emuLib is the shared SDK for emuStudio's plugin-based virtual computers. It provides
the compiler, CPU, memory, and device contracts used to assemble a virtual computer,
alongside host integration and reusable utilities.

## Operating Context

- Developers consume the library as `net.emustudio:emulib` through Maven or Gradle,
  or build it locally with the Gradle wrapper.
- Plugins expose contexts for inter-plugin communication and use `ApplicationApi`
  for host integration. Shared settings interfaces expose configuration.
- Swing helpers support desktop plugin interfaces, dialogs, and debugger tables.
- The library targets Java 11 bytecode. The current Gradle wrapper requires
  Java 17 or newer to run builds.

## Capabilities and Constraints

- Own shared plugin APIs, runtime services, settings and context contracts,
  reusable Swing helpers, and common utilities in this repository.
- Keep application wiring, bundled plugins, virtual computer configurations, and
  packaging in the sibling `emuStudio` repository. Public user and developer
  documentation belongs in `emustudio.github.io`.
- Check affected sibling repositories when a shared contract changes, including
  generators and reusable CPU tests where relevant.
- Prefer minimal changes. Discuss large refactors and new dependencies first.
- Every change requires a GitHub ticket; commit subjects use `[#123] Summary`
  with the relevant ticket number.
- No additional enduring constraints or product-specific accessibility needs were
  established during initialization.

## Brand Commitments

Preserve the name **emuLib** and its relationship to **emuStudio**.

## Evidence on Hand

- `README.md`: purpose, package overview, plugin development workflow, and usage.
- `build.gradle` and `settings.gradle`: Java library build, dependency coordinates,
  compilation target, and test configuration.
- `src/main/java/net/emustudio/emulib/plugins/`: shared plugin contracts.
- `src/main/java/net/emustudio/emulib/runtime/`: runtime APIs and shared services.
- `src/main/java/net/emustudio/emulib/runtime/ui/`: Swing helpers and debugger UI.
- `AGENTS.md`: repository responsibilities and change policies.
- `../emuStudio/PRODUCT.md`: broader product audience and desktop context.
- [GitHub issue #123](https://github.com/emustudio/emuLib/issues/123): this product
  context initialization.

## Product Principles

1. Serve plugin authors and application maintainers through explicit shared contracts.
2. Keep reusable functionality in emuLib and application-specific behavior in its owner.
3. Support consistent desktop workflows through reusable Swing helpers.
4. Make changes incrementally and check affected consumers across repositories.
