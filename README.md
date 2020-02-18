# emuLib
![emuLib Build](https://github.com/emustudio/emuLib/workflows/emuLib%20Build/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.emustudio/emuLib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.emustudio/emuLib)

emuLib is a run-time library used by [emuStudio](https://github.com/emustudio/emuStudio), universal emulation platform,
and its plugins.

### Content
 
- Core plugin API in the form of Java interfaces
- Some abstract implementations of the API which partially implement the common stuff
- Context pool or register which holds all registered plugins, which can be then obtained easily by other plugins 
- emuStudio main-module API which can be used by plugins
- Helper classes and methods, like:

    * Java Swing dialogs for showing errors and other messages
    * Radix conversion utils
    * Intel HEX file encoder
    * Universal file filter, usable in "Open file" Swing dialogs

emuLib also contains information about emulated computer (the context pool contains plugin objects and their connection
information), which is then used by emuStudio.

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
