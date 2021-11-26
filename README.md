# intellij-ghidra

<!-- Plugin description -->
A plugin that extends IDEA with a Ghidra launcher run configuration.
<!-- Plugin description end -->

## Building

1. Check if your Intellij IDEA version and edition matches the properties set in `gradle.properties` file:
```
platformType = IC
platformVersion = 2021.2
```
For the Intellij IDEA Community edition you need to keep `IC` as is, for the Ultimate edition it should become `IU`.

2. Run the [Gradle](https://gradle.org) to build the plugin
```sh
gradle buildPlugin
```
3. The resulting ZIP ready for installation is located at `build/distributions/intellij-ghidra-*.zip`

## Usage

At the moment the only functionality available is configuring the IDE with the path to Ghidra, and a custom run configuration.
This can be set up by configuring the Ghidra facet and then creating a _Ghidra Launcher_ run configuration, as shown below.

![Setup demonstration](https://github.com/garyttierney/intellij-ghidra/raw/master/media/intellij-ghidra-example.gif "Setup demonstration")
