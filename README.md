emuLib - emuStudio library
--------------------------

emuLib is run-time library used by [emuStudio](http://github.com/vbmacher/emuStudio) platform and related plug-ins.

It basically includes all main and related interfaces used for plug-ins implementation. Besides, some helper functions
are provided as well.

Main purpose of emuLib is to hold information about used emulated computer (plug-ins objects and their connection
information) that is provided to emuStudio. Currently, emuLib is not just a back-end of emulation and emuStudio is not
just a GUI front-end. By far, emulation control is located in emuStudio.


Installation
------------

The library should be put to `lib` directory where emuStudio is installed.
For example: `emuStudio/lib/emuLib.jar`.

[![Build Status](https://travis-ci.org/vbmacher/emuLib.png)](https://travis-ci.org/vbmacher/emuLib) [![Build Status](https://travis-ci.org/vbmacher/emuLib.png)](https://travis-ci.org/vbmacher/emuLib)
[![Coverage Status](https://coveralls.io/repos/vbmacher/emuLib/badge.png?branch=branch-9_0)](https://coveralls.io/r/vbmacher/emuLib)

