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

- `net.emustudio.emulib.plugins`
  Plugin interfaces, annotations, and abstract base classes
- `net.emustudio.emulib.runtime`
  Application-facing runtime APIs and context management
- `net.emustudio.emulib.runtime.settings`
  Shared settings interfaces for plugins and runtime code
- `net.emustudio.emulib.runtime.helpers`
  Helper utilities such as `Bits`, `NumberUtils`, `RadixUtils`, and `SleepUtils`
- `net.emustudio.emulib.runtime.io`
  `FileType`, Intel HEX, and TAP/TZX tape loading
- `net.emustudio.emulib.runtime.ui`
  Dialogs, Swing helpers, and debugger columns

## Performance Considerations

The table below groups helper methods by asymptotic cost.

- `n` = input array length
- `L` = emitted string length
- `s` = input string length

| Methods | Time complexity |
| --- | --- |
| `Bits(int, int)`, `Bits.toBytes()`, `Bits.reverseBytes()`, `Bits.reverseBits()`, `Bits.absolute()`, `Bits.shiftLeft()`, `Bits.shiftRight()`<br>`NumberUtils.reverseBits(...)`, `NumberUtils.readBits(...)`, `NumberUtils.readInt(...)`, `NumberUtils.writeInt(...)`, `NumberUtils.bcd2bin(...)`, `NumberUtils.bin2bcd(...)` | `O(1)` |
| `NumberUtils.nativeBytesToBytes(...)` | `O(n)` |
| `RadixUtils.formatHex(...)`, `RadixUtils.formatBinary(...)` | `O(L)` |
| `RadixUtils.parseRadix(...)`, `ParsingUtils.parseNumber(...)` | `O(s)` |

## Getting Started With Plugin Development

For full plugin development documentation, see the emuStudio developer guide:
<https://www.emustudio.net/documentation/developer/introduction/>.

At a high level:

- A plugin is represented by a single root object implementing `net.emustudio.emulib.plugins.Plugin` or one of its
  derived interfaces such as `CPU`, `Memory`, `Compiler`, or `Device`.
- Plugins can expose contexts for inter-plugin communication by implementing interfaces derived from
  `net.emustudio.emulib.plugins.Context`.
- Plugins usually extend `AbstractPlugin` (or `AbstractCPU`, `AbstractCompiler`). Lifecycle methods are no-op defaults;
  title and description come from `@PluginRoot`.
- Plugins obtain contexts of other plugins via `ContextPool.get(...)`, or the type-checked
  `getMemory(pluginID, Byte.class)` / `getDevice(pluginID, Byte.class)`.
- Compilers implement `compile(Path input, Path output)`; `AbstractCompiler` drives the workflow and reports
  `CompileException`s. ANTLR-based compilers reuse `AntlrLexicalAnalyzer` and `ParserErrorListener`.
- Plugins can communicate with the host application through `net.emustudio.emulib.runtime.ApplicationApi`.
- Configuration is exposed through `net.emustudio.emulib.runtime.settings.PluginSettings`.

## Deployment

When bundled manually, place the library in the `lib/` directory of the emuStudio installation.

Example:

```text
emuStudio/lib/emulib.jar
```

## Optional machine debugging

CPU plugins may additionally implement `net.emustudio.emulib.plugins.debugger.Debugging`. Its small interface compiles
machine-defined expressions and observes semantic execution steps. Live changes use the existing host execution
service. `CPU` defines no universal registers or state schema.

Plugins supply immutable `ValueView` and `StepView` presentation. The host owns rules, watches, history and bounded
recording files; saved rows, search tags and navigation links work offline without loading a CPU plugin.
Observation/formatting failures never pause emulation. Recording failures disable only the recorder and do not override
the emulation result. Invalid expressions remain saved but inactive until corrected.

`DebuggingSupport` shares observation mechanics; `ExpressionParser` shares optional syntax. Machine value names,
inspection, guest accesses and edits remain inside plugins. Use `ExecutionState.update` for edits and drain
`processUpdates` at every outer semantic step, including when observation is inactive.
