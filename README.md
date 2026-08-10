# GhidraDev

<!-- Plugin description -->
Adds support for Ghidra extensions and scripts written in Java to IntellIJ. 
The following additional features have been added to the IDE:

- New Project Wizard to create Ghidra Module/Script Projects
- Ghidra Framework facet and global library support
- Code Assistance from the Ghidra API
- Run Configuration integration to launch the current extension

(Formerly named intellij-ghidra)
<!-- Plugin description end -->

## Contents

- [Building](#building)
- [Usage](#usage)
  - [Creating a New Ghidra Project](#creating-a-new-ghidra-project)
  - [Importing an Existing Ghidra Project](#importing-an-existing-ghidra-project)
    - [Importing a Ghidra Module Project](#importing-a-ghidra-module-project)
    - [Importing a Ghidra Script Project](#importing-a-ghidra-script-project)
  - [Launching Ghidra](#launching-ghidra)
  - [Ghidra Settings](#ghidra-settings)
    - [Ghidra Module Project](#ghidra-module-project)
    - [Ghidra Script Project](#ghidra-script-project)
## Building

1. Check if your IntelliJ IDEA version and edition matches the properties set in `gradle.properties` file:
```
platformType = IU
platformVersion = 2026.1
```
For the IntelliJ IDEA Community Edition you need to keep `IC` as is, for the Ultimate edition it should become `IU`.

2. Also ensure your IntelliJ IDEA version is within bounds in `gradle.properties`:
```
pluginSinceBuild = 262
pluginUntilBuild = 262.*
```

3. Run the [Gradle](https://gradle.org) to build the plugin
```sh
gradle buildPlugin
```

4. The resulting ZIP ready for installation is located at `build/distributions/GhidraDev-*.zip`

## Usage

### Creating a New Ghidra Project

1. From the New Project Wizard, select `New Ghidra Project`.
2. Specify the path to your Ghidra installation under `Ghidra Path`.
3. Specify the project type (Ghidra Module or Ghidra Script).
4. Specify other options.
5. `Create`.

### Importing an Existing Ghidra Project

1. Open the Ghidra project using IntelliJ.
2. Depending on whether the project is a module or script project, follow the steps below.

#### Importing a Ghidra Module Project

1. For Ghidra module projects, this plugin relies fully on Gradle for build configuration.
2. Create a `gradle.properties` file.
3. In `gradle.properties`, add the following line, replacing `<PATH_TO_GHIDRA_INSTALLATION>` with the path to your Ghidra installation.
```groovy
GHIDRA_INSTALL_DIR=<PATH_TO_GHIDRA_INSTALLATION>
```
4. Add the following lines at the start of your `build.gradle` file.
```groovy
plugins {
    id 'idea'
}
```
5. Add the following lines in your `build.gradle` file.
```groovy
buildExtension.exclude '.idea/**'
buildExtension.exclude '**/*.iml'
buildExtension.exclude 'gradle/**'
buildExtension.exclude 'gradle.properties'

idea {
    module {
        sourceDirs += file('ghidra_scripts')
    }
}
```
6. Refresh gradle.

#### Importing a Ghidra Script Project

1. For Ghidra script projects, this plugin relies on IntelliJ's built-in build configurations.
2. Go to `File` > `Settings` > `Tools` > `Ghidra Settings`.
3. Under `Ghidra Path`, select the path to your Ghidra installation.
4. `OK`.

### Launching Ghidra

Launching Ghidra from IntelliJ will automatically attach your module to Ghidra for easier debugging and testing (If you are working on a Ghidra module project).

If you've created a new Ghidra project, the Ghidra run configuration should have been created for you in the process. 
If not, follow the steps below.

> [!NOTE]
> Do ensure the path to Ghidra has been correctly set as the run configuration gets the path to Ghidra from your project.
> 
> If you are working on a Ghidra script project, this can be found at `File` > `Settings` > `Tools` > `Ghidra Settings` under `Ghidra Path`.
> 
> If you are working on a Ghidra module project, this can be found in your `gradle.properties` file under `GHIDRA_INSTALL_DIR`.

1. On IntelliJ's top toolbar, select `Edit Configurations` to open the Run/Debug Configurations dialog.
2. On the top left, select `+` and `Ghidra Launcher`.
3. Give the run configuration a name (E.g. `Ghidra`).
4. Click `OK`.

### Ghidra Settings

#### Ghidra Module Project

Ghidra settings can be found in your `gradle.properties` file.

#### Ghidra Script Project

Ghidra settings can be found in `File` > `Settings` > `Tools` > `Ghidra Settings`.
