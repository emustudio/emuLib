emuLib - emuStudio library
--------------------------
[![Build Status](https://travis-ci.org/vbmacher/emuLib.svg)](https://travis-ci.org/vbmacher/emuLib)
[![Coverage Status](https://coveralls.io/repos/vbmacher/emuLib/badge.svg?branch=branch-9_0)](https://coveralls.io/r/vbmacher/emuLib?branch=branch-9_0)
[![Dependency Status](https://www.versioneye.com/user/projects/578295c176ef40003fba7d4a/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/578295c176ef40003fba7d4a)

emuLib is the run-time library used by [emuStudio](http://github.com/vbmacher/emuStudio) platform and its plug-ins.
The features include:
 
* Core plug-in API in the form of Java interfaces

* Some abstract implementations of the API which partially implement the common stuff

* Context pool or register which holds all registered plug-ins, which can be then obtained easily by other plug-ins 

* emuStudio API which can be used by plug-ins

* Helper classes and methods, like:

    * Java Swing dialogs for showing errors and other messages
    * Radix conversion utils
    * HEX file encoder
    * Universal file filter
 
The main purpose of emuLib is to hold information about used emulated computer (plug-ins objects and their connection
information) which is then used by emuStudio.

Design
------

Currently, emuLib is not just a back-end for the emulation and emuStudio is not just a front-end. So far, emulation
control is located in emuStudio. Design of the library does not conform well to Single Responsibility Principle,
but hopefully it will improve in the future versions.

Usage
-----

In order to use emuLib in your Maven project, it is necessary to declare in the `pom.xml` the emuStudio repository:

```
<repositories>
    ...
    <repository>
        <id>emustudio-repository</id>
        <name>emuStudio Repository</name>
        <url>http://emustudio.sf.net/repository/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

And then the dependency itself:

```
<dependencies>
    <dependency>
        <groupId>net.sf.emustudio</groupId>
        <artifactId>emuLib</artifactId>
        <version>9.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Installation in emuStudio
-------------------------

The library should be put to `lib/` directory where emuStudio is installed.
For example: `emuStudio/lib/emuLib.jar`.

License
-------

This project is released under GNU GPL v2 license.
