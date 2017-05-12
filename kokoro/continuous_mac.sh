#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

curl https://sdk.cloud.google.com | bash
GOOGLE_CLOUD_SDK_HOME=/Users/kbuilder/google-cloud-sdk
"$GOOGLE_CLOUD_SDK_HOME"/bin/gcloud components install app-engine-java

# temporary workaround until mvn is available in the image by default
# the integration tests rely on Maven being installed, cannot use the wrapper
wget http://www-us.apache.org/dist/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.zip
unzip apache-maven-3.5.0-bin.zip

M2_HOME="$(pwd)"/apache-maven-3.5.0
PATH=$PATH:$M2_HOME/bin

cd github/app-maven-plugin
./mvnw clean install cobertura:cobertura -B -U
# bash <(curl -s https://codecov.io/bash)
