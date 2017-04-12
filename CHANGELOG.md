# Change Log
All notable changes to this project will be documented in this file.

## 1.3.0-rc
### Added

* New goals to deploy App Engine configuration XMLs (cron.xml, dispatch.xml, dos.xml, datastore-indexes.xml, queue.xml) separately.
* New parameter `devserverVersion` to change between devappserver 1 and 2-alpha for local runs.


### Changed

* Javadoc update to indicate which parameters are supported by devappserver 1 and 2-alpha.

### Fixed

* :deploy goal should quietly skip non-war projects ([#171](https://github.com/GoogleCloudPlatform/app-maven-plugin/issues/85))

## 1.2.1
### Added

* N/A

### Changed

* N/A

### Fixed

* "Directories are not supported" issue when deploying ([#144](https://github.com/GoogleCloudPlatform/app-maven-plugin/issues/144))
