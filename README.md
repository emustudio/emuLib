# emuLib
![emuLib Build](https://github.com/emustudio/emuLib/workflows/emuLib%20Build/badge.svg)
![Maven Central Version](https://img.shields.io/maven-central/v/net.emustudio/emulib)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

emuLib is the shared runtime library used by [emuStudio](https://github.com/emustudio/emuStudio) and its plugins.
It provides the public plugin API, runtime services, UI helpers, and reusable utility classes used across the emuStudio ecosystem.

## What Is Included

- Plugin contracts and base classes for CPU, memory, compiler, and device plugins
- Runtime APIs for application integration, settings, context registration, and inter-plugin communication
- Utility classes for number conversion, radix formatting, Intel HEX handling, threading, and strings
- Swing-oriented UI helpers and reusable debugger table components

Packages under `net.emustudio.emulib` are public API by default. Packages named `internal` explicitly mark private API that must not be used by emuLib consumers, including plugins and the emuStudio application.

## Usage

This repository currently builds version `12.1.0-SNAPSHOT`. For released builds, prefer a published version from Maven Central in consumer
projects.

For Maven:

```xml
<dependency>
    <groupId>net.emustudio</groupId>
    <artifactId>emulib</artifactId>
    <version>12.1.0-SNAPSHOT</version>
</dependency>
```

For Gradle:

```groovy
implementation 'net.emustudio:emulib:12.1.0-SNAPSHOT'
```

## Compatibility

- The library itself targets Java 11 bytecode.
- The Gradle wrapper in this repository is Gradle 9, which requires Java 17 or newer to run local builds.
- Until version 9.0.0, emuLib used group `net.sf.emustudio` and artifactId `emuLib`.
- From version 10.0.0 onward, it uses group `net.emustudio` and artifactId `emulib`.

## Building Locally

Run tests:

```bash
./gradlew test
```

Generate Javadoc:

```bash
./gradlew javadoc
```

If Gradle fails with a JVM version error, point `JAVA_HOME` to a JDK 17+ installation before running the wrapper.

## Package Overview

- `net.emustudio.emulib.plugin`
  Contracts, base classes, and implementation helpers for plugins
- `net.emustudio.emulib.application`
  Host services implemented by emuStudio and called by plugins
- `net.emustudio.emulib.application.annotations`
  Live annotations shared through the host application
- `net.emustudio.emulib`
  General-purpose bit, number, radix, locking, sleep, and unchecked-call utilities
- `net.emustudio.emulib.io`
  File-format and I/O APIs
- `net.emustudio.emulib.ui`
  Shared UI constants, icons, and reusable Swing components
- `net.emustudio.emulib.audio`, `net.emustudio.emulib.recording`
  Reusable audio playback and media recording APIs

See the [C4 architecture documentation](docs/architecture.adoc) for ownership,
dependency direction, and runtime interaction diagrams.

## Performance Considerations

The table below groups helper methods by asymptotic cost.

- `n` = input array length
- `L` = emitted string length
- `s` = input string length

| Methods | Time complexity |
| --- | --- |
| `Bits(int, int)`, `Bits.toBytes()`, `Bits.reverseBytes()`, `Bits.reverseBits()`, `Bits.absolute()`, `Bits.shiftLeft()`, `Bits.shiftRight()`<br>`Numbers.reverseBits(...)`, `Numbers.readBits(...)`, `Numbers.readInt(...)`, `Numbers.writeInt(...)`, `Numbers.bcd2bin(...)`, `Numbers.bin2bcd(...)` | `O(1)` |
| `Numbers.nativeBytesToBytes(...)` | `O(n)` |
| `Numbers.formatHex(...)`, `Numbers.formatBinary(...)` | `O(L)` |
| `Numbers.parseRadix(...)`, `ParsingUtils.parseNumber(...)` | `O(s)` |

## Getting Started With Plugin Development

For full plugin development documentation, see the emuStudio developer guide:
<https://www.emustudio.net/documentation/developer/introduction/>.

At a high level:

- A plugin is represented by a single root object implementing `net.emustudio.emulib.plugin.Plugin` or one of its
  derived interfaces such as `CPU`, `Memory`, `Compiler`, or `Device`.
- Plugins can expose contexts for inter-plugin communication by implementing interfaces derived from
  `net.emustudio.emulib.plugin.Context`.
- Plugins usually extend `AbstractPlugin` (or `AbstractCPU`, `AbstractCompiler`). Lifecycle methods are no-op defaults;
  title and description come from `@PluginRoot`.
- Plugins obtain contexts of other plugins via `ContextPool.get(...)`, or the type-checked
  `getMemory(pluginID, Byte.class)` / `getDevice(pluginID, Byte.class)`.
- Compilers implement `compile(Path input, Path output)`; `AbstractCompiler` drives the workflow and reports
  `CompileException`s. ANTLR-based compilers reuse `AntlrLexicalAnalyzer` and `ParserErrorListener`.
- Plugins can communicate with the host application through `net.emustudio.emulib.application.ApplicationApi`.
- Configuration is exposed through `net.emustudio.emulib.application.settings.PluginSettings`.

## Deployment

When bundled manually, place the library in the `lib/` directory of the emuStudio installation.

Example:

```text
emuStudio/lib/emulib.jar
```

## Optional machine debugging

CPU plugins may additionally implement `net.emustudio.emulib.plugin.debugger.Debugging`. Its small interface compiles
machine-defined expressions and observes semantic execution steps. Plugin editors apply live changes directly,
including while the CPU runs. `CPU` defines no universal registers or state schema.

Plugins supply immutable `ValueView` and `StepView` presentation. The host owns rules, watches, history and bounded
recording files; saved rows, search tags and navigation links work offline without loading a CPU plugin.
Observation/formatting failures never pause emulation. Recording failures disable only the recorder and do not override
the emulation result. Invalid expressions remain saved but inactive until corrected.

`DebuggingSupport` shares observation mechanics; `ExpressionParser` shares optional syntax. Machine value names,
inspection, guest accesses and edits remain inside plugins. The host does not queue edits or coordinate them with CPU steps.

`CPUContext` exposes direct getters for run state, instruction location and measured frequency, plus `CPUListener`
registration. CPU plugins emit run-state and
internal-state notifications through their implementation; observers cannot publish or replace CPU state through the context API.
