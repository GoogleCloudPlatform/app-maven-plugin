# Change Log
All notable changes to this project will be documented in this file.

## 1.3.0-rc1
### Added

* New goals to deploy App Engine configuration XMLs/YMLs separately. ([#155](../../issues/155))
* Dev Appserver1 integration ([#158](../../issues/158))
* New parameter `devserverVersion` to change between Java Dev Appserver 1 and 2-alpha for local runs.
* Primitive [User Guide](USER_GUIDE.md)

### Changed

* Javadoc update to indicate which parameters are supported by Java Dev Appserver 1 and 2-alpha. ([#167](../../issues/167))
* Default local dev server is Java Dev Appserver1
* `appYamls` parameter is deprecated in favor of `services`

### Fixed

* :deploy goal should quietly skip non-war projects ([#171](../../issues/85))

## 1.2.1
### Added

* N/A

### Changed

* N/A

### Fixed

* "Directories are not supported" issue when deploying ([#144](../../issues/144))
