<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Ghidra Changelog

## [Unreleased]

## [0.5.0]
### Added

* Support for IntelliJ 2024.
* Discover Ghidra paths using `Utility.jar`
* Add validation to facet UI

## [0.4.2]
### Added

* Expandable text field for arguments by @ekilmer in https://github.com/garyttierney/intellij-ghidra/pull/28
* Support for IntelliJ 2023 by @mrexodia in https://github.com/garyttierney/intellij-ghidra/pull/32

### Changed

- Updated JetBrains vendor status.

## [0.4.1]
### Added
- Add support for IntelliJ 2022.3 by [LukBukkit](https://github.com/LukBukkit) in https://github.com/garyttierney/intellij-ghidra/pull/26.

## [0.4.0]
### Added

- Add support for IntelliJ 2022.2 by [agatti](https://github.com/agatti) and [ekilmer](https://github.com/ekilmer) in https://github.com/garyttierney/intellij-ghidra/pull/17.

### Fixed
- Fix headless checkbox not showing up by [ekilmer](https://github.com/ekilmer) in https://github.com/garyttierney/intellij-ghidra/pull/19

## [0.3.0]
### Changed

## [0.2.0]
### Added
- Add basic support for Headless mode. by [MatthewShao](https://github.com/MatthewShao) in https://github.com/garyttierney/intellij-ghidra/pull/1
- Add build instructions and update IDEA version by [XVilka](https://github.com/XVilka) in https://github.com/garyttierney/intellij-ghidra/pull/3

### Changed
- Re-based the repository on top of the IntelliJ Platform Plugin Template and integrated CI (#6).

### Fixed
- Use project-level MessageBus instead of deprecated module-level (#4).
- fix config persistence issue. by [MatthewShao](https://github.com/MatthewShao) in https://github.com/garyttierney/intellij-ghidra/pull/2
