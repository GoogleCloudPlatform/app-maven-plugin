#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

gcloud components update
gcloud components install app-engine-java

# Use adopt openjdk 8u202 until kokoro updates its macos images (b/130225695)
wget https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u202-b08/OpenJDK8U-jdk_x64_mac_hotspot_8u202b08.tar.gz
tar -xzf OpenJDK8U-jdk_x64_mac_hotspot_8u202b08.tar.gz

JAVA_HOME="$(pwd)"/jdk8u202-b08/Contents/Home/

# temporary workaround until mvn is available in the image by default
# the integration tests rely on Maven being installed, cannot use the wrapper
curl  https://storage.googleapis.com/cloud-tools-for-java-team-kokoro-build-cache/apache-maven-3.5.0-bin.tar.gz -o apache-maven-3.5.0-bin.tar.gz
tar -xzf apache-maven-3.5.0-bin.tar.gz

M2_HOME="$(pwd)"/apache-maven-3.5.0
PATH=$PATH:$M2_HOME/bin

cd github/app-maven-plugin
./mvnw clean install -B -U
# bash <(curl -s https://codecov.io/bash)
