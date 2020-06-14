VoidChess
=============

A simple chess program with the option of playing classic chess or Fischer chess (aka chess960) against the computer.

![chess game in progress](docs/voidchess.png "screenshot of VoidChess")

# Download

You can download the installer for Windows, Linux and macOS on [this project's main website](http://simon-void.github.io/voidchess).

# Generating an installer

You can generate an installer by running
```
./gradlew clean buildInstaller
```
on Linux and MacOS and by running
```
gradlew.bat clean buildInstaller
```
on Windows.
 
You might need to install some the local packets which `jpackage` uses to build installer,
but in this case the error message should be helpful.

The installer is generated into the `build/installer` folder. 