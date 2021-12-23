<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Ghidra Changelog

## [Unreleased]

### Changed
- Update README for JetBrains marketplace (#13).

### Fixed
- Don't panic when searching an empty Gradle root by [agatti](https://github.com/agatti) (#11).

## [0.2.0]
### Added
- Add basic support for Headless mode. by [MatthewShao](https://github.com/MatthewShao) in https://github.com/garyttierney/intellij-ghidra/pull/1
- Add build instructions and update IDEA version by [XVilka](https://github.com/XVilka) in https://github.com/garyttierney/intellij-ghidra/pull/3

### Changed
- Re-based the repository on top of the IntelliJ Platform Plugin Template and integrated CI (#6).

### Fixed
- Use project-level MessageBus instead of deprecated module-level (#4).
- fix config persistence issue. by [MatthewShao](https://github.com/MatthewShao) in https://github.com/garyttierney/intellij-ghidra/pull/2
