# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.4-SNAPSHOT] - Unreleased
### Added
- Added support for copying players and farmhands between save files.
- Implemented `PasteInputDialog` for user input through the terminal UI.
- Added JSON schema validation for `ParserInstruction` objects.

### Changed
- Updated dependencies:
  - Lombok to 1.18.36
  - Jakarta XML Bind API to 4.0.2
  - Jackson Databind to 2.18.2
  - Lanterna to 3.1.2
- Add .vscode settings.

### Fixed
- Fixed various bugs related to XML parsing and character migration.

## [1.0.3-SNAPSHOT] - Unreleased
### Added
- Initial project setup
- XML parsing capabilities with jakarta.xml.bind-api
- JSON support with jackson-databind
- Terminal UI implementation using lanterna
- Lombok integration for reduced boilerplate code

### Dependencies
- Lombok 1.18.36
- Jakarta XML Bind API 4.0.2
- Jackson Databind 2.18.2
- Lanterna 3.1.2

### Build
- Maven Assembly Plugin configuration for creating executable JAR with dependencies
- Java 17 target compilation

## [1.0.2] - 2024-03-XX
### Changed
- Version bump from 1.0.1 to 1.0.2

## [1.0.1] - 2024-03-XX
### Added
- Initial release