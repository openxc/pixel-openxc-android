PixelOpenXC 
============

A very simple application that shows a brake image on the [pixel board](http://ledpixelart.com) and utilizes [openxc](http://openxcplatform.com) to get the data from the vehicle

## Dependencies

**OpenXC Libraries + Hardware:** The [OpenXC Android
API](https://github.com/openxc/openxc-android) handles all vehicle data
connections. The library includes [installation
instructions](http://openxcplatform.com/android/api-guide.html), example code
and additional documentation in the README file. This library is needed to
access any vehicle data. The application needs the [OpenXC Enabler application for android](https://github.com/openxc/openxc-android), for use in a vehicle it also requires an [OpenXC Vehicle Interface](http://openxcplatform.com/vehicle-interface/hardware.html#ford-reference-design)

**PXEL Board** [PIXEL](http://ledpixelart.com/) is an open-source, interactive framed LED wall display that allows you to send animations and images from an Android device.

**IOIO Libraries** A [version of the IOIO Library](https://github.com/openxc/pixel-openxc-ioio) is used to connect to the board and is adapted from the popular [IOIO-board](https://github.com/ytai/ioio/). The board provides a host machine the capability of interfacing with external hardware over a variety of commonly used protocols. The original IOIO board has been specifically designed to work with Android devices. The newer IOIO-OTG ("on the go") boards work with both Android devices and PC's (details here). The IOIO board can be connected to its host over USB or Bluetooth, and provides a high-level Java API on the host side for using its I/O functions as if they were an integral part of the client. The [IOIO-wiki](https://github.com/ytai/ioio/wiki) provides further details about setting up the library and troubleshooting.

The three libraries required by the Pixel Project are:
- OpenXC Library
- IOIOLibAccessory
- IOIOLibAndroid
- IOIOLibBT

To pair the board with the device turn on the Pixel Board and Bluetooth pair using code : 4545. More instructions on the PixelBoard can be found [here](http://ledpixelart.com/for-developers-2/)
