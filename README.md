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
  Helper utilities such as `Bits`, `NumberUtils`, `RadixUtils`, and
  `StringUtils`
- `net.emustudio.emulib.runtime.io`
  Intel HEX import and export support
- `net.emustudio.emulib.runtime.ui`
  Swing helpers, formatter interfaces, and debugger UI components

## Performance Considerations

The table below groups methods by asymptotic cost.

- `n` = input array or list length
- `L` = emitted binary string length
- `p` = registered radix pattern count
- `s` = input string length
- `b` = input byte count
- `m` = arbitrary-precision numeric magnitude size

For the `O(m^2)` rows, the practical cost is dominated by `BigInteger`
parsing and formatting, so exact constants depend on the JDK implementation.

| Methods | Time complexity |
| --- | --- |
| `Bits(int, int)`, `Bits.toBytes()`, `Bits.reverseBytes()`, `Bits.reverseBits()`, `Bits.absolute()`, `Bits.shiftLeft()`, `Bits.shiftRight()`<br>`NumberUtils.reverseBits(...)`, `NumberUtils.readBits(...)`, all `NumberUtils.readInt(...)` overloads, all `NumberUtils.writeInt(...)` overloads, `NumberUtils.bcd2bin(...)`, `NumberUtils.bin2bcd(...)`<br>`RadixUtils.getInstance()`, `RadixUtils.addNumberPattern(...)`, `RadixUtils.formatByteHexString(...)`, both `RadixUtils.formatWordHexString(...)` overloads, `RadixUtils.formatDwordHexString(...)` | `O(1)` |
| `NumberUtils.numbersToBytes(...)`, `NumberUtils.numbersToNativeBytes(...)`, `NumberUtils.shortsToBytes(...)`, `NumberUtils.shortsToNativeBytes(...)`, `NumberUtils.shortsToNativeShorts(...)`, `NumberUtils.nativeShortsToNativeBytes(...)`, `NumberUtils.nativeIntsToNativeBytes(...)`, `NumberUtils.nativeShortsToBytes(...)`, `NumberUtils.nativeShortsToShorts(...)`, `NumberUtils.nativeBytesToBytes(...)`, `NumberUtils.nativeBytesToIntegers(...)`, `NumberUtils.nativeBytesToShorts(...)`, `NumberUtils.nativeBytesToInts(...)`, `NumberUtils.listToNativeInts(...)` | `O(n)` |
| `RadixUtils.formatBinaryString(int, int, int, boolean)`, `RadixUtils.formatBinaryString(int, int)` | `O(L)` |
| `RadixUtils.setDefaults()` | `O(p)` |
| `RadixUtils.parseRadix(String)`, `RadixUtils.parseRadix(String, int)` | `O(p * s)` |
| `RadixUtils.convertToRadix(byte[], 16, boolean)` | `O(b)` |
| `RadixUtils.convertToRadix(byte[], toRadix != 16, boolean)`, `RadixUtils.convertToRadix(String, int, int)` | `O(m^2)` |
| `RadixUtils.convertToNumber(String, int)`, `RadixUtils.convertToNumber(String, int, int)` | `O(m^2 + b)` |
| `RadixUtils.convertToRadix(String, int)` | `O(p * s + m^2)` |

## Getting Started With Plugin Development

For full plugin development documentation, see the emuStudio developer guide:
<https://www.emustudio.net/documentation/developer/introduction/>.

At a high level:

- A plugin is represented by a single root object implementing `net.emustudio.emulib.plugins.Plugin` or one of its
  derived interfaces such as `CPU`, `Memory`, `Compiler`, or `Device`.
- Plugins can expose contexts for inter-plugin communication by implementing interfaces derived from
  `net.emustudio.emulib.plugins.Context`.
- Plugins can communicate with the host application through `net.emustudio.emulib.runtime.ApplicationApi`.
- Shared configuration is exposed through the settings interfaces in `net.emustudio.emulib.runtime.settings`.

## Deployment

When bundled manually, place the library in the `lib/` directory of the emuStudio installation.

Example:

```text
emuStudio/lib/emulib.jar
```
