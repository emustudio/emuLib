# emuLib
![emuLib Build](https://github.com/emustudio/emuLib/workflows/emuLib%20Build/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.emustudio/emulib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.emustudio/emulib)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

emuLib is a run-time library used by [emuStudio](https://github.com/emustudio/emuStudio), universal emulation platform,
and its plugins.

## Usage

For Maven, use:

```
    <dependency>
        <groupId>net.emustudio</groupId>
        <artifactId>emulib</artifactId>
        <version>12.0.0</version>
    </dependency>
```

For Gradle, use:

```
    implementation 'net.emustudio:emulib:12.0.0'
```

### Important changes

Until version 9.0.0 emuLib used different group (`net.sf.emustudio`) and artifactId (`emuLib`). From version 10.0.0
onwards it uses group `net.emustudio` and artifactId `emulib` (all lowercase).

## Deployment

The library should be placed in `lib/` directory where emuStudio is installed.
For example: `emuStudio/lib/emulib.jar`.


## Getting started

**TLDR;** Click [here](https://www.emustudio.net/documentation/developer/introduction/) for more robust documentation of
plugin development. 

The library defines API for emuStudio plugins. A plugin is represented by single object which implements
`net.emustudio.emulib.plugins.Plugin` interface (or it's derivative). Plugins can also implement so-called "contexts",
which are used for inter-plugin communication.

Plugins can also communicate with emuStudio application API, using object `net.emustudio.emulib.runtime.ApplicationApi`
obtained during plugin construction.
 
Generally, package `net.emustudio.emulib.plugins` is the API which plugins need to implement.
Then, in package `net.emustudio.emulib.runtime.helpers` are utility classes which might be useful for the runtime, like: 

- Number utilities which can do various radix conversions or other number manipulations (`NumberUtils`, `RadixUtils`,
 `DecodingStrategies`)
- Intel HEX file generator/loader (`IntelHEX`)
