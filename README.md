# emuLib
![emuLib Build](https://github.com/emustudio/emuLib/workflows/emuLib%20Build/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.emustudio/emuLib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.emustudio/emuLib)

emuLib is a run-time library used by [emuStudio](https://github.com/emustudio/emuStudio), universal emulation platform,
and its plugins.

## License

This project is released under [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html) license.

## Usage

For Maven, use:

```
    <dependency>
        <groupId>net.emustudio</groupId>
        <artifactId>emulib</artifactId>
        <version>10.0.0</version>
    </dependency>
```

For Gradle, use:

```
    implementation 'net.emustudio:emulib:10.0.0'
```

## Deployment

The library should be placed in `lib/` directory where emuStudio is installed.
For example: `emuStudio/lib/emulib.jar`.


## Getting started

**TLDR;** Click [here](https://www.emustudio.net/devel/) for more robust documentation of plugin development. 

The library defines API for emuStudio plugins. A plugin is represented by single object which implements
`net.emustudio.emulib.plugins.Plugin` interface (or it's derivative). Plugins can also implement so-called "contexts",
which are used for inter-plugin communication.

Plugins can also communicate with emuStudio, using emuStudio API - object `net.emustudio.emulib.runtime.EmuStudio`
obtained during plugin object construction.
 
Generally, package `net.emustudio.emulib.plugins` is the API which plugins need to implement.
Then, in package `net.emustudio.emulib.runtime.helpers` are utility classes which might be useful for the runtime, like: 

- Number utilities which can do various radix conversions or other number manipulations (`NumberUtils`, `RadixUtils`)
- Intel HEX file generator/loader (`IntelHEX`)
